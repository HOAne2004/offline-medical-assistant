package com.example.trolyyte;

import android.app.Application;
import com.example.trolyyte.di.AppContainer;

public class MedicalAssistantApplication extends Application {

    // Instance duy nhất của AppContainer
    public AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo Dependency Injection Container
        appContainer = new AppContainer(this);
    }
}