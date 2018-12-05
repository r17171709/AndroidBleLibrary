package com.renyu.androidblelibrary.utils;

import com.renyu.blelibrary.utils.BLEFramework;

import java.util.UUID;

/**
 * Created by renyu on 2017/2/4.
 */

public class DataUtils {
    public static void enterOta(BLEFramework bleFramework, UUID serviceUUID, UUID characUUID) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) 0xa7;
        bleFramework.addWriteCommand(serviceUUID, characUUID, bytes);
    }

    public static void sendData(BLEFramework bleFramework, UUID serviceUUID, UUID characUUID, byte[] bytes) {
        bleFramework.addWriteCommand(serviceUUID, characUUID, bytes);
    }

    public static void sendData(BLEFramework bleFramework, UUID serviceUUID, UUID characUUID, byte byte_) {
        bleFramework.addWriteCommand(serviceUUID, characUUID, new byte[]{byte_});
    }
}
