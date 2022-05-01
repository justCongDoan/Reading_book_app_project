package com.example.bookapp1.adapters;

import static com.example.bookapp1.Constants.MAX_BYTES_PDF;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp1.MyApplication;
import com.example.bookapp1.databinding.RowPdfAdminBinding;
import com.example.bookapp1.filters.PdfAdminFilter;
import com.example.bookapp1.models.PdfModel;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PdfAdminAdapter extends RecyclerView.Adapter<PdfAdminAdapter.HolderPdfAdmin> implements Filterable {

    // context
    private Context context;

    // create an arraylist to holder list of PdfModels
    public ArrayList<PdfModel> pdfModelArrayList, filterList;

    // view binding
    private RowPdfAdminBinding binding;

    private PdfAdminFilter filter;

    private static final String TAG = "PDF_ADAPTER_TAG";

    // create a constructor
    public PdfAdminAdapter(Context context, ArrayList<PdfModel> pdfModelArrayList) {
        this.context = context;
        this.pdfModelArrayList = pdfModelArrayList;
        this.filterList = pdfModelArrayList;
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind layout using view binding
        binding = RowPdfAdminBinding.inflate
                (
                        LayoutInflater.from(context),
                        parent,
                        false
                );

        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        // get & set data, event, etc

        // get data
        PdfModel model = pdfModelArrayList.get(position);
        String title = model.getTitle(),
                description = model.getDescription();
        long timeStamp = model.getTimeStamp();

        // converting timestamp to dd/mm/yyyy format
        String formattedDate = MyApplication.timestampFormat(timeStamp);

        // set data
        holder.titleTV.setText(title);
        holder.descriptionTV.setText(description);
        holder.dateTV.setText(formattedDate);

        // loading further details like category, pdf from url, pdf size in separate functions
        loadCategory(model, holder);
        loadPdfFromUrl(model, holder);
        loadPdfSize(model, holder);
    }

    private void loadPdfSize(PdfModel model, HolderPdfAdmin holder) {
        // get file and its metadata from firebase storage by using url

        String pdfUrl = model.getUrl();

        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getMetadata()
                .addOnSuccessListener
                        (
                                new OnSuccessListener<StorageMetadata>() {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        // getting size in bytes
                                        double bytes = storageMetadata.getSizeBytes();

                                        Log.d(TAG, "onSuccess: " + model.getTitle() + " - " + bytes);

                                        // convert bytes to KB, MB
                                        double KB = bytes / 1024,
                                                MB = KB / 1024;

                                        if(MB >= 1)
                                        {
                                            holder.sizeTV.setText(String.format("%.2f", MB) + "MB");
                                        }
                                        else if(KB >= 1)
                                        {
                                            holder.sizeTV.setText(String.format("%.2f", KB) + "KB");
                                        }
                                        else
                                        {
                                            holder.sizeTV.setText(String.format("%.2f", bytes) + "Bytes");
                                        }
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // failed getting metadata
                                        Log.d(TAG, "onFailure: " + e.getMessage());
                                    }
                                }
                        );
    }

    private void loadPdfFromUrl(PdfModel model, HolderPdfAdmin holder) {
        // get file and its metadata from firebase storage by using url

        String pdfUrl = model.getUrl();
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener
                        (
                                new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Log.d(TAG, "onSuccess: " + model.getTitle() + " successfully got the file!");

                                        // set to pdf view
                                        holder.pdfView.fromBytes(bytes)
                                                .pages(0)
                                                .spacing(0)
                                                .swipeHorizontal(false)
                                                .enableSwipe(false)
                                                .onError
                                                        (
                                                                new OnErrorListener() {
                                                                    @Override
                                                                    public void onError(Throwable t) {
                                                                        // hide progress
                                                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                                                        Log.d(TAG, "onError: " + t.getMessage());
                                                                    }
                                                                }
                                                        )
                                                .onPageError
                                                        (
                                                                new OnPageErrorListener() {
                                                                    @Override
                                                                    public void onPageError(int page, Throwable t) {
                                                                        // hide progress
                                                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                                                        Log.d(TAG, "onPageError: " + t.getMessage());
                                                                    }
                                                                }
                                                        )
                                                .onLoad
                                                        (
                                                                new OnLoadCompleteListener() {
                                                                    @Override
                                                                    public void loadComplete(int nbPages) {
                                                                        // pdf loaded
                                                                        // hide progress
                                                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                                                        Log.d(TAG, "loadComplete: PDF file loaded!");
                                                                    }
                                                                }
                                                        ).load();
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: failed getting file from url due to " + e.getMessage());
                                    }
                                }
                        );
    }

    private void loadCategory(PdfModel model, HolderPdfAdmin holder) {
        // get category using categoryId

        String categoryId = model.getCategoryId();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.child(categoryId)
                .addListenerForSingleValueEvent
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // get category
                                        String category = "" + snapshot.child("category").getValue();

                                        // set to category text view
                                        holder.categoryTV.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }

    @Override
    public int getItemCount() {
        // return the number of records | list size
        return pdfModelArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null)
            filter = new PdfAdminFilter(filterList, this);
        return filter;
    }

    // create a view holder class for row_pdf_admin.xml
    class HolderPdfAdmin extends RecyclerView.ViewHolder
    {
        // UI Views of row_pdf_admin.xml
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTV, descriptionTV, categoryTV, sizeTV, dateTV;
        ImageButton moreInfoIBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            // initializing UI views
            pdfView = binding.pdfViewID;
            progressBar = binding.progressBarID;
            titleTV = binding.titleTV1ID;
            descriptionTV = binding.descriptionTV1ID;
            categoryTV = binding.categoryTV2ID;
            sizeTV = binding.sizeTVID;
            dateTV = binding.dateTVID;
            moreInfoIBtn = binding.moreInfoIBtnID;
        }
    }
}
