package com.renyu.blelibrary.impl;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

/**
 * Created by Administrator on 2018/11/20.
 */
public interface IScanAndConnRule {
    boolean rule21(ScanResult result);
    boolean rule(BluetoothDevice device);
}
