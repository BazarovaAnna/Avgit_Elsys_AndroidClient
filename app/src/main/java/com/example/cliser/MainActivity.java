package com.example.cliser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnClick= (Button) findViewById(R.id.btnClick_view);
        final TextView textV = (TextView) findViewById(R.id.text_view);
        final SocketClient sc=new SocketClient();
        btnClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sc.buttonClicked("butt1", textV);
            }
        });
    }
}
