package com.example.bookapp1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.bookapp1.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // view binding
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // handle clicking loginBtn event, start login screen
        binding.loginBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent
                                        (
                                                MainActivity.this,
                                                LoginActivity.class
                                        ));
                            }
                        }
                );

        // handle clicking skipBtn event, ignoring login session
        binding.skipBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent
                                        (
                                                MainActivity.this,
                                                DashboardUserActivity.class
                                        ));
                            }
                        }
                );
    }
}