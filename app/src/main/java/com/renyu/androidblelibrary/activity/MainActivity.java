package com.renyu.androidblelibrary.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.renyu.androidblelibrary.R;
import com.renyu.androidblelibrary.bean.BLECommandModel;
import com.renyu.androidblelibrary.params.Params;
import com.renyu.androidblelibrary.service.BLEService;
import com.renyu.blelibrary.bean.BLEDevice;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ArrayList<BLEDevice> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devices=new ArrayList<>();

        EventBus.getDefault().register(this);

        Button btn_scan= (Button) findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLEService.scan(MainActivity.this);
            }
        });

        Button btn_conn= (Button) findViewById(R.id.btn_conn);
        btn_conn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (devices.size()>0) {
                    BLEService.conn(devices.get(0).getDevice(), MainActivity.this);
                }
            }
        });

        Button btn_write= (Button) findViewById(R.id.btn_write);
        btn_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                BLEService.sendCommand(Params.BLE_COMMAND_GETTIME, null, MainActivity.this);

                HashMap<String, String> params=new HashMap<>();
                Calendar calendar= Calendar.getInstance();
                SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                params.put("time", format.format(calendar.getTime()));
                BLEService.sendWriteCommand(Params.BLE_COMMAND_TIME, params, MainActivity.this);
            }
        });

        Button btn_read= (Button) findViewById(R.id.btn_read);
        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLEService.sendReadCommand(Params.UUID_SERVICE_BATTERY, Params.UUID_SERVICE_BATTERY_READ, MainActivity.this);
            }
        });

        Button btn_rssi= (Button) findViewById(R.id.btn_rssi);
        btn_rssi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLEService.readRSSI(MainActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BLEDevice bleDevice) {
        devices.add(bleDevice);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BLECommandModel model) {
        Log.d("MainActivity", model.getValue().toString());
    }
}
