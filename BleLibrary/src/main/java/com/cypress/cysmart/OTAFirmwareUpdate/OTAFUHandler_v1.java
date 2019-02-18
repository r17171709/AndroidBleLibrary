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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.cypress.cysmart.BLEConnectionServices.BluetoothLeService;
import com.cypress.cysmart.CommonUtils.CheckSumUtils;
import com.cypress.cysmart.CommonUtils.Constants;
import com.cypress.cysmart.CommonUtils.ConvertUtils;
import com.cypress.cysmart.CommonUtils.Utils;
import com.cypress.cysmart.DataModelClasses.OTAFlashRowModel_v1;
import com.cypress.cysmart.impl.IOTAListener;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OTAFUHandler_v1 extends OTAFUHandlerBase {

    //NOTE: testing shows that there is no notable advantage of using SendDataWithoutResponse vs SendData
    private static final boolean USE_SEND_DATA_WITHOUT_RESPONSE = false;//Use SendDataWithoutResponse vs SendData
    public static final int SEND_DATA_WITHOUT_RESPONSE_DELAY = 250;//This delay is allows the peripheral to complete processing of the SendDataWithoutResponse before starting processing the ProgramData

    private OTAFirmwareWrite_v1 mOtaFirmwareWrite;
    private Map<String, List<OTAFlashRowModel_v1>> mFileContents;
    private byte mCheckSumType;
    private final int mMaxDataSize;
    private IOTAListener iotaListener;

    public OTAFUHandler_v1(Activity activity, BluetoothGattCharacteristic otaCharacteristic, String filepath, IOTAListener iotaListener) {
        super(activity, otaCharacteristic, Constants.ACTIVE_APP_NO_CHANGE, Constants.NO_SECURITY_KEY, filepath);
        //Prefer WriteNoResponse over WriteWithResponse
        if ((otaCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
            this.mMaxDataSize = BootLoaderCommands_v1.WRITE_NO_RESP_MAX_DATA_SIZE;
        } else {
            this.mMaxDataSize = BootLoaderCommands_v1.WRITE_WITH_RESP_MAX_DATA_SIZE;
        }
        this.iotaListener = iotaListener;
    }

    @Override
    public void prepareFileWrite() {
        mOtaFirmwareWrite = new OTAFirmwareWrite_v1(mOtaCharacteristic);
        final CustomFileReader_v1 customFileReader = new CustomFileReader_v1(mFilepath);
        /**
         * Reads the file content and provides a 1 second delay
         */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mFileContents = customFileReader.readLines();
                    startOTA();
                } catch (CustomFileReader_v1.InvalidFileFormatException e) {
                    // TODO: 2019/1/28 0028 ota_alert_invalid_file 
                }
            }
        }, 1000);
    }

    private void startOTA() {
        // TODO: 2019/1/28 0028 ota_file_read_complete 
        Utils.setStringSharedPreference(getActivity(), Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands_v1.ENTER_BOOTLOADER);

        OTAFlashRowModel_v1.Header headerRow = (OTAFlashRowModel_v1.Header) mFileContents.get(CustomFileReader_v1.KEY_HEADER).get(0);
        this.mCheckSumType = headerRow.mCheckSumType;
        this.mActiveApp = headerRow.mAppId;

        //Send Enter Bootloader command
        mOtaFirmwareWrite.OTAEnterBootLoaderCmd(mCheckSumType, headerRow.mProductId);
        // TODO: 2019/1/28 0028 ota_enter_bootloader 
    }

    @Override
    public void processOTAStatus(String status, Bundle extras) {
//        Log.d("OTA", status);
        iotaListener.updateStatus(status);
        if (extras.containsKey(Constants.EXTRA_ERROR_OTA)) {
            String errorMessage = extras.getString(Constants.EXTRA_ERROR_OTA);
            // TODO: 2019/1/28 0028 alert_message_ota_error 
        } else if (status.equalsIgnoreCase("" + BootLoaderCommands_v1.ENTER_BOOTLOADER)) {
            if (extras.containsKey(Constants.EXTRA_SILICON_ID) && extras.containsKey(Constants.EXTRA_SILICON_REV)) {
                OTAFlashRowModel_v1.Header headerRow = (OTAFlashRowModel_v1.Header) mFileContents.get(CustomFileReader_v1.KEY_HEADER).get(0);
                byte[] siliconIdReceived = extras.getByteArray(Constants.EXTRA_SILICON_ID);
                byte siliconRevReceived = extras.getByte(Constants.EXTRA_SILICON_REV);

                if (Arrays.equals(headerRow.mSiliconId, siliconIdReceived) && headerRow.mSiliconRev == siliconRevReceived) {
                    //Send Set Application Metadata command
                    OTAFlashRowModel_v1.AppInfo appInfoRow = (OTAFlashRowModel_v1.AppInfo) mFileContents.get(CustomFileReader_v1.KEY_APPINFO).get(0);

                    mOtaFirmwareWrite.OTASetAppMetadataCmd(mCheckSumType, mActiveApp, appInfoRow.mAppStart, appInfoRow.mAppSize);
                    Utils.setStringSharedPreference(getActivity(), Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands_v1.SET_APP_METADATA);
                    // TODO: 2019/1/28 0028 ota_set_application_metadata 
                } else {
                    //Wrong SiliconId and SiliconRev
                    // TODO: 2019/1/28 0028 alert_message_silicon_id_error 
                }
            }
        } else if (status.equalsIgnoreCase("" + BootLoaderCommands_v1.SET_APP_METADATA)) {
            List<OTAFlashRowModel_v1> dataRows = mFileContents.get(CustomFileReader_v1.KEY_DATA);
            int rowNum = Utils.getIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_NO);
            OTAFlashRowModel_v1 dataRow = dataRows.get(rowNum);
            if (dataRow instanceof OTAFlashRowModel_v1.EIV) {
                writeEiv(rowNum);//Set EIV
            } else if (dataRow instanceof OTAFlashRowModel_v1.Data) {
                writeData(rowNum);//Program data row
            }
        } else if (status.equalsIgnoreCase("" + BootLoaderCommands_v1.SET_EIV)) {
            int rowNum = Utils.getIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_NO);
            rowNum++;//Increment row number
            List<OTAFlashRowModel_v1> dataRows = mFileContents.get(CustomFileReader_v1.KEY_DATA);
            //Update progress bar
            int totalLines = dataRows.size();
            showProgress(rowNum, totalLines);
            iotaListener.updateProgress(rowNum * 100 / totalLines);
            if (rowNum < totalLines) {//Process next row
                Utils.setIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_NO, rowNum);
                Utils.setIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_START_POS, 0);
                OTAFlashRowModel_v1 dataRow = dataRows.get(rowNum);
                if (dataRow instanceof OTAFlashRowModel_v1.EIV) {
                    writeEiv(rowNum);//Set EIV
                } else if (dataRow instanceof OTAFlashRowModel_v1.Data) {
                    writeData(rowNum);//Program data row
                }
            }
            if (rowNum == totalLines) {//All rows have been processed
                Utils.setIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_NO, 0);
                Utils.setIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_START_POS, 0);
                //Programming done, send VerifyApplication command
                mOtaFirmwareWrite.OTAVerifyAppCmd(mCheckSumType, mActiveApp);
                Utils.setStringSharedPreference(getActivity(), Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands_v1.VERIFY_APP);
                // TODO: 2019/1/28 0028 ota_verify_application 
            }
        } else if (status.equalsIgnoreCase("" + BootLoaderCommands_v1.SEND_DATA)) {
            int rowNum = Utils.getIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_NO);
            writeData(rowNum);//Program data row
        } else if (status.equalsIgnoreCase("" + BootLoaderCommands_v1.SEND_DATA_WITHOUT_RESPONSE)) {
            int rowNum = Utils.getIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_NO);
            writeData(rowNum);//Program data row
        } else if (status.equalsIgnoreCase("" + BootLoaderCommands_v1.PROGRAM_DATA)) {
            int rowNum = Utils.getIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_NO);
            rowNum++;//Increment row number
            List<OTAFlashRowModel_v1> dataRows = mFileContents.get(CustomFileReader_v1.KEY_DATA);
            //Update progress bar
            int totalLines = dataRows.size();
            showProgress(rowNum, totalLines);
            iotaListener.updateProgress(rowNum * 100 / totalLines);
            if (rowNum < totalLines) {//Process next row
                Utils.setIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_NO, rowNum);
                Utils.setIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_START_POS, 0);
                OTAFlashRowModel_v1 dataRow = dataRows.get(rowNum);
                if (dataRow instanceof OTAFlashRowModel_v1.EIV) {
                    writeEiv(rowNum);//Set EIV
                } else if (dataRow instanceof OTAFlashRowModel_v1.Data) {
                    writeData(rowNum);//Program data row
                }
            }
            if (rowNum == totalLines) {//All rows have been processed
                Utils.setIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_NO, 0);
                Utils.setIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_START_POS, 0);
                //Programming done, send VerifyApplication command
                mOtaFirmwareWrite.OTAVerifyAppCmd(mCheckSumType, mActiveApp);
                Utils.setStringSharedPreference(getActivity(), Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands_v1.VERIFY_APP);
                // TODO: 2019/1/28 0028 ota_verify_application 
            }
        } else if (status.equalsIgnoreCase("" + BootLoaderCommands_v1.VERIFY_APP)) {
            if (extras.containsKey(Constants.EXTRA_VERIFY_APP_STATUS)) {
                byte statusReceived = extras.getByte(Constants.EXTRA_VERIFY_APP_STATUS);
                if (statusReceived == 1) {
                    //Send ExitBootloader command
                    mOtaFirmwareWrite.OTAExitBootloaderCmd(mCheckSumType);
                    Utils.setStringSharedPreference(getActivity(), Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands_v1.EXIT_BOOTLOADER);
                    // TODO: 2019/1/28 0028 ota_end_bootloader 
                } else {
                    // TODO: 2019/1/28 0028 alert_message_verify_application_error 
                }
            }
        } else if (status.equalsIgnoreCase("" + BootLoaderCommands_v1.EXIT_BOOTLOADER)) {
            // TODO: 2019/1/28 0028 ota_end_success 
            // TODO: 2019/1/28 0028 alert_message_bluetooth_disconnect
        }
    }

    private void writeData(int rowNum) {
        int startPosition = Utils.getIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_START_POS);
        OTAFlashRowModel_v1.Data dataRow = (OTAFlashRowModel_v1.Data) mFileContents.get(CustomFileReader_v1.KEY_DATA).get(rowNum);

        int payloadLength = dataRow.mData.length - startPosition;
        boolean isLastPacket = payloadLength <= mMaxDataSize;
        if (!isLastPacket) {
            payloadLength = mMaxDataSize;
        }
        byte[] payload = new byte[payloadLength];
        for (int i = 0; i < payloadLength; i++) {
            byte b = dataRow.mData[startPosition];
            payload[i] = b;
            startPosition++;
        }
        if (!isLastPacket) {
            if (USE_SEND_DATA_WITHOUT_RESPONSE) {
                //Send SendDataWithoutResponse command
                mOtaFirmwareWrite.OTASendDataWithoutResponseCmd(mCheckSumType, payload);
                Utils.setStringSharedPreference(getActivity(), Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands_v1.SEND_DATA_WITHOUT_RESPONSE);
                Utils.setIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_START_POS, startPosition);
                // TODO: 2019/1/28 0028 ota_program_row 
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //The following line is necessary as there is no response for SendDataWithoutResponse command
                        Intent intent = new Intent(BluetoothLeService.ACTION_OTA_STATUS_V1);
                        Bundle bundle = new Bundle();
                        intent.putExtras(bundle);
                        getActivity().sendBroadcast(intent);
                    }
                }, SEND_DATA_WITHOUT_RESPONSE_DELAY);
            } else {
                //Send SendData command
                mOtaFirmwareWrite.OTASendDataCmd(mCheckSumType, payload);
                Utils.setStringSharedPreference(getActivity(), Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands_v1.SEND_DATA);
                Utils.setIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_START_POS, startPosition);
                // TODO: 2019/1/28 0028 ota_program_row 
            }
        } else {
            //Send ProgramData command
            long crc32 = CheckSumUtils.crc32(dataRow.mData, dataRow.mData.length);
            byte[] baCrc32 = ConvertUtils.intToByteArray((int) crc32);
            mOtaFirmwareWrite.OTAProgramDataCmd(mCheckSumType, dataRow.mAddress, baCrc32, payload);
            Utils.setStringSharedPreference(getActivity(), Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands_v1.PROGRAM_DATA);
            Utils.setIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_START_POS, 0);
            // TODO: 2019/1/28 0028 ota_program_row 
        }
    }

    private void writeEiv(int rowNum) {
        OTAFlashRowModel_v1.EIV eivRow = (OTAFlashRowModel_v1.EIV) mFileContents.get(CustomFileReader_v1.KEY_DATA).get(rowNum);
        //Send SetEiv command
        mOtaFirmwareWrite.OTASetEivCmd(mCheckSumType, eivRow.mEiv);
        Utils.setStringSharedPreference(getActivity(), Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands_v1.SET_EIV);
        Utils.setIntSharedPreference(getActivity(), Constants.PREF_PROGRAM_ROW_START_POS, 0);
        // TODO: 2019/1/28 0028 ota_set_eiv
    }
}
