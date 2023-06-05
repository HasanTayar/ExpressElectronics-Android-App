package com.example.expresselectronics.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class EditProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    private User user;
    private String userId;

    private ImageView userImageView;
    private Button buttonChangeImage;
    private EditText editTextName;
    private EditText editTextUsername;
    private EditText editTextAddress;
    private Button buttonSave;

    private Uri imageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_fragment, container, false);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
        }
        user = new User();
        user.setId(userId);

        userImageView = view.findViewById(R.id.userImageView);
        buttonChangeImage = view.findViewById(R.id.buttonChangeImage);
        editTextName = view.findViewById(R.id.editTextName);
        editTextUsername = view.findViewById(R.id.editTextUsername);
        editTextAddress = view.findViewById(R.id.editTextAddress);
        buttonSave = view.findViewById(R.id.buttonSave);

        buttonChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        fetchUserProfile();

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(userImageView);
        }
    }

    private void fetchUserProfile() {
        if (userId != null) {
            db.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    user = document.toObject(User.class);

                                    // Populate the user profile fields
                                    editTextName.setText(user.getName());
                                    editTextUsername.setText(user.getUsername());
                                    editTextAddress.setText(user.getAddress());
                                    if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                                        Picasso.get().load(user.getImageUrl()).into(userImageView);
                                    } else {
                                        userImageView.setImageResource(R.drawable.ic_user_photo);
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "Failed to fetch user profile", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), "Failed to fetch user profile", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "User ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfile() {
        // Update the user object with the new information, including the imageUri
        user.setName(editTextName.getText().toString().trim());
        user.setUsername(editTextUsername.getText().toString().trim());
        user.setAddress(editTextAddress.getText().toString().trim());

        if (imageUri != null) {
            // Upload the image to Firebase Storage
            StorageReference imageRef = storageRef.child("user_images/" + userId);
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get the download URL of the uploaded image
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Update the user's profile image URL in the Firestore document
                                    user.setImageUrl(uri.toString());

                                    // Update the Firestore document with the updated user object
                                    db.collection("users").document(userId)
                                            .set(user)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Navigate back to the UserProfileFragment
                                                        FragmentManager fragmentManager = getParentFragmentManager();
                                                        fragmentManager.popBackStack();
                                                        Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // No image selected, update the Firestore document with the updated user object
            db.collection("users").document(userId)
                    .set(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Navigate back to the UserProfileFragment
                                FragmentManager fragmentManager = getParentFragmentManager();
                                fragmentManager.popBackStack();
                                Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
