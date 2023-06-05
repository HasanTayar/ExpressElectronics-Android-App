package com.example.expresselectronics.Fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.expresselectronics.Order.Order;
import com.example.expresselectronics.Order.OrderAdapter;
import com.example.expresselectronics.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListView orderHistoryListView;
    private OrderAdapter adapter;
    private List<Order> orderHistory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        orderHistoryListView = view.findViewById(R.id.listViewOrderHistory);
        orderHistory = new ArrayList<>();
        fetchOrderHistory();

        return view;
    }

    private void fetchOrderHistory() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).collection("orders")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Order order = document.toObject(Order.class);
                                    orderHistory.add(order);
                                }
                                adapter = new OrderAdapter(getActivity(), R.layout.order_item_layout, orderHistory);
                                orderHistoryListView.setAdapter(adapter);
                            } else {
                                Log.w(TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
    }
}

