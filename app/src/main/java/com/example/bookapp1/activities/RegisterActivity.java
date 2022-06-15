package com.example.bookapp1.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp1.databinding.ActivityRegisterBinding;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.RotatingPlane;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    // view binding
    private ActivityRegisterBinding binding;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    // progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // region note no.1
        /*
         *  1) Firebase Auth
         *  2) Firebase realtime database
         *  3) Firebase Storage
         * */
        // endregion

        // initializing firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Take your time while waiting...");
        progressDialog.setCanceledOnTouchOutside(false);

        Sprite RotatingPlane = new RotatingPlane();
        progressDialog.setIndeterminateDrawable(RotatingPlane);

        // handle clicking go back button event
        binding.backIBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onBackPressed();
                            }
                        }
                );

        // handle clicking register button event
        binding.registerBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                validatingData();
                            }
                        }
                );
    }

    private String name = "", email = "", password = "";

    private void validatingData() {
        // Before creating account, first, validating data

        // get data
        name = binding.nameETID.getText().toString().trim();
        email = binding.emailETID.getText().toString().trim();
        password = binding.passwordETID.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordETID.getText().toString().trim();

        // validating data
        if (TextUtils.isEmpty(name))
        {
            Toast.makeText(this, "Enter your name...", Toast.LENGTH_SHORT).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(this, "Invalid email!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Enter your password...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(confirmPassword))
        {
            Toast.makeText(this, "Confirm password first!", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(confirmPassword))
        {
            Toast.makeText(this, "The password doesn't match!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            createUserAccount();
        }
    }

    private void createUserAccount() {
        // show progress
        progressDialog.setMessage("Creating account...");
        progressDialog.show();

        // creating user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener
                        (
                                new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        // succeeded, now these will be added into firebase realtime database
                                        updateUserInfo();
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        // failed
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving user info...");

        // time stamp
        long timeStamp = System.currentTimeMillis();

        // get current user uid
        String uid = firebaseAuth.getUid();

        // setup data to add into the database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", "");
        hashMap.put("userType", "user");
        hashMap.put("timestamp", timeStamp);

        // set data to the database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // data added to the database
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Account created successfully!",
                                                Toast.LENGTH_SHORT).show();
                                        // since user's account is created => start user's dashboard
                                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                                        finish();
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        // data failed to add to the database
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
    }
}