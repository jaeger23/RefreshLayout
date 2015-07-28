package com.jaeger.refreshlayout;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.jaeger.refreshlayout.lib.RefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private RefreshLayout rlRefresh;
    private ListView lvStrings;
    private Button btnStopRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rlRefresh = (RefreshLayout) findViewById(R.id.rl_refresh);
        lvStrings = (ListView) findViewById(R.id.lv_strings);
        btnStopRefresh = (Button) findViewById(R.id.btn_stop_refresh);
        btnStopRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlRefresh.finishRefreshing();
            }
        });
        String str = "This is a refresh layout by jaeger. This class represents the basic building block for user interface components. A View occupies a rectangular area on the screen and is responsible for drawing and event handling. ";
        String[] strings = str.split(" ");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.tv_string, strings);
        lvStrings.setAdapter(adapter);

        rlRefresh.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {
            @Override
            public void refreshing() {
                Toast.makeText(MainActivity.this, "list is refreshing", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void completeRefresh() {
                Toast.makeText(MainActivity.this, "stop refreshing", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
