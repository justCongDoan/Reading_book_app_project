package com.example.bookapp1;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bookapp1.adapters.PdfUserAdapter;
import com.example.bookapp1.databinding.FragmentBookUserBinding;
import com.example.bookapp1.models.PdfModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookUserFragment extends Fragment {

    // these are passed while creating instance of this fragment
    private String categoryId, category, uid;

    private ArrayList<PdfModel> pdfModelArrayList;
    private PdfUserAdapter pdfUserAdapter;

    // view binding
    private FragmentBookUserBinding binding;

    private static final String TAG = "BOOKS_USER_TAG";

    public BookUserFragment() {
        // Required empty public constructor
    }

    public static BookUserFragment newInstance(String categoryId, String category, String uid) {
        BookUserFragment fragment = new BookUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBookUserBinding.inflate(LayoutInflater.from(getContext()), container, false);

        Log.d(TAG, "onCreateView: Category: " + category);
        if (category.equals("All"))
        {
            // load all books
            loadAllBooks();
        }
        else if (category.equals("Most Viewed"))
        {
            // load most viewed books
            loadMostViewedOrDownloadedBooks("viewsCount");
        }
        else if (category.equals("Most Downloaded"))
        {
            // load most downloaded books
            loadMostViewedOrDownloadedBooks("downloadsCount");
        }
        else
        {
            // load selected category books
            loadCategorizedBooks();
        }

        // search
        binding.searchET2ID.addTextChangedListener
                (
                        new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                // this method is called when user enters each character
                                try {
                                    pdfUserAdapter.getFilter().filter(charSequence);
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

        return binding.getRoot();
    }

    private void loadCategorizedBooks() {
        // initializing list
        pdfModelArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // clear the list before starting adding data to it
                                        pdfModelArrayList.clear();

                                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                                        {
                                            // get data
                                            PdfModel model = dataSnapshot.getValue(PdfModel.class);
                                            // add to the list
                                            pdfModelArrayList.add(model);
                                        }

                                        // setup adapter
                                        pdfUserAdapter = new PdfUserAdapter(getContext(), pdfModelArrayList);
                                        // set adapter to the recyclerView
                                        binding.bookRVID.setAdapter(pdfUserAdapter);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }

    private void loadMostViewedOrDownloadedBooks(String orderBy) {
        // initializing list
        pdfModelArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.orderByChild(orderBy).limitToLast(10)     // load 10 most viewed/downloaded books
                .addValueEventListener
                (
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // clear the list before starting adding data to it
                                pdfModelArrayList.clear();

                                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                                {
                                    // get data
                                    PdfModel model = dataSnapshot.getValue(PdfModel.class);
                                    // add to the list
                                    pdfModelArrayList.add(model);
                                }

                                // setup adapter
                                pdfUserAdapter = new PdfUserAdapter(getContext(), pdfModelArrayList);
                                // set adapter to the recyclerView
                                binding.bookRVID.setAdapter(pdfUserAdapter);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }
                );
    }

    private void loadAllBooks() {
        // initializing list
        pdfModelArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.addValueEventListener
                (
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // clear the list before starting adding data to it
                                pdfModelArrayList.clear();

                                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                                {
                                    // get data
                                    PdfModel model = dataSnapshot.getValue(PdfModel.class);
                                    // add to the list
                                    pdfModelArrayList.add(model);
                                }

                                // setup adapter
                                pdfUserAdapter = new PdfUserAdapter(getContext(), pdfModelArrayList);
                                // set adapter to the recyclerView
                                binding.bookRVID.setAdapter(pdfUserAdapter);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }
                );
    }
}