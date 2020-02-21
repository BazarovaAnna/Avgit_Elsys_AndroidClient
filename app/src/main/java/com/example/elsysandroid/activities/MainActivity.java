package com.example.elsysandroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elsysandroid.R;

public class MainActivity extends AppCompatActivity {

    private ImageView mainImage;
    private Button infoButton, remoteButton, mapButton;
    private Intent remoteIntent, mapIntent, infoIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainImage = findViewById(R.id.mainImageView);
        mainImage.setImageResource(R.drawable.elsys_mb_net_l);
        infoButton = findViewById(R.id.infoButton);
        remoteButton = findViewById(R.id.remoteButton);
        mapButton = findViewById(R.id.mapButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onInfoButtonClicked();
            }
        });
        remoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onRemoteButtonClicked();
            }
        });
        mapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onMapButtonClicked();
            }
        });
    }

    protected void onResume() {
        super.onResume();
    }

    private void onInfoButtonClicked() {
        //infoIntent = new Intent(activity, InfoActivity.class);
        //activity.startActivity(infoIntent);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.elsystems.ru/"));
        startActivity(browserIntent);
    }

    private void onRemoteButtonClicked() {
        remoteIntent = new Intent(this, LoginActivity.class);
        startActivity(remoteIntent);
    }

    private void onMapButtonClicked() {
        //mapIntent = new Intent(activity, MapActivity.class);
        //activity.startActivity(mapIntent);
    }
}