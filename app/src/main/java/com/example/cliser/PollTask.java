package com.example.cliser;

public class PollTask {
    String serverIP,password;
    public void Start(String aServerIP, String aPassword)
    {
        this.serverIP = aServerIP;
        this.password = aPassword;

        HTTPClient = new HttpClient { Timeout = TimeSpan.FromMilliseconds(15000) };
        HTTPResponse = null;
        CancelTokenSource = new CancellationTokenSource();
        RequestUri = String.Format("http://{0}{1}", ServerIP, Protocol.URL);

        Connection = false;
        CID = 10000;
        SID = 0;
        CommandID = 10000;
        Terminated = false;
        OnMessage?.Invoke("Начало опроса", "");
        NextPoll();

    }
}
