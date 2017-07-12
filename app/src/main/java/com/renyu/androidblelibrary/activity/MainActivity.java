package com.renyu.androidblelibrary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.renyu.androidblelibrary.R;
import com.renyu.androidblelibrary.adapter.MainAdapter;
import com.renyu.androidblelibrary.bean.BLECommandModel;
import com.renyu.androidblelibrary.bean.BLEConnectModel;
import com.renyu.androidblelibrary.service.BLEService;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.utils.BLEFramework;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<BLEDevice> devices;

    RecyclerView rv_commands;
    MainAdapter adapter;

    ArrayList<String> models;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, BLEService.class));

        devices=new ArrayList<>();
        models=new ArrayList<>();
        models.add("BLE获取电池电量信息");
        models.add("BLE获取设备名称");
        models.add("BLE获取设备ID");
        models.add("BLE获取设备版本");
        models.add("BLE芯片ID");
        models.add("设定时间");
        models.add("设置惯用手");
        models.add("绑定牙刷");
        models.add("设置userid");
        models.add("获取最近一次刷牙记录");
        models.add("获取刷牙记录总条数");
        models.add("获取指定一条刷牙记录");
        models.add("进入游戏模式");
        models.add("清空所有刷牙信息");
        models.add("通知当前app刷牙方位");
        models.add("获取uniqueid");
        models.add("进入固件升级");
        models.add("开始固件升级");

        EventBus.getDefault().register(this);

        Button btn_scan= (Button) findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devices.clear();
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

        rv_commands= (RecyclerView) findViewById(R.id.rv_commands);
        rv_commands.setHasFixedSize(true);
        rv_commands.setLayoutManager(new LinearLayoutManager(this));
        adapter=new MainAdapter(models, this);
        rv_commands.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        stopService(new Intent(MainActivity.this, BLEService.class));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BLEConnectModel model) {
        if (model.getBleState()== BLEFramework.STATE_SCANNING) {
            Toast.makeText(this, "正在扫描请稍后", Toast.LENGTH_SHORT).show();
        }
        if (model.getBleState()== BLEFramework.STATE_SCANNED) {
            Toast.makeText(this, "扫描完毕，发现" + devices.size() + "台设备", Toast.LENGTH_SHORT).show();
        }
        if (model.getBleState()==BLEFramework.STATE_SERVICES_DISCOVERED) {
            Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();
        }
        if (model.getBleState()==BLEFramework.STATE_DISCONNECTED) {
            Toast.makeText(this, "连接断开", Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BLEDevice bleDevice) {
        devices.add(bleDevice);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BLECommandModel model) {
        Toast.makeText(this, model.getValue().toString(), Toast.LENGTH_SHORT).show();
    }
}
