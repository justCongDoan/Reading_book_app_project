package com.example.bookapp1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
    String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // getting data from intent (bookId for example)
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

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
    }

    private void loadBookDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // get data
                                        String title = "" + snapshot.child("title").getValue(),
                                                description = "" + snapshot.child("description").getValue(),
                                                categoryId = "" + snapshot.child("categoryId").getValue(),
                                                viewsCount = "" + snapshot.child("viewsCount").getValue(),
                                                downloadsCount = "" + snapshot.child("downloadsCount").getValue(),
                                                url = "" + snapshot.child("url").getValue(),
                                                timeStamp = "" + snapshot.child("timeStamp").getValue();

                                        // formatting data
                                        String date = MyApplication.timestampFormat(Long.parseLong(timeStamp));

                                        MyApplication.loadCategory
                                                (
                                                        "" + categoryId,
                                                        binding.categoryTV3ID
                                                );
                                        MyApplication.loadPdfFromUrlSinglePage
                                                (
                                                        "" + url,
                                                        "" + title,
                                                        binding.pdfView1ID,
                                                        binding.progressBar1ID
                                                );
                                        MyApplication.loadPdfSize
                                                (
                                                        "" + url,
                                                        "" + title,
                                                        binding.sizeTVID
                                                );

                                        // set data
                                        binding.titleTV3ID.setText(title);
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