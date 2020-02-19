package com.example.elsysandroid;

import android.os.Handler;
import android.util.Log;

import org.w3c.dom.Element;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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

    Handler handler;

    /**
     * Функция, инициализирующая обмен с сервером и задающая базовые значения
     *
     * @param aServerIP IP-адрес сервера
     * @param aPassword пароль для шифрования данных
     */

    boolean stopAsyncTask = false;
    URL url;

    public PollTask() {
        handler = new Handler();
    }

    public void start(String aServerIP, String aPassword) throws MalformedURLException {
        serverIP = aServerIP;
        password = aPassword;
        requestUri = String.format("http://%s%s", serverIP, Protocol.URL);
        CID = 10000;
        SID = 0;
        commandID = 10000;
        url = new URL(String.format("http://%s%s", serverIP, Protocol.URL));
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
    public synchronized void sendCommand(Outs command) throws IOException {
        String nonce = Protocol.getNonce();
        Date now = new Date();
        Element xContent;

        String creationTime = Protocol.DATE_FORMAT.format(new Date(now.getTime() + timeCorrection));

        if (Math.abs(timeCorrection) > 5) {
            onMessage("Синхронизация времени");
            xContent = Protocol.getXContent(incCID(), SID, now);
        } else if (command != Outs.None) {
            xContent = makeCommand(command);
        } else {
            xContent = Protocol.getXContent(incCID(), SID);
        }
        content = Protocol.toString(xContent).getBytes("UTF8");

        String Digest = Protocol.getDigest(nonce, password, content, creationTime);

        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("ECNC-Auth", String.format("Nonce=\"%s\", Created=\"%s\", Digest=\"%s\"", nonce, creationTime, Digest));
        urlConnection.setRequestProperty("Date", Protocol.LOCAL_DATE_FORMAT.format(now));
        urlConnection.setRequestProperty("Connection", "close");

        urlConnection.setRequestProperty("Accept-Encoding", "identity");

        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection = (HttpURLConnection) url.openConnection();
        sendRequestAsync(xContent, content);
    }

    /**
     * Функция, реализующая асинхронное отправление запроса серверу
     * Здесь получается и расшифровывается ответ от сервера, который затем отправляется на обработчик
     * Затем клиент  опять переводится в режим подготовки запроса
     * {@value} responseCode код возврата
     * {@value} response ответ сервера в виде строки
     *
     * @param xContent нода xml кода
     * @param content  байтовая строка для отправки
     * @see PollTask#handleResponse(int, String)
     */
    private void sendRequestAsync(final Element xContent, final byte[] content) {
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

                    responseCode = urlConnection.getResponseCode();
                    response = urlConnection.getResponseMessage();

                    urlConnection.disconnect();
                } catch (final Exception e) {
                    e.printStackTrace();
                    //TODO process error
                    //19.02.2020 HukuToc2288
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
                    onError("Ошибка аутентификации");
                }
            }
        });
    }

    public abstract void onError(String message);

    public abstract void onMessage(String message);
}