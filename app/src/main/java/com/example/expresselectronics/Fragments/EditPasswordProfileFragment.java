package com.example.expresselectronics.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.expresselectronics.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditPasswordProfileFragment extends Fragment {

    private EditText editTextCurrentPassword;
    private EditText editTextNewPassword;
    private EditText editTextConfirmNewPassword;
    private Button buttonSave;

    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_password, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        editTextCurrentPassword = view.findViewById(R.id.editTextCurrentPassword);
        editTextNewPassword = view.findViewById(R.id.editTextNewPassword);
        editTextConfirmNewPassword = view.findViewById(R.id.editTextConfirmNewPassword);
        buttonSave = view.findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });

        return view;
    }

    private void updatePassword() {
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        final String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmNewPassword = editTextConfirmNewPassword.getText().toString().trim();

        // Validate new password fields
        if (newPassword.isEmpty()) {
            editTextNewPassword.setError("Enter a new password");
            editTextNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            editTextNewPassword.setError("Password must be at least 6 characters");
            editTextNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            editTextConfirmNewPassword.setError("Passwords do not match");
            editTextConfirmNewPassword.requestFocus();
            return;
        }

        // Reauthenticate the user with their current password
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Update the user's password
                            currentUser.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                FragmentManager fragmentManager = getParentFragmentManager();
                                                fragmentManager.popBackStack();
                                                Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getActivity(), "Failed to update password", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(getActivity(), "Failed to reauthenticate", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
