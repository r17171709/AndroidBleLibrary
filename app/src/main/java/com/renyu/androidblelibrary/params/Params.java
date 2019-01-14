package com.renyu.androidblelibrary.params;

import java.util.UUID;

/**
 * Created by renyu on 16/3/9.
 */
public class Params {
    // 设备精灵 OTA使用
    public static final UUID UUID_SERVICE_OTA = UUID.fromString("0a2be667-2416-4373-b583-1147d905e39f");
    public static final UUID UUID_SERVICE_OTA_WRITE = UUID.fromString("0000cdd2-0000-1000-8000-00805f9b34fb");

    // 牙刷
    public static final UUID UUID_SERVICE = UUID.fromString("E53A96FD-BC51-4A3A-A397-4B759661B7CF");
    public static final UUID UUID_SERVICE_WRITE = UUID.fromString("0000cdd2-0000-1000-8000-00805f9b34fb");
    //电池服务 UUID
    public static final UUID UUID_SERVICE_BATTERY = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_BATTERY_READ = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    //设备信息服务 UUID
    public static final UUID UUID_SERVICE_DEVICEINFO = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_DEVICEINFO_NAME = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_DEVICEINFO_ID = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_DEVICEINFO_VERSION = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_DEVICEINFO_CPUID = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");
    //BLE获取电池电量信息
    public static final int BLE_COMMAND_BATTERY = 106;
    //BLE获取设备名称
    public static final int BLE_COMMAND_INFONAME = 107;
    //BLE获取设备ID
    public static final int BLE_COMMAND_INFOID = 108;
    //BLE获取设备版本
    public static final int BLE_COMMAND_INFOVERSION = 109;
    //BLE芯片ID
    public static final int BLE_COMMAND_CPUID = 113;
    //设定时间
    public static final int BLE_COMMAND_TIME = 161;
    //设置惯用手
    public static final int BLE_COMMAND_HAND = 168;
    //绑定牙刷
    public static final int BLE_COMMAND_BINDTEETH = 241;
    //马达开关通知
    public static final int BLE_COMMAND_MOTORSWITCH = 242;
    //设置userid
    public static final int BLE_COMMAND_SETUSERID = 164;
    //获取最近一次刷牙记录
    public static final int BLE_COMMAND_GETCURRENTTEETHINFO = 178;
    //获取刷牙记录总条数
    public static final int BLE_COMMAND_GETALLCOUNT = 188;
    //Sensor数据
    public static final int BLE_COMMAND_SENSOR = 245;
    //获取指定一条刷牙记录
    public static final int BLE_COMMAND_GETONEINFO = 180;
    //进入游戏模式
    public static final int BLE_COMMAND_GAMESTART = 183;
    //清空所有刷牙信息
    public static final int BLE_COMMAND_CLEANTEETH = 185;
    //通知当前app刷牙位置
    public static final int BLE_COMMAND_SENDTEETHPOSTION = 186;
    //获取uniqueid
    public static final int BLE_COMMAND_GETUID = 197;
    //进入固件升级模式 172
    public static final int BLE_COMMAND_UPDATE = 172;

    // 手环
    public static final UUID UUID_SERVICE_WristBand_Verify = UUID.fromString("0000f017-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_WristBand_UID = UUID.fromString("00004402-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_WristBand_Code = UUID.fromString("0000246f-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_SERVICE_WristBand_Data = UUID.fromString("0000606f-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_WristBand_DataWrite = UUID.fromString("0000ad1d-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_SERVICE_WristBand_SpecialSet = UUID.fromString("0000ef17-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_WristBand_SpecialSetWrite = UUID.fromString("000007d3-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_SERVICE_WristBand_SET = UUID.fromString("0000565d-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_WristBand_SetWrite = UUID.fromString("0000a1c1-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_SERVICE_WristBand_READ = UUID.fromString("0000cb25-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_WristBand_READWrite = UUID.fromString("00008402-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_SERVICE_WristBand_AppNotify = UUID.fromString("0000dbc4-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_WristBand_AppNotifyWrite = UUID.fromString("00004383-0000-1000-8000-00805f9b34fb");

    /**
     * 以下为自定义部分
     */

    public static final int RESULT_ENABLE_BT = 101;

    public final static String SERVICEUUID = "SERVICEUUID";
    public final static String CHARACUUID = "CHARACUUID";
    public final static String COMMAND = "command";
    // 扫描指令
    public final static String SCAN = "SCAN";
    // 连接指令
    public final static String CONN = "CONN";
    // 扫描并连接指令
    public final static String SCANCONN = "SCANCONN";
    // 设备标志
    public final static String DEVICE = "DEVICE";
    // 写指令
    public final static String WRITE = "WRITE";
    // 读指令
    public final static String READ = "READ";
    // RSSI指令
    public final static String RSSI = "RSSI";
    // 断开连接指令
    public final static String DISCONN = "DISCONN";
    // 停止扫描
    public final static String STOPSCAN = "STOPSCAN";
    // 字节标志
    public final static String BYTECODE = "BYTECODE";
    // 进入OTA
    public final static String OTA = "OTA";
}
