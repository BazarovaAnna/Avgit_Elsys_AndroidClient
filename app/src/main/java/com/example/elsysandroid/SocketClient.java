package com.example.elsysandroid;
import android.widget.TextView;

/**
 * Класс-буфер между клиент-серверным взаимодействием и андроид-интерфейсом
 * @autor ITMO students Bazarova Anna, Denisenko Kirill, Ryabov Sergey
 * @version 1.0
 */
public class SocketClient {
    //реализовать клиентскую часть клиент-серверного приложения

    /** Поле, показывающее, была ли нажата до этого кнопка старт */
    private boolean start=false;
    /** Поле для вывода текста на экран
     * @see MainActivity
     */
    private static TextView tv;

    /**
     * Функция, выполняющаяся при нажатии на кнопку. В случае нажатия на кнопку с ID "butt1" - начинается http-обмен.
     * @param btnName - название кнопки - для того, чтобы разные кнопки обрабатывать по-разному
     * @param textV - поле, в которое будет выводиться текст
     * @see SocketClient#tv
     * {@value} textIP - IP адрес сервера
     * {@value} textPassword - пароль для шифрования
     * {@value} pt - экземпляр класса для клиент-серверного обмена
     */
    void buttonClicked(String btnName, TextView textV){
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

    /**
     * Функция, изменяющая текст в поле на экране андроид
     * @param str - текст, который хотим отобразить
     */
    public static void chText(String str){
        tv.setText(str);
    }
}