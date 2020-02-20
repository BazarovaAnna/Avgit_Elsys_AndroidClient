package com.example.elsysandroid.activities;

import com.example.elsysandroid.PollTask;
import com.example.elsysandroid.devices.Device;
import com.example.elsysandroid.devices.DeviceList;

import java.util.ArrayList;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Singleton
@Module
public class MainApplicationModule {

    @Singleton
    @Provides
    DeviceList deviceList() {
        return new DeviceList(new ArrayList<Device>(), new ArrayList<Device>(),
                new ArrayList<Device>(), new ArrayList<Device>());
    }

    @Singleton
    @Provides
    PollTask pollTask() {
        return new PollTask() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onMessage(String message) {

            }
        };
    }
}