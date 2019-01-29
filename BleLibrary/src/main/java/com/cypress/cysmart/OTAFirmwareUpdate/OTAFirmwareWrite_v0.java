/*
 * Copyright Cypress Semiconductor Corporation, 2014-2018 All rights reserved.
 *
 * This software, associated documentation and materials ("Software") is
 * owned by Cypress Semiconductor Corporation ("Cypress") and is
 * protected by and subject to worldwide patent protection (UnitedStates and foreign),
 * United States copyright laws and international
 * treaty provisions. Therefore, unless otherwise specified in a separate
 * license agreement between you and Cypress, this Software
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

import android.bluetooth.BluetoothGattCharacteristic;

import com.cypress.cysmart.BLEConnectionServices.BluetoothLeService;
import com.cypress.cysmart.CommonUtils.CheckSumUtils;
import com.cypress.cysmart.CommonUtils.Logger;
import com.cypress.cysmart.DataModelClasses.OTAFlashRowModel_v0;
import com.renyu.blelibrary.utils.BLEFramework;

/**
 * Separate class for handling the write operation during OTA firmware upgrade
 */
public class OTAFirmwareWrite_v0 {
    private static final int RADIX_HEX = 16;
    private static final int NON_CHECKSUMMABLE_SIZE = 3;
    private BluetoothGattCharacteristic mOtaCharacteristic;

    public OTAFirmwareWrite_v0(BluetoothGattCharacteristic otaCharacteristic) {
        this.mOtaCharacteristic = otaCharacteristic;
    }

