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
import com.renyu.androidblelibrary.adapter.Main2Adapter;
import com.renyu.androidblelibrary.bean.BLECommandModel;
import com.renyu.androidblelibrary.bean.BLEConnectModel;
import com.renyu.androidblelibrary.params.Params;
import com.renyu.androidblelibrary.service.BLEService2;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.utils.BLEFramework;
import com.renyu.blelibrary.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {

    // 指定当前Scan的Device
    String currentDevice;
    // 扫描到的Device设备集合
    ArrayList<BLEDevice> devices;

    RecyclerView rv_commands;
    Main2Adapter adapter;

    ArrayList<String> models;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, BLEService2.class));

        devices=new ArrayList<>();
        models=new ArrayList<>();
        models.add("验证UID");
        models.add("验证Code");
        models.add("时间设置");
        models.add("读取EBC和消耗的药瓶量");
        models.add("写入EBC和药瓶");
        models.add("读取每小时的步数");
        models.add("读取的前几天步数数据统计（理论上7天）");
        models.add("读取心率");
        models.add("睡眠时间");
        models.add("设置用户信息");

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
                    if (device.getDevice()!=null && device.getDevice().getName() != null && device.getDevice().getName().equals("ebaina")) {
                        BLEService2.conn(Main2Activity.this, device.getDevice());
                    }
                }
            }
        });

        rv_commands= findViewById(R.id.rv_commands);
        rv_commands.setHasFixedSize(true);
        rv_commands.setLayoutManager(new LinearLayoutManager(this));
        adapter=new Main2Adapter(models, this);
        rv_commands.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        stopService(new Intent(Main2Activity.this, BLEService2.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Params.RESULT_ENABLE_BT) {
                devices.clear();
                if (TextUtils.isEmpty(currentDevice)) {
                    BLEService2.scan(Main2Activity.this);
                } else {
                    BLEService2.scanAndConn(this, currentDevice);
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
                BLEService2.scan(Main2Activity.this);
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
                BLEService2.scanAndConn(this, name);
            }
            else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Params.RESULT_ENABLE_BT);
            }
        }
    }
}
