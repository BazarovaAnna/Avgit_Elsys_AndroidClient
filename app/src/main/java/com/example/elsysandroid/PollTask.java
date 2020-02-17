package com.example.elsysandroid;

import android.os.AsyncTask;
import android.util.Xml;
import com.loopj.android.http.*;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class PollTask extends AsyncTask<String, Void, String> {
    String ServerIP,Password;
    boolean Connection;
    int CID,SID;
    int CommandID;
    boolean Terminated;
    private byte[] Content;
    String RequestUri;
    AsyncHttpClient HTTPClient;
    //HttpResponseMessage HTTPResponse;

    SimpleDateFormat DateFormat;
    SimpleDateFormat LocalDateFormat;
    HttpURLConnection urlConnection;
    long TimeCorrection;
    private String response;
    private int responseCode;

    public void Start(String aServerIP, String aPassword)
    {
        this.ServerIP = aServerIP;
        this.Password = aPassword;

        HTTPClient = new AsyncHttpClient();
        HTTPClient.setTimeout(15000);

        response = null;

        RequestUri = String.format("http://%s%s", ServerIP, Protocol.URL);

        Connection = false;
        CID = 10000;
        SID = 0;
        CommandID = 10000;
        Terminated = false;

        SocketClient.chText("Начало опроса");

        DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        LocalDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        //NextPoll(); todo??

    }

    private int IncCID() {
        if (CID < 0x40000000)
            CID++;
        else
            CID = 1;
        return CID;
    }

    private void NextPoll()
    {
        if (Terminated)
        {
            TerminatePoll();
        }
        else
        {
            PrepareRequest();
            SendRequestAsync();
        }
    }

    private void TerminatePoll()
    {
        if (HTTPClient != null)
        {
            HTTPClient.cancelAllRequests(false);
            //HTTPClient.Dispose();
        }
    }

    private void PrepareRequest()
    {
        String Nonce = Protocol.GetNonce();
        Date now = new Date();

        String CreationTime = Protocol.DateFormat.format(new Date(now.getTime() + TimeCorrection));

        if (Math.abs(TimeCorrection) > 5)
        {
            SocketClient.chText("Синхронизация времени");
            //XContent = Protocol.GetXContent(IncCID(), SID, now);//todo XML
        }
        else {
            //XContent = Protocol.GetXContent(IncCID(), SID);//todo XML
        }
        try {

            String s = "<Envelope>\n  <Body>\n    <CID>10001</CID>\n    <SIDResp>0</SIDResp>\n  </Body>\n</Envelope>";
            Content = s.getBytes("UTF8");

            String Digest = Protocol.GetDigest(Nonce, Password, Content, CreationTime);

            URL url = new URL(String.format("http://%s%s", ServerIP, Protocol.URL));
            urlConnection = (HttpURLConnection)url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("ECNC-Auth", String.format("Nonce=\"%s\", Created=\"%s\", Digest=\"%s\"", Nonce, CreationTime, Digest));
            urlConnection.setRequestProperty("Date", LocalDateFormat.format(now));
            urlConnection.setRequestProperty("Connection", "close");
            //urlConnection.setRequestProperty("Content-Type", "application/json");todo XML
            urlConnection.setRequestProperty("Accept-Encoding", "identity");

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
        } catch (Exception e) {
            SocketClient.chText(e.getMessage());
        }
    }

    private void SendRequestAsync()
    {
        try
        {
            //if (XContent != null)//todo XML
            {
                //SocketClient.chText(new XElement("Client", new XAttribute("LocalTime", Protocol.DateFormat.format(new Date())), XContent));//todo XML
            }
            DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.write(Content);

            dataOutputStream.flush();
            dataOutputStream.close();

            responseCode = urlConnection.getResponseCode();
            response = urlConnection.getResponseMessage();

            urlConnection.disconnect();
        }
        catch(Exception e)
        {
            SocketClient.chText(e.getMessage());
            response = null;
        }
        //HandleResponse(responseCode,response);
        NextPoll();
    }

    @Override
    protected String doInBackground(String... strings) {

        NextPoll();
        return null;
    }

    /*private void HandleResponse(int responseCode,String response)
    {
        boolean connection = false;
        //todo* response parse from string into some collection that has headers & content as a tree-like structure
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
        }
    }*/

}
