/*
 * Copyright Cypress Semiconductor Corporation, 2014-2018 All rights reserved.
 *
 * This software, associated documentation and materials ("Software") is
 * owned by Cypress Semiconductor Corporation ("Cypress") and is
 * protected by and subject to worldwide patent protection (UnitedStates and foreign), United States copyright laws and international
 * treaty provisions. Therefore, unless otherwise specified in a separate license agreement between you and Cypress, this Software
 * must be treated like any other copyrighted material. Reproduction,
 * modification, translation, compilation, or representation of this
 * Software in any other form (e.g., paper, magnetic, optical, silicon)
 * is prohibited without Cypress's express written permission.
 *
 * Disclaimer: THIS SOFTWARE IS PROVIDED AS-IS, WITH NO WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * NONINFRINGEMENT, IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE. Cypress reserves the right to make changes
 * to the Software without notice. Cypress does not assume any liability
 * arising out of the application or use of Software or any product or
 * circuit described in the Software. Cypress does not authorize its
 * products for use as critical components in any products where a
 * malfunction or failure may reasonably be expected to result in
 * significant injury or death ("High Risk Product"). By including
 * Cypress's product in a High Risk Product, the manufacturer of such
 * system or application assumes all risk of such use and in doing so
 * indemnifies Cypress against all liability.
 *
 * Use of this Software may be limited by and subject to the applicable
 * Cypress software license agreement.
 *
 *
 */

package com.renyu.androidblelibrary.activity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.cypress.cysmart.BLEConnectionServices.BluetoothLeService;
import com.cypress.cysmart.CommonUtils.Constants;
import com.cypress.cysmart.CommonUtils.Logger;
import com.cypress.cysmart.CommonUtils.Utils;
import com.cypress.cysmart.OTAFirmwareUpdate.OTAFUHandler;
import com.cypress.cysmart.OTAFirmwareUpdate.OTAFUHandler_v0;
import com.cypress.cysmart.OTAFirmwareUpdate.OTAFUHandler_v1;
import com.renyu.blelibrary.utils.BLEFramework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * OTA update fragment
 */
public class OTAActivity extends AppCompatActivity {
    public static final String REGEX_MATCHES_CYACD2 = "(?i).*\\.cyacd2$";

    private static OTAFUHandler DUMMY_HANDLER = (OTAFUHandler) Proxy.newProxyInstance(OTAActivity.class.getClassLoader(), new Class<?>[]{OTAFUHandler.class}, new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            try {
                new RuntimeException().fillInStackTrace().printStackTrace(pw);
            } finally {
                pw.close();//this will close StringWriter as well
            }
            Logger.e("DUMMY_HANDLER: " + sw);//This is for developer to track the issue
            return null;
        }
    });

    private OTAFUHandler mOTAFUHandler = DUMMY_HANDLER;//Initializing to DUMMY_HANDLER to avoid NPEs

    private BroadcastReceiver mGattOTAStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (this) {
                processOTAStatus(intent);
            }
        }
    };

    private void processOTAStatus(Intent intent) {
        /**
         * Shared preference to hold the state of the bootloader
         */
        final String bootloaderState = Utils.getStringSharedPreference(this, Constants.PREF_BOOTLOADER_STATE);
        final String action = intent.getAction();
        Bundle extras = intent.getExtras();
        if (BluetoothLeService.ACTION_OTA_STATUS.equals(action)) {
            mOTAFUHandler.processOTAStatus(bootloaderState, extras);
        } else if (BluetoothLeService.ACTION_OTA_STATUS_V1.equals(action)) {
            mOTAFUHandler.processOTAStatus(bootloaderState, extras);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String currentFilePath = "/storage/emulated/0/CySmart/Wristhand Demo 1.cyacd2";
        mOTAFUHandler = createOTAFUHandler(BLEFramework.getBleFrameworkInstance().getOTACharacteristic(), (byte) -1, -1, currentFilePath);
        try {
            if (BLEFramework.getBleFrameworkInstance().getOTACharacteristic() != null) {
                mOTAFUHandler.prepareFileWrite();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mGattOTAStatusReceiver, Utils.makeGattUpdateIntentFilter());
    }

    @Override
    public void onDestroy() {
        if (mOTAFUHandler != DUMMY_HANDLER) {
            mOTAFUHandler.setPrepareFileWriteEnabled(false);//This is expected case. onDestroy might be invoked before the file to upgrade is selected.
        }
        unregisterReceiver(mGattOTAStatusReceiver);
        super.onDestroy();
    }

    @Nullable
    private OTAFUHandler createOTAFUHandler(BluetoothGattCharacteristic otaCharacteristic, byte activeApp, long securityKey, String filepath) {
        boolean isCyacd2File = filepath != null && isCyacd2File(filepath);
        Utils.setBooleanSharedPreference(this, Constants.PREF_IS_CYACD2_FILE, isCyacd2File);
        OTAFUHandler handler = DUMMY_HANDLER;
        if (otaCharacteristic != null && filepath != null && filepath != "") {
            handler = isCyacd2File
                    ? new OTAFUHandler_v1(this, otaCharacteristic, filepath)
                    : new OTAFUHandler_v0(this, otaCharacteristic, activeApp, securityKey, filepath);
        }
        return handler;
    }

    private boolean isCyacd2File(String file) {
        return file.matches(REGEX_MATCHES_CYACD2);
    }
}
