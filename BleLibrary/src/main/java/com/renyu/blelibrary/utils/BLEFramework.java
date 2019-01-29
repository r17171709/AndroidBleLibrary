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
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.cypress.cysmart.BLEConnectionServices.BluetoothLeService;
import com.cypress.cysmart.CommonUtils.Constants;
import com.cypress.cysmart.CommonUtils.Utils;
import com.cypress.cysmart.DataModelClasses.OTAParams;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.impl.BLEOTAListener;
import com.renyu.blelibrary.impl.BLERSSIListener;
import com.renyu.blelibrary.impl.BLEReadResponseListener;
import com.renyu.blelibrary.impl.BLEScan21CallBack;
import com.renyu.blelibrary.impl.BLEScanCallBack;
import com.renyu.blelibrary.impl.BLEScanCallBackListener;
import com.renyu.blelibrary.impl.BLEStateChangeListener;
import com.renyu.blelibrary.impl.BLEWriteErrorListener;
import com.renyu.blelibrary.impl.BLEWriteResponseListener;
import com.renyu.blelibrary.impl.IScanAndConnRule;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * Created by renyu on 2017/1/12.
 */

public class BLEFramework {

    private volatile static BLEFramework bleFramework;

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
    // 设备正在执行OTA
    public static final int STATE_SERVICES_OTA_DISCOVERED = 6;
    // 当前设备状态
    private int connectionState = STATE_DISCONNECTED;

    // 搜索到的设备
    private HashMap<String, BLEDevice> tempsDevices;
    // 搜索所需时间
    private int timeSeconds = 3000;
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
    // 当前连接的设备
    private BluetoothDevice currentDevice;

    // BLE连接回调
    private BLEScanCallBackListener bleScanCallBackListener;
    // BLE当前状态
    private BLEStateChangeListener bleStateChangeListener;
    // BLE返回值回调
    private BLEWriteResponseListener bleWriteResponseListener;
    // BLE读命令回调
    private BLEReadResponseListener bleReadResponseListener;
    // RSSI回调
    private BLERSSIListener blerssiListener;
    // OTA进度回调
    private BLEOTAListener bleotaListener;
    // BLE指令无返回回调
    private BLEWriteErrorListener bleWriteErrorListener;

    // OTA标记
    public static boolean isOTA = true;
    // OTA服务所需Characteristic
    private BluetoothGattCharacteristic mOTACharacteristic;
    private static boolean mOtaExitBootloaderCmdInProgress = false;
    private static Semaphore writeSemaphore = new Semaphore(1);

    public static BLEFramework getBleFrameworkInstance() {
        if (bleFramework == null) {
            synchronized (BLEFramework.class) {
                if (bleFramework == null) {
                    // 初始化BLE基本参数
                    bleFramework = new BLEFramework();
                }
            }
        }
        return bleFramework;
    }

    private BLEFramework() {

    }

    /**
     * 设置Context
     *
     * @param context
     */
    public void setParams(Context context) {
        this.context = context;
    }

