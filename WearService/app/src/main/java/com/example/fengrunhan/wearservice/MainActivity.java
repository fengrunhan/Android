package com.example.fengrunhan.wearservice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends WearableActivity {

    private static String TAG = "Service: ";
    private TextView mTextView;
    private Button mButton;



    public static MainActivity ins;

    private Location mLocation;
    private boolean mGpsPermission;
    private FusedLocationProviderClient mFusedLocationClient;
    private double longitude;
    private double latitude;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private LocationManager lm;
    private String provider;
    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private boolean mSerialPermission;
    private String SerialNum;

    private Intent intent;
    private Intent stopintent;

    private int stepcount = 0;

    private Timer mtimer = new Timer();
    TimerTask mtask = new TimerTask() {
        @Override
        public void run() {
            //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            //String date = dateFormat.format(new Date());
            String date = new Date().toLocaleString();
            Log.i(TAG, "run: " + date);
            //stepCounter();
            //getLocation();


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText("步数:" + stepcount + "经度:" + longitude + "纬度:" + latitude);
                }
            });

        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ins = this;

        mTextView = (TextView) findViewById(R.id.text);


        mButton = (Button)findViewById(R.id.button);


        mSerialPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
       /* if(!mSerialPermission){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        }*/

        mGpsPermission = (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED);
        if(!mGpsPermission || !mSerialPermission){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 1);
        }

        mTextView.setText("服务待启动");
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextView.setText("服务已启动");
                Intent AlarmIntent = new Intent(getApplicationContext(), AlarmService.class);
                startService(AlarmIntent);

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
    }



}
