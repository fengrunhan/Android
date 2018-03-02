package com.example.fengrunhan.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.google.zxing.encoding.EncodingHandler;
import com.google.zxing.qrcode.encoder.QRCode;
import com.example.fengrunhan.client.MainActivity;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

//import static com.example.fengrunhan.client.MainActivity.octo_show;

public class DetailActivity extends AppCompatActivity {


    ImageView qrcode;

    private boolean stop = false;
    Thread thread;
    String data;
    String filename;
    String output = "";
    String str = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        qrcode = (ImageView)findViewById(R.id.QrCode);

        Intent intent = getIntent();
        filename = intent.getStringExtra("filename");






        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("code", "run: thread");
                while (!stop)
                {
                    mHandler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(1000 * 20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();



    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    final String str = MainActivity.ins.tmp(filename, filename.length(), getUser());
         /*           final String str = "AwAAAGZyaArl2jgYvVPKPt3efTKOSH/BlfSBfSuYROlRX0JXeWHCfWIPAHpu1l/r6F3Jgfuj6RHxkdk2y36dB4Wo3Dbb6aFUaGUgeGVsJ" +
                            "25hZ2EgYXJlIGEgc2VlbWluZ2x5IGV4dGluY3QgcmFjZSBvZiBleHRyYWdhbGFjdGljIHNjaWVudGlzdHMuMjAxOC0wMi0wMSAxMjoxO" +
                            "ToxMzIwMTgtMDItMDEgMTI6NTc6MDQAcQ82CQCqixBPzS7UaueY4VRq5DO4HvkZjSkx+2izXNm5vBGp6JsVy6KBI0wL+Mis8uJ6uUsbJc" +
                            "FOqmxjI6jW1Q==";*/
                    Log.d("code", str);

                    try {
                        final Bitmap mBitmap = EncodingHandler.createQRCode(str, 500);

                        if(mBitmap != null)
                        {
                            Toast.makeText(getApplicationContext(), "二维码生成成功", Toast.LENGTH_LONG).show();
                            qrcode.setImageBitmap(mBitmap);
                        }
                    }
                    catch (WriterException e)
                    {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    private String getUser()
    {
        SharedPreferences settings = getSharedPreferences("octo_tmp", 0);
        String user = settings.getString("user", "NULL");
        return user;
    }

    @Override
    protected void onPause() {
        Log.i("code", "onPause: ");
        super.onPause();

        //thread.interrupt();
        stop = true;

    }

    @Override
    protected void onStop() {
        Log.i("code", "onStop: ");
        super.onStop();
    }
}
