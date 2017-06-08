package com.renyu.blelibrary.impl;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by renyu on 2017/6/8.
 */

public abstract class BLEScanCallBack implements BluetoothAdapter.LeScanCallback {

    String scanName;

    public BLEScanCallBack(String scanName) {
        this.scanName=scanName;
    }
}
