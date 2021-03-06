package com.example.fengrunhan.wearservice;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;
import static com.example.fengrunhan.wearservice.MainActivity.ins;

public class MyService extends Service {

    private boolean mSerialPermission;
    private String SerialNum;

    private SensorManager mSensorManager;
    private Sensor mSensor;



    private Location mLocation;

    private String role;
    private String time = "";
    private String createTime;
    private String message;


    private boolean mGpsPermission;
    private FusedLocationProviderClient mFusedLocationClient;

    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private double longitude;
    private double latitude;

    private int stepcount = 0;

    private int i = 0;

    private Timer mtimer = new Timer();
    TimerTask mtask = new TimerTask() {
        @Override
        public void run() {
            //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            //String date = dateFormat.format(new Date());
            //String date = new Date().toLocaleString();
            Log.i(TAG, "run: serialnum" +" " + SerialNum);
            stepCounter();

            getLocation();
            sendMessage();
        }
    };

    private Timer notificationTimer = new Timer();
    TimerTask notificationTask = new TimerTask() {
        @Override
        public void run() {
            setNotification(i);
        }
    };
   
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
        SerialNum = getSerialNum();
        createLocationCallback();
        createLocationRequest();


        mtimer.schedule(mtask, 0, 60000);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: service destroy");
        mtimer.cancel();
        notificationTimer.cancel();
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
        mSensorManager.unregisterListener(mySensorListener);
    }

    private void setNotification(int i){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("消息")
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        manager.notify(i, builder.build());
        notificationTask.cancel();
    }

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
                Log.i(TAG, "onLocationResult: longitude" + longitude + " latitude:" + latitude);
            }
        };
    }

    private void getLocation(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mGpsPermission = (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED);

        if(mGpsPermission){

            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
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
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());


        }
        else {
            Log.i(TAG, "getLocation: permission denied");
        }

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
                        String weather = rjson.getString("weather");
                        JSONArray remind = rjson.getJSONArray("reminds");
                        Log.i(TAG, "run: " + remind + " length" + remind.length());


                        if(remind.length() == 0){
                            Log.i(TAG, "run: no remind to notice");
                        }
                        else {
                            for(int i = 0; i < remind.length(); i++){
                                JSONObject remindDetail = remind.getJSONObject(i);
                                role = remindDetail.getString("role_name");
                                time = remindDetail.getString("remind_date");
                                createTime = remindDetail.getString("created_date");
                                message = remindDetail.getString("message");
                                Log.i(TAG, "run: " + role + " " + time + " " + createTime + " " + message);

                                //当前时间
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(new Date());
                                Log.i(TAG, "run: calendar" + calendar.getTime());
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d H:m:s");


                                //提醒时间
                                time = time.replace('T',' ');
                                time = time.replace("Z", "");
                                Date date = dateFormat.parse(time);

                                notificationTimer.schedule(notificationTask, date);

                                Log.i(TAG, "run: " + date.toString());
                            }
                        }
                        Log.i(TAG, "run: " + weather);
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