    public void initBLE() {
        if (context == null) {
            throw new RuntimeException("必须要先调用setParams方法");
        }

        // 初始化队列
        requestQueue = RequestQueue.getQueueInstance(bleFramework, bleWriteErrorListener);

        // 临时存储BLE全部设备
        tempsDevices = new HashMap<>();

        // 初始化BLE基本参数
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        handlerScan = new Handler(Looper.getMainLooper());

        // GATT回调
        bleGattCallback = new BluetoothGattCallback() {
            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
                // 开始搜索服务
                gatt.discoverServices();
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                switch (newState) {
                    // BLE连接完成
                    case BluetoothProfile.STATE_CONNECTED:
                        setConnectionState(STATE_CONNECTED);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            gatt.requestMtu(192);
                        }
                        break;
                    // BLE断开连接
                    case BluetoothProfile.STATE_DISCONNECTED:
                        close();
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                BLEFramework.this.gatt = gatt;
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (checkIsOTA()) {
                        if (gatt.getService(OTAParams.UUID_SERVICE_OTASERVICE) != null) {
                            BluetoothGattCharacteristic characteristic = gatt.getService(OTAParams.UUID_SERVICE_OTASERVICE).getCharacteristic(OTAParams.UUID_SERVICE_OTA);
                            if (enableNotification(characteristic, gatt, OTAParams.UUID_DESCRIPTOR_OTA)) {
                                mOTACharacteristic = characteristic;
                                setConnectionState(STATE_SERVICES_OTA_DISCOVERED);
                                return;
                            }
                        }
                    } else {
                        for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
                            System.out.println("bluetoothGattService:  " + bluetoothGattService.getUuid().toString());
                            for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()) {
                                System.out.println("bluetoothGattCharacteristic:  " + bluetoothGattCharacteristic.getUuid().toString());
                                for (BluetoothGattDescriptor bluetoothGattDescriptor : bluetoothGattCharacteristic.getDescriptors()) {
                                    System.out.println("bluetoothGattDescriptor:  " + bluetoothGattDescriptor.getUuid().toString());
                                    enableNotification(bluetoothGattCharacteristic, gatt, bluetoothGattDescriptor.getUuid());
                                }
                            }
                            System.out.println("\n");
                        }
                        // 连接通知服务完成
                        setConnectionState(STATE_SERVICES_DISCOVERED);
                        return;
                    }
                }
                disconnect();
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    requestQueue.release();
                }

                if (bleReadResponseListener != null) {
                    bleReadResponseListener.getResponseValues(characteristic.getUuid(), characteristic.getValue());
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    requestQueue.release();
                    if (!checkIsOTA()) {
                        return;
                    }
                }

                boolean isExitBootloaderCmd;
                synchronized (BLEFramework.class) {
                    isExitBootloaderCmd = mOtaExitBootloaderCmdInProgress;
                    if (mOtaExitBootloaderCmdInProgress) {
                        mOtaExitBootloaderCmdInProgress = false;
                    }
                }
                if (isExitBootloaderCmd) {
                    onOtaExitBootloaderComplete(status);
                }
                if (characteristic.getUuid().toString().equalsIgnoreCase(OTAParams.UUID_SERVICE_OTA.toString())) {
                    writeSemaphore.release();
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);

                //ota的指令
                if (OTAParams.UUID_SERVICE_OTA.toString().equals(characteristic.getUuid().toString())) {
                    broadcastNotifyUpdate(characteristic);
                } else {
                    if (bleWriteResponseListener != null) {
                        bleWriteResponseListener.getResponseValues(characteristic.getValue());
                    }
                }
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                if (blerssiListener != null) {
                    blerssiListener.getRssi(rssi);
                }
            }
        };
    }

    private boolean enableNotification(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt, UUID uuid) {
        boolean success = gatt.setCharacteristicNotification(characteristic, true);
        if (!success) {
            return false;
        }
        if (characteristic.getDescriptors().size() > 0) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
            if (descriptor != null) {
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
        if (gatt != null)
            gatt.disconnect();
    }

    /**
     * 开始扫描
     *
     * @return
     */
    public synchronized void startScan() {
        // BLE扫描回调
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            bleScan21CallBack = new BLEScan21CallBack() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (result != null && result.getDevice() != null && !tempsDevices.containsKey(result.getDevice().getAddress())) {
                        BLEDevice device1 = new BLEDevice();
                        device1.setRssi(result.getRssi());
                        device1.setDevice(result.getDevice());
                        device1.setScanRecord(result.getScanRecord().getBytes());
                        tempsDevices.put(result.getDevice().getAddress(), device1);
                        bleScanCallBackListener.getAllScanDevice(device1);
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
        } else {
            leScanCallback = new BLEScanCallBack() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (device != null && !tempsDevices.containsKey(device.getAddress())) {
                        BLEDevice device1 = new BLEDevice();
                        device1.setRssi(rssi);
                        device1.setDevice(device);
                        device1.setScanRecord(scanRecord);
                        tempsDevices.put(device.getAddress(), device1);
                        bleScanCallBackListener.getAllScanDevice(device1);
                    }
                }
            };
            boolean success = adapter.startLeScan(leScanCallback);
            if (success) {
                // 开始搜索
                setConnectionState(STATE_SCANNING);
                handlerScan.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopScan(false);
                    }
                }, timeSeconds);
            } else {
                setConnectionState(STATE_DISCONNECTED);
            }
        }
    }

    /**
     * 扫描完成直接连接
     *
     * @param iScanAndConnRule
     */
    public synchronized void startScanAndConn(final IScanAndConnRule iScanAndConnRule) {
        // BLE扫描回调
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            bleScan21CallBack = new BLEScan21CallBack() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (result != null && result.getDevice() != null && iScanAndConnRule.rule21(result)) {
                        // 已经发生了连接
                        if (connectionState <= STATE_SCANNED) {
                            BLEDevice device1 = new BLEDevice();
                            device1.setRssi(result.getRssi());
                            device1.setDevice(result.getDevice());
                            device1.setScanRecord(result.getScanRecord().getBytes());
                            bleScanCallBackListener.getAllScanDevice(device1);
                            stopScan(false);
                            startConn(result.getDevice());
                        }
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
        } else {
            leScanCallback = new BLEScanCallBack() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (device != null && !TextUtils.isEmpty(device.getName()) && iScanAndConnRule.rule(device)) {
                        // 已经发生了连接
                        if (connectionState <= STATE_SCANNED) {
                            BLEDevice device1 = new BLEDevice();
                            device1.setRssi(rssi);
                            device1.setDevice(device);
                            device1.setScanRecord(scanRecord);
                            bleScanCallBackListener.getAllScanDevice(device1);
                            stopScan(false);
                            startConn(device);
                        }
                    }
                }
            };
            boolean success = adapter.startLeScan(leScanCallback);
            if (success) {
                // 开始搜索
                setConnectionState(STATE_SCANNING);
                handlerScan.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopScan(true);
                    }
                }, timeSeconds);
            } else {
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
            if (bleScan21CallBack != null && adapter.isEnabled() && adapter.getBluetoothLeScanner() != null) {
                adapter.getBluetoothLeScanner().stopScan(bleScan21CallBack);
            }
        } else {
            if (leScanCallback != null && adapter != null) {
                adapter.stopLeScan(leScanCallback);
            }
        }
        if (scanConnFail) {
            // 扫描连接失败
            setConnectionState(STATE_DISCONNECTED);
        } else {
            // 搜索完毕
            setConnectionState(STATE_SCANNED);
        }
        tempsDevices.clear();
    }

    /**
     * 开始连接
     *
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
     *
     * @param state
     */
    private void setConnectionState(int state) {
        connectionState = state;
        if (bleStateChangeListener != null) {
            bleStateChangeListener.getCurrentState(state);
        }
    }

    /**
     * 获取设备当前状态
     *
     * @return
     */
    public int getConnectionState() {
        return connectionState;
    }

    /**
     * 发送数据
     *
     * @param serviceUUID
     * @param characUUID
     * @param value
     */
    void writeCharacteristic(UUID serviceUUID, UUID characUUID, byte[] value) {
        if (gatt != null) {
            BluetoothGattCharacteristic characteristic = gatt.getService(serviceUUID).getCharacteristic(characUUID);
            if (characteristic == null) {
                Log.d("BLEFramework", "writeCharacteristic中uuid不存在");
                return;
            }
            characteristic.setValue(value);
            if (!gatt.writeCharacteristic(characteristic)) {
                Log.d("BLEFramework", "writeCharacteristic失败");
            } else {
                Log.d("BLEFramework", "writeCharacteristic成功");
            }
        }
    }

    /**
     * 主动读数据
     *
     * @param serviceUUID
     * @param CharacUUID
     */
    void readCharacteristic(UUID serviceUUID, UUID CharacUUID) {
        if (gatt != null) {
            BluetoothGattCharacteristic characteristic = gatt.getService(serviceUUID).getCharacteristic(CharacUUID);
            if (characteristic == null) {
                Log.d("BLEFramework", "readCharacteristic中uuid不存在");
                return;
            }
            if (!gatt.readCharacteristic(characteristic)) {
                Log.d("BLEFramework", "readCharacteristic失败");
            } else {
                Log.d("BLEFramework", "readCharacteristic成功");
            }
        }
    }

    /**
     * 发送写命令队列
     *
     * @param sendValue
     */
    public void addWriteCommand(UUID serviceUUID, UUID characUUID, byte[] sendValue) {
        requestQueue.addWriteCommand(serviceUUID, characUUID, sendValue);
    }

    /**
     * 发送读命令队列
     *
     * @param serviceUUID
     * @param CharacUUID
     */
    public void addReadCommand(UUID serviceUUID, UUID CharacUUID) {
        requestQueue.addReadCommand(serviceUUID, CharacUUID);
    }

    /**
     * 读取RSSI
     */
    public void readRSSI() {
        if (gatt != null) {
            gatt.readRemoteRssi();
        }
    }

    /**
     * 获取当前连接的gatt对象
     *
     * @return
     */
    public BluetoothGatt getCurrentGatt() {
        if (gatt == null) throw new RuntimeException("BLE服务没有启动");
        return gatt;
    }

    /**
     * 获取当前连接的设备
     *
     * @return
     */
    public BluetoothDevice getCurrentBluetoothDevice() {
        if (currentDevice == null) throw new RuntimeException("BLE服务没有启动");
        return currentDevice;
    }

    /**
     * 设置BLE连接回调
     *
     * @param bleScanCallBackListener
     */
    public void setBleScanCallbackListener(BLEScanCallBackListener bleScanCallBackListener) {
        this.bleScanCallBackListener = bleScanCallBackListener;
    }

    /**
     * 设置BLE状态回调
     *
     * @param bleStateChangeListener
     */
    public void setBleStateChangeListener(BLEStateChangeListener bleStateChangeListener) {
        this.bleStateChangeListener = bleStateChangeListener;
    }

    /**
     * 设置BLE返回值回调
     *
     * @param bleWriteResponseListener
     */
    public void setBLEWriteResponseListener(BLEWriteResponseListener bleWriteResponseListener) {
        this.bleWriteResponseListener = bleWriteResponseListener;
    }

    /**
     * 设置OTA进度回调
     *
     * @param bleotaListener
     */
    public void setBleotaListener(BLEOTAListener bleotaListener) {
        this.bleotaListener = bleotaListener;
    }

    /**
     * 设置BLE读命令回调
     *
     * @param bleReadResponseListener
     */
    public void setBleReadResponseListener(BLEReadResponseListener bleReadResponseListener) {
        this.bleReadResponseListener = bleReadResponseListener;
    }

    public void setBlerssiListener(BLERSSIListener blerssiListener) {
        this.blerssiListener = blerssiListener;
    }

    /**
     * BLE指令无返回回调
     *
     * @param bleWriteErrorListener
     */
    public void setBleWriteErrorListener(BLEWriteErrorListener bleWriteErrorListener) {
        this.bleWriteErrorListener = bleWriteErrorListener;
    }

    /**
     * 设置扫描时间
     *
     * @param timeSeconds
     */
    public void setTimeSeconds(int timeSeconds) {
        this.timeSeconds = timeSeconds;
    }

    public synchronized boolean refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (gatt != null) {
                return (boolean) (Boolean) refresh.invoke(gatt);
            }
        } catch (Exception e) {

        }
        return false;
    }

    public void close() {
        if (gatt != null) {
            gatt.close();
        }
        gatt = null;
        currentDevice = null;

        setConnectionState(STATE_DISCONNECTED);
    }

    /**
     * 检查是否进入OTA模式
     */
    private boolean checkIsOTA() {
//        List<BluetoothGattService> gattServices=gatt.getServices();
//        for (BluetoothGattService gattService : gattServices) {
//            Log.d("BLEService", gattService.getUuid().toString());
//            if (gattService.getUuid().toString().equals(CommonParams.UUID_SERVICE_OTASERVICE.toString())) {
//                return true;
//            }
//        }
        return isOTA;
    }

    /**
     * 获取当前OTA连接的Characteristic
     * @return
     */
    public BluetoothGattCharacteristic getOTACharacteristic() {
        return mOTACharacteristic;
    }

    private void onOtaExitBootloaderComplete(int status) {
        Bundle bundle = new Bundle();
        bundle.putByteArray(Constants.EXTRA_BYTE_VALUE, new byte[]{(byte) status});
        Intent intentOTA = new Intent(BluetoothLeService.ACTION_OTA_DATA_AVAILABLE);
        intentOTA.putExtras(bundle);
        context.sendBroadcast(intentOTA);
    }

    private void broadcastNotifyUpdate(final BluetoothGattCharacteristic characteristic) {
        Bundle bundle = new Bundle();
        // Putting the byte value read for GATT Db
        bundle.putByteArray(Constants.EXTRA_BYTE_VALUE,
                characteristic.getValue());
        bundle.putString(Constants.EXTRA_BYTE_UUID_VALUE,
                characteristic.getUuid().toString());
        bundle.putInt(Constants.EXTRA_BYTE_INSTANCE_VALUE,
                characteristic.getInstanceId());
        bundle.putString(Constants.EXTRA_BYTE_SERVICE_UUID_VALUE,
                characteristic.getService().getUuid().toString());
        bundle.putInt(Constants.EXTRA_BYTE_SERVICE_INSTANCE_VALUE,
                characteristic.getService().getInstanceId());
        if (characteristic.getUuid().equals(OTAParams.UUID_SERVICE_OTA)) {
            boolean isCyacd2File = Utils.getBooleanSharedPreference(context, Constants.PREF_IS_CYACD2_FILE);
            String intentAction = isCyacd2File
                    ? BluetoothLeService.ACTION_OTA_DATA_AVAILABLE_V1
                    : BluetoothLeService.ACTION_OTA_DATA_AVAILABLE;
            Intent intentOTA = new Intent(intentAction);
            intentOTA.putExtras(bundle);
            context.sendBroadcast(intentOTA);
        }
    }

    public static BluetoothDevice getRemoteDevice() {
        return BLEFramework.getBleFrameworkInstance().getCurrentBluetoothDevice();
    }

    public static void writeOTABootLoaderCommand(BluetoothGattCharacteristic characteristic, byte[] value, boolean isExitBootloaderCmd) {
        synchronized (BLEFramework.class) {
            writeOTABootLoaderCommand(characteristic, value);
            if (isExitBootloaderCmd) {
                mOtaExitBootloaderCmdInProgress = true;
            }
        }
    }

    public static void writeOTABootLoaderCommand(BluetoothGattCharacteristic characteristic, byte[] value) {
        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
            writeOTABootLoaderCommandNoResponse(characteristic, value);
        } else {
            writeOTABootLoaderCommandWithResponse(characteristic, value);
        }
    }

    private static void writeOTABootLoaderCommandWithResponse(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (BLEFramework.getBleFrameworkInstance().getCurrentGatt() == null) {
            return;
        }
        characteristic.setValue(value);
        int counter = 20;
        boolean status;
        do {
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            status = BLEFramework.getBleFrameworkInstance().getCurrentGatt().writeCharacteristic(characteristic);
            if (!status) {
                try {
                    Thread.sleep(100, 0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while ((!status) && (counter-- > 0));
    }


    private static void writeOTABootLoaderCommandNoResponse(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (BLEFramework.getBleFrameworkInstance().getCurrentGatt() == null) {
            return;
        }

        final int mtuValue = 20;

        int totalLength = value.length;
        int localLength = 0;
        byte[] localValue = new byte[mtuValue];

        do {
            try {
                writeSemaphore.acquire();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            if (totalLength >= mtuValue) {
                for (int i = 0; i < mtuValue; i++) {
                    localValue[i] = value[localLength + i];
                }
                characteristic.setValue(localValue);
                totalLength -= mtuValue;
                localLength += mtuValue;
            } else {
                byte[] lastValue = new byte[totalLength];
                for (int i = 0; i < totalLength; i++) {
                    lastValue[i] = value[localLength + i];
                }
                characteristic.setValue(lastValue);
                totalLength = 0;
            }

            int counter = 20;
            boolean status;

            do {
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                status = BLEFramework.getBleFrameworkInstance().getCurrentGatt().writeCharacteristic(characteristic);
                if (!status) {
                    try {
                        Thread.sleep(100, 0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while ((!status) && (counter-- > 0));

            if (!status) {
                writeSemaphore.release();
            }
        } while (totalLength > 0);
    }
}
