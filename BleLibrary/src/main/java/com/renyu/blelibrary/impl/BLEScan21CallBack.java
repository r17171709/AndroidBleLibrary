package com.renyu.blelibrary.impl;

import android.bluetooth.le.ScanCallback;

/**
 * Created by Administrator on 2017/6/13.
 */

public class BLEScan21CallBack extends ScanCallback {

    String scanName;

    public BLEScan21CallBack(String scanName) {
        this.scanName=scanName;
    }
}
