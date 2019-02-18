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

package com.cypress.cysmart.OTAFirmwareUpdate;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;

public abstract class OTAFUHandlerBase implements OTAFUHandler {
    private Activity activity;
    final BluetoothGattCharacteristic mOtaCharacteristic;
    final String mFilepath;
    boolean mPrepareFileWriteEnabled = true;
    byte mActiveApp; //Dual-App Bootloader Active Application ID
    long mSecurityKey;

    OTAFUHandlerBase(Activity activity, BluetoothGattCharacteristic otaCharacteristic, byte activeApp, long securityKey, String filepath) {
        this.activity = activity;
        this.mOtaCharacteristic = otaCharacteristic;
        this.mActiveApp = activeApp;
        this.mSecurityKey = securityKey;
        this.mFilepath = filepath;
    }

    @Override
    public void setPrepareFileWriteEnabled(boolean enabled) {
        this.mPrepareFileWriteEnabled = enabled;
    }

    Activity getActivity() {
        return activity;
    }

    /**
     * Method to showToast progress bar
     */
    void showProgress(float fileLineNos, float totalLines) {
        // TODO: 2019/1/28 0028 进度
//        Log.d("OTA", ""+(int) ((fileLineNos / totalLines) * 100) + "%");
    }
}
