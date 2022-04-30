package com.example.bookapp1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.bookapp1.databinding.ActivityDashboardUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardUserActivity extends AppCompatActivity {

    // view binding
    private ActivityDashboardUserBinding binding;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initializing firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        // handle "click logout button" event
        binding.logoutBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                firebaseAuth.signOut();
                                checkUser();
                            }
                        }
                );
    }

    private void checkUser() {
        // get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null)
        {
            // not logged in, goto main screen
            startActivity
                    (
                            new Intent
                                    (
                                            this,
                                            MainActivity.class
                                    )
                    );
            finish();
        }
        else
        {
            // logged in, get user info
            String email = firebaseUser.getEmail();

            // set in textView of toolbar
            binding.subTitleTVID.setText(email);
        }
    }
}