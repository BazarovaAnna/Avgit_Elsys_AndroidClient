package com.example.elsysandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elsysandroid.App;
import com.example.elsysandroid.Outs;
import com.example.elsysandroid.R;
import com.example.elsysandroid.devices.Device;
import com.example.elsysandroid.devices.DeviceList;
import com.example.elsysandroid.devices.DevicesParser;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;

public class RemotePanelActivity extends AppCompatActivity {

    private Button backButton, goToMainButton, outputsButton, syncButton;
    private Intent backIntent, outputsIntent;
    private MainApplicationComponent applicationComponent;
    private DevicesParser devicesParser;
    private DeviceList deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remotepanel_layout);
        applicationComponent = ((App) getApplication()).getApplicationComponent();
        goToMainButton = findViewById(R.id.buttonGoToMain);
        goToMainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickGoToMainButton();
            }
        });
        backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickBackButton();
            }
        });
        outputsButton = findViewById(R.id.outputs);
        outputsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickOutputsButton();
            }
        });
        syncButton = findViewById(R.id.sync_button);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSyncButton();
            }
        });
        devicesParser = applicationComponent.getDevicesParser();
        deviceList = applicationComponent.getDeviceList();
    }

    protected void onClickGoToMainButton() {
        XmlPullParser xpp = getResources().getXml(R.xml.elsysconfig);
        if (devicesParser.parse(xpp)) {
            for (Device prod : deviceList.getOuts()) {
                Log.d("XML", prod.toString());
            }
            for (Device prod : deviceList.getInputs()) {
                Log.d("XML", prod.toString());
            }
            for (Device prod : deviceList.getReaders()) {
                Log.d("XML", prod.toString());
            }
            for (Device prod : deviceList.getDoors()) {
                Log.d("XML", prod.toString());
            }
        }
    }

    protected void onClickBackButton() {
        backIntent = new Intent(this, LoginActivity.class);
        startActivity(backIntent);
        onDestroy();
    }

    protected void onClickOutputsButton() {
        outputsIntent = new Intent(this, OutputsActivity.class);
        startActivity(outputsIntent);
    }

    protected void onSyncButton() {
        try {
            applicationComponent.getPollTask().sendCommand(Outs.SyncTime);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.cant_connect), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}