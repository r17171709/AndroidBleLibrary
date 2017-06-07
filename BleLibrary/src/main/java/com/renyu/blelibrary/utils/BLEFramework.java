package com.renyu.blelibrary.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.cypress.cysmart.CommonUtils.Constants;
import com.cypress.cysmart.OTAFirmwareUpdate.OTAService;
import com.cypress.cysmart.Params.CommonParams;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.impl.BLEConnectListener;
import com.renyu.blelibrary.impl.BLEOTAListener;
import com.renyu.blelibrary.impl.BLEResponseListener;
import com.renyu.blelibrary.impl.BLEStateChangeListener;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by renyu on 2017/1/12.
 */

public class BLEFramework {

    private volatile static BLEFramework bleFramework;

    // 服务UUID
    private UUID UUID_SERVICE = null;
    private UUID UUID_OTASERVICE = null;
    private UUID UUID_Characteristic_WRITE = null;
    private UUID UUID_Characteristic_READ = null;
    private UUID UUID_OTACharacteristic = null;
    private UUID UUID_DESCRIPTOR = null;
    private UUID UUID_OTADESCRIPTOR = null;

    // 设备连接断开
    public static final int STATE_DISCONNECTED = 0;
    // 设备正在扫描
    public static final int STATE_SCANNING = 1;
    // 设备扫描结束
    public static final int STATE_SCANNED = 2;
    // 设备正在连接
    public static final int STATE_CONNECTING = 3;
    // 设备连接成功
    public static final int STATE_CONNECTED = 4;
    // 设备配置服务成功
    public static final int STATE_SERVICES_DISCOVERED = 5;
    // 设备配置OTA服务成功
    public static final int STATE_SERVICES_OTA_DISCOVERED = 6;
    // 当前设备状态
    private int connectionState=STATE_DISCONNECTED;

    // 搜索到的设备
    private HashMap<String, BLEDevice> tempsDevices;
    // 搜索所需时间
    private int timeSeconds=5000;
    // 搜索Handler
    private Handler handlerScan;

    // 数据发送队列
    private static RequestQueue requestQueue;

    private Context context;
    private BluetoothAdapter adapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private BluetoothGattCallback bleGattCallback;
    // 当前连接的gatt对象
    private BluetoothGatt gatt;
    // 当前连接的Characteristic
    private BluetoothGattCharacteristic currentCharacteristic;
    // 当前连接的设备
    private BluetoothDevice currentDevice;

    // OTA升级完成
    private boolean endOTAFlag = false;

    // BLE连接回调
    private BLEConnectListener bleConnectListener;
    // BLE当前状态
    private BLEStateChangeListener bleStateChangeListener;
    // BLE返回值回调
    private BLEResponseListener bleResponseListener;
    // OTA进度回调
    private BLEOTAListener bleotaListener;

    public static BLEFramework getBleFrameworkInstance() {
        if (bleFramework==null) {
            synchronized (BLEFramework.class) {
                if (bleFramework==null) {
                    // 初始化BLE基本参数
                    bleFramework=new BLEFramework();
                }
            }
        }
        return bleFramework;
    }

    public void setParams(Context context,
                          UUID UUID_SERVICE,
                          UUID UUID_OTASERVICE,
                          UUID UUID_Characteristic_WRITE,
                          UUID UUID_Characteristic_READ,
                          UUID UUID_OTACharacteristic,
                          UUID UUID_DESCRIPTOR,
                          UUID UUID_OTADESCRIPTOR) {
        this.context=context;
        this.UUID_SERVICE=UUID_SERVICE;
        this.UUID_OTASERVICE=UUID_OTASERVICE;
        this.UUID_Characteristic_WRITE=UUID_Characteristic_WRITE;
        this.UUID_Characteristic_READ=UUID_Characteristic_READ;
        this.UUID_OTACharacteristic=UUID_OTACharacteristic;
        this.UUID_DESCRIPTOR=UUID_DESCRIPTOR;
        this.UUID_OTADESCRIPTOR=UUID_OTADESCRIPTOR;
    }

