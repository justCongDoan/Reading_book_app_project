package com.example.bookapp1.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.bookapp1.BookUserFragment;
import com.example.bookapp1.databinding.ActivityDashboardUserBinding;
import com.example.bookapp1.models.CategoryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardUserActivity extends AppCompatActivity {

    // progress dialog
    private ProgressDialog progressDialog;

    // showing tabs
    public ArrayList<CategoryModel> categoryModelArrayList;
    public ViewPagerAdapter viewPagerAdapter;

    // view binding
    private ActivityDashboardUserBinding binding;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initializing firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        setupViewPagerAdapter(binding.viewPagerID);
        binding.tabLayoutID.setupWithViewPager(binding.viewPagerID);

        // handle "click logout button" event
        binding.logoutBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                firebaseAuth.signOut();
                                startActivity
                                        (
                                                new Intent
                                                        (
                                                                DashboardUserActivity.this,
                                                                MainActivity.class
                                                        )
                                        );
                                finish();
                            }
                        }
                );

        // handle click => open profile
        binding.userProfileBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity
                                        (
                                                new Intent
                                                        (
                                                                DashboardUserActivity.this,
                                                                ProfileActivity.class
                                                        )
                                        );
                            }
                        }
                );
    }

    private void setupViewPagerAdapter(ViewPager viewPager)
    {
        viewPagerAdapter = new ViewPagerAdapter
                (
                        getSupportFragmentManager(),
                        FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                        this
                );

        categoryModelArrayList = new ArrayList<>();

        // load category from firebase
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent
                (
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // clearing data before adding to the list
                                categoryModelArrayList.clear();

                                // Load categories - static e.g: All, Most viewed, Most download

                                // adding data to the models
                                CategoryModel allCM = new CategoryModel("01", "All", "", 1),
                                        mostViewedCM = new CategoryModel("02", "Most Viewed", "", 1),
                                        mostDownloadedCM = new CategoryModel("03", "Most Downloaded", "", 1);

                                // add models to list
                                categoryModelArrayList.add(allCM);
                                categoryModelArrayList.add(mostViewedCM);
                                categoryModelArrayList.add(mostDownloadedCM);

                                // adding data to the view pager adapter
                                viewPagerAdapter.addFragment
                                        (
                                                BookUserFragment.newInstance
                                                        (
                                                                "" + allCM.getId(),
                                                                "" + allCM.getCategory(),
                                                                "" + allCM.getUid()
                                                        ),
                                                allCM.getCategory()
                                        );

                                // adding data to the view pager adapter
                                viewPagerAdapter.addFragment
                                        (
                                                BookUserFragment.newInstance
                                                        (
                                                                "" + mostViewedCM.getId(),
                                                                "" + mostViewedCM.getCategory(),
                                                                "" + mostViewedCM.getUid()
                                                        ),
                                                mostViewedCM.getCategory()
                                        );

                                // adding data to the view pager adapter
                                viewPagerAdapter.addFragment
                                        (
                                                BookUserFragment.newInstance
                                                        (
                                                                "" + mostDownloadedCM.getId(),
                                                                "" + mostDownloadedCM.getCategory(),
                                                                "" + mostDownloadedCM.getUid()
                                                        ),
                                                mostDownloadedCM.getCategory()
                                        );

                                // refreshing list
                                viewPagerAdapter.notifyDataSetChanged();

                                // Now loading from firebase
                                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                                {
                                    // get data
                                    CategoryModel model = dataSnapshot.getValue(CategoryModel.class);
                                    // add data to the list
                                    categoryModelArrayList.add(model);
                                    // add data to the viewPagerAdapter
                                    viewPagerAdapter.addFragment
                                            (
                                                    BookUserFragment.newInstance
                                                            (
                                                                    "" + model.getId(),
                                                                    "" + model.getCategory(),
                                                                    "" + model.getUid()
                                                            )
                                                    , model.getCategory()
                                            );
                                    // refresh list
                                    viewPagerAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }
                );

        // set adapter to the view pager
        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter
    {
        private ArrayList<BookUserFragment> fragmentArrayList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentArrayList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentArrayList.size();
        }

        private void addFragment(BookUserFragment fragment, String title)
        {
            // add fragment passed as parameter in fragmentArrayList
            fragmentArrayList.add(fragment);
            // add title passed as parameter in fragmentTitleList
            fragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void checkUser() {
        // get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null)
        {
            // not logged in
            binding.subTitleTVID.setText("Not logged in");
        }
        else
        {
            // logged in, get user info
            String email = firebaseUser.getEmail();

            // set in textView of toolbar
            binding.subTitleTVID.setText(email);
        }
    }
}