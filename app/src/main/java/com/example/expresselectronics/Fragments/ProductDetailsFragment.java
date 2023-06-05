package com.example.expresselectronics.Fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expresselectronics.Product.Product;
import com.example.expresselectronics.R;

import com.example.expresselectronics.User.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductDetailsFragment extends Fragment {

    private TextView textViewName;
    private String userId;
    private TextView textViewPrice;
    private TextView textViewDescription;
    private ImageView imageViewProduct;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_details, container, false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        textViewName = view.findViewById(R.id.textViewName);
        textViewPrice = view.findViewById(R.id.textViewPrice);
        textViewDescription = view.findViewById(R.id.textViewDescription);
        imageViewProduct = view.findViewById(R.id.imageViewProduct);
        if (user != null) {
            userId = user.getUid();
        } else {
            // You might want to handle the scenario when the user is not logged in.
        }

        // Initialize SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("product_prefs", Context.MODE_PRIVATE);

        displayProductDetails();

        Button addToCartButton = view.findViewById(R.id.buttonAddToCart);
        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productId = sharedPreferences.getString("product_id", null);
                if (productId != null) {
                    // Retrieve the current product from SharedPreferences
                    String productName = sharedPreferences.getString("product_name", "");
                    float productPrice = sharedPreferences.getFloat("product_price", 0);
                    String productDescription = sharedPreferences.getString("product_description", "");
                    String productImageUrl = sharedPreferences.getString("product_imageUrl", "");
                    int productQuantity = sharedPreferences.getInt("product_quantity", 1); // Fetch quantity, default is 1
                    Product product = new Product(productId, productName, productDescription, productPrice, productImageUrl, productQuantity);

                    List<Product> productList = new ArrayList<>();
                    productList.add(product);

                    // Pass the list of products to addToCart
                    addToCart(userId, productList);

                    // Show a toast
                    Toast.makeText(getContext(), "Product added to cart", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle case when product id is not available.
                    Toast.makeText(getContext(), "Failed to add product to cart", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Product id not found in SharedPreferences");
                }
            }
        });

        return view;
    }

    private void displayProductDetails() {
        String productName = sharedPreferences.getString("product_name", "");
        float productPrice = sharedPreferences.getFloat("product_price", 0);
        String productDescription = sharedPreferences.getString("product_description", "");
        String productImageUrl = sharedPreferences.getString("product_imageUrl", "");

        textViewName.setText(productName);
        textViewPrice.setText(String.valueOf(productPrice));
        textViewDescription.setText(productDescription);

         Picasso.get().load(productImageUrl).into(imageViewProduct);
    }

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private void addToCart(String userId, List<Product> products) {
        for (Product product : products) {
            db.collection("users").document(userId)
                    .collection("cart")
                    .document(product.getId())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Product existingProduct = document.toObject(Product.class);
                                    existingProduct.setQuantity(existingProduct.getQuantity() + product.getQuantity());

                                    db.collection("users").document(userId)
                                            .collection("cart")
                                            .document(product.getId())
                                            .set(existingProduct)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error updating document", e);
                                                }
                                            });
                                } else {
                                    db.collection("users").document(userId)
                                            .collection("cart")
                                            .document(product.getId())
                                            .set(product)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error writing document", e);
                                                }
                                            });
                                }
                            } else {
                                Log.w(TAG, "Error getting document.", task.getException());
                            }
                        }
                    });
        }
    }
}
