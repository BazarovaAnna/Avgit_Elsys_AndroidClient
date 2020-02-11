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

    public SocketClient() {

    }

    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            mBufferOut.flush();
        }
    }

    public void stopClient() {
        sendMessage("CLOSED_CONNECTION");//todo

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
            //InetAddress serverAddr = InetAddress.getByName(address);todo?

            try {
                socket = new Socket("serverAddr", 0000);//0000=SERVER PORT todo
                mRun = true;
                mBufferOut =
                        new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                                true);

                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                sendMessage("LOGIN_NAME"); // todo
                mMessageListener.onConnected();

                // ждем ответа
                while (mRun) {
                    if (mBufferOut.checkError()) {
                        mRun = false;
                    }

                    mServerMessage = mBufferIn.readLine();

                    if (mServerMessage != null && mMessageListener != null) {
                        mMessageListener.messageReceived(mServerMessage);
                    }
                }
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

    //реализовать шифрование

    public void buttonClicked(String btnName, TextView textV){
        if(btnName.equals("butt1")){

            textV.setText("Clicked");
            //сюда поместить обработчик события - что именно мы хотим сделать по кнопке
            sendMessage("");//todo   need a fix
        }
    }
}
