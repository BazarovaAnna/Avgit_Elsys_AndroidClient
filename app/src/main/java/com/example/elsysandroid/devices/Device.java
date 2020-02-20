package com.example.elsysandroid.devices;

public class Device {
    private String name;
    private String id;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString() {
        return "Device: " + name + " - " + id;
    }
}
