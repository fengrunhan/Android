package com.example.fengrunhan.wearservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

public class NotificationService extends Service {




    public NotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                int diff = intent.getIntExtra("diff", (int)(Math.random() * 100));
                String message = intent.getStringExtra("message");
                Log.i(TAG, "run: diff: " + diff + message);
                setNotification(diff, message);
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void setNotification(int i, String dbmessage){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("消息")
                .setContentText(dbmessage)
                //.setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 2500})
                .setSmallIcon(R.mipmap.ic_launcher);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(i, builder.build());
    }
}
