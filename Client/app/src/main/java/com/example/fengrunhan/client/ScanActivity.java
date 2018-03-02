package com.example.fengrunhan.client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.security.spec.ECField;

public class ScanActivity extends AppCompatActivity {

    TextView textView;
    TextView textView2;
    TextView textView3;
    String data = "";
    String showtime = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        textView = (TextView)findViewById(R.id.textView);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);

        //int result = MainActivity.ins.getVerifyFlag_tmp();
        //Log.i("code", "onCreate: result" + result);
        try
        {
            Intent intent = getIntent();
            data = intent.getStringExtra("info");
            String time = data.substring(data.length() - 38, data.length() - 19);
            showtime = data.substring(data.length() - 19, data.length());
            textView.setText("购票时间：" + time);
            textView3.setText("用票时间：" + showtime);
            textView2.setText("票名：" + data.substring(0, data.length() - 40));
            Toast.makeText(this, "检票成功", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, "扫码失败", Toast.LENGTH_SHORT).show();
        }


    }
}
