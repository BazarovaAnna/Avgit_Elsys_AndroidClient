package com.example.elsysandroid;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class ResponseHandler {
    private PollTask pollTask;

    private static final String NS = null;

    private static final String CIDResp = "CIDResp";
    private static final String SID = "SID";

    public ResponseHandler(PollTask pollTask) {
        this.pollTask = pollTask;
    }

    public void Handle(InputStream response) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(response, null);
        parser.nextTag();

        parser.require(XmlPullParser.START_TAG, NS, "Envelope");
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals(CIDResp)) {
                handleCID(parser);
            } else if (name.equals(SID)) {
                handleSID(parser);
            } else {
                skip(parser);
            }
        }
    }

    private void handleCID(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, NS, CIDResp);
        pollTask.CID = Integer.parseInt(readText(parser));
        parser.require(XmlPullParser.END_TAG, NS, CIDResp);
    }

    private void handleSID(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, NS, SID);
        pollTask.SID = Integer.parseInt(readText(parser));
        parser.require(XmlPullParser.END_TAG, NS, SID);
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}