package com.example.elsysandroid.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elsysandroid.Outs;
import com.example.elsysandroid.PollTask;
import com.example.elsysandroid.R;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Класс, обрабатывающий андроид-интерфейс.
 *
 * @author ITMO students Bazarova Anna, Denisenko Kirill, Ryabov Sergey, Chernyshev Nikita
 * @version 1.0
 */
public class MainActivityOld extends AppCompatActivity {

    public static TextView codeText;
    private PollTask pollTask;
    private EditText addressText, passwordText;
    private boolean started = false;

    /**
     * Метод, связывающий java-компоненты с объектами на экране android.
     * Для java-компонентов назначается слушатель, активирующий метод - соответствующее действие.
     * {@value} btnClick компонент для кнопки "Старт"
     * {@value} textV компонент для текстового поля.
     * {@value} sc экземпляр класса-буфера между клиент-серверным взаимодействием и андроид-интерфейсом
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_old);
        final TextView textV = (TextView) findViewById(R.id.lbl_view);
        codeText = findViewById(R.id.code_view);
        addressText = findViewById(R.id.addressText);
        passwordText = findViewById(R.id.passwordText);
        pollTask = new PollTask() {
            @Override
            public void onError(String message) {
                onMessage(message);
            }

            @Override
            public void onMessage(String message) {
                textV.setText(message);
            }
        };
    }

    public void onStartButtonClick(View view) {
        try {
            pollTask.start(addressText.getText().toString(), passwordText.getText().toString());
        } catch (MalformedURLException e) {
            showToast(getString(R.string.ip_malformed));
            addressText.setActivated(true);
            e.printStackTrace();
            return;
        }
        try {
            pollTask.sendCommand(Outs.None);
        } catch (IOException e) {
            showToast(getString(R.string.cant_connect));
        }
        //TODO: it's only shows connection but not success authentication
        //19.02.2020 HukuToc2288
        started = true;
    }

    public void onImpulseButtonClick(View view) {
        if (!started) {
            showToast(getString(R.string.need_auth));
            return;
        }
        try {
            pollTask.sendCommand(Outs.Impulse);
        } catch (IOException e) {
            showToast(getString(R.string.cant_connect));
        }
    }

    public void onSwonButtonClick(View view) {
        if (!started) {
            showToast(getString(R.string.need_auth));
            return;
        }
        try {
            pollTask.sendCommand(Outs.SwitchOn);
        } catch (IOException e) {
            showToast(getString(R.string.cant_connect));
        }
    }

    public void onSwoffButtonClick(View view) {
        if (!started) {
            showToast(getString(R.string.need_auth));
            return;
        }
        try {
            pollTask.sendCommand(Outs.SwitchOff);
        } catch (IOException e) {
            showToast(getString(R.string.cant_connect));
        }
    }

    public void onSyncButtonClick(View view) {
        try {
            pollTask.sendCommand(Outs.SyncTime);
        } catch (IOException e) {
            showToast(getString(R.string.cant_connect));
        }
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void onInvButtonClick(View view) {
        if (!started) {
            showToast(getString(R.string.need_auth));
            return;
        }
        try {
            pollTask.sendCommand(Outs.Invert);
        } catch (IOException e) {
            showToast(getString(R.string.cant_connect));
        }
    }
}