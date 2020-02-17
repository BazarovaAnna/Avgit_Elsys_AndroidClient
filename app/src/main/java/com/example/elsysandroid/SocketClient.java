package com.example.elsysandroid;


import android.widget.TextView;

public class SocketClient {

    //реализовать клиентскую часть клиент-серверного приложения

    private boolean start=false;
    private static TextView tv;

    public void buttonClicked(String btnName, TextView textV){
        if(btnName.equals("butt1")){

            textV.setText("Clicked");
            tv=textV;
            //сюда поместить обработчик события - что именно мы хотим сделать по кнопке
            String textIP="192.168.1.21";
            String textPassword="12345678";
            if(!start){
                start=true;
                PollTask pt=new PollTask();
                pt.Start(textIP, textPassword);
            }

        }
    }
    public static void chText(String str){
        tv.setText(str);
    }
}