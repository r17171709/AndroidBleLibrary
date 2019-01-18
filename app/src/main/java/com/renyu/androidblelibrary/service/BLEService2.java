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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
                // 读取EBC
                else if (value[0] == (byte) 0x9D) {
                    // 上次同步后，产生的EBC
                    byte[] lastEBCTemp = new byte[2];
                    lastEBCTemp[0] = value[2];
                    lastEBCTemp[1] = value[1];
                    int lastEBC = HexUtil.byte2ToInt(lastEBCTemp);

                    // 手环共产生的总EBC
                    byte[] totalEBCTemp = new byte[4];
                    totalEBCTemp[0] = value[8];
                    totalEBCTemp[1] = value[7];
                    totalEBCTemp[2] = value[6];
                    totalEBCTemp[3] = value[5];
                    int totalEBC = HexUtil.byte4ToInt(totalEBCTemp);

                    Log.d("BLEService2", "上次同步后，产生的EBC: " + lastEBC + " 手环共产生的总EBC: " + totalEBC);
                }
                // 读取药瓶剩余量
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

                    boolean isVerify = (value[1] | ~value[3]) == (int) value[11];
                    Log.d("BLEService2", "药品1的编号" + b1 + ",上次同步后，药品1的使用量:" + b1_use + " " +
                            "药品2的编号" + b2 + ",上次同步后，药品2的使用量:" + b2_use + " " +
                            "药品3的编号" + b3 + ",上次同步后，药品3的使用量:" + b3_use + " " +
                            "药品4的编号" + b4 + ",上次同步后，药品4的使用量:" + b4_use + " " +
                            "药品5的编号" + b5 + ",上次同步后，药品5的使用量:" + b5_use + " " +
                            "校验:" + isVerify);
                } else if (value[0] == (byte) 0x4C) {
                    Log.d("BLEService2", "药瓶写入成功");
                } else if (value[0] == (byte) 0x4D) {
                    Log.d("BLEService2", "药瓶加密数据长度有问题");
                } else if (value[0] == (byte) 0x4E) {
                    Log.d("BLEService2", "药瓶验证码错误");
                } else if (value[0] == (byte) 0x4F) {
                    Log.d("BLEService2", "药瓶写入失败");
                } else if (value[0] == (byte) 0x48) {
                    Log.d("BLEService2", "EBC写入成功");
                } else if (value[0] == (byte) 0x4B) {
                    Log.d("BLEService2", "EBC写入失败");
                }
                // 读取截止到当前时间总数据
                else if (value[0] == (byte) 0x80) {
                    int time = HexUtil.byte4ToInt(new byte[]{value[4], value[3], value[2], value[1]});

                    // 运动强度
                    String intensityDesp = "";
                    int intensity = (int) value[5];
                    if (intensity == 0x01) {
                        intensityDesp = "差";
                    } else if (intensity == 0x03) {
                        intensityDesp = "低";
                    } else if (intensity == 0x07) {
                        intensityDesp = "中";
                    } else if (intensity == 0x0F) {
                        intensityDesp = "高";
                    }

                    // 产生的总的EBC
                    int ebc = (int) value[6];

                    // 总的距离（单位：十米）
                    byte[] distanceTemp = new byte[2];
                    distanceTemp[0] = value[8];
                    distanceTemp[1] = value[7];
                    int distance = HexUtil.byte2ToInt(distanceTemp);

                    // 总的卡路里（单位：千卡）
                    byte[] calorieTemp = new byte[2];
                    calorieTemp[0] = value[10];
                    calorieTemp[1] = value[9];
                    int calorie = HexUtil.byte2ToInt(calorieTemp);

                    // 总的运动时间（单位：分钟）
                    byte[] exerciseTimeTemp = new byte[2];
                    exerciseTimeTemp[0] = value[12];
                    exerciseTimeTemp[1] = value[11];
                    int exerciseTime = HexUtil.byte2ToInt(exerciseTimeTemp);

                    // 总的步数
                    byte[] totalStepTemp = new byte[4];
                    totalStepTemp[0] = value[16];
                    totalStepTemp[1] = value[15];
                    totalStepTemp[2] = value[14];
                    totalStepTemp[3] = value[13];
                    int totalStep = HexUtil.byte4ToInt(totalStepTemp);

                    Log.d("BLEService2", "截止当前时间的运动数据 time:" + time * 3600 + " ebc:" + ebc + " distance: " + distance + " calorie:" + calorie + " exerciseTime:" + exerciseTime + " totalStep:" + totalStep);
                }
                // 读取每小时的步数,表示还有数据
                // 读取每小时的步数,表示没有数据了,后面Byte[1]-[16]有效
                else if (value[0] == (byte) 0x82 || value[0] == (byte) 0x81) {
                    byte[] temp = new byte[value.length - 1];
                    for (int i = 0; i < temp.length; i++) {
                        temp[i] = value[i + 1];
                    }

                    byte[] result = AESCBCNoPadding.decryption(temp);

                    int time = HexUtil.byte4ToInt(new byte[]{value[4], value[3], value[2], value[1]});

                    // 运动强度
                    String intensityDesp = "";
                    int intensity = (int) value[5];
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
                    int exerciseTime = value[6] & 0xff;

                    // 平均心率（开启实时心率有效，否则为0）
                    int heartRate = (int) value[7];

                    // 一小时产生的EBC
                    int ebc = (int) value[8];

                    // 一小时内距离（单位：十米）
                    byte[] distanceTemp = new byte[2];
                    distanceTemp[0] = value[10];
                    distanceTemp[1] = value[9];
                    int distance = HexUtil.byte2ToInt(distanceTemp);

                    // 一小时内消耗的卡路里（单位：千卡）
                    byte[] calorieTemp = new byte[2];
                    calorieTemp[0] = value[12];
                    calorieTemp[1] = value[11];
                    int calorie = HexUtil.byte2ToInt(calorieTemp);

                    // 一小时的总步数
                    byte[] totalStepTemp = new byte[2];
                    totalStepTemp[0] = value[14];
                    totalStepTemp[1] = value[13];
                    int totalStep = HexUtil.byte2ToInt(totalStepTemp);

                    Log.d("BLEService2", "读取每小时的步数 time:" + time * 3600 + " heartRate:" + heartRate + " ebc:" + ebc + " distance: " + distance + " calorie:" + calorie + " exerciseTime:" + exerciseTime + " totalStep:" + totalStep);
                }
                // 读取的前几天步数数据统计,表示还有数据
                // 读取的前几天步数数据统计,后面Byte[1]-[16]有效
                else if (value[0] == (byte) 0x86 || value[0] == (byte) 0x85) {
                    int time = HexUtil.byte4ToInt(new byte[]{value[4], value[3], value[2], value[1]});

                    // 运动强度
                    String intensityDesp = "";
                    int intensity = (int) value[5];
                    if (intensity == 0x01) {
                        intensityDesp = "差";
                    } else if (intensity == 0x03) {
                        intensityDesp = "低";
                    } else if (intensity == 0x07) {
                        intensityDesp = "中";
                    } else if (intensity == 0x0F) {
                        intensityDesp = "高";
                    }

                    // 今天到现在时间为止产生的总的EBC
                    int ebc = (int) value[6];

                    // 今天到现在时间为止的距离（单位：十米）
                    byte[] distanceTemp = new byte[2];
                    distanceTemp[0] = value[8];
                    distanceTemp[1] = value[7];
                    int distance = HexUtil.byte2ToInt(distanceTemp);

                    // 今天到现在时间为止消耗的卡路里（单位：千卡）
                    byte[] calorieTemp = new byte[2];
                    calorieTemp[0] = value[10];
                    calorieTemp[1] = value[9];
                    int calorie = HexUtil.byte2ToInt(calorieTemp);

                    // 今天到现在时间为止运动时间（单位：分钟）
                    byte[] exerciseTimeTemp = new byte[2];
                    exerciseTimeTemp[0] = value[12];
                    exerciseTimeTemp[1] = value[11];
                    int exerciseTime = HexUtil.byte2ToInt(exerciseTimeTemp);

                    // 今天到现在时间为止总步数
                    byte[] totalStepTemp = new byte[]{value[16], value[15], value[14], value[13]};
                    int totalStep = HexUtil.byte4ToInt(totalStepTemp);

                    Log.d("BLEService2", "读取的前几天步数 time:" + time * 3600 + " ebc:" + ebc + " distance: " + distance + " calorie:" + calorie + " exerciseTime:" + exerciseTime + " totalStep:" + totalStep);
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
                // 睡眠时间
                else if (value[0] == (byte) 0x9A) {
                    int last = (int) value[2];
                    if (last == 1) {
                        // 有数据
                    } else if (last == 0) {
                        // 没有数据
                    }

                    int time = (int) value[1];

                    // 入睡时间
                    int startHour = (int) value[4];
                    int startMinute = (int) value[3];

                    // 醒来时间
                    int wakeupHour = (int) value[6];
                    int wakeupMinute = (int) value[5];

                    // 浅睡时长
                    int lightHour = (int) value[8];
                    int lightMinute = (int) value[7];

                    // 深睡时长
                    int deepHour = (int) value[10];
                    int deepMinute = (int) value[9];

                    Log.d("BLEService2", "睡眠时间 记录的时间 " + time + " " + startHour + ":" + startMinute + " " + wakeupHour + ":" + wakeupMinute);
                } else if (value[0] == (byte) 0x8D || value[0] == (byte) 0x8E || value[0] == (byte) 0x8F) {
                    String mode = "";
                    if (value[1] == 0x01) {
                        mode = "跑步模式";
                    } else if (value[1] == 0x02) {
                        mode = "快走模式";
                    } else if (value[1] == 0x03) {
                        mode = "游泳模式";
                    }

                    int time = (int) (value[2]);

                    int startMin = (int) (value[3]);

                    int startHour = (int) (value[4]);

                    int endMin = (int) (value[5]);

                    int endHour = (int) (value[6]);

                    int heartRate = value[7] & 0xff;

                    // 运动步数
                    byte[] exerciseStepTemp = new byte[2];
                    exerciseStepTemp[0] = value[10];
                    exerciseStepTemp[1] = value[9];
                    int exerciseStep = HexUtil.byte2ToInt(exerciseStepTemp);

                    // 运动距离
                    byte[] exerciseDistanceTemp = new byte[2];
                    exerciseDistanceTemp[0] = value[12];
                    exerciseDistanceTemp[1] = value[11];
                    int exerciseDistance = HexUtil.byte2ToInt(exerciseDistanceTemp);

                    // 运动消耗的卡路里
                    byte[] calorieTemp = new byte[2];
                    calorieTemp[0] = value[14];
                    calorieTemp[1] = value[13];
                    int calorie = HexUtil.byte2ToInt(calorieTemp);

                    Log.d("BLEService2", "读取运动模式:" + mode + " 日期:" + time + " 开始时间为:" + startHour + ":" + startMin +
                            " 结束时间为:" + endHour + ":" + endMin + " heartRate:" + heartRate +
                            " 运动步数:" + exerciseStep + " 运动距离:" + exerciseDistance + " 运动消耗的卡路里:" + calorie);
                } else if (value[0] == (byte) 0x2C) {
                    Log.d("BLEService2", "用户信息写入成功");
                } else if (value[0] == (byte) 0xBA) {
                    ArrayList<Integer> alarms = new ArrayList<>();
                    for (int j = 1; j <= 13; j++) {
                        int total = j == 13 ? 4 : 8;
                        for (int i = 1; i <= total; i++) {
                            if (((value[j] >> (i - 1)) & 0x01) == 1) {
                                alarms.add(i + (j - 1) * 8);
                            }
                        }
                    }
                    Log.d("BLEService2", "读取到的闹钟信息数量为:" + alarms.size());
                    for (Integer alarm : alarms) {
                        readAlarm(BLEService2.this, alarm);
                    }
                } else if (value[0] == (byte) 0xB6) {
                    Log.d("BLEService2", "读取到的闹钟/事件提醒");
                } else if (value[0] == (byte) 0x30) {
                    Log.d("BLEService2", "事件设置成功");
                } else if (value[0] == (byte) 0x31) {
                    Log.d("BLEService2", "闹钟写入失败");
                } else if (value[0] == (byte) 0x32) {
                    Log.d("BLEService2", "添加时，该闹钟编码已存在");
                } else if (value[0] == (byte) 0x33) {
                    Log.d("BLEService2", "修改和删除时，该闹钟不存在");
                } else if (value[0] == (byte) 0xBB) {
                    int type = value[1];
                    if (type == 1) {
                        Log.d("BLEService2", "闹钟式提醒");
                    } else if (type == 0) {
                        int time = value[2];
                        Log.d("BLEService2", "间隔式提醒，久坐提醒时常：" + time + "分钟");
                    }
                } else if (value[0] == (byte) 0x3E) {
                    Log.d("BLEService2", "久坐提醒设置成功");
                } else if (value[0] == (byte) 0x3F) {
                    Log.d("BLEService2", "久坐提醒设置失败");
                } else if (value[0] == (byte) 0x26) {
                    Log.d("BLEService2", "间隔式提醒时长设置成功");
                } else if (value[0] == (byte) 0x27) {
                    Log.d("BLEService2", "间隔式提醒时长设置失败");
                } else if (value[0] == (byte) 0xB8) {
                    int theme = (int) value[1];

                    int hasDownloadTheme1 = (int) value[2];
                    int hasDownloadTheme2 = (int) value[3];

                    int light = (int) value[4];

                    boolean isOpenHeart = (int) value[5] == 1;

                    boolean noDisturb = (int) value[6] == 1;
                    int noDisturbStartHour = (int) value[8];
                    int noDisturbStartMinute = (int) value[7];
                    int noDisturbEndHour = (int) value[10];
                    int noDisturbEndMinute = (int) value[9];

                    int sleepStartHour = (int) value[12];
                    int sleepStartMinute = (int) value[11];
                    int sleepEndHour = (int) value[14];
                    int sleepEndMinute = (int) value[13];

                    boolean isOpenTarget = (int) value[15] == 1;
                    byte[] targetNum = {value[17], value[16]};
                    int target = HexUtil.byte2ToInt(targetNum);

                    Log.d("BLEService2", "读取手环一些信息");
                } else if (value[0] == (byte) 0xB9) {
                    Log.d("BLEService2", "电池电量：" + (int) value[1]);
                } else if (value[0] == (byte) 0x20) {
                    Log.d("BLEService2", "背光设置成功");
                } else if (value[0] == (byte) 0x21) {
                    Log.d("BLEService2", "背光设置失败");
                } else if (value[0] == (byte) 0x24) {
                    Log.d("BLEService2", "主题设置成功");
                } else if (value[0] == (byte) 0x25) {
                    Log.d("BLEService2", "主题设置失败");
                } else if (value[0] == (byte) 0x28) {
                    Log.d("BLEService2", "勿扰设置成功");
                } else if (value[0] == (byte) 0x29) {
                    Log.d("BLEService2", "勿扰设置失败");
                } else if (value[0] == (byte) 0x3C) {
                    Log.d("BLEService2", "达标提醒成功");
                } else if (value[0] == (byte) 0x3D) {
                    Log.d("BLEService2", "达标提醒失败");
                } else if (value[0] == (byte) 0x2E) {
                    Log.d("BLEService2", "解绑成功");
                } else if (value[0] == (byte) 0x2F) {
                    Log.d("BLEService2", "解绑失败");
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
                if (getBLEConnectModel().getBleState() == BLEFramework.STATE_SERVICES_DISCOVERED) {
                    bleFramework.addWriteCommand((UUID) intent.getSerializableExtra(Params.SERVICEUUID),
                            (UUID) intent.getSerializableExtra(Params.CHARACUUID),
                            intent.getByteArrayExtra(Params.BYTECODE));
                }
            }
            if (intent.getStringExtra(Params.COMMAND).equals(Params.READ)) {
                if (getBLEConnectModel().getBleState() == BLEFramework.STATE_SERVICES_DISCOVERED) {
                    bleFramework.addReadCommand((UUID) intent.getSerializableExtra(Params.SERVICEUUID),
                            (UUID) intent.getSerializableExtra(Params.CHARACUUID));
                }
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
        bytes[3] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        if (calendar.get(Calendar.HOUR_OF_DAY) > 0 && calendar.get(Calendar.HOUR_OF_DAY) < 12) {
            bytes[4] = (byte) 0;
        } else {
            bytes[4] = (byte) 1;
        }
        bytes[5] = (byte) 0;
        if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
            bytes[6] = 7;
        } else {
            bytes[6] = (byte) (calendar.get(Calendar.DAY_OF_WEEK) - 1);
        }
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
     * 读取EBC
     */
    public static void readEBC(Context context) {
        sendWriteCommand(Params.UUID_SERVICE_WristBand_Data, Params.UUID_SERVICE_WristBand_DataWrite, (byte) 0x9D, context);
    }

    /**
     * 读取药瓶
     */
    public static void readBottle(Context context) {
        sendWriteCommand(Params.UUID_SERVICE_WristBand_Data, Params.UUID_SERVICE_WristBand_DataWrite, (byte) 0x9F, context);
    }

    /**
     * 写入EBC
     *
     * @param value
     */
    public static void setEBC(int value, Context context) {
        byte[] nums = HexUtil.intToByte(value);
        byte[] bytes = new byte[5];
        bytes[0] = (byte) 0xC8;
        if (nums.length > 0) {
            bytes[4] = nums[0];
        }
        if (nums.length > 1) {
            bytes[3] = nums[1];
        }
        if (nums.length > 2) {
            bytes[2] = nums[2];
        }
        if (nums.length > 3) {
            bytes[1] = nums[3];
        }
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SpecialSet, Params.UUID_SERVICE_WristBand_SpecialSetWrite, bytes, context);
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
     */
    public static void setBottle(int b1, int b1_use, int b2, int b2_use, int b3, int b3_use, int b4, int b4_use, int b5, int b5_use, Context context) {
        byte[] bytes = new byte[17];
        bytes[0] = (byte) 0xCC;
        bytes[1] = (byte) b1;
        bytes[2] = (byte) b1_use;
        bytes[3] = (byte) b2;
        bytes[4] = (byte) b2_use;
        bytes[5] = (byte) b3;
        bytes[6] = (byte) b3_use;
        bytes[7] = (byte) b4;
        bytes[8] = (byte) b4_use;
        bytes[9] = (byte) b5;
        bytes[10] = (byte) b5_use;
        bytes[11] = (byte) 0;
        bytes[12] = (byte) 0;
        bytes[13] = (byte) 0;
        bytes[14] = (byte) 0;
        bytes[15] = (byte) 0;
        bytes[16] = (byte) (bytes[1] | ~bytes[3]);
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
     * 读取跑步模式的运动数据
     */
    public static void sportsRun(Context context) {
        sendWriteCommand(Params.UUID_SERVICE_WristBand_Data, Params.UUID_SERVICE_WristBand_DataWrite, (byte) 0x8D, context);
    }

    /**
     * 读取快走模式的运动数据
     */
    public static void sportsFastWalk(Context context) {
        sendWriteCommand(Params.UUID_SERVICE_WristBand_Data, Params.UUID_SERVICE_WristBand_DataWrite, (byte) 0x8E, context);
    }

    /**
     * 读取游泳模式的运动数据
     */
    public static void sportsSwim(Context context) {
        sendWriteCommand(Params.UUID_SERVICE_WristBand_Data, Params.UUID_SERVICE_WristBand_DataWrite, (byte) 0x8F, context);
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
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SpecialSet, Params.UUID_SERVICE_WristBand_SpecialSetWrite, bytes, context);
    }

    /**
     * 事件提醒
     *
     * @param context
     * @param operType
     * @param no
     * @param time
     * @param repeatType
     * @param content
     */
    private static void event(Context context, int operType, int no, boolean open, long time, int repeatType, String content) {
        SimpleDateFormat format = new SimpleDateFormat("HH", Locale.CHINA);
        int hour = Integer.parseInt(format.format(new Date(time)));
        format = new SimpleDateFormat("mm", Locale.CHINA);
        int minute = Integer.parseInt(format.format(new Date(time)));
        format = new SimpleDateFormat("dd", Locale.CHINA);
        int day = Integer.parseInt(format.format(new Date(time)));
        format = new SimpleDateFormat("MM", Locale.CHINA);
        int month = Integer.parseInt(format.format(new Date(time)));
        byte[] bytes = new byte[12 + content.getBytes().length];
        bytes[0] = (byte) 0xB0;
        bytes[1] = (byte) operType;
        bytes[2] = (byte) no;
        bytes[3] = (byte) 0;
        bytes[4] = (byte) (open ? 1 : 0);
        bytes[5] = (byte) minute;
        bytes[6] = (byte) hour;
        bytes[7] = (byte) repeatType;
        bytes[8] = (byte) 0;
        bytes[9] = (byte) day;
        bytes[10] = (byte) month;
        bytes[11] = (byte) content.getBytes().length;
        for (int i = 0; i < content.getBytes().length; i++) {
            bytes[12 + i] = content.getBytes()[i];
        }
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, bytes, context);
    }

    public static void eventAdd(Context context, int no, boolean open, long time, int repeatType, String content) {
        event(context, 0, no, open, time, repeatType, content);
    }

    public static void eventUpdate(Context context, int no, boolean open, long time, int repeatType, String content) {
        event(context, 1, no, open, time, repeatType, content);
    }

    public static void eventDelete(Context context, int no) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) 0xB0;
        bytes[1] = (byte) 2;
        bytes[2] = (byte) no;
        bytes[3] = (byte) 0;
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, bytes, context);
    }

    /**
     * 闹钟模式
     *
     * @param context
     * @param operType
     * @param no
     * @param time
     * @param repeatType
     * @param sleep
     */
    private static void alarmClock(Context context, int operType, int no, boolean open, long time, int repeatType, boolean sleep) {
        SimpleDateFormat format = new SimpleDateFormat("HH", Locale.CHINA);
        int hour = Integer.parseInt(format.format(new Date(time)));
        format = new SimpleDateFormat("mm", Locale.CHINA);
        int minute = Integer.parseInt(format.format(new Date(time)));
        byte[] bytes = new byte[9];
        bytes[0] = (byte) 0xB0;
        bytes[1] = (byte) operType;
        bytes[2] = (byte) no;
        bytes[3] = (byte) 2;
        bytes[4] = (byte) (open ? 1 : 0);
        bytes[5] = (byte) minute;
        bytes[6] = (byte) hour;
        bytes[7] = (byte) repeatType;
        bytes[8] = (byte) (sleep ? 1 : 0);
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, bytes, context);
    }

    public static void alarmClockAdd(Context context, int no, boolean open, long time, int repeatType, boolean sleep) {
        alarmClock(context, 0, no, open, time, repeatType, sleep);
    }

    public static void alarmClockUpdate(Context context, int no, boolean open, long time, int repeatType, boolean sleep) {
        alarmClock(context, 1, no, open, time, repeatType, sleep);
    }

    public static void alarmClockDelete(Context context, int no) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) 0xB0;
        bytes[1] = (byte) 2;
        bytes[2] = (byte) no;
        bytes[3] = (byte) 2;
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, bytes, context);
    }

    /**
     * 久坐提醒 闹钟式提醒
     *
     * @param context
     * @param operType
     * @param no
     * @param time
     * @param repeatType
     */
    private static void sedentaryByAlarm(Context context, int operType, int no, boolean open, long time, int repeatType) {
        SimpleDateFormat format = new SimpleDateFormat("HH", Locale.CHINA);
        int hour = Integer.parseInt(format.format(new Date(time)));
        format = new SimpleDateFormat("mm", Locale.CHINA);
        int minute = Integer.parseInt(format.format(new Date(time)));
        byte[] bytes = new byte[8];
        bytes[0] = (byte) 0xB0;
        bytes[1] = (byte) operType;
        bytes[2] = (byte) no;
        bytes[3] = (byte) 1;
        bytes[4] = (byte) (open ? 1 : 0);
        bytes[5] = (byte) minute;
        bytes[6] = (byte) hour;
        bytes[7] = (byte) repeatType;
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, bytes, context);
    }

    public static void sedentaryByAlarmAdd(Context context, int no, boolean open, long time, int repeatType) {
        sedentaryByAlarm(context, 0, no, open, time, repeatType);
    }

    public static void sedentaryByAlarmUpdate(Context context, int no, boolean open, long time, int repeatType) {
        sedentaryByAlarm(context, 1, no, open, time, repeatType);
    }

    public static void sedentaryByAlarmDelete(Context context, int no) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) 0xB0;
        bytes[1] = (byte) 2;
        bytes[2] = (byte) no;
        bytes[3] = (byte) 1;
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, bytes, context);
    }

    /**
     * 读取闹钟信息
     *
     * @param context
     */
    public static void allAlarmInfo(Context context) {
        sendWriteCommand(Params.UUID_SERVICE_WristBand_READ, Params.UUID_SERVICE_WristBand_READWrite, new byte[]{(byte) 0xBA}, context);
    }

    /**
     * 按编号读取闹钟时间
     *
     * @param context
     * @param no
     */
    public static void readAlarm(Context context, int no) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) 0xB6;
        bytes[1] = (byte) no;
        sendWriteCommand(Params.UUID_SERVICE_WristBand_READ, Params.UUID_SERVICE_WristBand_READWrite, bytes, context);
    }

    /**
     * 读取久坐提醒配置信息
     *
     * @param context
     */
    public static void sedentaryReminder(Context context) {
        sendWriteCommand(Params.UUID_SERVICE_WristBand_READ, Params.UUID_SERVICE_WristBand_READWrite, new byte[]{(byte) 0xBB}, context);
    }

    /**
     * 设置久坐提醒配置信息
     *
     * @param context
     * @param value
     */
    public static void settingSedentaryReminder(Context context, int value) {
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, new byte[]{(byte) 0xBE, (byte) value}, context);
    }

    /**
     * 久坐提醒 间隔式久坐提醒设置
     *
     * @param context
     * @param minute
     */
    public static void sedentaryByIntervalSetting(Context context, int minute) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) 0xA6;
        bytes[1] = (byte) minute;
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, bytes, context);
    }

    /**
     * 读取手环一些信息
     *
     * @param context
     */
    public static void readDeviceInfo(Context context) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) 0xB8;
        sendWriteCommand(Params.UUID_SERVICE_WristBand_READ, Params.UUID_SERVICE_WristBand_READWrite, bytes, context);
    }

    /**
     * 读取电池电量
     *
     * @param context
     */
    public static void readDeviceBattery(Context context) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) 0xB9;
        sendWriteCommand(Params.UUID_SERVICE_WristBand_READ, Params.UUID_SERVICE_WristBand_READWrite, bytes, context);
    }

    /**
     * 背光设置
     *
     * @param context
     * @param num
     */
    public static void screenLight(Context context, int num) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) 0xA0;
        bytes[1] = (byte) num;
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, bytes, context);
    }

    /**
     * 勿扰模式
     *
     * @param context
     * @param type
     * @param startHour
     * @param startMinute
     * @param endHour
     * @param endMinute
     */
    public static void noDisturb(Context context, int type, int startHour, int startMinute, int endHour, int endMinute) {
        byte[] bytes = new byte[type != 1 ? 2 : 6];
        bytes[0] = (byte) 0xA8;
        bytes[1] = (byte) type;
        if (type == 1) {
            bytes[2] = (byte) startMinute;
            bytes[3] = (byte) startHour;
            bytes[4] = (byte) endMinute;
            bytes[5] = (byte) endHour;
        }
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, bytes, context);
    }

    /**
     * 达标提醒
     *
     * @param context
     * @param num
     */
    public static void targetNotify(Context context, int num) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) 0xBC;
        bytes[1] = (byte) num;
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, bytes, context);
    }

    /**
     * 更换主题
     *
     * @param context
     * @param num
     */
    public static void choiceTheme(Context context, int num) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) 0xA4;
        bytes[1] = (byte) num;
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, bytes, context);
    }

    /**
     * 解绑
     *
     * @param context
     */
    public static void unBind(Context context) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) 0xAE;
        sendWriteCommand(Params.UUID_SERVICE_WristBand_SET, Params.UUID_SERVICE_WristBand_SetWrite, bytes, context);
    }

    /**
     * 发送App通知
     *
     * @param context
     * @param no
     * @param title
     * @param content
     * @param time
     */
    public static void sendAppNotify(Context context, int no, String title, String content, String time) {
        byte[] bytes = new byte[content.getBytes().length + title.getBytes().length + 4 + 5];
        bytes[0] = (byte) no;
        bytes[1] = (byte) title.getBytes().length;
        bytes[2] = (byte) content.getBytes().length;
        bytes[3] = (byte) 5;
        for (int i = 0; i < title.getBytes().length; i++) {
            bytes[i + 4] = title.getBytes()[i];
        }
        for (int i = 0; i < content.getBytes().length; i++) {
            bytes[i + title.getBytes().length + 4] = content.getBytes()[i];
        }
        bytes[content.getBytes().length + title.getBytes().length + 4] = time.getBytes()[0];
        bytes[content.getBytes().length + title.getBytes().length + 4 + 1] = time.getBytes()[1];
        bytes[content.getBytes().length + title.getBytes().length + 4 + 2] = time.getBytes()[2];
        bytes[content.getBytes().length + title.getBytes().length + 4 + 3] = time.getBytes()[3];
        bytes[content.getBytes().length + title.getBytes().length + 4 + 4] = time.getBytes()[4];
        sendWriteCommand(Params.UUID_SERVICE_WristBand_AppNotify, Params.UUID_SERVICE_WristBand_AppNotifyWrite, bytes, context);
    }

    /**
     * 手环型号
     *
     * @param context
     */
    public static void readName(Context context) {
        sendReadCommand(Params.UUID_SERVICE_WristBand_DeviceInfo, Params.UUID_SERVICE_WristBand_DeviceInfoName, context);
    }

    /**
     * 手环sn码
     *
     * @param context
     */
    public static void readSN(Context context) {
        sendReadCommand(Params.UUID_SERVICE_WristBand_DeviceInfo, Params.UUID_SERVICE_WristBand_DeviceInfoSN, context);
    }

    /**
     * 手环硬件版本
     *
     * @param context
     */
    public static void readHardware(Context context) {
        sendReadCommand(Params.UUID_SERVICE_WristBand_DeviceInfo, Params.UUID_SERVICE_WristBand_DeviceInfoHardware, context);
    }

    /**
     * 手环BLE版本
     *
     * @param context
     */
    public static void readFirmware(Context context) {
        sendReadCommand(Params.UUID_SERVICE_WristBand_DeviceInfo, Params.UUID_SERVICE_WristBand_DeviceInfoFirmware, context);
    }

    /**
     * 手环固件版本
     *
     * @param context
     */
    public static void readSoft(Context context) {
        sendReadCommand(Params.UUID_SERVICE_WristBand_DeviceInfo, Params.UUID_SERVICE_WristBand_DeviceInfoSoft, context);
    }
}
