package com.example.bookapp1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    // firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // initializing firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // start main screen after 2 seconds
        new Handler().postDelayed
                (
                        new Runnable() {
                            @Override
                            public void run() {
                                checkUser();
                            }
                        }
                        , 2000
                );
    }

    private void checkUser() {
        // get current user, if logged in
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null)
        {
            // user not logged in
            // start main screen
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }
        else
        {
            // after logging in, check user type, same as done in login screen
            // check in the database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent
                            (
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            // get user type
                                            String userType = "" + dataSnapshot.child("userType").getValue();

                                            // check user type
                                            if(userType.equals("user"))
                                            {
                                                // this is normal user, open user dashboard
                                                startActivity(new Intent(SplashActivity.this, DashboardUserActivity.class));
                                                finish();
                                            }
                                            else if(userType.equals("admin"))
                                            {
                                                // this is admin, open admin dashboard
                                                startActivity(new Intent(SplashActivity.this, DashboardAdminActivity.class));
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
}