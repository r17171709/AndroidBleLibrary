package com.cypress.cysmart.OTAFirmwareUpdate;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cypress.cysmart.DataModelClasses.OTAFlashRowModel;

public class OTAFirmwareWrite {
    private static final int ADDITIVE_OP = 8;
    private static final int BYTE_ARRAY_ID = 4;
    private static final int BYTE_ARRAY_SIZE = 7;
    private static final int BYTE_CHECKSUM = 4;
    private static final int BYTE_CHECKSUM_SHIFT = 5;
    private static final int BYTE_CHECKSUM_VER_ROW = 7;
    private static final int BYTE_CHECKSUM_VER_ROW_SHIFT = 8;
    private static final int BYTE_CMD_DATA_SIZE = 2;
    private static final int BYTE_CMD_DATA_SIZE_SHIFT = 3;
    private static final int BYTE_CMD_TYPE = 1;
    private static final int BYTE_PACKET_END = 6;
    private static final int BYTE_PACKET_END_VER_ROW = 9;
    private static final int BYTE_ROW = 5;
    private static final int BYTE_ROW_SHIFT = 6;
    private static final int BYTE_START_CMD = 0;
    private static final int RADIX = 16;
    private BluetoothGattCharacteristic mOTACharacteristic;
    private Context context;

    public OTAFirmwareWrite(BluetoothGattCharacteristic writeCharacteristic, Context context) {
        this.mOTACharacteristic = writeCharacteristic;
        this.context = context;
    }

