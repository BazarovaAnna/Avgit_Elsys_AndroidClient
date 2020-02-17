package com.example.cliser;

import android.util.Xml;

import com.loopj.android.http.*;

import java.time.Period;

public class PollTask {
    String ServerIP,Password;
    boolean Connection;
    int CID,SID;
    int CommandID;
    boolean Terminated;
    private byte[] Content;
    String RequestUri;
    AsyncHttpClient HTTPClient;
    AsyncHttpResponseHandler HTTPResponse;
    private Period TimeCorrection;
    public void Start(String aServerIP, String aPassword)
    {
        this.ServerIP = aServerIP;
        this.Password = aPassword;

        HTTPClient = new AsyncHttpClient();
        HTTPClient.setTimeout(15000);

        HTTPResponse = null;
        //CancelTokenSource = new CancellationTokenSource();
        RequestUri = String.format("http://{0}{1}", ServerIP, Protocol.URL);

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
        DateTime now = DateTime.Now;
        String CreationTime = (now.ToUniversalTime() + TimeCorrection).ToString("yyyy-MM-ddTHH:mm:ss.fffZ");

        if (Math.abs(TimeCorrection.TotalSeconds) > 5)
        {
            SocketClient.chText("Синхронизация времени");
            XContent = Protocol.GetXContent(IncCID(), SID, now);
        }
        else
        {
            XContent = Protocol.GetXContent(IncCID(), SID);
        }
        Content = Xml.Encoding.UTF_8.toString().getBytes(XContent.ToString());

        String Digest = Protocol.GetDigest(Nonce, Password, Content, CreationTime);

        HTTPClient.removeAllHeaders();
        HTTPClient.addHeader("ECNC-Auth", String.format("Nonce=\"{0}\", Created=\"{1}\", Digest=\"{2}\"", Nonce, CreationTime, Digest));
        HTTPClient.DefaultRequestHeaders.Date = now.ToUniversalTime();
        HTTPClient.DefaultRequestHeaders.ConnectionClose = true;
    }

    private void SendRequestAsync()
    {
        try
        {
            if (XContent != null)
            {
                SocketClient.chText(new XElement("Client", new XAttribute("LocalTime", DateTime.Now.ToLocalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ")), XContent));
            }

            HTTPResponse = await HTTPClient.PostAsync(RequestUri, new ByteArrayContent(Content), CancelTokenSource.Token);
        }
        catch(Exception e)
        {
            HTTPResponse = null;
        }
        HandleResponse();
        NextPoll();

    }

    private void HandleResponse()
    {
        boolean connection = false;
        //if (!CancelTokenSource.IsCancellationRequested)
            if (HTTPResponse != null)
                if ((HTTPResponse.StatusCode == HttpStatusCode.OK) || (HTTPResponse.StatusCode == HttpStatusCode.Unauthorized))
                {
                    connection = true;
                    if (HTTPResponse.getRequestHeaders().Date.HasValue)
                        TimeCorrection = HTTPResponse.Headers.Date.Value - DateTime.Now.ToUniversalTime();

                    try
                    {
                        XDocument Content = XDocument.Parse(HTTPResponse.Content.ReadAsStringAsync().Result);
                        if (Content.Root != null)
                        {
                            SocketClient.chText(new XElement("MBNet", new XAttribute("LocalTime", DateTime.Now.ToLocalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ")), Content.Root));
                            var BodyNodes = Content.Element("Envelope").Element("Body").Elements();
                            foreach (var node in BodyNodes)
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
    }
}
