package com.example.fengrunhan.showfile;

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

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)findViewById(R.id.listView);

        String[] strings = {"img", "title", "info", "time"};
        int[] ids = {R.id.img, R.id.title, R.id.info, R.id.time};

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, getData(), R.layout.list_item, strings, ids);
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, String> map = (HashMap<String, String>)listView.getItemAtPosition(i);

                String title = map.get("title");
                String info = map.get("info");

                Toast.makeText(getApplicationContext(), "你选择了第"+ (i + 1) +"个Item，itemTitle的值是：" + title + "itemInfo的值是:" + info, Toast.LENGTH_SHORT).show();
            }
        });

    }

        private List<HashMap<String, Object>> getData()
        {
            ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> map = null;

            for(int i = 1; i < 40; i++)
            {
                map = new HashMap<String, Object>();
                map.put("title", "票" + i);
                map.put("info", "漫威" + i);
                map.put("time", "11月30日");
                map.put("img", R.mipmap.ic_launcher);
                list.add(map);
            }

            return list;
        }

        private List<String> getFile()
        {
            List<String> filename = new ArrayList<String>();

            String path = getApplicationContext().getFilesDir().getAbsolutePath();
            Log.d("path: ", path);

            File mfile = new File(path);
            File[] files = mfile.listFiles();

            for(int i = 0; i < files.length; i++)
            {
                File file = files[i];
                filename.add(file.getPath());
            }

            return filename;
        }


}
