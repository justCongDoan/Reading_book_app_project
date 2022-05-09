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
import com.example.bookapp1.databinding.ActivityPdfDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PdfDetailActivity extends AppCompatActivity {

    // view binding
    private ActivityPdfDetailBinding binding;

    // pdf id, got from intent
    String bookId, bookTitle, bookUrl;

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
                                                        binding.progressBar1ID
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
}