package com.example.fengrunhan.wearservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class MyService extends Service {
   
    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: Service Created");
        
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: Start Command");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
