package com.example.bookapp1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp1.adapters.PdfAdminAdapter;
import com.example.bookapp1.databinding.ActivityPdfListAdminBinding;
import com.example.bookapp1.models.PdfModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {

    // view binding
    private ActivityPdfListAdminBinding binding;

    // create an arraylist to hold the list of data of PdfModel type
    private ArrayList<PdfModel> pdfModelArrayList;

    // adapter
    private PdfAdminAdapter pdfAdminAdapter;

    private String categoryId, categoryTitle;

    private static final String TAG = "PDF_LIST_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get data from intent
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");

        // set pdf category
        binding.subTitleTV1ID.setText(categoryTitle);
        
        loadPdfList();

        // searching
        binding.searchET1ID.addTextChangedListener
                (
                        new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                // search as and when user type each letter
                                try {
                                    pdfAdminAdapter.getFilter().filter(charSequence);
                                }
                                catch (Exception e) {
                                    Log.d(TAG, "onTextChanged: " + e.getMessage());
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        }
                );

        // handling click, go to previous activity
        binding.backIBtn3ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onBackPressed();
                            }
                        }
                );
    }

    private void loadPdfList() {
        // initializing list before adding data
        pdfModelArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        pdfModelArrayList.clear();
                                        for(DataSnapshot snapshot1 : snapshot.getChildren())
                                        {
                                            // get data
                                            PdfModel model = snapshot1.getValue(PdfModel.class);
                                            // add to list
                                            pdfModelArrayList.add(model);

                                            Log.d(TAG, "onDataChange: " + model.getId() + " " + model.getTitle());
                                        }
                                        // setup adapter
                                        pdfAdminAdapter = new PdfAdminAdapter(PdfListAdminActivity.this, pdfModelArrayList);
                                        binding.bookListRVID.setAdapter(pdfAdminAdapter);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }
}