    public void OTAEnterBootLoaderCmd(String checkSumType) {
        byte[] commandBytes = new byte[BYTE_CHECKSUM_VER_ROW];
        commandBytes[BYTE_START_CMD] = (byte) BYTE_CMD_TYPE;
        commandBytes[BYTE_CMD_TYPE] = (byte) 56;
        commandBytes[BYTE_CMD_DATA_SIZE] = (byte) BYTE_START_CMD;
        commandBytes[BYTE_CMD_DATA_SIZE_SHIFT] = (byte) BYTE_START_CMD;
        long checksum = Long.parseLong(Integer.toHexString(BootLoaderUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX), BYTE_CHECKSUM, commandBytes)), RADIX);
        commandBytes[BYTE_CHECKSUM] = (byte) ((int) checksum);
        commandBytes[BYTE_ROW] = (byte) ((int) (checksum >> BYTE_CHECKSUM_VER_ROW_SHIFT));
        commandBytes[BYTE_ROW_SHIFT] = (byte) 23;
        Log.e("OTAFirmwareWrite", "OTAEnterBootLoaderCmd");
        writeBytes(commandBytes);
    }

    private void writeBytes(byte[] commandBytes) {
        Intent intent=new Intent(context, OTAService.class);
        intent.putExtra("command", 3);
        intent.putExtra("commandBytes", commandBytes);
        context.startService(intent);
    }

    private void writeBytesWithExist(byte[] commandBytes) {
        Intent intent=new Intent(context, OTAService.class);
        intent.putExtra("command", 4);
        intent.putExtra("commandBytes", commandBytes);
        context.startService(intent);
    }

    public void OTAGetFlashSizeCmd(byte[] data, String checkSumType, int dataLength) {
        byte[] commandBytes = new byte[(dataLength + BYTE_CHECKSUM_VER_ROW)];
        commandBytes[BYTE_START_CMD] = (byte) BYTE_CMD_TYPE;
        commandBytes[BYTE_CMD_TYPE] = (byte) 50;
        commandBytes[BYTE_CMD_DATA_SIZE] = (byte) dataLength;
        commandBytes[BYTE_CMD_DATA_SIZE_SHIFT] = (byte) (dataLength >> BYTE_CHECKSUM_VER_ROW_SHIFT);
        int dataByteLocationStart = BYTE_CHECKSUM;
        for (int count = BYTE_START_CMD; count < dataLength; count += BYTE_CMD_TYPE) {
            commandBytes[dataByteLocationStart] = data[count];
            dataByteLocationStart += BYTE_CMD_TYPE;
        }
        int datByteLocationEnd = dataByteLocationStart;
        long checksum = Long.parseLong(Integer.toHexString(BootLoaderUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX), commandBytes.length, commandBytes)), RADIX);
        commandBytes[datByteLocationEnd] = (byte) ((int) checksum);
        commandBytes[datByteLocationEnd + BYTE_CMD_TYPE] = (byte) ((int) (checksum >> BYTE_CHECKSUM_VER_ROW_SHIFT));
        commandBytes[datByteLocationEnd + BYTE_CMD_DATA_SIZE] = (byte) 23;
        Log.e("OTAFirmwareWrite", "OTAGetFlashSizeCmd");
        writeBytes(commandBytes);
    }

    public void OTAProgramRowSendDataCmd(byte[] data, String checksumType) {
        int totalSize = data.length + BYTE_CHECKSUM_VER_ROW;
        byte[] commandBytes = new byte[totalSize];
        commandBytes[BYTE_START_CMD] = (byte) BYTE_CMD_TYPE;
        commandBytes[BYTE_CMD_TYPE] = (byte) 55;
        commandBytes[BYTE_CMD_DATA_SIZE] = (byte) data.length;
        commandBytes[BYTE_CMD_DATA_SIZE_SHIFT] = (byte) (data.length >> BYTE_CHECKSUM_VER_ROW_SHIFT);
        for (int i = BYTE_START_CMD; i < data.length; i += BYTE_CMD_TYPE) {
            commandBytes[i + BYTE_CHECKSUM] = data[i];
        }
        int checksum = BootLoaderUtils.calculateCheckSum2(Integer.parseInt(checksumType, RADIX), data.length + BYTE_CHECKSUM, commandBytes);
        commandBytes[totalSize - 3] = (byte) checksum;
        commandBytes[totalSize - 2] = (byte) (checksum >> BYTE_CHECKSUM_VER_ROW_SHIFT);
        commandBytes[totalSize - 1] = (byte) 23;
        Log.e("OTAFirmwareWrite", "OTAProgramRowSendDataCmd Send size--->" + commandBytes.length);
        writeBytes(commandBytes);
    }

    public void OTAProgramRowCmd(long rowMSB, long rowLSB, int arrayID, byte[] data, String checkSumType) {
        int totalSize = data.length + 10;
        byte[] commandBytes = new byte[totalSize];
        commandBytes[BYTE_START_CMD] = (byte) BYTE_CMD_TYPE;
        commandBytes[BYTE_CMD_TYPE] = (byte) 57;
        commandBytes[BYTE_CMD_DATA_SIZE] = (byte) (data.length + BYTE_CMD_DATA_SIZE_SHIFT);
        commandBytes[BYTE_CMD_DATA_SIZE_SHIFT] = (byte) ((data.length + BYTE_CMD_DATA_SIZE_SHIFT) >> BYTE_CHECKSUM_VER_ROW_SHIFT);
        commandBytes[BYTE_CHECKSUM] = (byte) arrayID;
        commandBytes[BYTE_ROW] = (byte) ((int) rowMSB);
        commandBytes[BYTE_ROW_SHIFT] = (byte) ((int) rowLSB);
        for (int i = BYTE_START_CMD; i < data.length; i += BYTE_CMD_TYPE) {
            commandBytes[i + BYTE_CHECKSUM_VER_ROW] = data[i];
        }
        int checksum = BootLoaderUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX), data.length + BYTE_CHECKSUM_VER_ROW, commandBytes);
        commandBytes[totalSize - 3] = (byte) checksum;
        commandBytes[totalSize - 2] = (byte) (checksum >> BYTE_CHECKSUM_VER_ROW_SHIFT);
        commandBytes[totalSize - 1] = (byte) 23;
        Log.e("OTAFirmwareWrite", "OTAProgramRowCmd send size--->" + commandBytes.length);
        writeBytes(commandBytes);
    }

    public void OTAVerifyRowCmd(long rowMSB, long rowLSB, OTAFlashRowModel model, String checkSumType) {
        byte[] commandBytes = new byte[(BYTE_CMD_DATA_SIZE_SHIFT + BYTE_CHECKSUM_VER_ROW)];
        commandBytes[BYTE_START_CMD] = (byte) BYTE_CMD_TYPE;
        commandBytes[BYTE_CMD_TYPE] = (byte) 58;
        commandBytes[BYTE_CMD_DATA_SIZE] = (byte) BYTE_CMD_DATA_SIZE_SHIFT;
        commandBytes[BYTE_CMD_DATA_SIZE_SHIFT] = (byte) BYTE_START_CMD;
        commandBytes[BYTE_CHECKSUM] = (byte) model.mArrayId;
        commandBytes[BYTE_ROW] = (byte) ((int) rowMSB);
        commandBytes[BYTE_ROW_SHIFT] = (byte) ((int) rowLSB);
        int checksum = BootLoaderUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX), BYTE_CHECKSUM_VER_ROW, commandBytes);
        commandBytes[BYTE_CHECKSUM_VER_ROW] = (byte) checksum;
        commandBytes[BYTE_CHECKSUM_VER_ROW_SHIFT] = (byte) (checksum >> BYTE_CHECKSUM_VER_ROW_SHIFT);
        commandBytes[BYTE_PACKET_END_VER_ROW] = (byte) 23;
        Log.e("OTAFirmwareWrite", "OTAVerifyRowCmd");
        writeBytes(commandBytes);
    }

    public void OTAVerifyCheckSumCmd(String checkSumType) {
        byte[] commandBytes = new byte[BYTE_CHECKSUM_VER_ROW];
        commandBytes[BYTE_START_CMD] = (byte) BYTE_CMD_TYPE;
        commandBytes[BYTE_CMD_TYPE] = (byte) 49;
        commandBytes[BYTE_CMD_DATA_SIZE] = (byte) 0;
        commandBytes[BYTE_CMD_DATA_SIZE_SHIFT] = (byte) 0;
        int checksum = BootLoaderUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX), BYTE_CHECKSUM, commandBytes);
        commandBytes[BYTE_CHECKSUM] = (byte) checksum;
        commandBytes[BYTE_ROW] = (byte) (checksum >> BYTE_CHECKSUM_VER_ROW_SHIFT);
        commandBytes[BYTE_ROW_SHIFT] = (byte) 23;
        Log.e("OTAFirmwareWrite", "OTAVerifyCheckSumCmd");
        writeBytes(commandBytes);
    }

    public void OTAExitBootloaderCmd(String checkSumType) {
        int COMMAND_SIZE = BYTE_START_CMD + BYTE_CHECKSUM_VER_ROW;
        byte[] commandBytes = new byte[BYTE_CHECKSUM_VER_ROW];
        commandBytes[BYTE_START_CMD] = (byte) BYTE_CMD_TYPE;
        commandBytes[BYTE_CMD_TYPE] = (byte) 59;
        commandBytes[BYTE_CMD_DATA_SIZE] = (byte) BYTE_START_CMD;
        commandBytes[BYTE_CMD_DATA_SIZE_SHIFT] = (byte) BYTE_START_CMD;
        int checksum = BootLoaderUtils.calculateCheckSum2(Integer.parseInt(checkSumType, RADIX), BYTE_CHECKSUM, commandBytes);
        commandBytes[BYTE_CHECKSUM] = (byte) checksum;
        commandBytes[BYTE_ROW] = (byte) (checksum >> BYTE_CHECKSUM_VER_ROW_SHIFT);
        commandBytes[BYTE_ROW_SHIFT] = (byte) 23;
        Log.e("OTAFirmwareWrite", "OTAExitBootloaderCmd");
        writeBytesWithExist(commandBytes);
    }
}