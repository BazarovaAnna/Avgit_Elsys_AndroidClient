package com.example.elsysandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Класс, обрабатывающий андроид-интерфейс.
 * @author ITMO students Bazarova Anna, Denisenko Kirill, Ryabov Sergey, Chernyshev Nikita
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    public static TextView codeText;

    /**
     * Метод, связывающий java-компоненты с объектами на экране android.
     * Для java-компонентов назначается слушатель, активирующий метод - соответствующее действие.
     * {@value} btnClick компонент для кнопки "Старт"
     * {@value} textV компонент для текстового поля.
     * {@value} sc экземпляр класса-буфера между клиент-серверным взаимодействием и андроид-интерфейсом
     * @see SocketClient
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnClick= (Button) findViewById(R.id.btnClick_view);
        final TextView textV = (TextView) findViewById(R.id.text_view);

        Button btnImp = (Button) findViewById(R.id.btnClick_imp);
        Button btnOn = (Button) findViewById(R.id.btnClick_swon);
        Button btnOff = (Button) findViewById(R.id.btnClick_swoff);
        Button btnInv = (Button) findViewById(R.id.btnClick_inv);
        codeText = findViewById(R.id.code_view);


        final SocketClient sc=new SocketClient();
        btnClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sc.buttonClicked("start", textV);
            }
        });

        btnImp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sc.buttonClicked("impulse", textV);
            }
        });

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sc.buttonClicked("on", textV);
            }
        });
        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sc.buttonClicked("off", textV);
            }
        });

        btnInv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sc.buttonClicked("inverse", textV);
            }
        });
    }
}