    // securityKey comes in little endian format
    public void OTAEnterBootLoaderCmd(String checkSumType, byte[] securityKey) {
        int idx = 0;
        int dataLength = 0;
        if (securityKey != null) {
            dataLength += securityKey.length;
        }
        final int commandSize = BootLoaderCommands_v0.BASE_CMD_SIZE + dataLength;
        final int checksummableDataSize = commandSize - NON_CHECKSUMMABLE_SIZE;

        byte[] commandBytes = new byte[commandSize];
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_START;
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.ENTER_BOOTLOADER;
        commandBytes[idx++] = (byte) dataLength;
        commandBytes[idx++] = (byte) (dataLength >> 8);
        if (securityKey != null) {
            // Sending in big endian format
            for (int i = securityKey.length - 1; i >= 0; --i) {
                commandBytes[idx++] = securityKey[i];
            }
        }
        int checkSum = CheckSumUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX_HEX), commandBytes, checksummableDataSize);
        commandBytes[idx++] = (byte) checkSum;
        commandBytes[idx++] = (byte) (checkSum >> 8);
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_END;

        Logger.e("OTAEnterBootLoaderCmd");
        BLEFramework.getBleFrameworkInstance().writeOTABootLoaderCommand(mOtaCharacteristic, commandBytes);
    }

    public void OTAGetAppStatusCmd(String checkSumType, byte appId) {
        int idx = 0;
        int dataLength = 1;
        final int commandSize = BootLoaderCommands_v0.BASE_CMD_SIZE + dataLength;
        final int checksummableDataSize = commandSize - NON_CHECKSUMMABLE_SIZE;

        byte[] commandBytes = new byte[commandSize];
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_START;
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.GET_APP_STATUS;
        commandBytes[idx++] = (byte) dataLength;
        commandBytes[idx++] = (byte) (dataLength >> 8);
        commandBytes[idx++] = appId;
        int checkSum = CheckSumUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX_HEX), commandBytes, checksummableDataSize);
        commandBytes[idx++] = (byte) checkSum;
        commandBytes[idx++] = (byte) (checkSum >> 8);
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_END;

        Logger.e("OTAGetAppStatusCmd");
        BLEFramework.getBleFrameworkInstance().writeOTABootLoaderCommand(mOtaCharacteristic, commandBytes);
    }

    public void OTAGetFlashSizeCmd(String checkSumType, byte[] data) {
        int idx = 0;
        final int commandSize = BootLoaderCommands_v0.BASE_CMD_SIZE + data.length;
        final int checksummableDataSize = commandSize - NON_CHECKSUMMABLE_SIZE;

        byte[] commandBytes = new byte[commandSize];
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_START;
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.GET_FLASH_SIZE;
        commandBytes[idx++] = (byte) data.length;
        commandBytes[idx++] = (byte) (data.length >> 8);
        for (int i = 0; i < data.length; ++i) {
            commandBytes[idx++] = data[i];
        }
        int checkSum = CheckSumUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX_HEX), commandBytes, checksummableDataSize);
        commandBytes[idx++] = (byte) checkSum;
        commandBytes[idx++] = (byte) (checkSum >> 8);
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_END;

        Logger.e("OTAGetFlashSizeCmd");
        BLEFramework.getBleFrameworkInstance().writeOTABootLoaderCommand(mOtaCharacteristic, commandBytes);
    }

    public void OTASendDataCmd(String checksumType, byte[] data) {
        int idx = 0;
        final int commandSize = BootLoaderCommands_v0.BASE_CMD_SIZE + data.length;
        final int checksummableDataSize = commandSize - NON_CHECKSUMMABLE_SIZE;

        byte[] commandBytes = new byte[commandSize];
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_START;
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.SEND_DATA;
        commandBytes[idx++] = (byte) (data.length);
        commandBytes[idx++] = (byte) (data.length >> 8);
        for (int i = 0; i < data.length; i++) {
            commandBytes[idx++] = data[i];
        }
        int checkSum = CheckSumUtils.calculateCheckSum2(Integer.parseInt(checksumType, RADIX_HEX), commandBytes, checksummableDataSize);
        commandBytes[idx++] = (byte) checkSum;
        commandBytes[idx++] = (byte) (checkSum >> 8);
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_END;

        Logger.e("OTASendDataCmd send size--->" + commandBytes.length);
        BLEFramework.getBleFrameworkInstance().writeOTABootLoaderCommand(mOtaCharacteristic, commandBytes);
    }

    public void OTAProgramRowCmd(String checkSumType, long rowMSB, long rowLSB, int arrayID, byte[] data) {
        int idx = 0;
        final int dataLength = 3 + data.length;
        final int commandSize = BootLoaderCommands_v0.BASE_CMD_SIZE + dataLength;
        final int checksummableDataSize = commandSize - NON_CHECKSUMMABLE_SIZE;

        byte[] commandBytes = new byte[commandSize];
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_START;
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PROGRAM_ROW;
        commandBytes[idx++] = (byte) dataLength;
        commandBytes[idx++] = (byte) (dataLength >> 8);
        commandBytes[idx++] = (byte) arrayID;
        commandBytes[idx++] = (byte) rowMSB;
        commandBytes[idx++] = (byte) rowLSB;
        for (int i = 0; i < data.length; i++) {
            commandBytes[idx++] = data[i];
        }
        int checkSum = CheckSumUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX_HEX), commandBytes, checksummableDataSize);
        commandBytes[idx++] = (byte) checkSum;
        commandBytes[idx++] = (byte) (checkSum >> 8);
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_END;

        Logger.e("OTAProgramRowCmd send size--->" + commandBytes.length);
        BLEFramework.getBleFrameworkInstance().writeOTABootLoaderCommand(mOtaCharacteristic, commandBytes);
    }

    public void OTAVerifyRowCmd(String checkSumType, long rowMSB, long rowLSB, OTAFlashRowModel_v0 model) {
        int idx = 0;
        final int dataLength = 3;
        final int commandSize = BootLoaderCommands_v0.BASE_CMD_SIZE + dataLength;
        final int checksummableDataSize = commandSize - NON_CHECKSUMMABLE_SIZE;

        byte[] commandBytes = new byte[commandSize];
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_START;
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.VERIFY_ROW;
        commandBytes[idx++] = (byte) (dataLength);
        commandBytes[idx++] = (byte) (dataLength >> 8);
        commandBytes[idx++] = (byte) model.mArrayId;
        commandBytes[idx++] = (byte) rowMSB;
        commandBytes[idx++] = (byte) rowLSB;
        int checkSum = CheckSumUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX_HEX), commandBytes, checksummableDataSize);
        commandBytes[idx++] = (byte) checkSum;
        commandBytes[idx++] = (byte) (checkSum >> 8);
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_END;

        Logger.e("OTAVerifyRowCmd");
        BLEFramework.getBleFrameworkInstance().writeOTABootLoaderCommand(mOtaCharacteristic, commandBytes);
    }

    public void OTAVerifyCheckSumCmd(String checkSumType) {
        int idx = 0;
        final int dataLength = 0;
        final int commandSize = BootLoaderCommands_v0.BASE_CMD_SIZE + dataLength;
        final int checksummableDataSize = commandSize - NON_CHECKSUMMABLE_SIZE;

        byte[] commandBytes = new byte[commandSize];
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_START;
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.VERIFY_CHECK_SUM;
        commandBytes[idx++] = (byte) (dataLength);
        commandBytes[idx++] = (byte) (dataLength >> 8);
        int checkSum = CheckSumUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX_HEX), commandBytes, checksummableDataSize);
        commandBytes[idx++] = (byte) checkSum;
        commandBytes[idx++] = (byte) (checkSum >> 8);
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_END;

        Logger.e("OTAVerifyCheckSumCmd");
        BLEFramework.getBleFrameworkInstance().writeOTABootLoaderCommand(mOtaCharacteristic, commandBytes);
    }

    public void OTASetActiveAppCmd(String checkSumType, byte appId) {
        int idx = 0;
        final int dataLength = 1;
        final int commandSize = BootLoaderCommands_v0.BASE_CMD_SIZE + dataLength;
        final int checksummableDataSize = commandSize - NON_CHECKSUMMABLE_SIZE;

        byte[] commandBytes = new byte[commandSize];
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_START;
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.SET_ACTIVE_APP;
        commandBytes[idx++] = (byte) (dataLength);
        commandBytes[idx++] = (byte) (dataLength >> 8);
        commandBytes[idx++] = appId;
        int checkSum = CheckSumUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX_HEX), commandBytes, checksummableDataSize);
        commandBytes[idx++] = (byte) checkSum;
        commandBytes[idx++] = (byte) (checkSum >> 8);
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_END;

        Logger.e("OTASetActiveAppCmd");
        BLEFramework.getBleFrameworkInstance().writeOTABootLoaderCommand(mOtaCharacteristic, commandBytes);
    }

    public void OTAExitBootloaderCmd(String checkSumType) {
        int idx = 0;
        final int dataLength = 0;
        final int commandSize = BootLoaderCommands_v0.BASE_CMD_SIZE + dataLength;
        final int checksummableDataSize = commandSize - NON_CHECKSUMMABLE_SIZE;

        byte[] commandBytes = new byte[commandSize];
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_START;
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.EXIT_BOOTLOADER;
        commandBytes[idx++] = (byte) dataLength;
        commandBytes[idx++] = (byte) (dataLength >> 8);
        int checkSum = CheckSumUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX_HEX), commandBytes, checksummableDataSize);
        commandBytes[idx++] = (byte) checkSum;
        commandBytes[idx++] = (byte) (checkSum >> 8);
        commandBytes[idx++] = (byte) BootLoaderCommands_v0.PACKET_END;

        Logger.e("OTAExitBootloaderCmd");
        BLEFramework.getBleFrameworkInstance().writeOTABootLoaderCommand(mOtaCharacteristic, commandBytes, true);
    }
}
