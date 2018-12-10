package com.renyu.blelibrary.impl;

import com.renyu.blelibrary.bean.BLEDevice;

import java.util.ArrayList;

/**
 * Created by renyu on 2017/1/12.
 */

public interface BLEScanCallBackListener {
    void getAllScanDevice(BLEDevice bleDevice);
}
