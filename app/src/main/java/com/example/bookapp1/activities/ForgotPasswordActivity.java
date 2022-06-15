package com.example.bookapp1.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp1.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    // view binding
    private ActivityForgotPasswordBinding binding;

    // create firebase auth
    private FirebaseAuth firebaseAuth;

    // progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initializing firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // initializing/ setting up progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // handle click => go back
        binding.backIBtn10ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onBackPressed();
                            }
                        }
                );

        // handle click => begin recovering password
        binding.submitEmailBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                validateData();
                            }
                        }
                );
    }

    private String email = "";

    private void validateData() {
        // get email
        email = binding.emailET1ID.getText().toString().trim();

        // validating data (it mustn't be empty first)
        if (email.isEmpty()) {Toast.makeText(this, "Please enter email.", Toast.LENGTH_SHORT).show();}
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            recoverPassword();
        }
    }

    private void recoverPassword() {
        // show progress
        progressDialog.setMessage("Sending password recovery instructions to " + email);
        progressDialog.show();

        // begin sending recovery
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // sent
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this,
                                        "Instructions to reset password were sent to " + email, Toast.LENGTH_SHORT).show();
                    }})
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // failed to send email
                                        progressDialog.dismiss();
                                        Toast.makeText(ForgotPasswordActivity.this, "Failed to send email due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );

    }
}