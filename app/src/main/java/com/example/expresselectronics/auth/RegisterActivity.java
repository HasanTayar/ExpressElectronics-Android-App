package com.example.expresselectronics.auth;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expresselectronics.MainActivity;
import com.example.expresselectronics.R;
import com.example.expresselectronics.User.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;

    private EditText nameEditText;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText addressEditText;
    private ImageView userImageView;

    private Uri selectedImageUri;

    private static final int SELECT_IMAGE_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        nameEditText = findViewById(R.id.editTextName);
        usernameEditText = findViewById(R.id.editTextUsername);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        addressEditText = findViewById(R.id.editTextAddress);
        userImageView = findViewById(R.id.userImageView);

        Button selectImageButton = findViewById(R.id.buttonSelectImage);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE_REQUEST);
            }
        });

        Button registerButton = findViewById(R.id.buttonRegister);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            userImageView.setImageURI(selectedImageUri);
        }
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        // Check if any of the fields is empty
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!isEmailValid(email)) {
            Toast.makeText(RegisterActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password length
        if (!isPasswordValid(password)) {
            Toast.makeText(RegisterActivity.this, "Password should be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User registration successful
                            FirebaseUser user = mAuth.getCurrentUser();
                            uploadImageAndCreateUser(user, name, username, address);
                        } else {
                            // User registration failed
                            Toast.makeText(RegisterActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Register: ", task.getException());
                        }
                    }
                });
    }

    private boolean isEmailValid(String email) {

        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {

        return password.length() >= 6;
    }


    private void uploadImageAndCreateUser(FirebaseUser user, String name, String username, String address) {
        if (selectedImageUri != null) {
            StorageReference userImageRef = mStorageRef.child("images/" + user.getUid());
            userImageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            userImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    User newUser = new User(user.getUid(), name, username, user.getEmail(), address, uri.toString());
                                    db.collection("users").document(user.getUid()).set(newUser)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        startMainActivity();
                                                    } else {
                                                        Toast.makeText(RegisterActivity.this, "User creation failed.", Toast.LENGTH_SHORT).show();
                                                        Log.w(TAG, "User creation failed: ", task.getException());
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(RegisterActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            User newUser = new User(user.getUid(), name, username, user.getEmail(), address, null);
            db.collection("users").document(user.getUid()).set(newUser)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                startMainActivity();
                            } else {
                                Toast.makeText(RegisterActivity.this, "User creation failed.", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "User creation failed: ", task.getException());
                            }
                        }
                    });
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
