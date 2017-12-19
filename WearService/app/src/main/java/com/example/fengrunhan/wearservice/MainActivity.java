package com.example.fengrunhan.wearservice;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
    private Button mButton2;


    public static MainActivity ins;

    private Location mLocation;
    private boolean mGpsPermission;
    private FusedLocationProviderClient mFusedLocationClient;
    private double longitude;
    private double latitude;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private LocationManager lm;
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
            stepCounter();
            getLocation();

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
        mButton2 = (Button)findViewById(R.id.button2);

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

        SerialNum = getSerialNum();
        createLocationCallback();
        createLocationRequest();
        // Enables Always-on
        setAmbientEnabled();
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //setNotification();
                //stepCounter();
                //getLocation();
                intent = new Intent(getApplicationContext(), MyService.class);
                startService(intent);
                //sendMessage();
            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopintent = new Intent(getApplicationContext(), MyService.class);
                stopService(stopintent);
            }
        });


        //mtimer.schedule(mtask, 0, 5000);

        /*lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(l != null){
            mTextView.setText(l.getLatitude() + ", " + l.getLatitude());
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "onLocationChanged:  lm changed");
                mTextView.setText("GPS" + location.getLatitude() + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });
        */

    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    //手表序列号
    private String getSerialNum(){
        mSerialPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        == PackageManager.PERMISSION_GRANTED;
        if(mSerialPermission){
            return Build.SERIAL;
        }
        else {
            return "No permission";
        }
    }

    private void setNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("消息")
                .setContentText("这是一条消息")
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(0, builder.build());
    }

    private void stepCounter(){
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        SensorEventListener mySensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                Log.i(TAG, "onSensorChanged: " + sensorEvent.values[0]);
                stepcount = (int) sensorEvent.values[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        mSensorManager.registerListener(mySensorListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        //mSensorManager.unregisterListener(mySensorListener);
    }

    private void getLocation(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mGpsPermission = (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) &&
                        (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED);
        if(!mGpsPermission){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
        }
        
        if(mGpsPermission){
            Log.i(TAG, "getLocation: mGpsPermission ed");
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        mLocation = location;
                        Log.d(TAG, "onSuccess: " + mLocation);
                        longitude = mLocation.getLongitude();
                        latitude = mLocation.getLatitude();
                    }
                    else{
                        Log.d(TAG, "onSuccess: location == null");
                    }
                }
            });

            if(Looper.myLooper() == null){
                Looper.prepare();
            }

            //Log.i(TAG, "getLocation: before request");
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            //Log.i(TAG, "getLocation: after request");



        }
        else {
            Log.i(TAG, "getLocation: permission denied");
        }

    }

    /* # 错误请求返回的
    {'status': 201, 'info': 'must have device id'}
    {'status': 202, 'info': 'must have step'}


    #　正常请求的status=200
    ## 有提醒时，reminds 为一个list
    {"reminds": [{"created_date": "2017-12-11T08:17:12.088Z", "role_name": "p1",
    "remind_date": "2017-12-11T12:16:43Z"}], "status": 200, "weather": "windy"}
    ##　无提醒时list 为空
    {"status": 200, "weather": "windy", "reminds": []}


    import requests

    url = "http://127.0.0.1:9000/watch/"
    data = {'device': 'd1', 'longitude': 123.0, 'latitude': 33.0,'step':10000}
    r = requests.post(url=url, data=data)
    print(r.text)

    */
    private void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback(){
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Log.i(TAG, "onLocationResult: location call back");
                mLocation = locationResult.getLastLocation();
                longitude = mLocation.getLongitude();
                latitude = mLocation.getLatitude();
            }
        };
    }

    private void sendMessage(){
        new Thread(){
            public void run(){
                try {
                    //String path = "http://121.43.166.69/watch/";
                    String path = "http://121.43.166.69/watch/";
                    URL url = new URL(path);

                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");//设置请求的方式

                    conn.connect();



                    String tmp = "device=" + SerialNum + "&longitude=" + longitude + "&latitude=" + latitude + "&step="
                            + stepcount;


                    Log.i(TAG, "run: str  " + tmp);
                    //-------------使用字节流发送数据--------------
                    //OutputStream out = conn.getOutputStream();
                    //BufferedOutputStream bos = new BufferedOutputStream(out);//缓冲字节流包装字节流
                    //byte[] bytes = jsonstr.getBytes("UTF-8");//把字符串转化为字节数组
                    //bos.write(bytes);//把这个字节数组的数据写入缓冲区中
                    //bos.flush();//刷新缓冲区，发送数据
                    //out.close();
                    //bos.close();

                    //------------字符流写入数据------------
                    OutputStream out = conn.getOutputStream();//输出流，用来发送请求，http请求实际上直到这个函数里面才正式发送出去
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));//创建字符流对象并用高效缓冲流包装它，便获得最高的效率,发送的是字符串推荐用字符流，其它数据就用字节流
                    bw.write(tmp);//把json字符串写入缓冲区中
                    bw.flush();//刷新缓冲区，把数据发送出去，这步很重要
                    out.close();
                    bw.close();//使用完关闭



                    int code = conn.getResponseCode();
                    if (code == 200) {
                        InputStream is = conn.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        String str = null;
                        StringBuffer buffer = new StringBuffer();
                        while((str = br.readLine())!=null){
                            buffer.append(str);
                        }
                        is.close();
                        br.close();
                        Log.i(TAG, "run: buffer" + buffer);
                        JSONObject rjson = new JSONObject(buffer.toString());
                        JSONArray remind = new JSONArray();
                        String weather = rjson.getString("weather");
                        //String remind = rjson.getString("reminds");
                        //JSONArray remind = new JSONArray("reminds");
                        remind = rjson.getJSONArray("reminds");
                        Log.i(TAG, "run: " + remind + " length" + remind.length());

                        String role;
                        String time = "";
                        String createTime;
                        if(remind.length() == 0){
                            Log.i(TAG, "run: no remind to notice");
                        }
                        else {
                            for(int i = 0; i < remind.length(); i++){
                                JSONObject remindDetail = remind.getJSONObject(i);
                                role = remindDetail.getString("role_name");
                                time = remindDetail.getString("remind_date");
                                createTime = remindDetail.getString("created_date");

                                Log.i(TAG, "run: " + role + " " + time + " " + createTime);
                            }
                        }

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        Log.i(TAG, "run: calendar" + calendar.getTime());


                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d H:m:s");

                        time = time.replace('T',' ');
                        time = time.replace("Z", "");
                        Date date = dateFormat.parse(time);

                        Calendar calendarnew = Calendar.getInstance();
                        calendarnew.setTime(date);
                        long val = calendarnew.getTimeInMillis() - calendar.getTimeInMillis();

                        Log.i(TAG, "run: val" + val);
                        Log.i(TAG, "run: calendar new" + calendarnew.getTime());

                        Log.i(TAG, "run: " + date.toString());

                        Log.i(TAG, "run: " + weather);
                        //boolean result = rjson.getBoolean("json");
                    }
                    else {
                        Log.i(TAG, "run: code" + code);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            };
        }.start();
    }

}
