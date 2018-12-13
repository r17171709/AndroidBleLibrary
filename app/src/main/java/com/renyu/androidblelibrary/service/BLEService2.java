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

import com.renyu.androidblelibrary.bean.BLEConnectModel;
import com.renyu.androidblelibrary.params.Params;
import com.renyu.androidblelibrary.utils.AESCBCNoPadding;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.bean.BLEErrorRequest;
import com.renyu.blelibrary.impl.BLEOTAListener;
import com.renyu.blelibrary.impl.BLERSSIListener;
import com.renyu.blelibrary.impl.BLEReadResponseListener;
import com.renyu.blelibrary.impl.BLEScanCallBackListener;
import com.renyu.blelibrary.impl.BLEStateChangeListener;
import com.renyu.blelibrary.impl.BLEWriteErrorListener;
import com.renyu.blelibrary.impl.BLEWriteResponseListener;
import com.renyu.blelibrary.impl.IScanAndConnRule;
import com.renyu.blelibrary.utils.BLEFramework;
import com.renyu.blelibrary.utils.HexUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
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
        bleFramework.setBleScanCallbackListener(new BLEScanCallBackListener() {
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
                BLEConnectModel model = new BLEConnectModel();
                model.setBleState(currentState);
                EventBus.getDefault().post(model);
            }
        });
        // 写回调
        bleFramework.setBLEWriteResponseListener(new BLEWriteResponseListener() {
            @Override
            public void getResponseValues(byte[] value) {
                Log.d("BLEService2", "white:");
                if ((int) value[0] == 0x00) {
                    Log.d("BLEService2", "验证码正确");
                } else if ((int) value[0] == 0x01) {
                    Log.d("BLEService2", "验证码错误");
                } else if ((int) value[0] == 0x02) {
                    Log.d("BLEService2", "验证码输入正确");
                } else if ((int) value[0] == 0x03) {
                    Log.d("BLEService2", "验证码输入错误");
                } else if (value[0] == (byte) 0x40) {
                    Log.d("BLEService2", "设置时间成功");
                } else if (value[0] == (byte) 0x41) {
                    Log.d("BLEService2", "时间加密数据长度有问题");
                } else if (value[0] == (byte) 0x42) {
                    Log.d("BLEService2", "时间验证码错误");
                } else if (value[0] == (byte) 0x43) {
                    Log.d("BLEService2", "时间写入错误");
                }
                // 读取EBC和消耗的药瓶量
                else if (value[0] == (byte) 0x9F) {
                    // 药品1的编号
                    int b1 = (int) value[1];
                    // 上次同步后，药品1的使用量
                    int b1_use = (int) value[2];
                    // 药品2的编号
                    int b2 = (int) value[3];
                    // 上次同步后，药品2的使用量
                    int b2_use = (int) value[4];
                    // 药品3的编号
                    int b3 = (int) value[5];
                    // 上次同步后，药品3的使用量
                    int b3_use = (int) value[6];
                    // 药品4的编号
                    int b4 = (int) value[7];
                    // 上次同步后，药品1的使用量
                    int b4_use = (int) value[8];
                    // 药品5的编号
                    int b5 = (int) value[9];
                    // 上次同步后，药品5的使用量
                    int b5_use = (int) value[10];
                    // 上次同步后，产生的EBC
                    byte[] totalEBCTemp = new byte[2];
                    totalEBCTemp[0] = value[12];
                    totalEBCTemp[1] = value[11];
                    int totalEBC = HexUtil.byte2ToInt(totalEBCTemp);
                    // 手环共产生的总积分
                    byte[] totalScoreTemp = new byte[2];
                    totalScoreTemp[0] = value[14];
                    totalScoreTemp[1] = value[13];
                    int totalScore = HexUtil.byte2ToInt(totalScoreTemp) + 65536 * ((int) value[15]);
                    boolean isVerify = (value[1] | ~value[3]) == (int) value[16];
                    Log.d("BLEService2", "药品1的编号" + b1 + ",上次同步后，药品1的使用量:" + b1_use + " " +
                            "药品2的编号" + b2 + ",上次同步后，药品2的使用量:" + b2_use + " " +
                            "药品3的编号" + b3 + ",上次同步后，药品3的使用量:" + b3_use + " " +
                            "药品4的编号" + b4 + ",上次同步后，药品4的使用量:" + b4_use + " " +
                            "药品5的编号" + b5 + ",上次同步后，药品5的使用量:" + b5_use + " " +
                            "上次同步后，产生的EBC:" + totalEBC + " 手环共产生的总积分:" + totalScore + " " +
                            "校验:" + isVerify);
                } else if (value[0] == (byte) 0x4C) {
                    Log.d("BLEService2", "EBC写入成功");
                } else if (value[0] == (byte) 0x4D) {
                    Log.d("BLEService2", "EBC加密数据长度有问题");
                } else if (value[0] == (byte) 0x4E) {
                    Log.d("BLEService2", "EBC验证码错误");
                } else if (value[0] == (byte) 0x4F) {
                    Log.d("BLEService2", "EBC写入失败");
                }
                // 读取每小时的步数,表示还有数据
                else if (value[0] == (byte) 0x86 || value[0] == (byte) 0x85) {
                    Log.d("BLEService2", "读取的前几天步数数据统计");

                    int time = (int) value[1];

                    // 运动强度
                    String intensityDesp = "";
                    int intensity = (int) value[2];
                    if (intensity == 0x01) {
                        intensityDesp = "差";
                    } else if (intensity == 0x03) {
                        intensityDesp = "低";
                    } else if (intensity == 0x07) {
                        intensityDesp = "中";
                    } else if (intensity == 0x0F) {
                        intensityDesp = "高";
                    }

                    // 平均心率（开启实时心率有效，否则为0）
                    int heartRate = (int) value[3];

                    // 今天到现在时间为止产生的总的EBC
                    int ebc = (int) value[4];

                    // 今天到现在时间为止的距离（单位：十米）
                    byte[] distanceTemp = new byte[2];
                    distanceTemp[0] = value[6];
                    distanceTemp[1] = value[5];
                    int distance = HexUtil.byte2ToInt(distanceTemp);

                    // 今天到现在时间为止消耗的卡路里（单位：千卡）
                    byte[] calorieTemp = new byte[2];
                    calorieTemp[0] = value[8];
                    calorieTemp[1] = value[7];
                    int calorie = HexUtil.byte2ToInt(calorieTemp);

                    // 今天到现在时间为止运动时间（单位：分钟）
                    byte[] exerciseTimeTemp = new byte[2];
                    exerciseTimeTemp[0] = value[10];
                    exerciseTimeTemp[1] = value[9];
                    int exerciseTime = HexUtil.byte2ToInt(exerciseTimeTemp);

                    // 今天到现在时间为止总步数
                    byte[] totalStepTemp = new byte[]{value[16], value[15], value[14], value[13]};
                    int totalStep = HexUtil.byte4ToInt(totalStepTemp);

                    if (value[0] == (byte) 0x80) {
                        Log.d("BLEService2", "读取当天的当前总步数 time:" + time + " heartRate:" + heartRate + " ebc:" + ebc + " distance: " + distance + " calorie:" + calorie + " totalStep:" + totalStep);
                    } else if (value[0] == (byte) 0x86 || value[0] == (byte) 0x85) {
                        Log.d("BLEService2", "读取的前几天步数 time:" + time + " heartRate:" + heartRate + " ebc:" + ebc + " distance: " + distance + " calorie:" + calorie + " totalStep:" + totalStep);
                    }
                }
                // 读取每小时的步数,表示还有数据
                // 读取每小时的步数,表示没有数据了，后面Byte[1]-[16]有效
                else if (value[0] == (byte) 0x82 || value[0] == (byte) 0x81) {
                    byte[] temp = new byte[value.length - 1];
                    for (int i = 0; i < temp.length; i++) {
                        temp[i] = value[i + 1];
                    }

                    byte[] result = AESCBCNoPadding.decryption(temp);

                    int time = (int) value[1];

                    int hour = (int) value[2];

                    // 运动强度
                    String intensityDesp = "";
                    int intensity = (int) value[3];
                    if (intensity == 0x01) {
                        intensityDesp = "差";
                    } else if (intensity == 0x03) {
                        intensityDesp = "低";
                    } else if (intensity == 0x07) {
                        intensityDesp = "中";
                    } else if (intensity == 0x0F) {
                        intensityDesp = "高";
                    }

                    // 一小时内运动的时间（单位：分钟）
                    int exerciseTime = (int) value[4];

                    // 平均心率（开启实时心率有效，否则为0）
                    int heartRate = (int) value[5];

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

                    Log.d("BLEService2", "读取每小时的步数 time:"+time+" hour:"+hour+" heartRate:" + heartRate + " ebc:" + ebc + " distance: " + distance + " calorie:" + calorie + " totalStep:" + totalStep);
                } else if (value[0] == (byte) 0x88) {
                    int min = (int) value[1];
                    int hour = (int) value[2];
                    int rate = (int) value[3];
                    int minMin = (int) value[4];
                    int minHour = (int) value[5];
                    int minRate = (int) value[6];
                    int fastMin = (int) value[7];
                    int fastHour = (int) value[8];
                    int fastRate = (int) value[9];
                    Log.d("BLEService2", "读取心率 记录的时间 " + hour + ":" + min + " 心率:" + rate);
                }
                // 睡眠时间，表示还有数据
                // 睡眠时间，表示没有数据了，后面Byte[1]-[4]有效
                else if (value[0] == (byte) 0x99 || value[0] == (byte) 0x9A) {
                    int time = (int) value[1];
                    int min = (int) value[2];
                    int hour = (int) value[3];
                    String statueDesp = "";
                    int statue = (int) value[4];
                    if (statue == 0x01) {
                        statueDesp = "入睡";
                    } else if (statue == 0x02) {
                        statueDesp = "浅睡";
                    } else if (statue == 0x03) {
                        statueDesp = "深睡";
                    } else if (statue == 0x04) {
                        statueDesp = "中途清醒";
                    } else if (statue == 0x05) {
                        statueDesp = "醒来";
                    }
                    Log.d("BLEService2", "睡眠时间 记录的时间 " + time + " " + hour + ":" + min + " 睡眠状态:" + statueDesp);
                }
                else if (value[0] == (byte) 0x9B) {
                    Log.d("BLEService2", "睡眠时间 没有数据了");
                } else if (value[0] == (byte) 0x2C) {
                    Log.d("BLEService2", "用户信息写入成功");
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
        // BLE指令无返回回调
        bleFramework.setBleWriteErrorListener(new BLEWriteErrorListener() {
            @Override
            public void getErrorRequest(BLEErrorRequest value) {
                Log.d("BLEService2", "出错");
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
                bleFramework.addWriteCommand((UUID) intent.getSerializableExtra(Params.SERVICEUUID),
                        (UUID) intent.getSerializableExtra(Params.CHARACUUID),
                        intent.getByteArrayExtra(Params.BYTECODE));
            }
            if (intent.getStringExtra(Params.COMMAND).equals(Params.READ)) {
                bleFramework.addReadCommand((UUID) intent.getSerializableExtra(Params.SERVICEUUID),
                        (UUID) intent.getSerializableExtra(Params.CHARACUUID));
            }
            if (intent.getStringExtra(Params.COMMAND).equals(Params.RSSI)) {
                bleFramework.readRSSI();
            }
            if (intent.getStringExtra(Params.COMMAND).equals(Params.DISCONN)) {
                bleFramework.stopScan(true);
                bleFramework.disconnect();
            }
            if (intent.getStringExtra(Params.COMMAND).equals(Params.STOPSCAN)) {
                bleFramework.stopScan(true);
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
     *
     * @param serviceUUID
     * @param CharacUUID
     * @param bytes
     * @param context
     */
    public static void sendWriteCommand(UUID serviceUUID, UUID CharacUUID, byte[] bytes, Context context) {
        Intent intent = new Intent(context, BLEService2.class);
        intent.putExtra(Params.COMMAND, Params.WRITE);
        intent.putExtra(Params.SERVICEUUID, serviceUUID);
        intent.putExtra(Params.CHARACUUID, CharacUUID);
        intent.putExtra(Params.BYTECODE, bytes);
        context.startService(intent);
    }

    public static void sendWriteCommand(UUID serviceUUID, UUID CharacUUID, byte byte_, Context context) {
        byte[] bytes = new byte[1];
        bytes[0] = byte_;
        Intent intent = new Intent(context, BLEService2.class);
        intent.putExtra(Params.COMMAND, Params.WRITE);
        intent.putExtra(Params.SERVICEUUID, serviceUUID);
        intent.putExtra(Params.CHARACUUID, CharacUUID);
        intent.putExtra(Params.BYTECODE, bytes);
        context.startService(intent);
    }

    /**
     * 发送读指令
     *
     * @param serviceUUID
     * @param CharacUUID
     * @param context
     */
    public static void sendReadCommand(final UUID serviceUUID, final UUID CharacUUID, Context context) {
        Intent intent = new Intent(context, BLEService2.class);
        intent.putExtra(Params.COMMAND, Params.READ);
        intent.putExtra(Params.SERVICEUUID, serviceUUID);
        intent.putExtra(Params.CHARACUUID, CharacUUID);
        context.startService(intent);
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
     * 停止扫描
     *
     * @param context
     */
    public static void stopScan(Context context) {
        Intent intent = new Intent(context, BLEService.class);
        intent.putExtra(Params.COMMAND, Params.STOPSCAN);
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
     * 获取设备连接状态
     *
     * @return
     */
    public static BLEConnectModel getBLEConnectModel() {
        BLEConnectModel bleConnectModel = new BLEConnectModel();
        bleConnectModel.setBleState(BLEFramework.getBleFrameworkInstance().getConnectionState());
        return bleConnectModel;
    }

    /**
     * 进入OTA升级
     *
     * @param context
     */
    public static void enterOta(Context context) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) 0xa7;
        sendWriteCommand(Params.UUID_SERVICE_OTA, Params.UUID_SERVICE_OTA_WRITE, bytes, context);
    }

    /**
     * 开始OTA升级
     *
     * @param context
     */
    public static void startOTA(Context context) {
        Intent intent = new Intent(context, BLEService2.class);
        intent.putExtra(Params.COMMAND, Params.OTA);
        context.startService(intent);
    }

    /**
     * 验证UID
     *
     * @param value
     */
    public static void verifyUID(String value, Context context) {
        byte[] bytes = new byte[11];
        for (int i = 0; i < bytes.length; i++) {
            if (value.getBytes().length > i) {
                bytes[i] = value.getBytes()[i];
            } else {
                bytes[i] = 0x00;
            }
        }
        sendWriteCommand(Params.UUID_SERVICE_WristBand_Verify, Params.UUID_SERVICE_WristBand_UID, bytes, context);
    }

    /**
     * 验证Code
     *
     * @param value
     */
    public static void verifyCode(String value, Context context) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < value.length(); i++) {
            int num = Integer.parseInt(value.substring(i, i + 1));
            bytes[i] = (byte) num;
        }
        sendWriteCommand(Params.UUID_SERVICE_WristBand_Verify, Params.UUID_SERVICE_WristBand_Code, bytes, context);
    }

    /**
     * 时间设置
     */
    public static void setTime(Context context) {
        Calendar calendar = Calendar.getInstance();
        byte[] bytes = new byte[17];
        bytes[0] = (byte) 0xC0;
        bytes[1] = (byte) calendar.get(Calendar.SECOND);
        bytes[2] = (byte) calendar.get(Calendar.MINUTE);
        bytes[3] = (byte) calendar.get(Calendar.HOUR);
        if (calendar.get(Calendar.HOUR) > 0 && calendar.get(Calendar.HOUR) < 12) {
            bytes[4] = (byte) 0;
        } else {
            bytes[4] = (byte) 1;
        }
        bytes[5] = (byte) 0;
        bytes[6] = (byte) calendar.get(Calendar.DAY_OF_WEEK);
        bytes[7] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        bytes[8] = (byte) (calendar.get(Calendar.MONTH) + 1);
        bytes[9] = (byte) Integer.parseInt(("" + calendar.get(Calendar.YEAR)).substring(2, 4));
        bytes[10] = (byte) 0;
        bytes[11] = (byte) 0;
        bytes[12] = (byte) 0;
        bytes[13] = (byte) 0;
        bytes[14] = (byte) 0;
        bytes[15] = (byte) 0;
        bytes[16] = (byte) 0x5A;
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SpecialSet, Params.UUID_SERVICE_WristBand_SpecialSetWrite, bytes, context);
    }

    /**
     * 读取EBC和消耗的药瓶量
     */
    public static void readEBC(Context context) {
        sendWriteCommand(Params.UUID_SERVICE_WristBand_Data, Params.UUID_SERVICE_WristBand_DataWrite, (byte) 0x9F, context);
    }

    /**
     * 写入EBC和药瓶
     *
     * @param b1
     * @param b1_use
     * @param b2
     * @param b2_use
     * @param b3
     * @param b3_use
     * @param b4
     * @param b4_use
     * @param b5
     * @param b5_use
     * @param totalScore
     */
    public static void setEBC(int b1, int b1_use, int b2, int b2_use, int b3, int b3_use, int b4, int b4_use, int b5, int b5_use, int totalScore, Context context) {
        byte[] bytes = new byte[17];
        bytes[0] = (byte) 0xCC;
        bytes[1] = (byte) 0xA5;
        bytes[2] = (byte) b1;
        bytes[3] = (byte) b1_use;
        bytes[4] = (byte) b2;
        bytes[5] = (byte) b2_use;
        bytes[6] = (byte) b3;
        bytes[7] = (byte) b3_use;
        bytes[8] = (byte) b4;
        bytes[9] = (byte) b4_use;
        bytes[10] = (byte) b5;
        bytes[11] = (byte) b5_use;
        bytes[12] = (byte) 0;
        byte[] totalScoreTemp = HexUtil.intToByte(totalScore);
        bytes[13] = totalScoreTemp[1];
        bytes[14] = totalScoreTemp[0];
        bytes[15] = (byte) 0;
        bytes[16] = (byte) (bytes[1] | bytes[2] | bytes[4]);
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SpecialSet, Params.UUID_SERVICE_WristBand_SpecialSetWrite, bytes, context);
    }

    /**
     * 读取每小时的步数
     */
    public static void hourTotalStep(Context context) {
        sendWriteCommand(Params.UUID_SERVICE_WristBand_Data, Params.UUID_SERVICE_WristBand_DataWrite, (byte) 0x82, context);
    }

    /**
     * 读取的前几天步数数据统计（理论上7天）
     */
    public static void dayTotalStep(Context context) {
        sendWriteCommand(Params.UUID_SERVICE_WristBand_Data, Params.UUID_SERVICE_WristBand_DataWrite, (byte) 0x86, context);
    }

    /**
     * 读取心率
     */
    public static void heartRate(Context context) {
        sendWriteCommand(Params.UUID_SERVICE_WristBand_Data, Params.UUID_SERVICE_WristBand_DataWrite, (byte) 0x88, context);
    }

    /**
     * 睡眠时间
     */
    public static void sleepTime(Context context) {
        sendWriteCommand(Params.UUID_SERVICE_WristBand_Data, Params.UUID_SERVICE_WristBand_DataWrite, (byte) 0x9A, context);
    }

    /**
     * 写入用户数据
     */
    public static void writeUserInfo(Context context, int sex, int height, int weight, int tatgetStep) {
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAC;
        bytes[1] = (byte) sex;
        bytes[2] = (byte) height;
        bytes[3] = (byte) weight;
        byte[] temp = HexUtil.intToByte(tatgetStep);
        bytes[4] = temp[1];
        bytes[5] = temp[0];
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, bytes, context);
    }
}
