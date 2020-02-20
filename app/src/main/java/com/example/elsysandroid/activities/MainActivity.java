package com.example.elsysandroid.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elsysandroid.R;

public class MainActivity extends AppCompatActivity {

    private MainActivityController activityController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityController = new MainActivityController(this, savedInstanceState);
    }

    protected void onResume() {
        super.onResume();
    }
}