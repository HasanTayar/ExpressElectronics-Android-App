package com.example.expresselectronics.Order;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.expresselectronics.Product.Product;
import com.example.expresselectronics.R;

import java.util.List;

public class OrderAdapter extends ArrayAdapter<Order> {
    private Context context;
    private List<Order> orders;

    public OrderAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Order> objects) {
        super(context, resource, objects);
        this.context = context;
        this.orders = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.order_item_layout, null);

        TextView textViewOrderId = view.findViewById(R.id.textViewOrderId);
        TextView textViewOrderDate = view.findViewById(R.id.textViewOrderDate);
        TextView textViewTotalPrice = view.findViewById(R.id.textViewTotalPrice);
        TextView textViewOrderItems = view.findViewById(R.id.textViewOrderItems); // New TextView for Order Items

        Order order = orders.get(position);

        textViewOrderId.setText(order.getId());
        textViewOrderDate.setText(order.getDate());
        textViewTotalPrice.setText(String.valueOf(order.getTotalPrice()));

        StringBuilder sb = new StringBuilder();
        for (Product product : order.getOrderedItems()) {
            sb.append(product.getName()).append(" x ").append(product.getQuantity()).append("\n");
        }

        textViewOrderItems.setText(sb.toString());

        return view;
    }
}

