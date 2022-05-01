package com.example.bookapp1.filters;

import android.widget.Filter;

import com.example.bookapp1.adapters.CategoryAdapter;
import com.example.bookapp1.adapters.PdfAdminAdapter;
import com.example.bookapp1.models.CategoryModel;
import com.example.bookapp1.models.PdfModel;

import java.util.ArrayList;

public class PdfAdminFilter extends Filter {

    // arrayList for searching category
    ArrayList<PdfModel> filterList;

    // adapter (in which filter is needed to be implemented)
    PdfAdminAdapter pdfAdminAdapter;

    // constructor
    public PdfAdminFilter(ArrayList<PdfModel> filterList, PdfAdminAdapter pdfAdminAdapter) {
        this.filterList = filterList;
        this.pdfAdminAdapter = pdfAdminAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();

        // checking for null and empty value
        if (charSequence != null && charSequence.length() > 0)
        {
            // changing to upper case, or lower case to avoid sensitive cases
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<PdfModel> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++)
            {
                // validating
                if (filterList.get(i).getTitle().toUpperCase().contains(charSequence))
                {
                    // adding to the filtered list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else
        {
            results.count = filterList.size();
            results.values = filterList;
        }

        // return exactly the results having processed and done before
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        // applying filter's changes
        pdfAdminAdapter.pdfModelArrayList = (ArrayList<PdfModel>) filterResults.values;

        // notifying changes
        pdfAdminAdapter.notifyDataSetChanged();
    }
}
