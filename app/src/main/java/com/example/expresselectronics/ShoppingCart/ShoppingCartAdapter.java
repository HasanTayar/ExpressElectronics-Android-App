package com.example.expresselectronics.ShoppingCart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresselectronics.Product.Product;
import com.example.expresselectronics.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder> {
    private Context context;
    public static final String TAG = "ShoppingCartAdapter";
    private List<Product> products;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ShoppingCartAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.textViewProductId.setText(product.getName());

        holder.textViewProductPrice.setText(String.valueOf(product.getPrice() * product.getQuantity()));
        holder.textViewProductQuantity.setText(String.valueOf(product.getQuantity()));
        Picasso.get().load(product.getImageUrl()).placeholder(R.drawable.placeholder_image).into(holder.productImageView);
        holder.decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = product.getQuantity();
                if (quantity > 0) {
                    product.setQuantity(quantity - 1);
                    if (product.getQuantity() == 0) {
                        products.remove(position);
                        notifyDataSetChanged();
                        removeFromFirestore(product);
                    } else {
                        notifyItemChanged(position);
                        updateFirestore(product);
                    }
                }
            }
        });

        holder.increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = product.getQuantity();
                product.setQuantity(quantity + 1);
                notifyItemChanged(position);
                updateFirestore(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private void removeFromFirestore(Product product) {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("cart")
                .document(product.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Log the success
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Log the error
                    }
                });
    }

    private void updateFirestore(Product product) {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("cart")
                .document(product.getId())
                .set(product)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Log the success
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Log the error
                    }
                });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView textViewProductId;
        TextView textViewProductName;
        TextView textViewProductDescription;
        TextView textViewProductPrice;
        TextView textViewProductQuantity;
        Button decreaseQuantityButton;
        Button increaseQuantityButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            textViewProductId = itemView.findViewById(R.id.textViewProductId);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductDescription = itemView.findViewById(R.id.textViewProductDescription);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewProductQuantity = itemView.findViewById(R.id.textViewProductQuantity);
            decreaseQuantityButton = itemView.findViewById(R.id.decreaseQuantityButton);
            increaseQuantityButton = itemView.findViewById(R.id.increaseQuantityButton);
        }
    }
}
