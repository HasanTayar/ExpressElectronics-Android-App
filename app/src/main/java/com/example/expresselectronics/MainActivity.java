package com.example.expresselectronics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

//import com.example.expresselectronics.Fragments.OrderHistoryFragment;
import com.example.expresselectronics.Fragments.OrderHistoryFragment;
import com.example.expresselectronics.Fragments.ProductFragment;
import com.example.expresselectronics.Fragments.ShoppingCartFragment;
import com.example.expresselectronics.Fragments.UserProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String TRANSACTION_SUCCESS_ACTION = "TRANSACTION_SUCCESS";

    private final ShoppingCartFragment cartFragment = new ShoppingCartFragment();
    private final ProductFragment productFragment = new ProductFragment();
    private final OrderHistoryFragment orderHistoryFragment = new OrderHistoryFragment();
    private final UserProfileFragment userProfileFragment = new UserProfileFragment();

    private BottomNavigationView bottomNav;

    private BroadcastReceiver transactionSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bottomNav.setSelectedItemId(R.id.nav_product);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, productFragment).commit();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateProfileMenu();
            }
        }, 2000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(transactionSuccessReceiver, new IntentFilter(TRANSACTION_SUCCESS_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(transactionSuccessReceiver);
    }

    private void updateProfileMenu() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            Uri photoUri = currentUser.getPhotoUrl();

            if (photoUri != null) {
                Picasso.get().load(photoUri).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Drawable icon = new BitmapDrawable(getResources(), bitmap);
                        bottomNav.getMenu().findItem(R.id.nav_user_profile).setIcon(icon);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Log.e(TAG, "Failed to load profile picture.");
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        // Here you can place a placeholder while the image loads
                    }
                });
            }
            bottomNav.getMenu().findItem(R.id.nav_user_profile).setTitle(displayName != null ? displayName : "User Profile");

            String userId = currentUser.getUid();
            logUserData(userId);
            logUserSubCollection(userId, "cart");
            // Add additional logUserSubCollection calls for other subcollections
        }
    }

    private void logUserData(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "User Document Data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such user document");
                    }
                } else {
                    Log.d(TAG, "get user document failed with ", task.getException());
                }
            }
        });
    }

    private void logUserSubCollection(String userId, String subCollectionName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference collectionRef = db.collection("users").document(userId).collection(subCollectionName);

        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        Log.d(TAG, "Document in " + subCollectionName + ": " + doc.getData());
                    }
                } else {
                    Log.d(TAG, "Error getting documents in " + subCollectionName, task.getException());
                }
            }
        });
    }
    public ShoppingCartFragment getCartFragment() {
        return cartFragment;
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    int id = item.getItemId();

                    if (id == R.id.nav_cart) {
                        selectedFragment = cartFragment;
                    } else if (id == R.id.nav_product) {
                        selectedFragment = productFragment;
                    } else if (id == R.id.nav_order_history) {
                        selectedFragment = orderHistoryFragment;
                    } else if (id == R.id.nav_user_profile) {
                        selectedFragment = userProfileFragment;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                    return true;
                }
            };
}