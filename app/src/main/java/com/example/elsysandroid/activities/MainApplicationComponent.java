package com.example.elsysandroid.activities;

import com.example.elsysandroid.PollTask;
import com.example.elsysandroid.devices.DeviceList;
import com.example.elsysandroid.devices.DevicesParser;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {MainApplicationModule.class})
public interface MainApplicationComponent {
    DeviceList getDeviceList();
    DevicesParser getDevicesParser();
    PollTask getPollTask();
}