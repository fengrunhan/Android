package com.example.fengrunhan.wearservice;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

public class AlarmService extends Service {

    private boolean mSerialPermission;
    private String SerialNum;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private int stepcount = 0;

    private boolean mGpsPermission;
    private FusedLocationProviderClient mFusedLocationClient;

    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000 * 10;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private double longitude;
    private double latitude;
    private Location mLocation;

    private String role;
    private String time = "";
    private String createTime;
    private String message;

    SQLiteDatabase db;
    private String dbpath;


    public AlarmService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbpath = getApplicationContext().getFilesDir().getAbsolutePath();
        Log.i(TAG, "onCreate: " + dbpath);
        String create = "CREATE TABLE IF NOT EXISTS remind (id integer primary key autoincrement, " + "role varchar(30), " +
                "createtime varchar(30), remindtime varchar(30), message varchar(100), noticed integer)";
        String dbname = dbpath + "/remind.db";
        Log.i(TAG, "onCreate: " + dbname);

        db = SQLiteDatabase.openOrCreateDatabase(dbname, null);
        db.execSQL(create);
        db.close();

        createLocationCallback();
        createLocationRequest();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: start service");

        new Thread(new Runnable() {
            @Override
            public void run() {
                //setNotification((int)(Math.random()*100), "" + new Date().toString());
                SerialNum = getSerialNum();
                stepCounter();
                getLocation();
                sendMessage();
                query();

            }
        }).start();

        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int second = 1000 * 60;
        long triggerTime = SystemClock.elapsedRealtime() + second;

        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void query(){
        Log.i(TAG, "query: query begin");
        db = SQLiteDatabase.openOrCreateDatabase(dbpath + "/remind.db", null);
        Cursor cursor = db.rawQuery("select * from remind where noticed = 0", null);
        if(cursor.moveToFirst())
        {

            for(int num = 0; num < cursor.getCount(); num++)
            {
                cursor.move(num);
                int dbid = cursor.getInt(0);
                String dbrole = cursor.getString(1);
                String dbcreatetime = cursor.getString(2);
                String dbremindtime = cursor.getString(3);
                final String dbmessage = cursor.getString(4);
                int dbnoticed = cursor.getInt(5);
                Log.i(TAG, "run: database" + dbid + dbrole + dbcreatetime + dbremindtime + dbmessage + dbnoticed);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d H:m:s");
                try {
                    Date end = dateFormat.parse(dbremindtime);
                    Date begin = new Date();
                    final long diff = (end.getTime() - begin.getTime()) / 1000;
                    Log.i(TAG, "query: begin:" + begin + end + diff);

                    if(diff < 300)
                    {
                        Log.i(TAG, "query: diff" + diff);
                        AlarmManager alarmmanager = (AlarmManager)getSystemService(ALARM_SERVICE);

                        long triggerTime = SystemClock.elapsedRealtime() + diff * 1000;

                        Intent intent = new Intent(this, NotificationReceiver.class);
                        intent.putExtra("message", dbmessage);
                        intent.putExtra("diff", Math.abs((int) diff));
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int)(Math.random() * 500), intent, 0);

                        if(diff <= 0)
                        {
                            alarmmanager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 3 * 1000, pendingIntent);
                        }
                        else
                        {
                            alarmmanager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
                        }


                        String update = "update remind set noticed = 1 where id = " + dbid;
                        db.execSQL(update);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            Log.i(TAG, "query: getCount is none");
        }
        db.close();


    }

    private void sendMessage(){
        new Thread(){
            public void run(){
                try {

                    Log.i(TAG, "run: Thread:" + Thread.currentThread().getId());
                    //String path = "http://121.43.166.69/watch/";
                    String path = "http://192.168.2.249:8000/watch/";
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setConnectTimeout(10000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");//设置请求的方式
                    conn.connect();

                    String tmp = "device=" + SerialNum + "&longitude=" + longitude + "&latitude=" + latitude + "&step=" + stepcount;
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
                        //String weather = rjson.getString("weather");
                        JSONObject weather = new JSONObject(rjson.getString("weather"));
                        String wind_sc = weather.getString("wind_sc");
                        String wind_dir = weather.getString("wind_dir");
                        String cond_txt = weather.getString("cond_txt");

                        JSONArray remind = rjson.getJSONArray("reminds");
                        Log.i(TAG, "run: weather:" + wind_sc + wind_dir + cond_txt);
                        Log.i(TAG, "run: " + remind + " remind length：" + remind.length());

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
                                //Log.i(TAG, "run: " + role + " " + time + " " + createTime + " " + message);

                                createTime = createTime.replace("T", " ");
                                createTime = createTime.replace("Z", "");
                                time = time.replace("T"," ");
                                time = time.replace("Z", "");
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d H:m:s");
                                Date date = dateFormat.parse(time);
                                Log.i(TAG, "run: " + date.toString());

                                Date now = new Date();
                                final long interval = (date.getTime() - now.getTime()) / 1000;
                                if(interval < 300)
                                {
                                    Log.i(TAG, "run: interval < 300 interval:" + interval);

                                    AlarmManager alarmmanager = (AlarmManager)getSystemService(ALARM_SERVICE);
                                    long triggerTime = SystemClock.elapsedRealtime() + interval * 1000;

                                    Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
                                    intent.putExtra("message", message);
                                    intent.putExtra("diff", Math.abs((int)interval));
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), (int)(Math.random() * 300), intent, 0);

                                    if(interval <= 0)
                                    {
                                        alarmmanager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 3 * 1000, pendingIntent);
                                    }
                                    else
                                    {
                                        alarmmanager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
                                    }

                                }
                                else {
                                    Log.i(TAG, "run: interval > 300 save into database");
                                    db = SQLiteDatabase.openOrCreateDatabase(dbpath + "/remind.db", null);
                                    String sql = "insert into remind(role, createtime, remindtime, message, noticed) " +
                                            "values('" + role + "','" + createTime + "','" + time + "','" + message + "','0')";
                                    db.execSQL(sql);
                                    Log.i(TAG, "run: write into database success");
                                    db.close();
                                }
                            }
                        }
                    }
                    else {
                        Log.i(TAG, "run: code" + code);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    sendMessage();
                }
            };
        }.start();
    }



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

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
