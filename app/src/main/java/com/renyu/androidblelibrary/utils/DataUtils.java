package com.renyu.androidblelibrary.utils;

import com.renyu.blelibrary.utils.BLEFramework;

import java.util.UUID;

/**
 * Created by renyu on 2017/2/4.
 */

public class DataUtils {
    public static void enterOta(UUID serviceUUID, UUID characUUID, BLEFramework bleFramework) {
        byte[] bytes=new byte[1];
        bytes[0]=(byte) 0xa7;
        bleFramework.addWriteCommand(serviceUUID, characUUID, bytes);
    }
}
