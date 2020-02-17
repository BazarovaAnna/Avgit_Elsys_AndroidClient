package com.example.elsysandroid;

import android.util.Xml;
import com.loopj.android.http.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import oracle.iot.message.HttpResponseMessage;


public class PollTask {
    String ServerIP,Password;
    boolean Connection;
    int CID,SID;
    int CommandID;
    boolean Terminated;
    private byte[] Content;
    String RequestUri;
    AsyncHttpClient HTTPClient;

    HttpResponseMessage HTTPResponse;
    long TimeCorrection;


    //Xml;

    public void Start(String aServerIP, String aPassword)
    {
        this.ServerIP = aServerIP;
        this.Password = aPassword;

        HTTPClient = new AsyncHttpClient();
        HTTPClient.setTimeout(15000);

        HTTPResponse = null;//todo HTTP
        //CancelTokenSource = new CancellationTokenSource();
        RequestUri = String.format("http://%s%s", ServerIP, Protocol.URL);

        Connection = false;
        CID = 10000;
        SID = 0;
        CommandID = 10000;
        Terminated = false;
        SocketClient.chText("Начало опроса");
        NextPoll();

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

        if (Math.abs(TimeUnit.MILLISECONDS.toSeconds(TimeCorrection)) > 5)
        {
            SocketClient.chText("Синхронизация времени");
            XContent = Protocol.GetXContent(IncCID(), SID, now);//todo XML
        }
        else
        {
            XContent = Protocol.GetXContent(IncCID(), SID);//todo XML
        }
        Content = Xml.Encoding.UTF_8.toString().getBytes(XContent.ToString());//todo XML

        String Digest = Protocol.GetDigest(Nonce, Password, Content, CreationTime);

        HTTPClient.removeAllHeaders();
        HTTPClient.addHeader("ECNC-Auth", String.format("Nonce=\"%s\", Created=\"%s\", Digest=\"%s\"", Nonce, CreationTime, Digest));
        HTTPClient.addHeader("Date",now.toString());
        HTTPClient.addHeader("Connection", "Close");
    }

    private void SendRequestAsync()
    {
        try
        {
            if (XContent != null)//todo XML
            {//String.format("%s = %d", "joe", 35)
                SocketClient.chText(new XElement("Client", new XAttribute("LocalTime", Protocol.DateFormat.format(new Date())), XContent));//todo XML
            }

            HTTPResponse = await HTTPClient.PostAsync(RequestUri, new ByteArrayContent(Content), CancelTokenSource.Token);//todo HTTP
        }
        catch(Exception e)
        {
            HTTPResponse = null;//todo HTTP
        }
        HandleResponse();
        NextPoll();

    }

    private void HandleResponse()
    {
        boolean connection = false;
        //if (!CancelTokenSource.IsCancellationRequested)
            if (HTTPResponse != null)//todo HTTP
                if ((HTTPResponse.statusCode == HttpStatusCode.OK) || (HTTPResponse.StatusCode == HttpStatusCode.Unauthorized))//todo HTTP
                {
                    connection = true;
                    if (HTTPResponse.Headers.Date.HasValue)//todo HTTP
                        TimeCorrection = HTTPResponse.Headers.Date.Value - new Date().getTime();//todo HTTP

                    try
                    {
                        XDocument Content = XDocument.Parse(HTTPResponse.Content.ReadAsStringAsync().Result);//todo XML
                        if (Content.Root != null)//todo XML
                        {
                            SocketClient.chText(new XElement("MBNet", new XAttribute("LocalTime", Protocol.DateFormat.format(new Date())), Content.Root));//todo  XML
                            var BodyNodes = Content.Element("Envelope").Element("Body").Elements();//todo XML
                            /*foreach (var node in BodyNodes)//todo JAVA
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
                            }*/

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
    }
}
