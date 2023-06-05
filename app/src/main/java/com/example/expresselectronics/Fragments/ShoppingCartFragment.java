package com.example.expresselectronics.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresselectronics.Product.Product;
import com.example.expresselectronics.R;
import com.example.expresselectronics.ShoppingCart.ShoppingCartAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCartFragment extends Fragment {
    private RecyclerView listViewShoppingCart;
    private TextView textViewTotalPrice , textViewEmptyCart;
    private Button submitButton;
    private double totalPrice;

    private ShoppingCartAdapter adapter;
    private List<Product> products;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public static final String TAG = "ShoppingCartFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_cart, container, false);

        listViewShoppingCart = view.findViewById(R.id.listViewShoppingCart);
        textViewTotalPrice = view.findViewById(R.id.textViewTotalPrice);
        textViewEmptyCart = view.findViewById(R.id.textViewEmptyCart);
        submitButton = view.findViewById(R.id.submitButton);

        products = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user signed in!");
            return view;
        }

        db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUser.getUid()).collection("cart")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Convert document to Product
                                Product product = document.toObject(Product.class);
                                // Add to your list
                                products.add(product);
                            }
                            // Notify adapter
                            adapter.notifyDataSetChanged();
                            // Recalculate total price
                            calculateTotalPrice();

                            // Check if the shopping cart is empty
                            if (products.isEmpty()) {
                                textViewTotalPrice.setVisibility(View.GONE);
                                submitButton.setVisibility(View.GONE);
                            } else {
                                textViewTotalPrice.setVisibility(View.VISIBLE);
                                submitButton.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


        adapter = new ShoppingCartAdapter(getContext(), products);
        listViewShoppingCart.setLayoutManager(new LinearLayoutManager(getContext()));
        listViewShoppingCart.setAdapter(adapter);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create new PaymentFragment
                PaymentFragment paymentFragment = new PaymentFragment();

                // Navigate to PaymentFragment
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, paymentFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private void calculateTotalPrice() {
        totalPrice = 0;
        for (Product product : products) {
            totalPrice += product.getPrice() * product.getQuantity();
        }
        textViewTotalPrice.setText("Total Price: " + totalPrice);

        if (products.isEmpty()) {
            textViewEmptyCart.setVisibility(View.VISIBLE);
        } else {
            textViewEmptyCart.setVisibility(View.GONE);
        }
    }


    // Getter methods for cart items and total price
    public List<Product> getCartItems() {
        return products;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

}
