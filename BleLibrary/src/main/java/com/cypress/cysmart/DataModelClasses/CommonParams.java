package com.cypress.cysmart.DataModelClasses;

import java.util.UUID;

/**
 * Created by renyu on 2017/1/12.
 */

public class CommonParams {
    //ota UUID
    public static final UUID UUID_SERVICE_OTASERVICE=UUID.fromString("00060000-F8CE-11E4-ABF4-0002A5D5C51B");
    public static final UUID UUID_SERVICE_OTA=UUID.fromString("00060001-F8CE-11E4-ABF4-0002A5D5C51B");
    public static final UUID UUID_DESCRIPTOR_OTA=UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    /**
     * GATT Status constants
     */
    public final static String ACTION_OTA_DATA_AVAILABLE =
            "com.cysmart.bluetooth.le.ACTION_OTA_DATA_AVAILABLE";
    public static boolean mFileupgradeStarted=false;
}
