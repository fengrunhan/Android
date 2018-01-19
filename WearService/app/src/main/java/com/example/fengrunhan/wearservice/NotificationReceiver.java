package com.example.fengrunhan.wearservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by Feng Runhan on 2018/1/18.
 */

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: on Receive");

        int diff = intent.getIntExtra("diff", (int)(Math.random() * 100));
        String message = intent.getStringExtra("message");

        Log.i(TAG, "onReceive: NotificationReceiver" + diff + message);

        Intent i = new Intent(context, NotificationService.class);
        i.putExtra("message", message);
        i.putExtra("diff", diff);

        context.startService(i);
    }
}
