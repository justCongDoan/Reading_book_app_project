package com.example.bookapp1.filters;

import android.widget.Filter;

import com.example.bookapp1.adapters.PdfUserAdapter;
import com.example.bookapp1.models.PdfModel;

import java.util.ArrayList;

public class PdfUserFilter extends Filter {

    // create an arraylist in which we want to search
    ArrayList<PdfModel> filterList;

    // create an adapter in which filter needs to be implemented
    PdfUserAdapter pdfUserAdapter;

    // create a constructor
    public PdfUserFilter(ArrayList<PdfModel> filterList, PdfUserAdapter pdfUserAdapter) {
        this.filterList = filterList;
        this.pdfUserAdapter = pdfUserAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        // check for NOT null/empty value
        if (charSequence != null || charSequence.length() > 0)
        {
            // not null, nor empty
            // change to uppercase or lowercase to avoid the sensitivity
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<PdfModel> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++)
            {
                // validating
                if (filterList.get(i).getTitle().toUpperCase().contains(charSequence))
                {
                    // searching for matched results and adding to the list
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

        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        // applying filter changes
        pdfUserAdapter.pdfModelArrayList = (ArrayList<PdfModel>) filterResults.values;

        // notifying changes
        pdfUserAdapter.notifyDataSetChanged();
    }
}