    public void initBLE() {
        if (context == null) {
            throw new RuntimeException("必须要先调用setParams方法");
        }

        // 初始化队列
        requestQueue=RequestQueue.getQueueInstance(bleFramework);

        // 临时存储BLE全部设备
        tempsDevices=new HashMap<>();

        // 初始化BLE基本参数
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter= manager.getAdapter();
        handlerScan=new Handler(Looper.getMainLooper());
        // BLE扫描回调
        leScanCallback=new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (device!=null && !tempsDevices.containsKey(device.getAddress())) {
                    BLEDevice device1=new BLEDevice();
                    device1.setRssi(rssi);
                    device1.setDevice(device);
                    device1.setScanRecord(scanRecord);
                    tempsDevices.put(device.getAddress(), device1);
                    bleConnectListener.getAllScanDevice(device1);
                }
            }
        };
        // GATT回调
        bleGattCallback=new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                BLEFramework.this.gatt=gatt;
                switch (newState) {
                    // BLE连接完成
                    case BluetoothProfile.STATE_CONNECTED:
                        setConnectionState(STATE_CONNECTED);
                        // 开始搜索服务
                        gatt.discoverServices();
                        break;
                    // BLE断开连接
                    case BluetoothProfile.STATE_DISCONNECTED:
                        gatt.close();
                        BLEFramework.this.gatt=null;
                        BLEFramework.this.currentCharacteristic=null;
                        BLEFramework.this.currentDevice=null;
                        setConnectionState(STATE_DISCONNECTED);
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                BLEFramework.this.gatt=gatt;
                if (status==BluetoothGatt.GATT_SUCCESS) {
                    if (checkIsOTA()) {
                        if (gatt.getService(BLEFramework.this.UUID_OTASERVICE)!=null) {
                            BluetoothGattCharacteristic characteristic = gatt.getService(BLEFramework.this.UUID_OTASERVICE).getCharacteristic(BLEFramework.this.UUID_OTACharacteristic);
                            if (enableNotification(characteristic, gatt, BLEFramework.this.UUID_OTADESCRIPTOR)) {
                                currentCharacteristic = characteristic;
                                setConnectionState(STATE_SERVICES_OTA_DISCOVERED);
                                return;
                            }
                        }
                    }
                    else {
                        if (gatt.getService(BLEFramework.this.UUID_SERVICE)!=null) {
                            BluetoothGattCharacteristic characteristic = gatt.getService(BLEFramework.this.UUID_SERVICE).getCharacteristic(BLEFramework.this.UUID_Characteristic_READ);
                            if (enableNotification(characteristic, gatt, BLEFramework.this.UUID_DESCRIPTOR)) {
                                // 连接通知服务完成
                                currentCharacteristic = characteristic;
                                setConnectionState(STATE_SERVICES_DISCOVERED);
                                return;
                            }
                        }
                    }
                }
                disconnect();
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                BLEFramework.this.gatt=gatt;
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                BLEFramework.this.gatt=gatt;
                if (status==BluetoothGatt.GATT_SUCCESS) {
                    requestQueue.release();
                }

                if (!checkIsOTA()) {
                    return;
                }
                synchronized (BLEFramework.class) {
                    if(endOTAFlag) {
                        endOTAFlag=false;

                        Intent intent=new Intent(context, OTAService.class);
                        intent.putExtra("command", 2);
                        intent.putExtra("status", status);
                        context.startService(intent);
                    }
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                BLEFramework.this.gatt=gatt;

                //ota的指令
                if (checkIsOTA()) {
                    Intent intentOTA = new Intent(CommonParams.ACTION_OTA_DATA_AVAILABLE);
                    Bundle mBundle = new Bundle();
                    mBundle.putByteArray(Constants.EXTRA_BYTE_VALUE, characteristic.getValue());
                    mBundle.putString(Constants.EXTRA_BYTE_UUID_VALUE, characteristic.getUuid().toString());
                    intentOTA.putExtras(mBundle);
                    BLEFramework.this.context.sendBroadcast(intentOTA);
                }
                else {
                    if (bleResponseListener!=null) {
                        bleResponseListener.getResponseValues(characteristic.getValue());
                    }
                }
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                BLEFramework.this.gatt=gatt;
            }
        };
    }

    private boolean enableNotification(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt, UUID uuid) {
        boolean success = gatt.setCharacteristicNotification(characteristic, true);
        if(!success) {
            return false;
        }
        if (characteristic.getDescriptors().size()>0) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
            if(descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
                return true;
            }
        }
        return false;
    }

    /**
     * 断开BLE连接
     */
    public void disconnect() {
        if (gatt!=null)
            gatt.disconnect();
    }

    /**
     * 开始扫描
     * @return
     */
    public void startScan() {
        boolean success=adapter.startLeScan(leScanCallback);
        if (success) {
            // 开始搜索
            setConnectionState(STATE_SCANNING);
            handlerScan.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }
            }, timeSeconds);
        }
        else {
            setConnectionState(STATE_DISCONNECTED);
        }
    }

    /**
     * 结束扫描
     */
    public void stopScan() {
        handlerScan.removeCallbacksAndMessages(null);
        adapter.stopLeScan(leScanCallback);
        // 搜索完毕
        setConnectionState(STATE_SCANNED);
        tempsDevices.clear();
    }

    /**
     * 开始连接
     * @param device
     */
    public void startConn(BluetoothDevice device) {
        currentDevice = device;
        // 开始连接
        setConnectionState(STATE_CONNECTING);
        device.connectGatt(context, false, bleGattCallback);
    }

    /**
     * 设置当前状态
     * @param state
     */
    private void setConnectionState(int state) {
        connectionState=state;
        if (bleStateChangeListener!=null) {
            bleStateChangeListener.getCurrentState(state);
        }
    }

    /**
     * 发送数据
     * @param value
     */
    protected void writeCharacteristic(byte[] value) {
        writeCharacteristic(UUID_Characteristic_WRITE, value);
    }

    /**
     * 发送数据
     * @param uuid
     * @param value
     */
    protected void writeCharacteristic(UUID uuid, byte[] value) {
        if (gatt!=null) {
            BluetoothGattCharacteristic characteristic = gatt.getService(UUID_SERVICE).getCharacteristic(uuid);
            if (characteristic==null) {
                Log.d("BLEFramework", "writeCharacteristic中uuid不存在");
                return;
            }
            characteristic.setValue(value);
            if (!gatt.writeCharacteristic(characteristic)) {
                Log.d("BLEFramework", "writeCharacteristic失败");
            }
            else {
                Log.d("BLEFramework", "writeCharacteristic成功");
            }
        }
    }

    /**
     * 主动读数据
     * @param serviceUUID
     * @param CharacUUID
     */
    protected void readCharacteristic(UUID serviceUUID, UUID CharacUUID) {
        if (gatt!=null) {
            BluetoothGattCharacteristic characteristic = gatt.getService(serviceUUID).getCharacteristic(CharacUUID);
            if (characteristic==null) {
                Log.d("BLEFramework", "readCharacteristic中uuid不存在");
                return;
            }
            if (!gatt.readCharacteristic(characteristic)) {
                Log.d("BLEFramework", "readCharacteristic失败");
            }
            else {
                Log.d("BLEFramework", "readCharacteristic成功");
            }
        }
    }

    /**
     * 发送指令
     * @param sendValue
     */
    public void addCommand(byte[] sendValue) {
        requestQueue.add(sendValue);
    }

    /**
     * 检查是否进入OTA模式
     */
    public boolean checkIsOTA() {
        List<BluetoothGattService> gattServices=gatt.getServices();
        for (BluetoothGattService gattService : gattServices) {
            if (gattService.getUuid().toString().equals(BLEFramework.this.UUID_OTASERVICE.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 开启OTA升级
     */
    public void startOTA() {
        Intent intent=new Intent(context, OTAService.class);
        intent.putExtra("command", 1);
        context.startService(intent);
    }

    /**
     * OTA升级结束
     */
    public void endOTA() {
        endOTAFlag=true;
    }

    /**
     * 获取当前连接的gatt对象
     * @return
     */
    public BluetoothGatt getCurrentGatt() {
        if (gatt==null) throw new RuntimeException("BLE服务没有启动");
        return gatt;
    }

    /**
     * 获取当前连接的Characteristic
     * @return
     */
    public BluetoothGattCharacteristic getCurrentCharacteristic() {
        if (currentCharacteristic==null) throw new RuntimeException("BLE服务没有启动");
        return currentCharacteristic;
    }

    /**
     * 获取当前连接的设备
     * @return
     */
    public BluetoothDevice getCurrentBluetoothDevice() {
        if (currentDevice==null) throw new RuntimeException("BLE服务没有启动");
        return currentDevice;
    }

    public void updateOTAProgress(int progress) {
        if (bleotaListener!=null) {
            bleotaListener.showProgress(progress);
        }
    }

    /**
     * 设置BLE连接回调
     * @param bleConnectListener
     */
    public void setBleConnectListener(BLEConnectListener bleConnectListener) {
        this.bleConnectListener = bleConnectListener;
    }

    /**
     * 设置BLE状态回调
     * @param bleStateChangeListener
     */
    public void setBleStateChangeListener(BLEStateChangeListener bleStateChangeListener) {
        this.bleStateChangeListener = bleStateChangeListener;
    }

    /**
     * 设置BLE返回值回调
     * @param bleResponseListener
     */
    public void setBleResponseListener(BLEResponseListener bleResponseListener) {
        this.bleResponseListener = bleResponseListener;
    }

    /**
     * 设置OTA进度回调
     * @param bleotaListener
     */
    public void setBleotaListener(BLEOTAListener bleotaListener) {
        this.bleotaListener = bleotaListener;
    }

    /**
     * 设置扫描时间
     * @param timeSeconds
     */
    public void setTimeSeconds(int timeSeconds) {
        this.timeSeconds=timeSeconds;
    }
}
