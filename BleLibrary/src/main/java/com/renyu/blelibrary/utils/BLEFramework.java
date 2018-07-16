package com.renyu.blelibrary.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.impl.BLEConnectListener;
import com.renyu.blelibrary.impl.BLEOTAListener;
import com.renyu.blelibrary.impl.BLERSSIListener;
import com.renyu.blelibrary.impl.BLEReadResponseListener;
import com.renyu.blelibrary.impl.BLEScan21CallBack;
import com.renyu.blelibrary.impl.BLEScanCallBack;
import com.renyu.blelibrary.impl.BLEStateChangeListener;
import com.renyu.blelibrary.impl.BLEWriteResponseListener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by renyu on 2017/1/12.
 */

public class BLEFramework {

    private volatile static BLEFramework bleFramework;

    // 服务UUID
    private UUID UUID_SERVICE = null;
    private UUID UUID_Characteristic_WRITE = null;
    private UUID UUID_Characteristic_READ = null;
    private UUID UUID_DESCRIPTOR = null;

    // 设备连接断开
    public static final int STATE_DISCONNECTED = 0;
    // 设备正在扫描
    public static final int STATE_SCANNING = 1;
    // 设备扫描结束
    public static final int STATE_SCANNED = 2;
    // 设备正在连接
    private static final int STATE_CONNECTING = 3;
    // 设备连接成功
    private static final int STATE_CONNECTED = 4;
    // 设备配置服务成功
    public static final int STATE_SERVICES_DISCOVERED = 5;
    // 当前设备状态
    private int connectionState=STATE_DISCONNECTED;

    // 搜索到的设备
    private HashMap<String, BLEDevice> tempsDevices;
    // 搜索所需时间
    private int timeSeconds=10000;
    // 搜索Handler
    private Handler handlerScan;

    // 数据发送队列
    private static RequestQueue requestQueue;

    private Context context;
    private BluetoothAdapter adapter;
    private BLEScanCallBack leScanCallback;
    private BLEScan21CallBack bleScan21CallBack;
    private BluetoothGattCallback bleGattCallback;
    // 当前连接的gatt对象
    private BluetoothGatt gatt;
    // 当前连接的Characteristic
    private BluetoothGattCharacteristic currentCharacteristic;
    // 当前连接的设备
    private BluetoothDevice currentDevice;

    // BLE连接回调
    private BLEConnectListener bleConnectListener;
    // BLE当前状态
    private BLEStateChangeListener bleStateChangeListener;
    // BLE返回值回调
    private BLEWriteResponseListener bleWriteResponseListener;
    // OTA进度回调
    private BLEOTAListener bleotaListener;
    // BLE读命令回调
    private BLEReadResponseListener bleReadResponseListener;
    // RSSI回调
    private BLERSSIListener blerssiListener;

