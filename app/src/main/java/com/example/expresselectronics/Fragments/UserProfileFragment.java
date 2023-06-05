package com.example.expresselectronics.Fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.expresselectronics.R;
import com.example.expresselectronics.User.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class UserProfileFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private User user;
    private String userId;

    private TextView textViewName, textViewEmail, textViewUsername, textViewAddress;
    private ImageView userImageView;
    private Button buttonEditProfile , buttonChangePassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_fragment, container, false);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = firebaseUser.getUid();
        user = new User();
        user.setId(userId);
        textViewName = view.findViewById(R.id.textViewName);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewUsername = view.findViewById(R.id.textViewUsername);
        textViewAddress = view.findViewById(R.id.textViewAddress);
        userImageView = view.findViewById(R.id.userImageView);
        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        buttonChangePassword = view.findViewById(R.id.buttonChangePassword);
        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the current fragment with EditProfileFragment
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new EditProfileFragment());
                fragmentTransaction.addToBackStack(null); // Add the transaction to the back stack
                fragmentTransaction.commit();
            }
        });
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the current fragment with EditProfileFragment
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new EditPasswordProfileFragment());
                fragmentTransaction.addToBackStack(null); // Add the transaction to the back stack
                fragmentTransaction.commit();
            }
        });

        fetchUserProfile();

        return view;
    }

    private void fetchUserProfile() {
        if(userId != null){
            db.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    user = document.toObject(User.class);

                                    textViewName.setText(user.getName());
                                    textViewEmail.setText(user.getEmail());
                                    textViewUsername.setText(user.getUsername());
                                    textViewAddress.setText(user.getAddress());
                                    if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                                        Picasso.get().load(user.getImageUrl()).into(userImageView);
                                    } else {

                                        userImageView.setImageResource(R.drawable.ic_user_photo);
                                    }
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
        } else {
            Log.e(TAG, "fetchUserProfile: User ID is null");
        }
    }
}
