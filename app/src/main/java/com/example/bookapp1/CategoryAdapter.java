package com.example.bookapp1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp1.databinding.RowCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.HolderCategory> implements Filterable
{
    private Context context;
    public ArrayList<CategoryModel> categoryModelArrayList, filterArrayList;

    // view binding
    private RowCategoryBinding binding;

    // create instance of the filter class
    private CategoryFilter categoryFilter;

    public CategoryAdapter(Context context, ArrayList<CategoryModel> categoryModelArrayList) {
        this.context = context;
        this.categoryModelArrayList = categoryModelArrayList;
        this.filterArrayList = categoryModelArrayList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind row_category.xml
        binding = RowCategoryBinding.inflate
                (
                        LayoutInflater.from(context),
                        parent,
                        false
                );

        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        // get data
        CategoryModel model = categoryModelArrayList.get(position);
        String id = model.getId(),
                category = model.getCategory(),
                uid = model.getUid();
        long timeStamp = model.getTimeStamp();

        // set data
        holder.categoryTV.setText(category);

        // handle "click, delete category" event
        holder.deleteIBtn.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // confirm deleting dialog
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Delete")
                                        .setMessage("Do you really want to delete this category?")
                                        .setPositiveButton
                                                (
                                                        "Yes",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                // begin deleting
                                                                Toast.makeText(context, "Deleting category...", Toast.LENGTH_SHORT).show();
                                                                deleteCategory(model, holder);
                                                            }
                                                        }
                                                )
                                        .setNegativeButton
                                                (
                                                        "No",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                dialogInterface.dismiss();
                                                            }
                                                        }
                                                ).show();
                            }
                        }
                );
    }

    private void deleteCategory(CategoryModel model, HolderCategory holder) {
        // get category's id to delete
        String id = model.getId();
        // Firebase DB > Categories > categoryId
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.child(id)
                .removeValue()
                .addOnSuccessListener
                        (
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // deleted successfully
                                        Toast.makeText(context, "Deleted category successfully!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // failed to delete
                                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
    }

    @Override
    public int getItemCount() {
        return categoryModelArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(categoryFilter == null)
            categoryFilter = new CategoryFilter(filterArrayList, this);
        return categoryFilter;
    }

    // create a holder class to hold UI views for row_category.xml
    class HolderCategory extends RecyclerView.ViewHolder {

        // UI views of row_category.xml
        TextView categoryTV;
        ImageButton deleteIBtn;

        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            // initializing UI views
            categoryTV = binding.categoryTVID;
            deleteIBtn = binding.deleteIBtnID;
        }
    }
}
