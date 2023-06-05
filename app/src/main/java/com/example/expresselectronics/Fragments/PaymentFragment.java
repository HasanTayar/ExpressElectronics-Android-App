package com.example.expresselectronics.Fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.expresselectronics.MainActivity;
import com.example.expresselectronics.Order.Order;
import com.example.expresselectronics.Product.Product;
import com.example.expresselectronics.R;
import com.example.expresselectronics.User.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PaymentFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;
    private String orderId;
    private double amount;
    private ShoppingCartFragment cartFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        // Get current logged in user id
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get shopping cart fragment from MainActivity
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            cartFragment = mainActivity.getCartFragment();
        }

        // Process payment if cartFragment is not null
        if (cartFragment != null) {
            processPayment();
        } else {
            Toast.makeText(getActivity(), "Could not fetch cart items.", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    private void processPayment() {
        // Generate a random order ID
        String orderId = UUID.randomUUID().toString();

        // Get the current date
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Get the current user's shopping cart items and total price
        List<Product> orderedItems = cartFragment.getCartItems();
        double totalPrice = cartFragment.getTotalPrice();

        // Create new order
        Order order = new Order(orderId, orderedItems, date, totalPrice);

        // Get reference to the new order document
        DocumentReference orderRef = db.collection("users").document(userId).collection("orders").document(orderId);

        // Add order to Firestore
        orderRef.set(order)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Payment processed successfully, update the UI
                        clearFirestoreCart(userId);
                        Toast.makeText(getActivity(), "Payment successful", Toast.LENGTH_SHORT).show();

                        // Broadcast successful transaction
                        Intent intent = new Intent(MainActivity.TRANSACTION_SUCCESS_ACTION);
                        getActivity().sendBroadcast(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to process the payment, handle the error
                        Toast.makeText(getActivity(), "Transaction failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void clearFirestoreCart(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference cartRef = db.collection("users").document(userId).collection("cart");

        // query documents
        cartRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // begin deleting each document
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        cartRef.document(document.getId()).delete();
                    }
                } else {
                    Log.d(TAG, "Error getting cart documents: ", task.getException());
                }
            }
        });
    }

}

