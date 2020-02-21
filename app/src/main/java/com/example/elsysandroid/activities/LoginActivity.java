package com.example.elsysandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elsysandroid.App;
import com.example.elsysandroid.Outs;
import com.example.elsysandroid.PollTaskListener;
import com.example.elsysandroid.R;

import java.io.IOException;
import java.net.MalformedURLException;

public class LoginActivity extends AppCompatActivity {
    private EditText addressText;
    private EditText passwordText;
    private MainApplicationComponent applicationComponent;
    PollTaskListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationComponent = ((App) getApplication()).getApplicationComponent();
        setContentView(R.layout.login_layout);
        addressText = findViewById(R.id.edit_user);
        passwordText = findViewById(R.id.edit_password);
        listener = new PollTaskListener() {
            @Override
            public void onError(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMessage(String message) {
                if (message.equals("success")){
                    Intent success = new Intent(LoginActivity.this, RemotePanelActivity.class);
                    startActivity(success);
                }
            }
        };
        applicationComponent.getPollTask().setPollTaskListener(listener);
    }

    public void login(View view) {
        try {
            applicationComponent.getPollTask().start(addressText.getText().toString(), passwordText.getText().toString());
        } catch (MalformedURLException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.ip_malformed), Toast.LENGTH_SHORT).show();
            addressText.setActivated(true);
            e.printStackTrace();
            return;
        }
        applicationComponent.getPollTask().sendCommand(Outs.None);
            //todo Проверка данных
    }
}