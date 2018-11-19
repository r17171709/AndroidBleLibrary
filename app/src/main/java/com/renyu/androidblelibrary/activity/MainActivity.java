package com.renyu.androidblelibrary.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.renyu.androidblelibrary.R;
import com.renyu.androidblelibrary.adapter.MainAdapter;
import com.renyu.androidblelibrary.bean.BLECommandModel;
import com.renyu.androidblelibrary.bean.BLEConnectModel;
import com.renyu.androidblelibrary.params.Params;
import com.renyu.androidblelibrary.service.BLEService;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.utils.BLEFramework;
import com.renyu.blelibrary.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // 指定当前Scan的Device
    String currentDevice;
    // 扫描到的Device设备集合
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

        EventBus.getDefault().register(this);

        Button btn_scan= findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBlueTooth();
            }
        });

        Button btn_conn= findViewById(R.id.btn_conn);
        btn_conn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (BLEDevice device : devices) {
                    if (device.getDevice()!=null && device.getDevice().getName() != null && device.getDevice().getName().equals("iite-j6J7Nj")) {
                        BLEService.conn(device.getDevice(), MainActivity.this);
                    }
                }
            }
        });

        rv_commands= findViewById(R.id.rv_commands);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Params.RESULT_ENABLE_BT) {
                devices.clear();
                if (TextUtils.isEmpty(currentDevice)) {
                    BLEService.scan(MainActivity.this);
                } else {
                    BLEService.scanAndConn(this, currentDevice);
                }
            }
        }
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
        Toast.makeText(this, model.getValue(), Toast.LENGTH_SHORT).show();
    }

    /**
     * 打开蓝牙开关
     */
    public void openBlueTooth() {
        currentDevice = null;
        if (Utils.checkBluetoothAvaliable(this)) {
            if (Utils.checkBluetoothOpen(this)) {
                devices.clear();
                BLEService.scan(MainActivity.this);
            }
            else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Params.RESULT_ENABLE_BT);
            }
        }
    }

    public void openBlueTooth(String name) {
        currentDevice = name;
        if (Utils.checkBluetoothAvaliable(this)) {
            if (Utils.checkBluetoothOpen(this)) {
                BLEService.scanAndConn(this, name);
            }
            else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Params.RESULT_ENABLE_BT);
            }
        }
    }
}
