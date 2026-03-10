package com.example.nearbuyhq.dashboard;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nearbuyhq.R;

public class Analytics extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}
