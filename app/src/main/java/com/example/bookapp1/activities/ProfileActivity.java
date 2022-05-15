package com.example.bookapp1.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookapp1.MyApplication;
import com.example.bookapp1.R;
import com.example.bookapp1.adapters.FavoritePdfAdapter;
import com.example.bookapp1.databinding.ActivityProfileBinding;
import com.example.bookapp1.models.PdfModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    // view binding
    private ActivityProfileBinding binding;

    // create firebase auth for loading user data using uid
    private FirebaseAuth firebaseAuth;
    // create firebase current user
    private FirebaseUser firebaseUser;

    // create an arraylist to hold the books
    private ArrayList<PdfModel> pdfModelArrayList;
    // create an adapter to set in recyclerView
    private FavoritePdfAdapter favoritePdfAdapter;

    // progress dialog
    private ProgressDialog progressDialog;
    
    private static final String TAG = "PROFILE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // resetting data of user's info
        binding.accountTypeTVID.setText("N/A");
        binding.memberDateTVID.setText("N/A");
        binding.favouriteBooksCountTVID.setText("N/A");
        binding.accountStatusTVID.setText("N/A");

        // setup firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        // get current user
        firebaseUser = firebaseAuth.getCurrentUser();

        // initializing/setting up progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadUserInfo();
        loadFavoriteBooks();

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

        // handle click => verifying user (if not)
        binding.accountStatusTVID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (firebaseUser.isEmailVerified())
                                {
                                    // if having verified already
                                    Toast.makeText(ProfileActivity.this, "Already verified.", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    // if not, show confirmation dialog first
                                    emailVerificationDialog();
                                }
                            }
                        }
                );
    }

    private void emailVerificationDialog() {
        // Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please wait")
                .setMessage("Are you sure you want to to send email verification instructions to your email: " + firebaseUser.getEmail() + "?")
                .setPositiveButton
                        (
                                "SEND",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        sendEmailVerification();
                                    }
                                }
                        )
                .setNegativeButton
                        (
                                "CANCEL",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }
                        ).show();
    }

    private void sendEmailVerification() {
        // show progress
        progressDialog.setMessage("Sending email verification instructions to your email: " + firebaseUser.getEmail());
        progressDialog.show();

        firebaseUser.sendEmailVerification()
                .addOnSuccessListener
                        (
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // successfully sent
                                        progressDialog.dismiss();
                                        Toast.makeText(ProfileActivity.this, "Instructions were sent to your email: " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // failed to send
                                        progressDialog.dismiss();
                                        Toast.makeText(ProfileActivity.this, "Failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
    }

    private void loadFavoriteBooks() {
        // initializing list
        pdfModelArrayList = new ArrayList<>();

        // load favorite books from the database
        // Users > userId > Favourite
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favourite")
                .addValueEventListener
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // clear list before starting adding data
                                        pdfModelArrayList.clear();
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                                        {
                                            // get only the bookId here, and will get other details in adapter using that bookId
                                            String bookId = "" + dataSnapshot.child("bookId").getValue();

                                            // setting data to the model
                                            PdfModel pdfModel = new PdfModel();
                                            pdfModel.setId(bookId);

                                            // add model to the list
                                            pdfModelArrayList.add(pdfModel);
                                        }

                                        // set the number of favorite books
                                        binding.favouriteBooksCountTVID.setText("" + pdfModelArrayList.size());

                                        // setting up adapter
                                        favoritePdfAdapter = new FavoritePdfAdapter(ProfileActivity.this, pdfModelArrayList);

                                        // set adapter to recyclerView
                                        binding.bookRV1ID.setAdapter(favoritePdfAdapter);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }

    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading user information of user " + firebaseAuth.getUid());

        // get email verification status, after that, user must re-login to get changes
        if (firebaseUser.isEmailVerified())
        {
            binding.accountStatusTVID.setText("Verified");
        }
        else
        {
            binding.accountStatusTVID.setText("Not verified");
        }

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
// Friday, May 13th, 2022 - justCongDoan