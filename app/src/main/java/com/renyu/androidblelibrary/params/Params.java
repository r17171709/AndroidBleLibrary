package com.renyu.androidblelibrary.params;

import java.util.UUID;

/**
 * Created by renyu on 16/3/9.
 */
public class Params {

    //BLE相关
    //服务 UUID
    // 牙刷
    public static final UUID UUID_SERVICE=UUID.fromString("E53A96FD-BC51-4A3A-A397-4B759661B7CF");
    public static final UUID UUID_SERVICE_WRITE=UUID.fromString("0000cdd2-0000-1000-8000-00805f9b34fb");
    //电池服务 UUID
    public static final UUID UUID_SERVICE_BATTERY=UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_BATTERY_READ=UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    //设备信息服务 UUID
    public static final UUID UUID_SERVICE_DEVICEINFO=UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_DEVICEINFO_NAME=UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_DEVICEINFO_ID=UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_DEVICEINFO_VERSION=UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_DEVICEINFO_CPUID=UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");
    // OTA使用
    public static final UUID UUID_SERVICE_OTA=UUID.fromString("0a2be667-2416-4373-b583-1147d905e39f");

    /**
     * bluetoothGattService:00001800-0000-1000-8000-00805f9b34fb
     * bluetoothGattCharacteristic:00002a00-0000-1000-8000-00805f9b34fb
     * bluetoothGattCharacteristic:00002a01-0000-1000-8000-00805f9b34fb
     * bluetoothGattCharacteristic:00002a04-0000-1000-8000-00805f9b34fb
     *
     * bluetoothGattService:00001801-0000-1000-8000-00805f9b34fb
     * bluetoothGattCharacteristic:00002a05-0000-1000-8000-00805f9b34fb
     * bluetoothGattDescriptor:00002902-0000-1000-8000-00805f9b34fb
     *
     * bluetoothGattService:0000180a-0000-1000-8000-00805f9b34fb
     * bluetoothGattCharacteristic:00002a29-0000-1000-8000-00805f9b34fb
     * bluetoothGattCharacteristic:00002a25-0000-1000-8000-00805f9b34fb
     * bluetoothGattCharacteristic:00002a27-0000-1000-8000-00805f9b34fb
     * bluetoothGattCharacteristic:00002a26-0000-1000-8000-00805f9b34fb
     *
     * bluetoothGattService:e53a96fd-bc51-4a3a-a397-4b759661b7cf
     * bluetoothGattCharacteristic:0000cdd1-0000-1000-8000-00805f9b34fb
     * bluetoothGattDescriptor:00002902-0000-1000-8000-00805f9b34fb
     * bluetoothGattCharacteristic:0000cdd2-0000-1000-8000-00805f9b34fb
     *
     * bluetoothGattService:0000180f-0000-1000-8000-00805f9b34fb
     * bluetoothGattCharacteristic:00002a19-0000-1000-8000-00805f9b34fb
     * bluetoothGattDescriptor:00002904-0000-1000-8000-00805f9b34fb
     * bluetoothGattDescriptor:00002902-0000-1000-8000-00805f9b34fb
     */

    /**
     * 手环
     bluetoothGattService:  00001800-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00002a00-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00002a01-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00002a04-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00002aa6-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00002ac9-0000-1000-8000-00805f9b34fb

     bluetoothGattService:  00001801-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00002a05-0000-1000-8000-00805f9b34fb
     bluetoothGattDescriptor:  00002902-0000-1000-8000-00805f9b34fb
     setCharacteristicNotification() - uuid: 00002a05-0000-1000-8000-00805f9b34fb enable: true

     bluetoothGattService:  00006096-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00001ad2-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00001ad3-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00001ad4-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00001ad5-0000-1000-8000-00805f9b34fb

     bluetoothGattService:  00001802-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00002a06-0000-1000-8000-00805f9b34fb

     bluetoothGattService:  0000f017-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  0000fcd6-0000-1000-8000-00805f9b34fb
     bluetoothGattDescriptor:  00002902-0000-1000-8000-00805f9b34fb
     setCharacteristicNotification() - uuid: 0000fcd6-0000-1000-8000-00805f9b34fb enable: true
     bluetoothGattCharacteristic:  0000246f-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00004402-0000-1000-8000-00805f9b34fb

     bluetoothGattService:  0000606f-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  0000ad1d-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  0000e680-0000-1000-8000-00805f9b34fb
     bluetoothGattDescriptor:  00002902-0000-1000-8000-00805f9b34fb
     setCharacteristicNotification() - uuid: 0000e680-0000-1000-8000-00805f9b34fb enable: true

     bluetoothGattService:  0000565d-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  0000a1c1-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00004042-0000-1000-8000-00805f9b34fb
     bluetoothGattDescriptor:  00002902-0000-1000-8000-00805f9b34fb
     setCharacteristicNotification() - uuid: 00004042-0000-1000-8000-00805f9b34fb enable: true

     bluetoothGattService:  0000cb25-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  00008402-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  0000874e-0000-1000-8000-00805f9b34fb
     bluetoothGattDescriptor:  00002902-0000-1000-8000-00805f9b34fb
     setCharacteristicNotification() - uuid: 0000874e-0000-1000-8000-00805f9b34fb enable: true

     bluetoothGattService:  0000ef17-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  000007d3-0000-1000-8000-00805f9b34fb
     bluetoothGattCharacteristic:  000080b4-0000-1000-8000-00805f9b34fb
     bluetoothGattDescriptor:  00002902-0000-1000-8000-00805f9b34fb
     setCharacteristicNotification() - uuid: 000080b4-0000-1000-8000-00805f9b34fb enable: true

     bluetoothGattService:  c5d283d8-2d29-4a1b-99bb-47cf9a34670b
     bluetoothGattCharacteristic:  6e947c7e-d842-4611-9577-db61f80eddc8
     bluetoothGattCharacteristic:  9fb8a672-e511-4b23-968f-7bebe2f266c8
     bluetoothGattDescriptor:  00002902-0000-1000-8000-00805f9b34fb
     setCharacteristicNotification() - uuid: 9fb8a672-e511-4b23-968f-7bebe2f266c8 enable: true
     */

