package com.example.cliser;

import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class SocketClient {

    //реализовать клиентскую часть клиент-серверного приложения
    private String mServerMessage;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false; // флаг, определяющий, запущен ли сервер
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;
    private Socket socket;
    private String passwd="12345678";

    public SocketClient() {
        /*TODO
        Порядок действий
        1) старт
            передать нужный ip
            передать нужный пароль (использовать нужный пароль)
        2) клик
            http запрос на клик
         */
    }

    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            String m=ElsysSDK2.GetDigest(message,passwd);
            mBufferOut.println(m);
            mBufferOut.flush();
        }
    }

    public void stopClient() {
        sendMessage("CLOSED_CONNECTION");//todo что отправляем при отсоединении?

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {
        try {
            //InetAddress serverAddr = InetAddress.getByName(address);todo? как это работает и что нам тут указывать

            try {
                socket = new Socket("serverAddr", 0000);//0000=SERVER PORT todo какой у нас порт сервера?
                mRun = true;
                mBufferOut =
                        new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                                true);

                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                sendMessage("LOGIN_NAME"); // todo что мы должны отправлять в качестве приветствия во время тройного рукопожатия?
                mMessageListener.onConnected();

                // ждем ответа
                while (mRun) {
                    if (mBufferOut.checkError()) {
                        mRun = false;
                    }

                    mServerMessage = mBufferIn.readLine();//todo расшифровать? это вообще нужно? а он вообще шиврует то что нам отправляет?

                    if (mServerMessage != null && mMessageListener != null) {
                        mMessageListener.messageReceived(mServerMessage);
                    }
                }
                //todo не понимаю где вставить сам http обмен, по идее здесь где-то но где конкретно хз, и там уже реализовать формирование заголовков и т.п.
            } catch (Exception e) {
            } finally {
                if (socket != null && socket.isConnected()) {
                    socket.close();
                }
            }
        } catch (Exception e) {
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public boolean isRunning() {
        return mRun;
    }

    public interface OnMessageReceived {
        void messageReceived(String message);
        void onConnected();
    }

    public void buttonClicked(String btnName, TextView textV){
        if(btnName.equals("butt1")){

            textV.setText("Clicked");
            //сюда поместить обработчик события - что именно мы хотим сделать по кнопке
            //sendMessage("");//todo   need a fix
            String textIP="192.168.1.21";
            String textPassword="12345678";
            PollTask.Start(textIP, textPassword);

        }
    }
}
