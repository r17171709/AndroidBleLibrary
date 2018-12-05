package com.cypress.cysmart.OTAFirmwareUpdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.cypress.cysmart.CommonUtils.Constants;
import com.cypress.cysmart.CommonUtils.Utils;
import com.cypress.cysmart.DataModelClasses.OTAParams;

public class OTAResponseReceiver extends BroadcastReceiver {
    private static final int CASE_ABORT = 15;
    private static final int CASE_BTLDR = 11;
    private static final int CASE_ERR_ACTIVE = 13;
    private static final int CASE_ERR_APP = 12;
    private static final int CASE_ERR_ARRAY = 9;
    private static final int CASE_ERR_CHECKSUM = 8;
    private static final int CASE_ERR_CMD = 5;
    private static final int CASE_ERR_DATA = 4;
    private static final int CASE_ERR_DEVICE = 6;
    private static final int CASE_ERR_EOF = 2;
    private static final int CASE_ERR_FILE = 1;
    private static final int CASE_ERR_LENGTH = 3;
    private static final int CASE_ERR_ROW = 10;
    private static final int CASE_ERR_UNK = 14;
    private static final int CASE_ERR_VERSION = 7;
    private static final int CASE_SUCCESS = 0;
    private static final int CHECKSUM_END = 6;
    private static final int CHECKSUM_START = 4;
    private static final String CYRET_ABORT = "CYRET_ABORT";
    private static final String CYRET_BTLDR = "CYRET_BTLDR";
    private static final String CYRET_ERR_ACTIVE = "CYRET_ERR_ACTIVE";
    private static final String CYRET_ERR_APP = "CYRET_ERR_APP";
    private static final String CYRET_ERR_ARRAY = "CYRET_ERR_ARRAY";
    private static final String CYRET_ERR_CHECKSUM = "CYRET_ERR_CHECKSUM";
    private static final String CYRET_ERR_CMD = "CYRET_ERR_CMD";
    private static final String CYRET_ERR_DATA = "CYRET_ERR_DATA";
    private static final String CYRET_ERR_DEVICE = "CYRET_ERR_DEVICE";
    private static final String CYRET_ERR_EOF = "CYRET_ERR_EOF";
    private static final String CYRET_ERR_FILE = "CYRET_ERR_FILE";
    private static final String CYRET_ERR_LENGTH = "CYRET_ERR_LENGTH";
    private static final String CYRET_ERR_ROW = "CYRET_ERR_ROW";
    private static final String CYRET_ERR_UNK = "CYRET_ERR_UNK";
    private static final String CYRET_ERR_VERSION = "CYRET_ERR_VERSION";
    private static final int DATA_END = 10;
    private static final int DATA_START = 8;
    private static final int END_ROW_END = 16;
    private static final int END_ROW_START = 12;
    private static final int RADIX = 16;
    private static final int RESPONSE_END = 4;
    private static final int RESPONSE_START = 2;
    private static final int SILICON_ID_END = 16;
    private static final int SILICON_ID_START = 8;
    private static final int SILICON_REV_END = 18;
    private static final int SILICON_REV_START = 16;
    private static final int START_ROW_END = 12;
    private static final int START_ROW_START = 8;
    private static final int STATUS_END = 6;
    private static final int STATUS_START = 4;
    private Context mContext;

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        this.mContext = context;
        if (OTAParams.ACTION_OTA_DATA_AVAILABLE.equals(action)) {
            String hexValue = Utils.ByteArraytoHex(intent.getByteArrayExtra(Constants.EXTRA_BYTE_VALUE));
            if (Utils.getStringSharedPreference(this.mContext, Constants.PREF_BOOTLOADER_STATE).equalsIgnoreCase("56")) {
                parseEnterBootLoaderAcknowledgement(hexValue);
            } else if (Utils.getStringSharedPreference(this.mContext, Constants.PREF_BOOTLOADER_STATE).equalsIgnoreCase("50")) {
                parseGetFlashSizeAcknowledgement(hexValue);
            } else if (Utils.getStringSharedPreference(this.mContext, Constants.PREF_BOOTLOADER_STATE).equalsIgnoreCase("55")) {
                parseParseSendDataAcknowledgement(hexValue);
            } else if (Utils.getStringSharedPreference(this.mContext, Constants.PREF_BOOTLOADER_STATE).equalsIgnoreCase("57")) {
                parseParseRowAcknowledgement(hexValue);
            } else if (Utils.getStringSharedPreference(this.mContext, Constants.PREF_BOOTLOADER_STATE).equalsIgnoreCase("58")) {
                parseVerifyRowAcknowledgement(hexValue);
            } else if (Utils.getStringSharedPreference(this.mContext, Constants.PREF_BOOTLOADER_STATE).equalsIgnoreCase("49")) {
                parseVerifyCheckSum(hexValue);
            } else if (Utils.getStringSharedPreference(this.mContext, Constants.PREF_BOOTLOADER_STATE).equalsIgnoreCase("59")) {
                parseExitBootloader(hexValue);
            } else {
                Log.i("OTAResponseReceiver", "In Receiver No case " + Utils.getStringSharedPreference(this.mContext, Constants.PREF_BOOTLOADER_STATE));
            }
        }
    }

    private void parseParseSendDataAcknowledgement(String hexValue) {
        String result = hexValue.trim().replace(" ", "");
        String response = result.substring(RESPONSE_START, STATUS_START);
        String status = result.substring(STATUS_START, STATUS_END);
        int reponseBytes = Integer.parseInt(response, SILICON_REV_START);
        switch (reponseBytes) {
            case CASE_SUCCESS /*0*/:
                Log.i("OTAResponseReceiver", "CYRET_SUCCESS");
                Intent intent = new Intent(BootLoaderUtils.ACTION_OTA_STATUS);
                Bundle mBundle = new Bundle();
                mBundle.putString(Constants.EXTRA_SEND_DATA_ROW_STATUS, status);
                intent.putExtras(mBundle);
                this.mContext.sendBroadcast(intent);
            default:
                broadCastErrors(reponseBytes);
                Log.i("OTAResponseReceiver", "CYRET ERROR");
        }
    }

    private void parseEnterBootLoaderAcknowledgement(String parse) {
        String result = parse.trim().replace(" ", "");
        String response = result.substring(RESPONSE_START, STATUS_START);
        Log.e("OTAResponseReceiver", "Response>>>>>" + result);
        int reponseBytes = Integer.parseInt(response, SILICON_REV_START);
        switch (reponseBytes) {
            case CASE_SUCCESS /*0*/:
                Log.i("OTAResponseReceiver", "CYRET_SUCCESS");
                String siliconID = result.substring(START_ROW_START, SILICON_REV_START);
                String siliconRev = result.substring(SILICON_REV_START, SILICON_REV_END);
                Intent intent = new Intent(BootLoaderUtils.ACTION_OTA_STATUS);
                Bundle mBundle = new Bundle();
                mBundle.putString(Constants.EXTRA_SILICON_ID, siliconID);
                mBundle.putString(Constants.EXTRA_SILICON_REV, siliconRev);
                intent.putExtras(mBundle);
                this.mContext.sendBroadcast(intent);
            default:
                broadCastErrors(reponseBytes);
                Log.i("OTAResponseReceiver", "CYRET ERROR");
        }
    }

    private void parseGetFlashSizeAcknowledgement(String parse) {
        String result = parse.trim().replace(" ", "");
        String response = result.substring(RESPONSE_START, STATUS_START);
        Log.e("OTAResponseReceiver", "Get flash size Response>>>>>" + result);
        int reponseBytes = Integer.parseInt(response, SILICON_REV_START);
        switch (reponseBytes) {
            case CASE_SUCCESS /*0*/:
                Log.i("OTAResponseReceiver", "CYRET_SUCCESS");
                int startRow = BootLoaderUtils.swap(Integer.parseInt(result.substring(START_ROW_START, START_ROW_END), SILICON_REV_START));
                int endRow = BootLoaderUtils.swap(Integer.parseInt(result.substring(START_ROW_END, SILICON_REV_START), SILICON_REV_START));
                Intent intent = new Intent(BootLoaderUtils.ACTION_OTA_STATUS);
                Bundle mBundle = new Bundle();
                mBundle.putString(Constants.EXTRA_START_ROW, "" + startRow);
                mBundle.putString(Constants.EXTRA_END_ROW, "" + endRow);
                intent.putExtras(mBundle);
                this.mContext.sendBroadcast(intent);
            default:
                broadCastErrors(reponseBytes);
                Log.i("OTAResponseReceiver", "CYRET ERROR");
        }
    }

    private void parseParseRowAcknowledgement(String parse) {
        String result = parse.trim().replace(" ", "");
        String response = result.substring(RESPONSE_START, STATUS_START);
        String status = result.substring(STATUS_START, STATUS_END);
        int reponseBytes = Integer.parseInt(response, SILICON_REV_START);
        switch (reponseBytes) {
            case CASE_SUCCESS /*0*/:
                Log.i("OTAResponseReceiver", "CYRET_SUCCESS");
                Intent intent = new Intent(BootLoaderUtils.ACTION_OTA_STATUS);
                Bundle mBundle = new Bundle();
                mBundle.putString(Constants.EXTRA_PROGRAM_ROW_STATUS, status);
                intent.putExtras(mBundle);
                this.mContext.sendBroadcast(intent);
            default:
                broadCastErrors(reponseBytes);
                Log.i("OTAResponseReceiver", "CYRET ERROR");
        }
    }

    private void parseVerifyRowAcknowledgement(String parse) {
        String result = parse.trim().replace(" ", "");
        String response = result.substring(RESPONSE_START, STATUS_START);
        String data = result.substring(START_ROW_START, DATA_END);
        int reponseBytes = Integer.parseInt(response, SILICON_REV_START);
        switch (reponseBytes) {
            case CASE_SUCCESS /*0*/:
                Log.i("OTAResponseReceiver", "CYRET_SUCCESS");
                Intent intent = new Intent(BootLoaderUtils.ACTION_OTA_STATUS);
                Bundle mBundle = new Bundle();
                mBundle.putString(Constants.EXTRA_VERIFY_ROW_STATUS, response);
                mBundle.putString(Constants.EXTRA_VERIFY_ROW_CHECKSUM, data);
                intent.putExtras(mBundle);
                this.mContext.sendBroadcast(intent);
            default:
                broadCastErrors(reponseBytes);
                Log.i("OTAResponseReceiver", "CYRET ERROR");
        }
    }

    private void parseVerifyCheckSum(String parse) {
        String result = parse.trim().replace(" ", "");
        String response = result.substring(RESPONSE_START, STATUS_START);
        String checkSumStatus = result.substring(STATUS_START, STATUS_END);
        int reponseBytes = Integer.parseInt(response, SILICON_REV_START);
        switch (reponseBytes) {
            case CASE_SUCCESS /*0*/:
                Log.i("OTAResponseReceiver", "CYRET_SUCCESS");
                Intent intent = new Intent(BootLoaderUtils.ACTION_OTA_STATUS);
                Bundle mBundle = new Bundle();
                mBundle.putString(Constants.EXTRA_VERIFY_CHECKSUM_STATUS, checkSumStatus);
                intent.putExtras(mBundle);
                this.mContext.sendBroadcast(intent);
            default:
                broadCastErrors(reponseBytes);
                Log.i("OTAResponseReceiver", "CYRET ERROR");
        }
    }

    private void parseExitBootloader(String parse) {
        String response = parse.trim().replace(" ", "");
        Log.e("OTAResponseReceiver", "Reponse Byte Exit>>" + response);
        Intent intent = new Intent(BootLoaderUtils.ACTION_OTA_STATUS);
        Bundle mBundle = new Bundle();
        mBundle.putString(Constants.EXTRA_VERIFY_EXIT_BOOTLOADER, response);
        intent.putExtras(mBundle);
        this.mContext.sendBroadcast(intent);
    }

    public void broadCastErrorMessage(String errorMessage) {
        Intent intent = new Intent(BootLoaderUtils.ACTION_OTA_STATUS);
        Bundle mBundle = new Bundle();
        mBundle.putString(Constants.EXTRA_ERROR_OTA, errorMessage);
        intent.putExtras(mBundle);
        this.mContext.sendBroadcast(intent);
    }

    public void broadCastErrors(int errorkey) {
        switch (errorkey) {
            case CASE_ERR_FILE /*1*/:
                Log.i("OTAResponseReceiver", CYRET_ERR_FILE);
                broadCastErrorMessage(CYRET_ERR_FILE);
            case RESPONSE_START /*2*/:
                Log.i("OTAResponseReceiver", CYRET_ERR_EOF);
                broadCastErrorMessage(CYRET_ERR_EOF);
            case CASE_ERR_LENGTH /*3*/:
                Log.i("OTAResponseReceiver", CYRET_ERR_LENGTH);
                broadCastErrorMessage(CYRET_ERR_LENGTH);
            case STATUS_START /*4*/:
                Log.i("OTAResponseReceiver", CYRET_ERR_DATA);
                broadCastErrorMessage(CYRET_ERR_DATA);
            case CASE_ERR_CMD /*5*/:
                Log.i("OTAResponseReceiver", CYRET_ERR_CMD);
                broadCastErrorMessage(CYRET_ERR_CMD);
            case STATUS_END /*6*/:
                Log.i("OTAResponseReceiver", CYRET_ERR_DEVICE);
                broadCastErrorMessage(CYRET_ERR_DEVICE);
            case CASE_ERR_VERSION /*7*/:
                Log.i("OTAResponseReceiver", CYRET_ERR_VERSION);
                broadCastErrorMessage(CYRET_ERR_VERSION);
            case START_ROW_START /*8*/:
                Log.i("OTAResponseReceiver", CYRET_ERR_CHECKSUM);
                broadCastErrorMessage(CYRET_ERR_CHECKSUM);
            case CASE_ERR_ARRAY /*9*/:
                Log.i("OTAResponseReceiver", CYRET_ERR_ARRAY);
                broadCastErrorMessage(CYRET_ERR_ARRAY);
            case DATA_END /*10*/:
                Log.i("OTAResponseReceiver", CYRET_ERR_ROW);
                broadCastErrorMessage(CYRET_ERR_ROW);
            case CASE_BTLDR /*11*/:
                Log.i("OTAResponseReceiver", CYRET_BTLDR);
                broadCastErrorMessage(CYRET_BTLDR);
            case START_ROW_END /*12*/:
                Log.i("OTAResponseReceiver", CYRET_ERR_APP);
                broadCastErrorMessage(CYRET_ERR_APP);
            case CASE_ERR_ACTIVE /*13*/:
                Log.i("OTAResponseReceiver", CYRET_ERR_ACTIVE);
                broadCastErrorMessage(CYRET_ERR_ACTIVE);
            case CASE_ERR_UNK /*14*/:
                Log.i("OTAResponseReceiver", CYRET_ERR_UNK);
                broadCastErrorMessage(CYRET_ERR_UNK);
            case CASE_ABORT /*15*/:
                Log.i("OTAResponseReceiver", CYRET_ABORT);
                broadCastErrorMessage(CYRET_ABORT);
            default:
                Log.i("OTAResponseReceiver", "CYRET DEFAULT");
        }
    }
}