    //BLE相关 牙刷
    //BLE获取电池电量信息
    public static final int BLE_COMMAND_BATTERY=106;
    //BLE获取设备名称
    public static final int BLE_COMMAND_INFONAME=107;
    //BLE获取设备ID
    public static final int BLE_COMMAND_INFOID=108;
    //BLE获取设备版本
    public static final int BLE_COMMAND_INFOVERSION=109;
    //BLE芯片ID
    public static final int BLE_COMMAND_CPUID=113;
    //设定时间
    public static final int BLE_COMMAND_TIME=161;
    //设置惯用手
    public static final int BLE_COMMAND_HAND=168;
    //绑定牙刷
    public static final int BLE_COMMAND_BINDTEETH=241;
    //马达开关通知
    public static final int BLE_COMMAND_MOTORSWITCH=242;
    //设置userid
    public static final int BLE_COMMAND_SETUSERID=164;
    //获取最近一次刷牙记录
    public static final int BLE_COMMAND_GETCURRENTTEETHINFO=178;
    //获取刷牙记录总条数
    public static final int BLE_COMMAND_GETALLCOUNT=188;
    //Sensor数据
    public static final int BLE_COMMAND_SENSOR=245;
    //获取指定一条刷牙记录
    public static final int BLE_COMMAND_GETONEINFO=180;
    //进入游戏模式
    public static final int BLE_COMMAND_GAMESTART=183;
    //清空所有刷牙信息
    public static final int BLE_COMMAND_CLEANTEETH=185;
    //通知当前app刷牙位置
    public static final int BLE_COMMAND_SENDTEETHPOSTION=186;
    //获取uniqueid
    public static final int BLE_COMMAND_GETUID=197;
    //进入固件升级模式 172
    public static final int BLE_COMMAND_UPDATE=172;

    public static final int RESULT_ENABLE_BT = 101;

    public final static String SERVICEUUID="SERVICEUUID";
    public final static String CHARACUUID="CHARACUUID";
    public final static String COMMAND="command";
    // 扫描指令
    public final static String SCAN="SCAN";
    // 连接指令
    public final static String CONN="CONN";
    // 扫描并连接指令
    public final static String SCANCONN="SCANCONN";
    // 设备标志
    public final static String DEVICE="DEVICE";
    // 写指令
    public final static String WRITE="WRITE";
    // 读指令
    public final static String READ="READ";
    // RSSI指令
    public final static String RSSI="RSSI";
    // 断开连接指令
    public final static String DISCONN="DISCONN";
    // 字节标志
    public final static String BYTECODE="BYTECODE";
    // 进入OTA
    public final static String OTA="OTA";

    // 手环
    public static final UUID UUID_SERVICE_WristBand_Verify=UUID.fromString("0000f017-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_WristBand_UID=UUID.fromString("00004402-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_WristBand_Code=UUID.fromString("0000246f-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_SERVICE_WristBand_Data=UUID.fromString("0000606f-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_WristBand_DataWrite=UUID.fromString("0000ad1d-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_SERVICE_WristBand_SET=UUID.fromString("0000ef17-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_WristBand_SETWrite=UUID.fromString("000007d3-0000-1000-8000-00805f9b34fb");
}
