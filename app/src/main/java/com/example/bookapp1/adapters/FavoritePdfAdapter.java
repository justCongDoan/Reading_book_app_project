package com.example.bookapp1.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp1.MyApplication;
import com.example.bookapp1.activities.PdfDetailActivity;
import com.example.bookapp1.databinding.RowPdfFavoriteBinding;
import com.example.bookapp1.models.PdfModel;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.ChasingDots;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoritePdfAdapter extends RecyclerView.Adapter<FavoritePdfAdapter.HolderPdfFavorite> {

    private Context context;
    private ArrayList<PdfModel> pdfModelArrayList;

    // view binding for row_pdf_favorite.xml
    private RowPdfFavoriteBinding binding;

    private static final String TAG = "FAV_BOOK_TAG";

    // create a constructor
    public FavoritePdfAdapter(Context context, ArrayList<PdfModel> pdfModelArrayList) {
        this.context = context;
        this.pdfModelArrayList = pdfModelArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind/inflate row_pdf_favorite.xml
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorite holder, int position) {
        // get, set data, handle click
        PdfModel model = pdfModelArrayList.get(position);

        loadPdfDetails(model, holder);

        // handle click => open pdf detail page
        holder.itemView.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent
                                        (
                                                context,
                                                PdfDetailActivity.class
                                        );
                                intent.putExtra("bookId", model.getId());   // pass BOOK id, NOT category id
                                context.startActivity(intent);
                            }
                        }
                );

        // handle click => remove from favorite list
        holder.removeFavIBtn.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                MyApplication.removeFromFavourite(context, model.getId());  // pass BOOK id, NOT category id
                            }
                        }
                );
    }

    private void loadPdfDetails(PdfModel model, HolderPdfFavorite holder) {
        String bookId = model.getId();
        Log.d(TAG, "loadPdfDetails: Book Details of book ID: " + bookId);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // get book data
                                        String bookTitle = "" + snapshot.child("title").getValue(),
                                                description = "" + snapshot.child("description").getValue(),
                                                categoryId = "" + snapshot.child("categoryId").getValue(),
                                                bookUrl = "" + snapshot.child("url").getValue(),
                                                timestamp = "" + snapshot.child("timeStamp").getValue(),
                                                uid = "" + snapshot.child("uid").getValue(),
                                                viewsCount = "" + snapshot.child("viewsCount").getValue(),
                                                downloadsCount = "" + snapshot.child("downloadsCount").getValue();

                                        // set to model
                                        model.setFavorite(true);
                                        model.setTitle(bookTitle);
                                        model.setDescription(description);
                                        model.setTimeStamp(Long.parseLong(timestamp));
                                        model.setCategoryId(categoryId);
                                        model.setUid(uid);
                                        model.setUrl(bookUrl);

                                        // formatting data
                                        String date = MyApplication.timestampFormat(Long.parseLong(timestamp));

                                        MyApplication.loadCategory
                                                (
                                                        categoryId,
                                                        holder.categoryTV
                                                );
                                        MyApplication.loadPdfFromUrlSinglePage
                                                (
                                                        "" + bookUrl,
                                                        "" + bookTitle,
                                                        holder.pdfView,
                                                        holder.progressBar,
                                                        null
                                                );
                                        MyApplication.loadPdfSize
                                                (
                                                        "" + bookUrl,
                                                        "" + bookTitle,
                                                        holder.sizeTV
                                                );

                                        // set data to the views
                                        holder.titleTV.setText(bookTitle);
                                        holder.descriptionTV.setText(description);
                                        holder.dateTV.setText(date);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }

    @Override
    public int getItemCount() {
        return pdfModelArrayList.size();   // return list size or the number of rezords
    }

    // create ViewHolder class
    class HolderPdfFavorite extends RecyclerView.ViewHolder
    {
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTV, descriptionTV, categoryTV, sizeTV, dateTV;
        ImageButton removeFavIBtn;

        public HolderPdfFavorite(@NonNull View itemView) {
            super(itemView);

            // initializing UI view of row_pdf_favorite.xml
            pdfView = binding.pdfView3ID;
            progressBar = binding.progressBar3ID;

           /* Sprite foldingCube = new Wave();
            progressBar.setIndeterminateDrawable(foldingCube);*/

            titleTV = binding.titleTV6ID;
            descriptionTV = binding.descriptionTV3ID;
            removeFavIBtn = binding.removeFavBookIBtnID;
            categoryTV = binding.categoryTV4ID;
            sizeTV = binding.sizeTV1ID;
            dateTV = binding.dateTV1ID;
        }
    }
}
// Friday, May 13th, 2022 - justCongDoan