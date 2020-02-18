package com.example.elsysandroid;


import android.os.Handler;

import org.w3c.dom.Element;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;


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
     * {@value} s строка, которую отправляем (ВРЕМЕННО!!!)
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
            //urlConnection.setRequestProperty("Content-Type", "application/json");todo XML
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
<<<<<<< HEAD
    private void HandleResponse(int responseCode, String response) {

=======
    private void HandleResponse(final int responseCode, String response) {
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
        /*boolean connection = false;
        //todo* response parse from string into some collection that has headers & content as a tree-like structure or smth like that
        //if (!CancelTokenSource.IsCancellationRequested)
            if (response != null)
                if ((responseCode == 200) || (responseCode == 401))
                {
                    connection = true;
                    if (HTTPResponse.Headers.Date.HasValue)//todo HTTP cannot fix w/o *
                        TimeCorrection = HTTPResponse.Headers.Date.Value - new Date().getTime();//todo HTTP cannot fix w/o *
                    try
                    {
                        XDocument Content = XDocument.Parse(HTTPResponse.Content.ReadAsStringAsync().Result);//todo XML, HTTP cannot fix w/o *
                        if (Content.Root != null)//todo XML
                        {
                            SocketClient.chText(new XElement("MBNet", new XAttribute("LocalTime", Protocol.DateFormat.format(new Date())), Content.Root));//todo  XML
                            var BodyNodes = Content.Element("Envelope").Element("Body").Elements();//todo XML
                            foreach (var node in BodyNodes)//todo JAVA
                            {
                                if (node.Name == "CIDResp") uint.TryParse(node.Value, out CIDResp);
                                if (node.Name == "SID") uint.TryParse(node.Value, out SID);
                                if (node.Name == "Events") HandleEvents(node);
                                if (node.Name == "DevStates") HandleDevStates(node);
                                if (node.Name == "OnlineStatus") HandleOnlineStatus(node);
                                if (node.Name == "UpdSysConfigResponse") HandleInitDevTree(node);
                                if (node.Name == "UpdAPBConfigResponse") HandleLoadAPB(node);
                                if (node.Name == "ChangesResults") HandleChangesResult(node);
                                if (node.Name == "ChangesResponse") HandleChangesResponse(node);
                                if (node.Name == "ErrCode") HandleError(node.Value);
                                if (node.Name == "ConfigGUID") CheckConfigGUID(node.Value);
                                if (node.Name == "ConnectedDevices") HandleConnectedDevices(node);
                                if (node.Name == "DisconnectedDevices") HandleDisconnectedDevices(node);
                                if (node.Name == "ConnectedMBNets") HandleConnectedMBNets(node);
                                if (node.Name == "DisconnectedMBNets") HandleDisconnectedMBNets(node);
                                if (node.Name == "ControlCmdsResponse") HandleControlCmdsResponse(node);
                                if (node.Name == "NumericalHWParams") HandleNumericalHWParams(node);
                            }
                        }
                    }
                    catch(Exception e)
                    {
                    }
                }
        if (Connection != connection)
        {
            Connection = connection;
            if (Connection)
                SocketClient.chText("Восстановление связи");
            else
                SocketClient.chText("Потеря связи");
        }*/
>>>>>>> 6f6ff7523cc60fad265fe684878f4f757cea38f9
    }
}