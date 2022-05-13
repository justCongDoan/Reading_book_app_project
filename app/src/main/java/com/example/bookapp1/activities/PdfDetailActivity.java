package com.example.bookapp1.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bookapp1.MyApplication;
import com.example.bookapp1.R;
import com.example.bookapp1.adapters.FavoritePdfAdapter;
import com.example.bookapp1.databinding.ActivityPdfDetailBinding;
import com.example.bookapp1.models.PdfModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfDetailActivity extends AppCompatActivity {

    // view binding
    private ActivityPdfDetailBinding binding;

    // pdf id, got from intent
    String bookId, bookTitle, bookUrl;

    boolean isInMyFavourite = false;

    private FirebaseAuth firebaseAuth;

    private static final String TAG = "DOWNLOAD_TAG";

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

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null)
        {
            checkIsFavourite();
        }

        loadBookDetails();

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