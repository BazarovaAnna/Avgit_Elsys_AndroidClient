package com.example.elsysandroid.devices;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import javax.inject.Inject;

public class DevicesParser {

    private DeviceList deviceList;

    @Inject
    public DevicesParser(DeviceList deviceList) {
        this.deviceList = deviceList;
    }

    public boolean parse(XmlPullParser xpp) {
        boolean status = true;
        Device currentDevice = null;
        boolean inEntryGroup = false;
        boolean inEntryDevice = false;
        String textValue = "";

        ArrayList<Device> inputs = deviceList.getInputs();
        ArrayList<Device> outs = deviceList.getOuts();
        ArrayList<Device> readers = deviceList.getReaders();
        ArrayList<Device> doors = deviceList.getDoors();

        inputs.clear();
        outs.clear();
        doors.clear();
        readers.clear();

        try {
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                String tagName = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        switch (tagName.toLowerCase()) {
                            case "outs":
                            case "inputs":
                            case "readers":
                            case "doors":
                                inEntryGroup = true;
                                break;
                            case "out":
                            case "input":
                            case "reader":
                            case "door":
                                if (inEntryGroup) {
                                    inEntryDevice = true;
                                    currentDevice = new Device();
                                    break;
                                }
                        }
                        break;
                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (inEntryGroup) {
                            if (inEntryDevice) {
                                switch (tagName.toLowerCase()) {
                                    case "out":
                                        outs.add(currentDevice);
                                        inEntryDevice = false;
                                        break;
                                    case "input":
                                        inputs.add(currentDevice);
                                        inEntryDevice = false;
                                        break;
                                    case "reader":
                                        readers.add(currentDevice);
                                        inEntryDevice = false;
                                        break;
                                    case "door":
                                        doors.add(currentDevice);
                                        inEntryDevice = false;
                                        break;
                                    case "name":
                                        currentDevice.setName(textValue);
                                        break;
                                    case "id":
                                        currentDevice.setId(textValue);
                                        break;
                                }
                            }
                        }
                        if ("outs".equalsIgnoreCase(tagName) || "inputs".equalsIgnoreCase(tagName)
                                || "doors".equalsIgnoreCase(tagName) || "readers".equalsIgnoreCase(tagName))
                            inEntryGroup = false;
                        break;
                    default:
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        }
        return status;
    }
}