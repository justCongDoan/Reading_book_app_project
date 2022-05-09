package com.example.bookapp1.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp1.MyApplication;
import com.example.bookapp1.activities.PdfDetailActivity;
import com.example.bookapp1.databinding.RowPdfUserBinding;
import com.example.bookapp1.filters.PdfUserFilter;
import com.example.bookapp1.models.PdfModel;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class PdfUserAdapter extends RecyclerView.Adapter<PdfUserAdapter.HolderPdfUser> implements Filterable {

    private Context context;
    public ArrayList<PdfModel> pdfModelArrayList, filterList;
    private PdfUserFilter filter;

    private RowPdfUserBinding binding;

    private static final String TAG = "ADAPTER_PDF_USER_TAG";

    public PdfUserAdapter(Context context, ArrayList<PdfModel> pdfModelArrayList) {
        this.context = context;
        this.pdfModelArrayList = pdfModelArrayList;
        this.filterList = pdfModelArrayList;
    }

    @NonNull
    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind the view
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {
        // get, set data, handle clicking, etc

        // get data
        PdfModel model = pdfModelArrayList.get(position);
        String bookId = model.getId(),
                title = model.getTitle(),
                description = model.getDescription(),
                pdfUrl = model.getUrl(),
                categoryId = model.getCategoryId();
        long timestamp = model.getTimeStamp();

        // convert time
        String date = MyApplication.timestampFormat(timestamp);

        // set data
        holder.titleTV.setText(title);
        holder.descriptionTV.setText(description);
        holder.dateTV.setText(date);

        MyApplication.loadPdfFromUrlSinglePage
                (
                        "" + pdfUrl,
                        "" + title,
                        holder.pdfView,
                        holder.progressBar
                );
        MyApplication.loadCategory
                (
                        "" + categoryId,
                        holder.categoryTV
                );
        MyApplication.loadPdfSize
                (
                        "" + pdfUrl,
                        "" + title,
                        holder.sizeTV
                );

        // handle click => show pdf details activity
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
                                intent.putExtra("bookId", bookId);
                                context.startActivity(intent);
                            }
                        }
                );
    }

    @Override
    public int getItemCount() {
        return pdfModelArrayList.size();    // return the list size or the number of records
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new PdfUserFilter(filterList, this);
        return filter;
    }

    class HolderPdfUser extends RecyclerView.ViewHolder
    {
        TextView titleTV, descriptionTV, categoryTV, sizeTV, dateTV;
        PDFView pdfView;
        ProgressBar progressBar;

        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);

            titleTV = binding.titleTV1ID;
            descriptionTV = binding.descriptionTV1ID;
            categoryTV = binding.categoryTV2ID;
            sizeTV = binding.sizeTVID;
            dateTV = binding.dateTVID;
            pdfView = binding.pdfViewID;
            progressBar = binding.progressBarID;
        }
    }
}
