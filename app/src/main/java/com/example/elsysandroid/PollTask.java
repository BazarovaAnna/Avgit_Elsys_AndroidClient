package com.example.elsysandroid;


import android.os.Handler;

import org.w3c.dom.Element;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;


/**
 * Класс для клиент-серверного обмена
 *
 * @author ITMO students Bazarova Anna, Denisenko Kirill, Ryabov Sergey, Chernyshev Nikita
 * @version 1.0
 */
public class PollTask {
    /**
     * Поля, содержащие IP сервера и пароль для шифрования
     */
    String ServerIP, Password;
    /**
     * Поле, показывающее, установлено ли соединение
     */
    boolean Connection;
    /**
     * Поля, соответствующие Client ID и Server ID
     */
    int CID, SID;
    /**
     * Поле, соответствующее ID команды
     */
    int CommandID;
    /**
     * Поля, показывающее, была ли связь разорвана
     */
    boolean Terminated;
    /**
     * Поле контента - тела отправляемого сообщения
     */
    private byte[] Content;
    /**
     * Поле, содержащее URI запроса
     */
    String RequestUri;

    /**
     * Поле - Http соединение с сервером
     */
    HttpURLConnection urlConnection;
    /**
     * Поле для корректирования локального времени под серверное
     */
    long TimeCorrection;//понадобится в реализации HandleResponse
    /**
     * Поле - ответ сервера в виде строки
     */
    private String response;//понадобится в реализации HandleResponse
    /**
     * Поле - код возврата (200 - ок)
     */
    private int responseCode;//понадобится в реализации HandleResponse
    /**
     * Поле - нода xml кода
     */
    Element XContent;
    /**
     * Поле - команда
     */
    Outs command;

    Handler handler;

    /**
     * Функция, инициализирующая обмен с сервером и задающая базовые значения
     *
     * @param aServerIP IP-адрес сервера
     * @param aPassword пароль для шифрования данных
     */

    boolean stopAsyncTask = false;

    public void Start(String aServerIP, String aPassword) {
        this.ServerIP = aServerIP;
        this.Password = aPassword;
        this.command = Outs.None;
        response = null;

        RequestUri = String.format("http://%s%s", ServerIP, Protocol.URL);

        Connection = false;
        CID = 10000;
        SID = 0;
        CommandID = 10000;
        Terminated = false;

        SocketClient.chText("Начало опроса");

        handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stopAsyncTask) {
                    PrepareRequest();
                    SendRequestAsync();
                }
            }
        });
        thread.start();
    }

    public synchronized void sendCommand(Outs command) {
        this.command = command;
    }

    /**
     * Функция, реализующая инкрементацию поля CID в некоторых пределах
     *
     * @return CID+1 или 1
     * @see PollTask#CID
     */
    private int IncCID() {
        if (CID < 0x40000000)
            CID++;
        else
            CID = 1;
        return CID;
    }

    /**
     * Функция, реализующая инкрементацию поля CommandID в некоторых пределах
     *
     * @return CommandID+1 или 1
     * @see PollTask#CommandID
     */
    private int IncCommandID() {
        if (CommandID < 0x40000000)
            CommandID++;
        else
            CommandID = 1;
        return CommandID;
    }


    /**
     * Функция подготовки запроса
     * Здесь происходит коррекция времени с сервером, формирование заголовков, формирование и шифрование контента
     * {@value} now время начала формирования запроса по клиенту
     * {@value} CreationTime время начала формирования запроса по серверу
     * {@value} XContent данные в формате xml
     * {@value} s строка, которую отправляем
     *
     * @see Protocol#GetDigest(String, String, byte[], String)
     * @see Protocol#DateFormat
     */
    private synchronized void PrepareRequest() {
        String Nonce = Protocol.GetNonce();
        Date now = new Date();

        String CreationTime = Protocol.DateFormat.format(new Date(now.getTime() + TimeCorrection));

        if (Math.abs(TimeCorrection) > 5) {
            SocketClient.chText("Синхронизация времени");
            XContent = Protocol.GetXContent(IncCID(), SID, now);
        } else if (command != Outs.None) {
            XContent = makeCommand(command);
            command = Outs.None;
        } else {
            XContent = Protocol.GetXContent(IncCID(), SID);
        }
        try {
            Content = Protocol.toString(XContent).getBytes("UTF8");

            String Digest = Protocol.GetDigest(Nonce, Password, Content, CreationTime);

            URL url = new URL(String.format("http://%s%s", ServerIP, Protocol.URL));
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("ECNC-Auth", String.format("Nonce=\"%s\", Created=\"%s\", Digest=\"%s\"", Nonce, CreationTime, Digest));
            urlConnection.setRequestProperty("Date", Protocol.LocalDateFormat.format(now));
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setRequestProperty("Accept-Encoding", "identity");

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
        } catch (Exception e) {
            SocketClient.chText(e.getMessage());
        }
    }

    /**
     * Функция, реализующая асинхронное отправление запроса серверу
     * Здесь получается и расшифровывается ответ от сервера, который затем отправляется на обработчик
     * Затем клиент  опять переводится в режим подготовки запроса
     * {@value} responseCode код возврата
     * {@value} response ответ сервера в виде строки
     *
     * @see PollTask#HandleResponse(int, String)
     */
    private void SendRequestAsync() {
        try {
            if (XContent != null) {
                SocketClient.chText("Sending request");
            }
            DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.write(Content);

            dataOutputStream.flush();
            dataOutputStream.close();

            responseCode = urlConnection.getResponseCode();
            response = urlConnection.getResponseMessage();

            urlConnection.disconnect();
        } catch (final Exception e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    SocketClient.chText(e.getMessage());
                    response = null;
                }
            });
        }
        HandleResponse(responseCode, response);
    }

    /**
     * Функция формирует строку для отправления серверу
     *
     * @param aCommand команда, которую хотим отправить
     * @return возвращает сторку для отправления
     */
    private Element makeCommand(Outs aCommand) {
        if (aCommand == Outs.None) {
            return Protocol.GetXContent(IncCID(), SID);
        }
        return Protocol.GetXContent(IncCID(), SID, Protocol.GetCommand(8, aCommand.getDevType().getCode(), aCommand.getCode(), IncCommandID()));
    }


    /**
     * Функция парсит ответ сервера из строки в структуру для обработки,
     * затем в зависимости от результата обработки задает параметры для формирования нового запроса
     *
     * @param responseCode код возврата
     * @param response     ответ сервера в виде строки
     * @see PollTask#PrepareRequest()
     * @see PollTask#SendRequestAsync()
     */

    private void HandleResponse(int responseCode, String response) {
        //todo FiRsT
    }
}