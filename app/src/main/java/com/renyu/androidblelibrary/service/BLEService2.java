package com.renyu.androidblelibrary.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.renyu.androidblelibrary.bean.BLEConnectModel;
import com.renyu.androidblelibrary.params.Params;
import com.renyu.androidblelibrary.utils.AESCBCNoPadding;
import com.renyu.androidblelibrary.utils.DataUtils;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.impl.BLEConnectListener;
import com.renyu.blelibrary.impl.BLEOTAListener;
import com.renyu.blelibrary.impl.BLERSSIListener;
import com.renyu.blelibrary.impl.BLEReadResponseListener;
import com.renyu.blelibrary.impl.BLEStateChangeListener;
import com.renyu.blelibrary.impl.BLEWriteResponseListener;
import com.renyu.blelibrary.impl.IScanAndConnRule;
import com.renyu.blelibrary.utils.BLEFramework;
import com.renyu.blelibrary.utils.HexUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

/**
 * Created by renyu on 2017/6/6.
 */

public class BLEService2 extends Service {

    BLEFramework bleFramework;

    @Override
    public void onCreate() {
        super.onCreate();

        bleFramework = BLEFramework.getBleFrameworkInstance();
        bleFramework.setParams(this.getApplicationContext());
        // 扫描设备回调
        bleFramework.setBleConnectListener(new BLEConnectListener() {
            @Override
            public void getAllScanDevice(BLEDevice bleDevice) {
                Log.d("BLEService2", bleDevice.getDevice().getName() + " " + bleDevice.getDevice().getAddress());
                EventBus.getDefault().post(bleDevice);
            }
        });
        // 当前扫描状态回调
        bleFramework.setBleStateChangeListener(new BLEStateChangeListener() {
            @Override
            public void getCurrentState(int currentState) {
                Log.d("BLEService2", "currentState:" + currentState);
                BLEConnectModel model=new BLEConnectModel();
                model.setBleState(currentState);
                EventBus.getDefault().post(model);
            }
        });
        // 写回调
        bleFramework.setBLEWriteResponseListener(new BLEWriteResponseListener() {
            @Override
            public void getResponseValues(byte[] value) {
                Log.d("BLEService2", "white:");
                // 验证UID
                if ((int) value[0] == 0x01) {

                }
                // 读取每小时的步数,表示还有数据
                // 读取每小时的步数,表示没有数据了，后面Byte[1]-[16]有效
                else if (value[0] == (byte) 0x82 || value[0] == (byte) 0x80) {
                    byte[] temp = new byte[value.length - 1];
                    for (int i = 0; i < temp.length; i++) {
                        temp[i] = value[i + 1];
                    }

                    byte[] result = AESCBCNoPadding.decryption(temp);

                    // 一小时产生的EBC
                    int ebc = (int) value[6];

                    // 一小时内距离（单位：十米）
                    byte[] distanceTemp = new byte[2];
                    distanceTemp[0] = value[8];
                    distanceTemp[1] = value[7];
                    int distance = HexUtil.byte2ToInt(distanceTemp);

                    // 一小时内消耗的卡路里（单位：千卡）
                    byte[] calorieTemp = new byte[2];
                    calorieTemp[0] = value[10];
                    calorieTemp[1] = value[9];
                    int calorie = HexUtil.byte2ToInt(calorieTemp);

                    // 一小时的总步数
                    byte[] totalStepTemp = new byte[2];
                    totalStepTemp[0] = value[12];
                    totalStepTemp[1] = value[11];
                    int totalStep = HexUtil.byte2ToInt(totalStepTemp);

//                    Toast.makeText(BLEService2.this, result, Toast.LENGTH_SHORT).show();
                }
                // 读取每小时的步数,表示没有数据了，后面Byte[1]-[16]无效
                else if (value[0] == (byte) 0x00) {
                    Toast.makeText(BLEService2.this, "读取每小时的步数 没有数据了", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 读回调
        bleFramework.setBleReadResponseListener(new BLEReadResponseListener() {
            @Override
            public void getResponseValues(UUID CharacUUID, byte[] value) {
                Log.d("BLEService2", "read:");
            }
        });
        // RSSI回调
        bleFramework.setBlerssiListener(new BLERSSIListener() {
            @Override
            public void getRssi(int rssi) {
                Log.d("BLEService2", "rssi:" + rssi);
            }
        });
        // OTA回调
        bleFramework.setBleotaListener(new BLEOTAListener() {
            @Override
            public void showProgress(int progress) {
                Log.d("BLEService2", "ota:" + progress);
            }
        });
        bleFramework.initBLE();

        // 注册蓝牙开关广播
        registerReceiver(bluetoothStatueReceiver, makeFilter());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getStringExtra(Params.COMMAND) != null) {
            if (intent.getStringExtra(Params.COMMAND).equals(Params.SCAN)) {
                bleFramework.startScan();
            }
            if (intent.getStringExtra(Params.COMMAND).equals(Params.CONN)) {
                bleFramework.startConn((BluetoothDevice) intent.getParcelableExtra(Params.DEVICE));
            }
            if (intent.getStringExtra(Params.COMMAND).equals(Params.SCANCONN)) {
                final String deviceName = intent.getStringExtra(Params.DEVICE);
                bleFramework.startScanAndConn(new IScanAndConnRule() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public boolean rule21(ScanResult scanResult) {
                        return !TextUtils.isEmpty(scanResult.getDevice().getName()) && scanResult.getDevice().getName().equals(deviceName);
                    }

                    @Override
                    public boolean rule(BluetoothDevice bluetoothDevice) {
                        return false;
                    }
                });
            }
            if (intent.getStringExtra(Params.COMMAND).equals(Params.WRITE)) {
                bleFramework.addWriteCommand(Params.UUID_SERVICE, Params.UUID_SERVICE_WRITE, intent.getByteArrayExtra(Params.BYTECODE));
            }
            if (intent.getStringExtra(Params.COMMAND).equals(Params.READ)) {
                bleFramework.addReadCommand((UUID) intent.getSerializableExtra(Params.SERVICEUUID), (UUID) intent.getSerializableExtra(Params.CHARACUUID));
            }
            if (intent.getStringExtra(Params.COMMAND).equals(Params.RSSI)) {
                bleFramework.readRSSI();
            }
            if (intent.getStringExtra(Params.COMMAND).equals(Params.DISCONN)) {
                bleFramework.stopScan(true);
                bleFramework.disconnect();
            }
            if (intent.getStringExtra(Params.COMMAND).equals(Params.OTA)) {
                bleFramework.startOTA();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 关闭蓝牙开关广播
        unregisterReceiver(bluetoothStatueReceiver);
        // 断开蓝牙
        bleFramework.disconnect();
    }

    private IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    // 蓝牙开关监听
    private BroadcastReceiver bluetoothStatueReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            break;
                        case BluetoothAdapter.STATE_ON:
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            bleFramework.stopScan(true);
                            bleFramework.disconnect();
                            break;
                    }
                    break;
            }
        }
    };

    /**
     * 发送写指令
     */
    public static void sendWriteCommand() {

    }

    /**
     * 发送读指令
     */
    public static void sendReadCommand() {

    }

    /**
     * 连接设备
     *
     * @param bluetoothDevice
     * @param context
     */
    public static void conn(Context context, BluetoothDevice bluetoothDevice) {
        Intent intent = new Intent(context, BLEService2.class);
        intent.putExtra(Params.COMMAND, Params.CONN);
        intent.putExtra(Params.DEVICE, bluetoothDevice);
        context.startService(intent);
    }

    /**
     * 断开连接
     *
     * @param context
     */
    public static void disconn(Context context) {
        Intent intent = new Intent(context, BLEService2.class);
        intent.putExtra(Params.COMMAND, Params.DISCONN);
        context.startService(intent);
    }

    /**
     * 扫描设备
     *
     * @param context
     */
    public static void scan(Context context) {
        Intent intent = new Intent(context, BLEService2.class);
        intent.putExtra(Params.COMMAND, Params.SCAN);
        context.startService(intent);
    }

    /**
     * 扫描并连接指定设备
     *
     * @param context
     */
    public static void scanAndConn(Context context, String deviceName) {
        Intent intent = new Intent(context, BLEService2.class);
        intent.putExtra(Params.COMMAND, Params.SCANCONN);
        intent.putExtra(Params.DEVICE, deviceName);
        context.startService(intent);
    }

    /**
     * 读取RSSI
     *
     * @param context
     */
    public static void readRSSI(Context context) {
        Intent intent = new Intent(context, BLEService2.class);
        intent.putExtra(Params.COMMAND, Params.RSSI);
        context.startService(intent);
    }

    /**
     * ota升级
     *
     * @param context
     */
    public static void startOTA(Context context) {
        Intent intent = new Intent(context, BLEService2.class);
        intent.putExtra(Params.COMMAND, Params.OTA);
        context.startService(intent);
    }

    /**
     * 获取设备连接状态
     *
     * @return
     */
    public static BLEConnectModel getBLEConnectModel() {
        BLEConnectModel bleConnectModel = new BLEConnectModel();
        bleConnectModel.setBleState(BLEFramework.getBleFrameworkInstance().getConnectionState());
        return bleConnectModel;
    }

    public static void enterOta() {
        DataUtils.enterOta(BLEFramework.getBleFrameworkInstance(), Params.UUID_SERVICE, Params.UUID_SERVICE_WRITE);
    }

    /**
     * 验证UID
     * @param value
     */
    public static void verifyUID(String value) {
        byte[] bytes=new byte[11];
        for (int i = 0; i < bytes.length; i++) {
            if (value.getBytes().length > i) {
                bytes[i] = value.getBytes()[i];
            }
            else {
                bytes[i] = 0x00;
            }
        }
        DataUtils.sendData(BLEFramework.getBleFrameworkInstance(), Params.UUID_SERVICE_WristBand_Verify, Params.UUID_SERVICE_WristBand_UID, bytes);
    }

    /**
     * 验证Code
     * @param value
     */
    public static void verifyCode(String value) {
        DataUtils.sendData(BLEFramework.getBleFrameworkInstance(), Params.UUID_SERVICE_WristBand_Verify, Params.UUID_SERVICE_WristBand_Code, value.getBytes());
    }

    /**
     * 读取每小时的步数
     */
    public static void hourTotalStep() {
        DataUtils.sendData(BLEFramework.getBleFrameworkInstance(), Params.UUID_SERVICE_WristBand_Data, Params.UUID_SERVICE_WristBand_Command, (byte) 0x82);
    }
}
