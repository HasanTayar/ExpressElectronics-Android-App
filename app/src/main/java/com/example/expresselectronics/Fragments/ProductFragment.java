package com.example.expresselectronics.Fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresselectronics.Product.Product;
import com.example.expresselectronics.Product.ProductAdapter;
import com.example.expresselectronics.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment implements ProductAdapter.OnItemClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView productRecyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private SharedPreferences sharedPreferences;

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        productRecyclerView = view.findViewById(R.id.listViewProducts); // Make sure this ID matches your RecyclerView in your XML
        productList = new ArrayList<>();
        adapter = new ProductAdapter(productList);
        adapter.setOnItemClickListener(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext()); // This line is new
        productRecyclerView.setLayoutManager(layoutManager); // This line is new

        productRecyclerView.setAdapter(adapter);

        sharedPreferences = getActivity().getSharedPreferences("product_prefs", Context.MODE_PRIVATE);

        fetchProducts();

        return view;
    }


    private void fetchProducts() {
        db.collection("products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Product product = document.toObject(Product.class);
                                productList.add(product);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void onItemClick(Product product) {
        // Store the product details in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("product_id", product.getId());
        editor.putString("product_name", product.getName());
        editor.putFloat("product_price", (float) product.getPrice());
        editor.putString("product_description", product.getDescription());
        editor.putString("product_imageUrl", product.getImageUrl());
        editor.apply();

        // Launch ProductDetailsFragment
        ProductDetailsFragment fragment = new ProductDetailsFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}