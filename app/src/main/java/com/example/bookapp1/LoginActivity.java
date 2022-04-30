package com.example.bookapp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp1.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    // view binding
    private ActivityLoginBinding binding;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    // progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initializing firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Take your time while waiting...");
        progressDialog.setCanceledOnTouchOutside(false);

        // handle clicking "go to register screen" event
        binding.noAccountTVID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity
                                        (
                                                new Intent
                                                        (
                                                                LoginActivity.this,
                                                                RegisterActivity.class
                                                        )
                                        );
                            }
                        }
                );

        // handle "click to begin login" event
        binding.loginBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                validatingData();
                            }
                        }
                );
    }

    private String email = "", password = "";

    private void validatingData() {
        // Before accessing the app, first, validating data

        // getting data
        email = binding.emailETID.getText().toString().trim();
        password = binding.passwordETID.getText().toString().trim();

        // validating data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(this, "Invalid email!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Enter your password...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loginUser();
        }
    }

    private void loginUser() {
        // show progress
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        // login user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener
                        (
                                new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        // after login successfully, we have to check if user is user or admin
                                        checkUser();
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        // login failed
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
    }

    private void checkUser() {
        progressDialog.setMessage("Checking user...");

        // check if user is user or admin from realtime database
        // get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // check in the database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        progressDialog.dismiss();
                                        // get user type
                                        String userType = "" + dataSnapshot.child("userType").getValue();

                                        // check user type
                                        if(userType.equals("user"))
                                        {
                                            // this is normal user, open user dashboard
                                            startActivity
                                                    (
                                                            new Intent
                                                                    (
                                                                            LoginActivity.this,
                                                                            DashboardUserActivity.class
                                                                    )
                                                    );
                                            finish();
                                        }
                                        else if(userType.equals("admin"))
                                        {
                                            // this is admin, open admin dashboard
                                            startActivity
                                                    (
                                                            new Intent
                                                                    (
                                                                            LoginActivity.this,
                                                                            DashboardAdminActivity.class
                                                                    )
                                                    );
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );
    }
}