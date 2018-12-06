package com.renyu.blelibrary.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

/**
 * Created by renyu on 16/2/29.
 */
public class Utils {
    /**
     * 判断手机是否支持BLE
     *
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
        if (adapter == null) {
            Toast.makeText(context, "该手机不支持BLE", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * 判断蓝牙是否开启
     *
     * @param context
     * @return
     */
    public static boolean checkBluetoothOpen(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter.isEnabled();
    }
}
