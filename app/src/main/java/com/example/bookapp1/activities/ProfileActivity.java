package com.example.bookapp1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookapp1.MyApplication;
import com.example.bookapp1.R;
import com.example.bookapp1.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    // view binding
    private ActivityProfileBinding binding;

    // create firebase auth for loading user data using uid
    private FirebaseAuth firebaseAuth;
    
    private static final String TAG = "PROFILE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // setup firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        // handle click => start profile editing screen
        binding.editProfileIBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity
                                        (
                                                new Intent
                                                        (
                                                                ProfileActivity.this,
                                                                EditProfileActivity.class
                                                        )
                                        );
                            }
                        }
                );

        // handle click => go back
        binding.backIBtn7ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onBackPressed();
                            }
                        }
                );
    }

    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading user information of user " + firebaseAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // get all info of selected user from snapshot
                                        String name = "" + snapshot.child("name").getValue(),
                                                email = "" + snapshot.child("email").getValue(),
                                                profileImage = "" + snapshot.child("profileImage").getValue(),
                                                timestamp = "" + snapshot.child("timestamp").getValue(),
                                                uid = "" + snapshot.child("uid").getValue(),
                                                userType = "" + snapshot.child("userType").getValue();

                                        // formatting date to dd/MM/yyyy
                                        String formattedDate = MyApplication.timestampFormat(Long.parseLong(timestamp));

                                        // set data to the UI
                                        binding.profileEmailTVID.setText(email);
                                        binding.profileNameTVID.setText(name);
                                        binding.memberDateTVID.setText(formattedDate);
                                        binding.accountTypeTVID.setText(userType);

                                        // setting image using glide
                                        Glide.with(ProfileActivity.this)
                                                .load(profileImage)
                                                .placeholder(R.drawable.ic_person_gray)
                                                .into(binding.profileSIVID);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }
}