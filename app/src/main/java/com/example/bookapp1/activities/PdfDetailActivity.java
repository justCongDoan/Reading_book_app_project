package com.example.bookapp1.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bookapp1.MyApplication;
import com.example.bookapp1.R;
import com.example.bookapp1.adapters.CommentAdapter;
import com.example.bookapp1.databinding.ActivityPdfDetailBinding;
import com.example.bookapp1.databinding.AddCommentDialogBinding;
import com.example.bookapp1.models.CommentModel;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.ChasingDots;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {

    // view binding
    private ActivityPdfDetailBinding binding;

    // pdf id, got from intent
    String bookId, bookTitle, bookUrl;

    boolean isInMyFavourite = false;

    private FirebaseAuth firebaseAuth;

    private static final String TAG = "DOWNLOAD_TAG";

    // progress dialog
    private ProgressDialog progressDialog;

    // create an arraylist to hold comments
    private ArrayList<CommentModel> commentModelArrayList;
    // create an adapter to set to the recyclerView
    private CommentAdapter commentAdapter;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // getting data from intent (bookId for example)
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        // at first, hiding download button, as we need url of the book to load later in function loadBookDetails()
        binding.downloadBookBtnID.setVisibility(View.GONE);

        // initializing progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null)
        {
            checkIsFavourite();
        }

        loadBookDetails();
        loadComments();

        // increase book view count, whenever this page starts
        MyApplication.increasingBookViewCount(bookId);



        // go back
        binding.backIBtn5ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onBackPressed();
                            }
                        }
                );

        binding.readBookBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // create activity for reading book (pdf file)
                                Intent intent1 = new Intent
                                        (
                                                PdfDetailActivity.this,
                                                PdfViewActivity.class
                                        );
                                intent1.putExtra("bookId", bookId);
                                startActivity(intent1);
                            }
                        }
                );

        // handle click => download pdf file
        binding.downloadBookBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.d(TAG, "onClick: Checking permission...");
                                if (ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        == PackageManager.PERMISSION_GRANTED)
                                {
                                    Log.d(TAG, "onClick: The permission is already granted, the book can now be downloaded!");
                                    MyApplication.downloadBook(PdfDetailActivity.this, "" + bookId, "" + bookTitle, "" + bookUrl);
                                }
                                else
                                {
                                    Log.d(TAG, "onClick: The permission is not granted! Requesting...");
                                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                }
                            }
                        }
                );

        // handle clicking => add/remove favourite book
        binding.favouriteBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (firebaseAuth.getCurrentUser() == null)
                                {
                                    Toast.makeText(PdfDetailActivity.this, "You haven't logged in yet!", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    if (isInMyFavourite)
                                    {
                                        // in favourite, remove from the favourite list
                                        MyApplication.removeFromFavourite(PdfDetailActivity.this, bookId);
                                    }
                                    else
                                    {
                                        // not in favourite, add to the favourite list
                                        MyApplication.addToFavourite(PdfDetailActivity.this, bookId);
                                    }
                                }
                            }
                        }
                );

        // handle click => show comment adding dialog
        binding.addCommentIBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // first, user must log in in order to add comment
                                if (firebaseAuth.getCurrentUser() == null)
                                {
                                    Toast.makeText(PdfDetailActivity.this, "Please log in...", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    addCommentDialog();
                                }
                            }
                        }
                );
    }

    private void loadComments() {
        // initializing arraylist before adding data into it
        commentModelArrayList = new ArrayList<>();

        // create database path to load comments
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).child("Comments")
                .addValueEventListener
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // clear arraylist before start adding data into it
                                        commentModelArrayList.clear();
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                                        {
                                            // get data as model, spellings of variables in the model must be as same as in firebase
                                            CommentModel model = dataSnapshot.getValue(CommentModel.class);
                                            // add to the arraylist
                                            commentModelArrayList.add(model);
                                        }
                                        // setting up adapter
                                        commentAdapter = new CommentAdapter(PdfDetailActivity.this, commentModelArrayList);
                                        // set adapter to the recyclerView
                                        binding.commentsRVID.setAdapter(commentAdapter);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }

    private String comment = "";

    private void addCommentDialog() {
        // inflating bind view for dialog
        AddCommentDialogBinding addCommentDialogBinding = AddCommentDialogBinding.inflate(LayoutInflater.from(this));

        // setting up alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(addCommentDialogBinding.getRoot());

        // create & show alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // handle click => dismiss dialog
        addCommentDialogBinding.backIBtn9ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                            }
                        }
                );

        // handle click => add comment
        addCommentDialogBinding.submitCommentBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // get data
                                comment = addCommentDialogBinding.commentTIETID.getText().toString().trim();
                                // validating data
                                if (TextUtils.isEmpty(comment))
                                {
                                    Toast.makeText(PdfDetailActivity.this, "Enter your comment...", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    alertDialog.dismiss();
                                    addComment();
                                }
                            }
                        }
                );
    }

    private void addComment() {
        // show progress
        progressDialog.setMessage("Adding comment...");
        progressDialog.show();

        // create timestamp for comment id, comment time
        String timestamp = "" + System.currentTimeMillis();

        // setting up data to add into the database for the comments
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestamp);
        hashMap.put("bookId", "" + bookId);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("comment", "" + comment);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        // create Database path to add data into it
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener
                        (
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(PdfDetailActivity.this, "Comment Added successfully!", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // failed to add comment
                                        progressDialog.dismiss();
                                        Toast.makeText(PdfDetailActivity.this, "Failed to add comment due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
    }

    // request storage permission
    private ActivityResultLauncher<String> requestPermissionLauncher
            = registerForActivityResult
            (
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted)
                        {
                            Log.d(TAG, "Permission Granted!");
                            MyApplication.downloadBook(this, "" + bookId, "" + bookTitle, "" + bookUrl);
                        }
                        else
                        {
                            Log.d(TAG, "Permission denied!");
                            Toast.makeText(this, "Permission was denied!", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    private void loadBookDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // get data
                                        bookTitle = "" + snapshot.child("title").getValue();
                                        String description = "" + snapshot.child("description").getValue(),
                                                categoryId = "" + snapshot.child("categoryId").getValue(),
                                                viewsCount = "" + snapshot.child("viewsCount").getValue(),
                                                downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                                        bookUrl = "" + snapshot.child("url").getValue();
                                        String timeStamp = "" + snapshot.child("timeStamp").getValue();

                                        // required data is loaded, show download button
                                        binding.downloadBookBtnID.setVisibility(View.VISIBLE);

                                        // formatting data
                                        String date = MyApplication.timestampFormat(Long.parseLong(timeStamp));

                                        MyApplication.loadCategory
                                                (
                                                        "" + categoryId,
                                                        binding.categoryTV3ID
                                                );
                                        MyApplication.loadPdfFromUrlSinglePage
                                                (
                                                        "" + bookUrl,
                                                        "" + bookTitle,
                                                        binding.pdfView1ID,
                                                        binding.progressBar1ID,
                                                        binding.pagesTVID
                                                );
                                        MyApplication.loadPdfSize
                                                (
                                                        "" + bookUrl,
                                                        "" + bookTitle,
                                                        binding.sizeTVID
                                                );

                                        // set data
                                        binding.titleTV3ID.setText(bookTitle);
                                        binding.descriptionTV2ID.setText(description);
                                        binding.viewsTVID.setText(viewsCount.replace("null", "N/A"));
                                        binding.downloadTVID.setText(downloadsCount.replace("null", "N/A"));
                                        binding.dateTVID.setText(date);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }

    private void checkIsFavourite()
    {
        // logged in => check whether it's in the favourite list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favourite").child(bookId)
                .addValueEventListener
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        isInMyFavourite = snapshot.exists();
                                        // true <=> existed
                                        // false <=> not existed
                                        if (isInMyFavourite)
                                        {
                                            // exists in the favourite list
                                            binding.favouriteBtnID.setCompoundDrawablesRelativeWithIntrinsicBounds
                                                    (
                                                            0,
                                                            R.drawable.ic_favorite_white,
                                                            0,
                                                            0
                                                    );
                                            binding.favouriteBtnID.setText("Remove Favourite");
                                        }
                                        else
                                        {
                                            // exists in the favourite list
                                            binding.favouriteBtnID.setCompoundDrawablesRelativeWithIntrinsicBounds
                                                    (
                                                            0,
                                                            R.drawable.ic_favorite_border_white,
                                                            0,
                                                            0
                                                    );
                                            binding.favouriteBtnID.setText("Add Favourite");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }
}
// Friday, May 13th, 2022 - justCongDoan