    // app默认重连3次
    // 发起重连次数
    private int retryCount=0;

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
                          UUID UUID_Characteristic_WRITE,
                          UUID UUID_Characteristic_READ,
                          UUID UUID_DESCRIPTOR) {
        this.context=context;
        this.UUID_SERVICE=UUID_SERVICE;
        this.UUID_Characteristic_WRITE=UUID_Characteristic_WRITE;
        this.UUID_Characteristic_READ=UUID_Characteristic_READ;
        this.UUID_DESCRIPTOR=UUID_DESCRIPTOR;
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
                        // 连接不上的时候进行重连
                        if (connectionState==STATE_CONNECTING && retryCount<3 && currentDevice!=null) {
                            Log.d("BLEFramework", "重连重试");
                            Observable.timer(1, TimeUnit.SECONDS)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<Long>() {
                                        @Override
                                        public void accept(Long aLong) throws Exception {
                                            startConn(currentDevice);
                                            retryCount++;
                                        }
                                    });
                        }
                        else {
                            gatt.close();
                            BLEFramework.this.gatt=null;
                            BLEFramework.this.currentCharacteristic=null;
                            BLEFramework.this.currentDevice=null;

                            setConnectionState(STATE_DISCONNECTED);
                            retryCount=0;
                        }
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                BLEFramework.this.gatt=gatt;
                if (status==BluetoothGatt.GATT_SUCCESS) {
                    if (gatt.getService(BLEFramework.this.UUID_SERVICE)!=null) {
                        BluetoothGattCharacteristic characteristic = gatt.getService(BLEFramework.this.UUID_SERVICE).getCharacteristic(BLEFramework.this.UUID_Characteristic_READ);
                        if (enableNotification(characteristic, gatt, BLEFramework.this.UUID_DESCRIPTOR)) {
                            // 连接通知服务完成
                            currentCharacteristic = characteristic;
                            setConnectionState(STATE_SERVICES_DISCOVERED);
                            retryCount=0;
                            return;
                        }
                    }
                }
                disconnect();
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                BLEFramework.this.gatt=gatt;
                if (status==BluetoothGatt.GATT_SUCCESS) {
                    requestQueue.release();
                }

                if (bleReadResponseListener!=null) {
                    bleReadResponseListener.getResponseValues(characteristic.getUuid(), characteristic.getValue());
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                BLEFramework.this.gatt=gatt;
                if (status==BluetoothGatt.GATT_SUCCESS) {
                    requestQueue.release();
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                BLEFramework.this.gatt=gatt;

                if (bleWriteResponseListener!=null) {
                    bleWriteResponseListener.getResponseValues(characteristic.getValue());
                }
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                BLEFramework.this.gatt=gatt;

                if (blerssiListener!=null) {
                    blerssiListener.getRssi(rssi);
                }
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
    public synchronized void disconnect() {
        if (gatt!=null)
            gatt.disconnect();
    }

    /**
     * 开始扫描
     * @return
     */
    public synchronized void startScan() {
        // BLE扫描回调
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            bleScan21CallBack=new BLEScan21CallBack() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (result !=null && result.getDevice()!=null && !tempsDevices.containsKey(result.getDevice().getAddress())) {
                        BLEDevice device1=new BLEDevice();
                        device1.setRssi(result.getRssi());
                        device1.setDevice(result.getDevice());
                        device1.setScanRecord(result.getScanRecord().getBytes());
                        tempsDevices.put(result.getDevice().getAddress(), device1);
                        bleConnectListener.getAllScanDevice(device1);
                    }
                }
            };
            adapter.getBluetoothLeScanner().startScan(bleScan21CallBack);
            // 开始搜索
            setConnectionState(STATE_SCANNING);
            handlerScan.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan(false);
                }
            }, timeSeconds);
        }
        else {
            leScanCallback=new BLEScanCallBack() {
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
            boolean success=adapter.startLeScan(leScanCallback);
            if (success) {
                // 开始搜索
                setConnectionState(STATE_SCANNING);
                handlerScan.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopScan(false);
                    }
                }, timeSeconds);
            }
            else {
                setConnectionState(STATE_DISCONNECTED);
            }
        }
    }

    /**
     * 扫描完成直接连接
     * @param deviceName
     */
    public synchronized void startScanAndConn(final String deviceName) {
        // BLE扫描回调
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            bleScan21CallBack=new BLEScan21CallBack() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (result !=null && result.getDevice()!=null && !TextUtils.isEmpty(result.getDevice().getName()) && result.getDevice().getName().equals(deviceName)) {
                        BLEDevice device1=new BLEDevice();
                        device1.setRssi(result.getRssi());
                        device1.setDevice(result.getDevice());
                        device1.setScanRecord(result.getScanRecord().getBytes());
                        bleConnectListener.getAllScanDevice(device1);
                        stopScan(false);
                        startConn(result.getDevice());
                    }
                }
            };
            adapter.getBluetoothLeScanner().startScan(bleScan21CallBack);
            // 开始搜索
            setConnectionState(STATE_SCANNING);
            handlerScan.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan(true);
                }
            }, timeSeconds);
        }
        else {
            leScanCallback=new BLEScanCallBack() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (device!=null && !TextUtils.isEmpty(device.getName()) && deviceName.equals(device.getName())) {
                        BLEDevice device1=new BLEDevice();
                        device1.setRssi(rssi);
                        device1.setDevice(device);
                        device1.setScanRecord(scanRecord);
                        bleConnectListener.getAllScanDevice(device1);
                        stopScan(false);
                        startConn(device);
                    }
                }
            };
            boolean success=adapter.startLeScan(leScanCallback);
            if (success) {
                // 开始搜索
                setConnectionState(STATE_SCANNING);
                handlerScan.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopScan(true);
                    }
                }, timeSeconds);
            }
            else {
                setConnectionState(STATE_DISCONNECTED);
            }
        }
    }

    /**
     * 结束扫描
     */
    public synchronized void stopScan(boolean scanConnFail) {
        handlerScan.removeCallbacksAndMessages(null);
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            if (bleScan21CallBack!=null && adapter.isEnabled() && adapter.getBluetoothLeScanner()!=null) {
                adapter.getBluetoothLeScanner().stopScan(bleScan21CallBack);
            }
        }
        else {
            if (leScanCallback!=null && adapter!=null) {
                adapter.stopLeScan(leScanCallback);
            }
        }
        if (scanConnFail) {
            // 扫描连接失败
            setConnectionState(STATE_DISCONNECTED);
        }
        else {
            // 搜索完毕
            setConnectionState(STATE_SCANNED);
        }
        tempsDevices.clear();
    }

    /**
     * 开始连接
     * @param device
     */
    public synchronized void startConn(BluetoothDevice device) {
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
     * 获取设备当前状态
     * @return
     */
    public int getConnectionState() {
        return connectionState;
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
     * 发送写命令队列
     * @param sendValue
     */
    public void addWriteCommand(byte[] sendValue) {
        requestQueue.addWriteCommand(sendValue);
    }

    /**
     * 发送读命令队列
     * @param serviceUUID
     * @param CharacUUID
     */
    public void addReadCommand(final UUID serviceUUID, final UUID CharacUUID) {
        requestQueue.addReadCommand(serviceUUID, CharacUUID);
    }

    /**
     * 读取RSSI
     */
    public void readRSSI() {
        gatt.readRemoteRssi();
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
            if (progress==-1) {
                Toast.makeText(context, "升级失败", Toast.LENGTH_SHORT).show();
            }
            else if (progress==101) {
                Toast.makeText(context, "升级成功", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context, "升级完成"+progress+"%", Toast.LENGTH_SHORT).show();
            }
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
     * @param bleWriteResponseListener
     */
    public void setBLEWriteResponseListener(BLEWriteResponseListener bleWriteResponseListener) {
        this.bleWriteResponseListener = bleWriteResponseListener;
    }

    /**
     * 设置OTA进度回调
     * @param bleotaListener
     */
    public void setBleotaListener(BLEOTAListener bleotaListener) {
        this.bleotaListener = bleotaListener;
    }

    /**
     * 设置BLE读命令回调
     * @param bleReadResponseListener
     */
    public void setBleReadResponseListener(BLEReadResponseListener bleReadResponseListener) {
        this.bleReadResponseListener = bleReadResponseListener;
    }

    public void setBlerssiListener(BLERSSIListener blerssiListener) {
        this.blerssiListener = blerssiListener;
    }

    /**
     * 设置扫描时间
     * @param timeSeconds
     */
    public void setTimeSeconds(int timeSeconds) {
        this.timeSeconds=timeSeconds;
    }

    public synchronized boolean refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && gatt != null) {
                final boolean success = (Boolean) refresh.invoke(gatt);
                return success;
            }
        } catch (Exception e) {

        }
        return false;
    }
}
