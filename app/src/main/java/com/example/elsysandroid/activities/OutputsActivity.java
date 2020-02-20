package com.example.elsysandroid.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elsysandroid.App;
import com.example.elsysandroid.Outs;
import com.example.elsysandroid.R;

import java.io.IOException;

public class OutputsActivity extends AppCompatActivity {
    private OutputsActivityController controller;
    private MainApplicationComponent applicationComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationComponent = ((App) getApplication()).getApplicationComponent();
        controller = new OutputsActivityController(applicationComponent.getDeviceList(), this);
        controller.onActivityCreate();
    }

    public void onSwonButtonClick(View view) {
        try {
            applicationComponent.getPollTask().sendCommand(Outs.SwitchOn, controller.selectedDevice.getId());
        } catch (IOException e) {
            showToast(getString(R.string.cant_connect));
        }
    }

    public void onSwoffButtonClick(View view) {
        try {
            applicationComponent.getPollTask().sendCommand(Outs.SwitchOff, controller.selectedDevice.getId());
        } catch (IOException e) {
            showToast(getString(R.string.cant_connect));
        }
    }

    public void onImpulseButtonClick(View view) {
        try {
            applicationComponent.getPollTask().sendCommand(Outs.Impulse, controller.selectedDevice.getId());
        } catch (IOException e) {
            showToast(getString(R.string.cant_connect));
        }
    }

    public void onInvButtonClick(View view) {
        try {
            applicationComponent.getPollTask().sendCommand(Outs.Invert, controller.selectedDevice.getId());
        } catch (IOException e) {
            showToast(getString(R.string.cant_connect));
        }
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}