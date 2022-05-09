package com.example.bookapp1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp1.Constants;
import com.example.bookapp1.databinding.ActivityPdfViewBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PdfViewActivity extends AppCompatActivity {

    // view binding
    private ActivityPdfViewBinding binding;

    private String bookId;

    private static final String TAG = "PDF_VIEW_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get bookId from intent
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        Log.d(TAG, "onCreate: BookId: " + bookId);
        
        loadBookDetails();

        // handle clicking "go back" button
        binding.backIBtn6ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onBackPressed();
                            }
                        }
                );
    }

    private void loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Getting PDF file's URL...");
        // create database reference to get the book details
        // Step 1: Get book Url using book id
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // get book url
                                        String pdfUrl = "" + snapshot.child("url").getValue();
                                        Log.d(TAG, "onDataChange: PDF URL: " + pdfUrl);
                                        
                                        // Step 2: load pdf using its url from firebase storage
                                        loadBookFromUrl(pdfUrl);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }

    private void loadBookFromUrl(String pdfUrl) {
        Log.d(TAG, "loadBookFromUrl: Get PDF from storage.");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener
                        (
                                new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        // loading pdf using bytes
                                        binding.pdfView2ID.fromBytes(bytes)
                                                // swipe horizontal:
                                                //      true - horizontal
                                                //      false - vertical
                                                .swipeHorizontal(false)
                                                .onPageChange
                                                        (
                                                                new OnPageChangeListener() {
                                                                    @Override
                                                                    public void onPageChanged(int page, int pageCount) {
                                                                        // set current and total pages in toolbar subtitle
                                                                        int currentPage = (page + 1);   // starting from page 0, +1 to move to next page
                                                                        binding.toolbarSubtitleTVID.setText(currentPage + "/" + pageCount);     // e.g: 3/412
                                                                        Log.d(TAG, "onPageChanged: " + currentPage + "/" + pageCount);
                                                                    }
                                                                }
                                                        )
                                                .onError
                                                        (
                                                                new OnErrorListener() {
                                                                    @Override
                                                                    public void onError(Throwable t) {
                                                                        Log.d(TAG, "onError: " + t.getMessage());
                                                                        Toast.makeText(PdfViewActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                        )
                                                .onPageError
                                                        (
                                                                new OnPageErrorListener() {
                                                                    @Override
                                                                    public void onPageError(int page, Throwable t) {
                                                                        Log.d(TAG, "onPageError: " + t.getMessage());
                                                                        Toast.makeText(PdfViewActivity.this, "Error on page " + page + " " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                        ).load();
                                        binding.progressBar2ID.setVisibility(View.GONE);
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: " + e.getMessage());
                                        // failed to load book
                                        binding.progressBar2ID.setVisibility(View.GONE);
                                    }
                                }
                        );
    }
}