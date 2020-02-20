package com.example.elsysandroid.devices;

import java.util.ArrayList;

public class DeviceList {
    private final ArrayList<Device> inputs;
    private final ArrayList<Device> outs;
    private final ArrayList<Device> readers;
    private final ArrayList<Device> doors;

    public ArrayList<Device> getOuts() {
        return outs;
    }

    public ArrayList<Device> getInputs() {
        return inputs;
    }

    public ArrayList<Device> getReaders() {
        return readers;
    }

    public ArrayList<Device> getDoors() {
        return doors;
    }

    public DeviceList(ArrayList<Device> outs, ArrayList<Device> inputs,
                       ArrayList<Device> readers, ArrayList<Device> doors) {
        this.outs = outs;
        this.inputs = inputs;
        this.readers = readers;
        this.doors = doors;
    }
}