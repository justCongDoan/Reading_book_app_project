package com.example.bookapp1.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp1.databinding.ActivityCategoryAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {

    // view binding
    private ActivityCategoryAddBinding binding;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    // progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initializing firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Take your time while waiting...");
        progressDialog.setCanceledOnTouchOutside(false);

        // handle "clicking go back button" event
        binding.backIBtn1ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onBackPressed();
                            }
                        }
                );

        // handle "begin uploading category" event
        binding.submitToAddCategoryBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                validateData();
                            }
                        }
                );
    }

    private String category = "";

    private void validateData() {
        // before adding category, first, validating data

        // get data
        category = binding.categoryETID.getText().toString().trim();
        // validating (if it's not empty)
        if(TextUtils.isEmpty(category))
        {
            Toast.makeText(this, "Enter the category first!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            addingCategoryFirebase();
        }
    }

    private void addingCategoryFirebase() {
        // show progress
        progressDialog.setMessage("Adding category...");
        progressDialog.show();

        // get time stamp
        long timeStamp = System.currentTimeMillis();

        // setup info to add into the firebase database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timeStamp);
        hashMap.put("category", "" + category);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        // adding to the firebase database... Database Root > Categories > categoryId > category info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.child("" + timeStamp)
                .setValue(hashMap)
                .addOnSuccessListener
                        (
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // adding category successfully
                                        progressDialog.dismiss();
                                        Toast.makeText(CategoryAddActivity.this, "Category added successfully!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // adding category successfully
                                        progressDialog.dismiss();
                                        Toast.makeText(CategoryAddActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
    }
}