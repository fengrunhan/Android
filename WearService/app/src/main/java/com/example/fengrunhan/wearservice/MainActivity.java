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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends WearableActivity {

    private static String TAG = "Service: ";
    private TextView mTextView;
    private Button mButton;

    private boolean mSerialPermission;

    private Location mLocation;
    private boolean mGpsPermission;
    private FusedLocationProviderClient mFusedLocationClient;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private String SerialNum;

    private Intent intent;

    private Timer mtimer = new Timer();
    TimerTask mtask = new TimerTask() {
        @Override
        public void run() {
            //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            //String date = dateFormat.format(new Date());
            String date = new Date().toLocaleString();
            Log.i(TAG, "run: " + date);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);
        mButton = (Button)findViewById(R.id.button);

        // Enables Always-on
        setAmbientEnabled();
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getApplicationContext(), MyService.class);
                startService(intent);
            }
        });

        //mtimer.schedule(mtask, 0, 5000);
/*        while (true)
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String data = dateFormat.format(new Date());
        }*/

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

        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    mLocation = location;
                    Log.d(TAG, "onSuccess: " + mLocation);
                }
                else{
                    Log.d(TAG, "onSuccess: location == null");
                }
            }
        });
    }


    private void sendMessage(){
        new Thread(){
            public void run(){
                try {
                    String path = "";
                    URL url = new URL(path);

                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");//设置请求的方式
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");//设置消息的类型
                    conn.connect();

                    JSONObject json = new JSONObject();
                    //json.put("name", URLEncoder.encode("", "UTF-8"));
                    String jsonstr = json.toString();

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
                    bw.write(jsonstr);//把json字符串写入缓冲区中
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
                        JSONObject rjson = new JSONObject(buffer.toString());
                        //boolean result = rjson.getBoolean("json");
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            };
        }.start();
    }

}
