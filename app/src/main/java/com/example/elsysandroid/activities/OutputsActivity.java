package com.example.elsysandroid.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elsysandroid.App;
import com.example.elsysandroid.Outs;
import com.example.elsysandroid.PollTask;
import com.example.elsysandroid.PollTaskListener;
import com.example.elsysandroid.R;

import java.io.IOException;

public class OutputsActivity extends AppCompatActivity {
    private OutputsActivityController controller;
    private MainApplicationComponent applicationComponent;
    PollTaskListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationComponent = ((App) getApplication()).getApplicationComponent();
        controller = new OutputsActivityController(applicationComponent.getDeviceList(), this);
        controller.onActivityCreate();
        listener = new PollTaskListener() {
            @Override
            public void onError(String message) {
                showToast(getString(R.string.cant_connect));
            }

            @Override
            public void onMessage(String message) {
                if (message.equals("success"))
                    showToast(getString(R.string.done));
            }
        };
        applicationComponent.getPollTask().addPollTaskListener(listener);
    }

    public void onSwonButtonClick(View view) {
        applicationComponent.getPollTask().sendCommand(Outs.SwitchOn, controller.selectedDevice.getId());
    }

    public void onSwoffButtonClick(View view) {
        applicationComponent.getPollTask().sendCommand(Outs.SwitchOff, controller.selectedDevice.getId());
    }

    public void onImpulseButtonClick(View view) {
        applicationComponent.getPollTask().sendCommand(Outs.Impulse, controller.selectedDevice.getId());
    }

    public void onInvButtonClick(View view) {
        applicationComponent.getPollTask().sendCommand(Outs.Invert, controller.selectedDevice.getId());
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        applicationComponent.getPollTask().removePollTaskListener(listener);
        super.onDestroy();
    }
}