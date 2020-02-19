package com.example.elsysandroid;

import android.os.Handler;
import android.util.Log;

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
    String serverIP, password;
    /**
     * Поле, показывающее, установлено ли соединение
     */
    boolean connection;
    /**
     * Поля, соответствующие Client ID и Server ID
     */
    int CID, SID;
    /**
     * Поле, соответствующее ID команды
     */
    int commandID;
    /**
     * Поля, показывающее, была ли связь разорвана
     */
    boolean terminated;
    /**
     * Поле контента - тела отправляемого сообщения
     */
    private byte[] content;
    /**
     * Поле, содержащее URI запроса
     */
    String requestUri;

    /**
     * Поле - Http соединение с сервером
     */
    HttpURLConnection urlConnection;
    /**
     * Поле для корректирования локального времени под серверное
     */
    long timeCorrection;
    /**
     * Поле - ответ сервера в виде строки
     */
    private String response;
    /**
     * Поле - код возврата (200 - ок)
     */
    private int responseCode;


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

    public void start(String aServerIP, String aPassword) {
        this.serverIP = aServerIP;
        this.password = aPassword;
        this.command = Outs.None;
        response = null;

        requestUri = String.format("http://%s%s", serverIP, Protocol.URL);

        connection = false;
        CID = 10000;
        SID = 0;
        commandID = 10000;
        terminated = false;

        SocketClient.chText("Начало опроса");
        handler = new Handler();
    }

    /**
     * Функция, реализующая инкрементацию поля CID в некоторых пределах
     *
     * @return CID+1 или 1
     * @see PollTask#CID
     */
    private int incCID() {
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
     * @see PollTask#commandID
     */
    private int incCommandID() {
        if (commandID < 0x40000000)
            commandID++;
        else
            commandID = 1;
        return commandID;
    }


    /**
     * Функция подготовки запроса
     * Здесь происходит коррекция времени с сервером, формирование заголовков, формирование и шифрование контента
     * {@value} now время начала формирования запроса по клиенту
     * {@value} CreationTime время начала формирования запроса по серверу
     * {@value} XContent данные в формате xml
     * {@value} s строка, которую отправляем
     *
     * @see Protocol#getDigest(String, String, byte[], String)
     * @see Protocol#DATE_FORMAT
     */
    public synchronized void sendCommand(Outs command) {
        String nonce = Protocol.getNonce();
        Date now = new Date();
        Element xContent;

        String creationTime = Protocol.DATE_FORMAT.format(new Date(now.getTime() + timeCorrection));

        if (Math.abs(timeCorrection) > 5) {
            SocketClient.chText("Синхронизация времени");
            xContent = Protocol.getXContent(incCID(), SID, now);
        } else if (command != Outs.None) {
            xContent = makeCommand(command);
        } else {
            xContent = Protocol.getXContent(incCID(), SID);
        }
        try {
            content = Protocol.toString(xContent).getBytes("UTF8");

            String Digest = Protocol.getDigest(nonce, password, content, creationTime);

            URL url = new URL(String.format("http://%s%s", serverIP, Protocol.URL));
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("ECNC-Auth", String.format("Nonce=\"%s\", Created=\"%s\", Digest=\"%s\"", nonce, creationTime, Digest));
            urlConnection.setRequestProperty("Date", Protocol.LOCAL_DATE_FORMAT.format(now));
            urlConnection.setRequestProperty("Connection", "close");

            urlConnection.setRequestProperty("Accept-Encoding", "identity");

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            sendRequestAsync(xContent, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Функция, реализующая асинхронное отправление запроса серверу
     * Здесь получается и расшифровывается ответ от сервера, который затем отправляется на обработчик
     * Затем клиент  опять переводится в режим подготовки запроса
     * {@value} responseCode код возврата
     * {@value} response ответ сервера в виде строки
     * @param xContent нода xml кода
     * @param content байтовая строка для отправки
     * @see PollTask#handleResponse(int, String)
     */
    private void sendRequestAsync(final Element xContent, final byte[] content) {
        if (xContent != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    SocketClient.chText("Sending request");
                }
            });

        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
                    dataOutputStream.write(content);

                    dataOutputStream.flush();
                    dataOutputStream.close();

                    responseCode = urlConnection.getResponseCode();
                    response = urlConnection.getResponseMessage();

                    urlConnection.disconnect();
                } catch (final Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            SocketClient.chText(e.getMessage());
                            response = null;
                        }
                    });
                    stopAsyncTask = true;
                }
                handleResponse(responseCode, response);
            }
        });
        thread.start();
    }

    /**
     * Функция формирует строку для отправления серверу
     *
     * @param aCommand команда, которую хотим отправить
     * @return возвращает сторку для отправления
     */
    private Element makeCommand(Outs aCommand) {
        if (aCommand == Outs.None) {
            return Protocol.getXContent(incCID(), SID);
        }
        return Protocol.getXContent(incCID(), SID, Protocol.getCommand(8, aCommand.getDevType().getCode(), aCommand.getCode(), incCommandID()));
    }


    /**
     * Функция парсит ответ сервера из строки в структуру для обработки,
     * затем в зависимости от результата обработки задает параметры для формирования нового запроса
     *
     * @param responseCode код возврата
     * @param response     ответ сервера в виде строки
     * @see PollTask#sendRequestAsync(Element, byte[])
     */

    private void handleResponse(final int responseCode, String response) {
        Log.d("Response", response + ": " + response);
        handler.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.codeText.setText(String.valueOf(responseCode));
                if (responseCode == 401) {
                    stopAsyncTask = true;
                    SocketClient.chText("Ошибка аутентификации");
                }
            }
        });
    }
}