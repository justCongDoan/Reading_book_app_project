package com.example.bookapp1.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp1.databinding.ActivityAddingPdfFileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class AddingPdfFileActivity extends AppCompatActivity {

    // setup view binding
    private ActivityAddingPdfFileBinding binding;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    // progress dialog
    private ProgressDialog progressDialog;

    // create arraylist to hold pdf categories
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    // URI of picked pdf
    private Uri pdfUri = null;

    private static final int PDF_PICK_CODE = 1000;

    // TAG for debugging
    private static final String TAG = "ADD_PDF_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddingPdfFileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initializing firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        
        loadingPdfCategories();

        // setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // handle click "go to previous activity" event
        binding.backIBtn2ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onBackPressed();
                            }
                        }
                );

        // handle "click, attach pdf" event
        binding.attachIBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                pdfPickIntent();
                            }
                        }
                );

        // handle "click, pick category" event
        binding.categoryTV1ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                pickingCategoryDialog();
                            }
                        }
                );

        // handle "click, upload pdf" event
        binding.uploadBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // validating data
                                validatingData();
                            }
                        }
                );
    }

    private String title = "", description = "";

    private void validatingData() {
        // Step 1: Validate data
        Log.d(TAG, "validatingData: validating data...");
        // get data
        title = binding.titleETID.getText().toString().trim();
        description = binding.descriptionETID.getText().toString().trim();
        // validate data
        if(TextUtils.isEmpty(title))
        {
            Toast.makeText(this, "Please enter title...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description))
        {
            Toast.makeText(this, "Please enter description...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(selectedCategoryTitle))
        {
            Toast.makeText(this, "Please pick a category...", Toast.LENGTH_SHORT).show();
        }
        else if(pdfUri == null)
        {
            Toast.makeText(this, "Please pick a pdf file...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // uploading all data (if those are valid)
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        // Step 2: Upload Pdf file to firebase storage
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...");

        // show progress
        progressDialog.setMessage("Uploading pdf file...");
        progressDialog.show();

        // timeStamp
        long timeStamp = System.currentTimeMillis();

        // path of pdf file in firebase storage
        String filePathAndName = "Books/" + timeStamp;
        // storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: PDF file is uploaded to the storage...");
                        Log.d(TAG, "onSuccess: Getting PDF url...");

                        // getting pdf url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = "" + uriTask.getResult();
                                        
                        // uploading to the firebase database
                        uploadingPdfInfoToDB(uploadedPdfUrl, timeStamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: PDF uploading failed due to " + e.getMessage());
                        Toast.makeText(AddingPdfFileActivity.this, "PDF uploading failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadingPdfInfoToDB(String uploadedPdfUrl, long timeStamp) {
        // Step 3: Upload Pdf info to firebase database
        Log.d(TAG, "uploadingPdfInfoToDB: uploading Pdf info to firebase database...");
        progressDialog.setMessage("Uploading PDF info...");
        String uid = firebaseAuth.getUid();
        // setup data to upload
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + uid);
        hashMap.put("id", "" + timeStamp);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("categoryId", "" + selectedCategoryId);
        hashMap.put("url", "" + uploadedPdfUrl);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);

        // db reference: DB > Books
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child("" + timeStamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Uploaded successfully!");
                        Toast.makeText(AddingPdfFileActivity.this, "Uploaded successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload to the database due to " + e.getMessage());
                        Toast.makeText(AddingPdfFileActivity.this, "Failed to upload to the database due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadingPdfCategories() {
        Log.d(TAG, "loadingPdfCategories: Loading pdf categories...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        // db reference to load categories... db > Categories
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent
                (
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // clear before adding data
                                categoryTitleArrayList.clear();
                                categoryIdArrayList.clear();

                                for(DataSnapshot snapshot1 : snapshot.getChildren())
                                {
                                    // get id and title of category
                                    String categoryId = "" + snapshot1.child("id").getValue(),
                                            categoryTitle = "" + snapshot1.child("category").getValue();

                                    // add to respective arraylist
                                    categoryTitleArrayList.add(categoryTitle);
                                    categoryIdArrayList.add(categoryId);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }
                );
    }

    // creating selected category id and category title
    private String selectedCategoryId, selectedCategoryTitle;

    private void pickingCategoryDialog() {
        Log.d(TAG, "pickingCategoryDialog: showing category pick dialog");

        // getting string array of categories from arraylist
        String[] categoriesArray = new String[categoryTitleArrayList.size()];

        for (int i = 0; i < categoryTitleArrayList.size(); i++)
        {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        // alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems
                        (
                                categoriesArray,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // handle clicking item
                                        // get clicked item from list
                                        selectedCategoryTitle = categoryTitleArrayList.get(i);
                                        selectedCategoryId = categoryIdArrayList.get(i);

                                        // set to category textView
                                        binding.categoryTV1ID.setText(selectedCategoryTitle);

                                        Log.d(TAG, "onClick: Selected Category: " + selectedCategoryId + " - " + selectedCategoryTitle);
                                    }
                                }
                        ).show();
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent...");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult
                (
                        Intent.createChooser(intent, "Select Pdf"),
                        PDF_PICK_CODE
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            if(requestCode == PDF_PICK_CODE)
            {
                Log.d(TAG, "onActivityResult: PDF picked");

                pdfUri = data.getData();
                
                Log.d(TAG, "onActivityResult: URI: " + pdfUri);
            }
        }
        else
        {
            Log.d(TAG, "onActivityResult: picking pdf cancelled");
            Toast.makeText(this, "picking pdf cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}