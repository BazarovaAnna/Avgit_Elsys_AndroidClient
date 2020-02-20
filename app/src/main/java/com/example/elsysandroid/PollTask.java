package com.example.elsysandroid;

import android.os.Handler;
import android.util.Log;

import org.w3c.dom.Element;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;


/**
 * Класс для клиент-серверного обмена
 *
 * @author ITMO students Bazarova Anna, Denisenko Kirill, Ryabov Sergey, Chernyshev Nikita
 * @version 1.0
 */
public abstract class PollTask {
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
     * Поле для корректирования локального времени под серверное
     */
    long timeCorrection;

    Handler handler;

    ResponseHandler responseHandler;

    /**
     * Функция, инициализирующая обмен с сервером и задающая базовые значения
     *
     * @param aServerIP IP-адрес сервера
     * @param aPassword пароль для шифрования данных
     */

    URL url;

    public PollTask() {
        handler = new Handler();
        responseHandler = new ResponseHandler(this);
    }

    public void start(String aServerIP, String aPassword) throws MalformedURLException {
        serverIP = aServerIP;
        password = aPassword;
        requestUri = String.format("http://%s%s", serverIP, Protocol.URL);
        CID = 10000;
        SID = 0;
        commandID = 10000;
        url = new URL(String.format(requestUri));
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

    public synchronized void sendCommand(Outs command) throws IOException {
        sendCommand(command, 0);
    }

    /**
     * Функция подготовки запроса
     * Здесь происходит коррекция времени с сервером, формирование заголовков, формирование и шифрование контента
     * {@value} now время начала формирования запроса по клиенту
     * {@value} CreationTime время начала формирования запроса по серверу
     * {@value} XContent данные в формате xml
     * {@value} s строка, которую отправляем
     *
     * @param command отправляемая команда
     * @param deviceId идентификатор устройства.
     *
     * @see Protocol#getDigest(String, String, byte[], String)
     * @see Protocol#DATE_FORMAT
     */
    public synchronized void sendCommand(Outs command, int deviceId) throws IOException {
        String nonce = Protocol.getNonce();
        Date now = new Date();
        Element xContent;
        HttpURLConnection urlConnection;

        String creationTime = Protocol.DATE_FORMAT.format(new Date(now.getTime() + timeCorrection));

        if (command != Outs.None) {
            xContent = makeCommand(command, deviceId);
        } else {
            xContent = Protocol.getXContent(incCID(), SID);
        }
        content = Protocol.toString(xContent).getBytes("UTF8");

        String Digest = Protocol.getDigest(nonce, password, content, creationTime);

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("ECNC-Auth", String.format("Nonce=\"%s\", Created=\"%s\", Digest=\"%s\"", nonce, creationTime, Digest));
        urlConnection.setRequestProperty("Date", Protocol.LOCAL_DATE_FORMAT.format(now));
        urlConnection.setRequestProperty("Connection", "close");

        urlConnection.setRequestProperty("Accept-Encoding", "identity");

        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        sendRequestAsync(urlConnection, xContent, content);
    }

    /**
     * Функция, реализующая асинхронное отправление запроса серверу
     * Здесь получается и расшифровывается ответ от сервера, который затем отправляется на обработчик
     * Затем клиент  опять переводится в режим подготовки запроса
     * {@value} responseCode код возврата
     * {@value} response ответ сервера в виде строки
     *
     * @param urlConnection HTTP соединение с сервером
     * @param xContent нода xml кода
     * @param content  байтовая строка для отправки
     * @see PollTask#handleResponse(HttpURLConnection)
     */
    private void sendRequestAsync(final HttpURLConnection urlConnection, final Element xContent, final byte[] content) {
        if (xContent == null) {
            //TODO find out what to do here
            //19.02.2020 HukuToc2288
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
                    dataOutputStream.write(content);

                    dataOutputStream.flush();
                    dataOutputStream.close();

                } catch (final Exception e) {
                    e.printStackTrace();
                    //TODO process error
                    //19.02.2020 HukuToc2288
                }
                handleResponse(urlConnection);
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
    private Element makeCommand(Outs aCommand, int deviceId) {
        if (aCommand == Outs.None) {
            return Protocol.getXContent(incCID(), SID);
        }
        if (aCommand == Outs.SyncTime) {
            return Protocol.getXContent(incCID(), SID, new Date());
        }
        return Protocol.getXContent(incCID(), SID, Protocol.getCommand(deviceId, aCommand.getDevType().getCode(), aCommand.getCode(), incCommandID()));
    }

    /**
     * Функция парсит ответ сервера из строки в структуру для обработки,
     * затем в зависимости от результата обработки задает параметры для формирования нового запроса
     *
     * @param urlConnection HTTP соединение с сервером
     * @see PollTask#sendRequestAsync(HttpURLConnection, Element, byte[])
     */
    private void handleResponse(final HttpURLConnection urlConnection) {
        try {
            Log.d("Response", urlConnection.getResponseCode() + ": " + urlConnection.getResponseMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (urlConnection.getDate() != 0) {
                    timeCorrection = urlConnection.getDate() - new Date().getTime();
                }

                int responseCode = 0;
                try {
                    responseCode = urlConnection.getResponseCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //MainActivity.codeText.setText(String.valueOf(responseCode));
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    onError("Ошибка аутентификации");
                }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try {
                        responseHandler.Handle(urlConnection.getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                urlConnection.disconnect();
            }
        });
    }

    public abstract void onError(String message);

    public abstract void onMessage(String message);
}