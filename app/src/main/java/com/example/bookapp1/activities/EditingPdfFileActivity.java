package com.example.bookapp1.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp1.databinding.ActivityEditingPdfFileBinding;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.RotatingPlane;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class EditingPdfFileActivity extends AppCompatActivity {

    // view binding
    private ActivityEditingPdfFileBinding binding;

    // get book id from intent started from PdfAdminAdapter
    private String bookId;

    // progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    private static final String TAG = "BOOK_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditingPdfFileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // book id got from intent started from PdfAdminAdapter
        bookId = getIntent().getStringExtra("bookId");

        // setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        Sprite RotatingPlane = new RotatingPlane();
        progressDialog.setIndeterminateDrawable(RotatingPlane);

        loadCategories();
        loadBookInfo();

        // handle clicking => pick category
        binding.categoryTV1ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                categoryDialog();
                            }
                        }
                );

        // handle clicking => go to previous screen
        binding.backIBtn4ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onBackPressed();
                            }
                        }
                );

        // handle clicking => begin updating
        binding.updateBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                validateData();
                            }
                        }
                );
    }

    String title = "", description = "";

    private void validateData() {
        // get data
        title = binding.titleET1ID.getText().toString().trim();
        description = binding.descriptionET1ID.getText().toString().trim();

        // validate data
        if (TextUtils.isEmpty(title))
        {
            Toast.makeText(this, "Please enter title.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description))
        {
            Toast.makeText(this, "Please enter description.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryId))
        {
            Toast.makeText(this, "Please pick a category.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            updatePdfInfo();
        }
    }

    private void updatePdfInfo() {
        Log.d(TAG, "updatePdf: Starting updating pdf info to the database...");

        // show progress
        progressDialog.setMessage("Updating book info...");
        progressDialog.show();

        // setup data to update to the database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", title);
        hashMap.put("description", description);
        hashMap.put("categoryId", "" + selectedCategoryId);

        // start updating
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener
                        (
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Book updated successfully.");
                                        progressDialog.dismiss();
                                        Toast.makeText(EditingPdfFileActivity.this, "Book info updated successfully!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to update info due to " + e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(EditingPdfFileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
    }

    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info...");

        DatabaseReference bookReference = FirebaseDatabase.getInstance().getReference("Books");
        bookReference.child(bookId)
                .addListenerForSingleValueEvent
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        selectedCategoryId = "" + snapshot.child("categoryId").getValue();
                                        String description = "" + snapshot.child("description").getValue(),
                                                title = "" + snapshot.child("title").getValue();

                                        // set to views
                                        binding.titleET1ID.setText(title);
                                        binding.descriptionET1ID.setText(description);

                                        Log.d(TAG, "onDataChange: Loading book category info...");
                                        DatabaseReference bookCategoryReference = FirebaseDatabase.getInstance().getReference("Categories");
                                        bookCategoryReference.child(selectedCategoryId)
                                                .addListenerForSingleValueEvent
                                                        (
                                                                new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        // get category
                                                                        String category = "" + snapshot.child("category").getValue();
                                                                        // set to category text view
                                                                        binding.categoryTV1ID.setText(category);
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                }
                                                        );
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }

    private String selectedCategoryId = "", selectedCategoryTitle = "";

    private void categoryDialog()
    {
        // create a string array from arrayList of string
        String[] categoriesArray = new String[categoryTitleArrayList.size()];

        for (int i = 0; i < categoryTitleArrayList.size(); i++)
        {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        // create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose category")
                .setItems
                        (
                                categoriesArray,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        selectedCategoryId = categoryIdArrayList.get(i);
                                        selectedCategoryTitle = categoryTitleArrayList.get(i);

                                        // set to textView
                                        binding.categoryTV1ID.setText(selectedCategoryTitle);
                                    }
                                }
                        ).show();
    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories...");

        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent
                (
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                categoryIdArrayList.clear();
                                categoryTitleArrayList.clear();

                                for (DataSnapshot snapshot1 : snapshot.getChildren())
                                {
                                    String id = "" + snapshot1.child("id").getValue(),
                                            category = "" + snapshot1.child("category").getValue();
                                    categoryIdArrayList.add(id);
                                    categoryTitleArrayList.add(category);

                                    Log.d(TAG, "onDataChange: ID: " + id);
                                    Log.d(TAG, "onDataChange: Category: " + category);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }
                );
    }
}