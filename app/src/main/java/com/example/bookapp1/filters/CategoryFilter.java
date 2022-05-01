package com.example.bookapp1.filters;

import android.widget.Filter;

import com.example.bookapp1.adapters.CategoryAdapter;
import com.example.bookapp1.models.CategoryModel;

import java.util.ArrayList;

public class CategoryFilter extends Filter {

    // arrayList for searching category
    ArrayList<CategoryModel> filterList;

    // adapter (in which filter is needed to be implemented)
    CategoryAdapter categoryAdapter;

    // constructor
    public CategoryFilter(ArrayList<CategoryModel> filterList, CategoryAdapter categoryAdapter) {
        this.filterList = filterList;
        this.categoryAdapter = categoryAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();

        // checking for null and empty value
        if (charSequence != null && charSequence.length() > 0)
        {
            // changing to upper case, or lower case to avoid sensitive cases
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<CategoryModel> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++)
            {
                // validating
                if (filterList.get(i).getCategory().toUpperCase().contains(charSequence))
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
        categoryAdapter.categoryModelArrayList = (ArrayList<CategoryModel>) filterResults.values;

        // notifying changes
        categoryAdapter.notifyDataSetChanged();
    }
}
