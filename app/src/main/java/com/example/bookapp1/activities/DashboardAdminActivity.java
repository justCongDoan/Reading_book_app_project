package com.example.bookapp1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp1.adapters.CategoryAdapter;
import com.example.bookapp1.databinding.ActivityDashboardAdminBinding;
import com.example.bookapp1.models.CategoryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {

    // view binding
    private ActivityDashboardAdminBinding binding;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    // create an arraylist to store category
    private ArrayList<CategoryModel> categoryModelArrayList;

    // create an adapter
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initializing firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadingCategories();

        // editing text change listener, search
        binding.searchETID.addTextChangedListener
                (
                        new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                // calling this method everytime user type each letter
                                try {
                                    categoryAdapter.getFilter().filter(charSequence);
                                }
                                catch (Exception e) {

                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        }
                );

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

        // handle "start adding book category screen" event
        binding.addCategoryBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity
                                        (
                                                new Intent
                                                        (
                                                                DashboardAdminActivity.this,
                                                                CategoryAddActivity.class
                                                        )
                                        );
                            }
                        }
                );

        // handle "click => start adding pdf file screen" event
        binding.addPdfFileFabID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity
                                        (
                                                new Intent
                                                        (
                                                                DashboardAdminActivity.this,
                                                                AddingPdfFileActivity.class
                                                        )
                                        );
                            }
                        }
                );
    }

    private void loadingCategories() {
        // initializing arraylist
        categoryModelArrayList = new ArrayList<>();

        // get all categories from firebase > Categories
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addValueEventListener
                (
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // clear arraylist before adding data into it
                                categoryModelArrayList.clear();

                                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                                {
                                    // get data
                                    CategoryModel model = dataSnapshot.getValue(CategoryModel.class);

                                    // add it to arraylist
                                    categoryModelArrayList.add(model);
                                }

                                // setup adapter
                                categoryAdapter = new CategoryAdapter(DashboardAdminActivity.this, categoryModelArrayList);

                                // set adapter to recycleView
                                binding.categoriesRVID.setAdapter(categoryAdapter);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

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