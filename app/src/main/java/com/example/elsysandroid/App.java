package com.example.elsysandroid;

import android.app.Activity;
import android.app.Application;

import com.example.elsysandroid.activities.DaggerMainApplicationComponent;
import com.example.elsysandroid.activities.MainApplicationComponent;
import com.example.elsysandroid.activities.MainApplicationModule;

public class App extends Application {
    private MainApplicationComponent applicationComponent;

    public static App get(Activity activity) {
        return (App) activity.getApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        applicationComponent = DaggerMainApplicationComponent.builder()
                .mainApplicationModule(new MainApplicationModule())
                .build();
    }

    public MainApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }
}