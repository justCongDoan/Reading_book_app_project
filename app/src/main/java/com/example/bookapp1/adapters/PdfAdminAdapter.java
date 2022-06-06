package com.example.bookapp1.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp1.activities.EditingPdfFileActivity;
import com.example.bookapp1.MyApplication;
import com.example.bookapp1.activities.PdfDetailActivity;
import com.example.bookapp1.databinding.RowPdfAdminBinding;
import com.example.bookapp1.filters.PdfAdminFilter;
import com.example.bookapp1.models.PdfModel;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.ChasingDots;
import com.github.ybq.android.spinkit.style.Wave;

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

    // progress dialog
    private ProgressDialog progressDialog;

    // create a constructor
    public PdfAdminAdapter(Context context, ArrayList<PdfModel> pdfModelArrayList) {
        this.context = context;
        this.pdfModelArrayList = pdfModelArrayList;
        this.filterList = pdfModelArrayList;

        // initializing progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

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
                description = model.getDescription(),
                pdfId = model.getId(),
                pdfUrl = model.getUrl(),
                categoryId = model.getCategoryId();
        long timeStamp = model.getTimeStamp();

        // converting timestamp to dd/mm/yyyy format
        String formattedDate = MyApplication.timestampFormat(timeStamp);

        // set data
        holder.titleTV.setText(title);
        holder.descriptionTV.setText(description);
        holder.dateTV.setText(formattedDate);

        // loading further details like category, pdf from url, pdf size in separate functions
        MyApplication.loadCategory
                (
                        "" + categoryId,
                        holder.categoryTV
                );
        MyApplication.loadPdfFromUrlSinglePage
                (
                        "" + pdfUrl,
                        "" + title,
                        holder.pdfView,
                        holder.progressBar,
                        null
                );
        MyApplication.loadPdfSize
                (
                        "" + pdfUrl,
                        "" + title,
                        holder.sizeTV
                );

        // handle clicking => show dialog with two options:
        //      1) Edit
        //      2) Delete
        holder.moreInfoIBtn.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                moreOptionsDialog(model, holder);
                            }
                        }
                );

        // handle clicking book/pdf => open pdf details page, passing pdf/book id to get its details
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
                                intent.putExtra("bookId", pdfId);
                                context.startActivity(intent);
                            }
                        }
                );
    }

    private void moreOptionsDialog(PdfModel model, HolderPdfAdmin holder) {
        String bookId = model.getId(),
                bookUrl = model.getUrl(),
                bookTitle = model.getTitle();

        // show options in the dialog
        String[] options = {"Edit", "Delete"};

        // create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose an option:")
                .setItems
                        (
                                options,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // handle clinking dialog option
                                        if(i == 0)
                                        {
                                            // "edit" is chosen => open new activity to edit the book info
                                            Intent intent = new Intent(context, EditingPdfFileActivity.class);
                                            intent.putExtra("bookId", model.getId());
                                            context.startActivity(intent);
                                        }
                                        else if(i == 1)
                                        {
                                            // "delete" is chosen
                                            MyApplication.deleteBook
                                                    (
                                                            context,
                                                            "" + bookId,
                                                            "" + bookUrl,
                                                            "" + bookTitle
                                                    );
                                        }
                                    }
                                }
                        ).show();
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

            /*Sprite wave = new ChasingDots();
            progressBar.setIndeterminateDrawable(wave);*/

            titleTV = binding.titleTV1ID;
            descriptionTV = binding.descriptionTV1ID;
            categoryTV = binding.categoryTV2ID;
            sizeTV = binding.sizeTVID;
            dateTV = binding.dateTVID;
            moreInfoIBtn = binding.moreInfoIBtnID;
        }
    }
}
