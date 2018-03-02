package com.example.fengrunhan.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import static com.example.fengrunhan.client.MainActivity.octo_show;

public class ListActivity extends AppCompatActivity {
    private ListView listView;

    private SimpleAdapter simpleAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);



        listView = (ListView)findViewById(R.id.listView);

        final String[] strings = {"img", "title", "info", "time"};
        final int[] ids = {R.id.img, R.id.title, R.id.info, R.id.time};

        simpleAdapter = new SimpleAdapter(this, getData(), R.layout.list_item, strings, ids);
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, String> map = (HashMap<String, String>)listView.getItemAtPosition(i);

                String title = map.get("title");
                String info = map.get("info");
                //String str ="";
                //String str = tmp(info, info.length(), getUser());
                //String str = MainActivity.ins.tmp(info, info.length(), getUser());
                //Log.d("code", str);
                //Toast.makeText(getApplicationContext(), "你选择了第"+ (i + 1) +"个Item，itemTitle的值是：" + title + "itemInfo的值是:" + info, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ListActivity.this, DetailActivity.class);
                //intent.putExtra("info", str);
                intent.putExtra("filename", info);

                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>)listView.getItemAtPosition(position);
                String title = map.get("title");
                String info = map.get("info");
                Log.i("code", "onItemLongClick: " + title + info);
                Log.i("code", "onItemLongClick: " + getApplicationContext().getFilesDir().getAbsolutePath());

                File todelete = new File(info);
                if(todelete.exists())
                {
                    boolean flag = todelete.delete();
                    if(flag == true)
                    {
                        Toast.makeText(getApplicationContext(), "删除文件成功", Toast.LENGTH_SHORT).show();
                        simpleAdapter = new SimpleAdapter(getApplicationContext(), getData(), R.layout.list_item, strings, ids);
                        listView.setAdapter(simpleAdapter);
                        //simpleAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "删除文件失败", Toast.LENGTH_SHORT).show();
                    }
                    Log.i("code", "onItemLongClick: exists");
                }
                else
                {
                    Log.i("code", "onItemLongClick: not exists");
                }

                return true;
            }
        });
    }


    private List<HashMap<String, Object>> getData()
    {
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = null;

        //String path = getApplicationContext().getFilesDir().getAbsolutePath();
        String path = "/data/data/com.example.fengrunhan.client";

        File mfile = new File(path);
        File[] files = mfile.listFiles();

        for(int i = 1; i < files.length; i++)
        {
            map = new HashMap<String, Object>();
            File file = files[i];
            if(file.getName().startsWith("20"))
            {
                map.put("title", file.getName());
                map.put("info", file.getPath());
                map.put("time", "xx月xx日");
                map.put("img", R.mipmap.ic_launcher);
                list.add(map);
            }

        }

        return list;
    }

    private String getUser()
    {
        SharedPreferences settings = getSharedPreferences("octo_tmp", 0);
        String user = settings.getString("user", "NULL");
        return user;
    }

}
