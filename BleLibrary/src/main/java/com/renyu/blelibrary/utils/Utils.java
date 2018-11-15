package com.renyu.blelibrary.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by renyu on 16/2/29.
 */
public class Utils {
    /**
     * 判断手机是否支持BLE
     * @param context
     * @return
     */
    public static boolean checkBluetoothAvaliable(Context context) {
        // 判断是否支持BLE
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, "该手机不支持BLE", Toast.LENGTH_LONG).show();
            return false;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        if (adapter==null) {
            Toast.makeText(context, "该手机不支持BLE", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * 判断蓝牙是否开启
     * @param context
     * @return
     */
    public static boolean checkBluetoothOpen(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter.isEnabled();
    }

    /**
     * 有符号转无符号
     * @param bytes
     */
    public static String sign2nosign(byte bytes) {
        int result = bytes&0xff;
//        System.out.println("无符号数: \t"+result);
//        System.out.println("2进制bit位: \t"+Integer.toBinaryString(result));
        return ""+Integer.toBinaryString(result);
    }

    /**
     * 二进制转十进制
     * @param hex
     * @return
     */
    public static int convert2To10(String hex) {
        return Integer.valueOf(hex, 2);
    }

    /**
     * 十进制转十六进制
     * @param i
     * @return
     */
    public static String convert10To16(int i) {
        return Integer.toHexString(i);
    }
}
