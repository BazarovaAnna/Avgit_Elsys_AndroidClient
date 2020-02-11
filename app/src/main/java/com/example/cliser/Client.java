package com.example.cliser;

import android.widget.TextView;

public class Client {

    //реализовать клиентскую часть клиент-серверного приложения
    

    public static void buttonClicked(String btnName, TextView textV){
        if(btnName.equals("butt1")){

            textV.setText("Clicked");
            //сюда поместить обработчик события - что именно мы хотим сделать по кнопке
        }
    }
}
