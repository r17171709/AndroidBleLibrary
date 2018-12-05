package com.cypress.cysmart.OTAFirmwareUpdate;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.cypress.cysmart.CommonUtils.Constants;
import com.cypress.cysmart.CommonUtils.GattAttributes;
import com.cypress.cysmart.CommonUtils.Utils;
import com.cypress.cysmart.DataModelClasses.OTAParams;
import com.cypress.cysmart.DataModelClasses.OTAFlashRowModel;
import com.renyu.blelibrary.utils.BLEFramework;

import java.util.ArrayList;

/**
 * Created by renyu on 2017/2/7.
 */

public class OTAService extends Service implements FileReadStatusUpdater {
    
    private static String mSiliconID;
    private static String mSiliconRev;
    private static String mCheckSumType;
    private boolean HANDLER_FLAG = true;
    private int mTotalLines = 0;
    private ArrayList<OTAFlashRowModel> mFlashRowList;
    //本次连接设备的地址
    private String mBluetoothDeviceAddress;
    //ota特征值
    private BluetoothGattCharacteristic mOTACharacteristic;
    //自定义ota写对象
    private OTAFirmwareWrite otaFirmwareWrite;
    private int mProgressBarPosition = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter=new IntentFilter();
        filter.addAction(BootLoaderUtils.ACTION_OTA_STATUS);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiver, filter);

        mOTACharacteristic=BLEFramework.getBleFrameworkInstance().getOTACharacteristic();
        mBluetoothDeviceAddress=BLEFramework.getBleFrameworkInstance().getCurrentBluetoothDevice().getAddress();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null && intent.getIntExtra("command", 0)==1) {
            startOta();
        }
        if (intent!=null && intent.getIntExtra("command", 0)==2) {
            onOtaExitBootloaderComplete(intent.getIntExtra("status", 0));
        }
        if (intent!=null && intent.getIntExtra("command", 0)==3) {
            writeOTABootLoaderCommand(mOTACharacteristic, intent.getByteArrayExtra("commandBytes"));
        }
        if (intent!=null && intent.getIntExtra("command", 0)==4) {
            writeOTABootLoaderCommand(mOTACharacteristic, intent.getByteArrayExtra("commandBytes"), true);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (this) {
                final String sharedPrefStatus = Utils.getStringSharedPreference(context, Constants.PREF_BOOTLOADER_STATE);
                final String action = intent.getAction();
                Bundle extras = intent.getExtras();
                if (BootLoaderUtils.ACTION_OTA_STATUS.equals(action)) {
                    if (sharedPrefStatus.equalsIgnoreCase("" + BootLoaderCommands.ENTER_BOOTLOADER)) {
                        String siliconIDReceived, siliconRevReceived;
                        if (extras.containsKey(Constants.EXTRA_SILICON_ID) && extras.containsKey(Constants.EXTRA_SILICON_REV)) {
                            siliconIDReceived = extras.getString(Constants.EXTRA_SILICON_ID);
                            siliconRevReceived = extras.getString(Constants.EXTRA_SILICON_REV);
                            if (siliconIDReceived.equalsIgnoreCase(mSiliconID) && siliconRevReceived.equalsIgnoreCase(mSiliconRev)) {
                                /**
                                 * SiliconID and SiliconRev Verified
                                 * Sending Next coommand
                                 */
                                //Getting the arrayID
                                OTAFlashRowModel modelData = mFlashRowList.get(0);
                                byte[] data = new byte[1];
                                data[0] = (byte) modelData.mArrayId;
                                int dataLength = 1;
                                /**
                                 * Writing the next command
                                 * Changing the shared preference value
                                 */
                                otaFirmwareWrite.OTAGetFlashSizeCmd(data, mCheckSumType, dataLength);
                                Utils.setStringSharedPreference(context, Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands.GET_FLASH_SIZE);
                                Log.d("BLEService", "执行获取flash大小的命令");
                            }
                        }

                    }
                    else if (sharedPrefStatus.equalsIgnoreCase("" + BootLoaderCommands.GET_FLASH_SIZE)) {
                        /**
                         * verifying the rows to be programmed within the bootloadable area of flash
                         * not done for time being
                         */
                        int PROGRAM_ROW_NO = Utils.getIntSharedPreference(context, Constants.PREF_PROGRAM_ROW_NO);
                        writeProgrammableData(PROGRAM_ROW_NO);
                    }
                    else if (sharedPrefStatus.equalsIgnoreCase("" + BootLoaderCommands.SEND_DATA)) {
                        /**
                         * verifying the status and sending the next command
                         * Changing the shared preference value
                         */
                        if (extras.containsKey(Constants.EXTRA_SEND_DATA_ROW_STATUS)) {
                            String statusReceived = extras.getString(Constants.EXTRA_SEND_DATA_ROW_STATUS);
                            if (statusReceived.equalsIgnoreCase("00")) {
                                //Succes status received.Send programmable data
                                int PROGRAM_ROW_NO = Utils.getIntSharedPreference(context, Constants.PREF_PROGRAM_ROW_NO);
                                writeProgrammableData(PROGRAM_ROW_NO);
                            }
                        }
                    }
                    else if (sharedPrefStatus.equalsIgnoreCase("" + BootLoaderCommands.PROGRAM_ROW)) {
                        String statusReceived;
                        if (extras.containsKey(Constants.EXTRA_PROGRAM_ROW_STATUS)) {
                            statusReceived = extras.getString(Constants.EXTRA_PROGRAM_ROW_STATUS);
                            if (statusReceived.equalsIgnoreCase("00")) {
                                /**
                                 * Program Row Status Verified
                                 * Sending Next coommand
                                 */
                                //Getting the arrayI
                                int PROGRAM_ROW = Utils.getIntSharedPreference(context, Constants.PREF_PROGRAM_ROW_NO);
                                OTAFlashRowModel modelData = mFlashRowList.get(PROGRAM_ROW);
                                long rowMSB = Long.parseLong(modelData.mRowNo.substring(0, 2), 16);
                                long rowLSB = Long.parseLong(modelData.mRowNo.substring(2, 4), 16);
                                /**
                                 * Writing the next command
                                 * Changing the shared preference value
                                 */
                                otaFirmwareWrite.OTAVerifyRowCmd(rowMSB, rowLSB, modelData, mCheckSumType);
                                Utils.setStringSharedPreference(context, Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands.VERIFY_ROW);
                                Log.d("BLEService", "执行获取flash大小的命令");
                            }
                        }
                    }
                    else if (sharedPrefStatus.equalsIgnoreCase("" + BootLoaderCommands.VERIFY_ROW)) {
                        String statusReceived, checksumReceived;
                        if (extras.containsKey(Constants.EXTRA_VERIFY_ROW_STATUS) && extras.containsKey(Constants.EXTRA_VERIFY_ROW_CHECKSUM)) {
                            statusReceived = extras.getString(Constants.EXTRA_VERIFY_ROW_STATUS);
                            checksumReceived = extras.getString(Constants.EXTRA_VERIFY_ROW_CHECKSUM);
                            if (statusReceived.equalsIgnoreCase("00")) {
                                /**
                                 * Program Row Status Verified
                                 * Sending Next coommand
                                 */
                                int PROGRAM_ROW_NO = Utils.getIntSharedPreference(context, Constants.PREF_PROGRAM_ROW_NO);
                                //Getting the arrayID
                                OTAFlashRowModel modelData = mFlashRowList.get(PROGRAM_ROW_NO);
                                long rowMSB = Long.parseLong(modelData.mRowNo.substring(0, 2), 16);
                                long rowLSB = Long.parseLong(modelData.mRowNo.substring(2, 4), 16);

                                byte[] checkSumVerify = new byte[6];
                                checkSumVerify[0] = (byte) modelData.mRowCheckSum;
                                checkSumVerify[1] = (byte) modelData.mArrayId;
                                checkSumVerify[2] = (byte) rowMSB;
                                checkSumVerify[3] = (byte) rowLSB;
                                checkSumVerify[4] = (byte) (modelData.mDataLength);
                                checkSumVerify[5] = (byte) ((modelData.mDataLength) >> 8);
                                String fileCheckSumCalculated = Integer.toHexString(BootLoaderUtils.calculateCheckSumVerifyRow(6, checkSumVerify));
                                int fileCheckSumCalculatedLength = fileCheckSumCalculated.length();
                                if(fileCheckSumCalculatedLength == 1){
                                    fileCheckSumCalculated = "0" + fileCheckSumCalculated;
                                    fileCheckSumCalculatedLength ++;
                                }
                                String fileCheckSumByte = fileCheckSumCalculated.substring((fileCheckSumCalculatedLength - 2), fileCheckSumCalculatedLength);
                                if (fileCheckSumByte.equalsIgnoreCase(checksumReceived)) {
                                    PROGRAM_ROW_NO = PROGRAM_ROW_NO + 1;
                                    //Shows ProgressBar status
                                    showProgress(mProgressBarPosition, PROGRAM_ROW_NO, mFlashRowList.size());
                                    if (PROGRAM_ROW_NO < mFlashRowList.size()) {
                                        Utils.setIntSharedPreference(context, Constants.PREF_PROGRAM_ROW_NO, PROGRAM_ROW_NO);
                                        Utils.setIntSharedPreference(context, Constants.PREF_PROGRAM_ROW_START_POS, 0);
                                        writeProgrammableData(PROGRAM_ROW_NO);
                                    }
                                    if (PROGRAM_ROW_NO == mFlashRowList.size()) {
                                        Utils.setIntSharedPreference(context, Constants.PREF_PROGRAM_ROW_NO, 0);
                                        Utils.setIntSharedPreference(context, Constants.PREF_PROGRAM_ROW_START_POS, 0);
                                        /**
                                         * Writing the next command
                                         * Changing the shared preference value
                                         */
                                        Utils.setStringSharedPreference(context, Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands.VERIFY_CHECK_SUM);
                                        otaFirmwareWrite.OTAVerifyCheckSumCmd(mCheckSumType);
                                        Log.d("BLEService", "执行验证检查操作");
                                    }
                                }
                            }
                        }

                    }
                    else if (sharedPrefStatus.equalsIgnoreCase("" + BootLoaderCommands.VERIFY_CHECK_SUM)) {
                        String statusReceived;
                        if (extras.containsKey(Constants.EXTRA_VERIFY_CHECKSUM_STATUS)) {
                            statusReceived = extras.getString(Constants.EXTRA_VERIFY_CHECKSUM_STATUS);
                            if (statusReceived.equalsIgnoreCase("01")) {
                                /**
                                 * Verify Status Verified
                                 * Sending Next coommand
                                 */
                                //Getting the arrayID
                                otaFirmwareWrite.OTAExitBootloaderCmd(mCheckSumType);
                                Utils.setStringSharedPreference(context, Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands.EXIT_BOOTLOADER);
                                Log.d("BLEService", "bootloader结束");
                            }
                        }

                    }
                    else if(sharedPrefStatus.equalsIgnoreCase("" + BootLoaderCommands.EXIT_BOOTLOADER)){
                        OTAParams.mFileupgradeStarted = false;
                        saveDeviceAddress();
                        Log.d("BLEService", "ota固件升级成功");
                        if (secondFileUpdatedNeeded()) {
                            Log.d("BLEService", "堆栈升级成功完成。应用程序升级悬而未决");

                            // 发射进度
                            BLEFramework.getBleFrameworkInstance().updateOTAProgress(-1);
                        }
                        else {
                            Log.d("BLEService", "ota固件升级成功");

                            BLEFramework.getBleFrameworkInstance().updateOTAProgress(101);
                        }
                        OTAParams.mFileupgradeStarted = false;
                        BLEFramework.getBleFrameworkInstance().close();
                        stopSelf();
                    }
                    if (extras.containsKey(Constants.EXTRA_ERROR_OTA)) {
                        BLEFramework.getBleFrameworkInstance().updateOTAProgress(-1);
                        String errorMessage = extras.getString(Constants.EXTRA_ERROR_OTA);
                        Log.d("BLEService", errorMessage);
                        stopSelf();
                    }
                }
                if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    if (state == BluetoothDevice.BOND_BONDING) {
                        // Bonding...
                        Log.i("BLEService", "Bonding is in process....");
                    }
                    else if (state == BluetoothDevice.BOND_BONDED) {
                        Log.d("BLEService", "Paired");
                    }
                    else if (state == BluetoothDevice.BOND_NONE) {
                        Log.d("BLEService", "Unpaired");
                    }
                }
            }
        }
    };

    public void startOta() {
        prepareFileWriting(Environment.getExternalStorageDirectory().getPath()+ "/CySmart/BLE_OTA_Bootloadable.cyacd");
        mProgressBarPosition = 1;
        initializeBondingIFnotBonded();
    }

    private void prepareFileWriting(String mCurrentFilePath) {
        Utils.setIntSharedPreference(OTAService.this, Constants.PREF_PROGRAM_ROW_NO, 0);
        Utils.setIntSharedPreference(OTAService.this, Constants.PREF_PROGRAM_ROW_START_POS, 0);
        if (mOTACharacteristic != null) {
            otaFirmwareWrite = new OTAFirmwareWrite(mOTACharacteristic, OTAService.this);
        }
        final CustomFileReader customFileReader;
        customFileReader = new CustomFileReader(mCurrentFilePath);
        customFileReader.setFileReadStatusUpdater(this);
        String[] headerData = customFileReader.analyseFileHeader();
        mSiliconID = headerData[0];
        mSiliconRev = headerData[1];
        mCheckSumType = headerData[2];
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (HANDLER_FLAG) {
                    mTotalLines = customFileReader.getTotalLines();
                    mFlashRowList = customFileReader.readDataLines();
                }
            }
        }, 1000);
    }

    private void initializeBondingIFnotBonded() {
        getBondedState();
    }

    private boolean getBondedState() {
        Boolean bonded;
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mBluetoothDeviceAddress);
        bonded = device.getBondState() == BluetoothDevice.BOND_BONDED;
        return bonded;
    }

    public void writeOTABootLoaderCommand(BluetoothGattCharacteristic characteristic, byte[] value, boolean isExitBootloaderCmd) {
        synchronized (BLEFramework.class) {
            writeOTABootLoaderCommand(characteristic, value);
            if (isExitBootloaderCmd) {
                BLEFramework.getBleFrameworkInstance().setM_otaExitBootloaderCmdInProgress(true);
            }
        }
    }

    public synchronized void writeOTABootLoaderCommand(BluetoothGattCharacteristic characteristic, byte[] value) {
        String serviceName = GattAttributes.lookupUUID(characteristic.getService().getUuid(), characteristic.getService().getUuid().toString());
        String characteristicName = GattAttributes.lookupUUID(characteristic.getUuid(), characteristic.getUuid().toString());
        String characteristicValue = Utils.ByteArraytoHex(value);
        if (BLEFramework.getBleFrameworkInstance().getCurrentGatt() != null) {
            byte[] valueByte = value;
            characteristic.setValue(valueByte);
            int counter = 20;
            boolean status;
            do {
                status = BLEFramework.getBleFrameworkInstance().getCurrentGatt().writeCharacteristic(characteristic);
                if(!status) {
                    Log.v("CYSMART","writeCharacteristic() status: False");
                    try {
                        Thread.sleep(100,0);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } while (!status && (counter-- > 0));


            if(status) {
                String dataLog =
                        "[" + serviceName + "|" + characteristicName + "] " +
                                "Write request sent with value, " +

                                "[ " + characteristicValue + " ]";
                Log.i("CYSMART", dataLog);
                Log.v("CYSMART", dataLog);
            }
            else {
                Log.v("CYSMART", "writeOTABootLoaderCommand failed!");
            }
        }
    }

    private void writeProgrammableData(int rowPosition) {
        int startPosition = Utils.getIntSharedPreference(OTAService.this, Constants.PREF_PROGRAM_ROW_START_POS);
        Log.e("BLEService", "Row: " + rowPosition + "Start Pos: " + startPosition);
        OTAFlashRowModel modelData = mFlashRowList.get(rowPosition);
        int verifyDataLength = modelData.mDataLength - startPosition;
        if (checkProgramRowCommandToSend(verifyDataLength)) {
            long rowMSB = Long.parseLong(modelData.mRowNo.substring(0, 2), 16);
            long rowLSB = Long.parseLong(modelData.mRowNo.substring(2, 4), 16);
            int dataLength = modelData.mDataLength - startPosition;
            byte[] dataToSend = new byte[dataLength];
            for (int pos = 0; pos < dataLength; pos++) {
                if (startPosition < modelData.mData.length) {
                    byte data = modelData.mData[startPosition];
                    dataToSend[pos] = data;
                    startPosition++;
                } else {
                    break;
                }
            }
            otaFirmwareWrite.OTAProgramRowCmd(rowMSB, rowLSB, modelData.mArrayId, dataToSend, mCheckSumType);
            Utils.setStringSharedPreference(OTAService.this, Constants.PREF_BOOTLOADER_STATE,  ""+ BootLoaderCommands.PROGRAM_ROW);
            Utils.setIntSharedPreference(OTAService.this, Constants.PREF_PROGRAM_ROW_START_POS, 0);
            Log.d("BLEService", "固件升级中");
        }
        else {
            int dataLength = BootLoaderCommands.MAX_DATA_SIZE;
            byte[] dataToSend = new byte[dataLength];
            for (int pos = 0; pos < dataLength; pos++) {
                if (startPosition < modelData.mData.length) {
                    byte data = modelData.mData[startPosition];
                    dataToSend[pos] = data;
                    startPosition++;
                }
                else {
                    break;
                }
            }
            otaFirmwareWrite.OTAProgramRowSendDataCmd(dataToSend, mCheckSumType);
            Utils.setStringSharedPreference(OTAService.this, Constants.PREF_BOOTLOADER_STATE, "" + BootLoaderCommands.SEND_DATA);
            Utils.setIntSharedPreference(OTAService.this, Constants.PREF_PROGRAM_ROW_START_POS, startPosition);
            Log.d("BLEService", "固件升级中");
        }
    }

    /**
     * Method to show progress bar
     *
     * @param fileLineNos
     * @param totalLines
     */

    private void showProgress(int fileStatus, float fileLineNos, float totalLines) {
        if (fileStatus == 1) {
            Log.i("BLEService", (int) fileLineNos+"  "+(int) totalLines+"  "+(int) ((fileLineNos / totalLines) * 100) + "%");
            BLEFramework.getBleFrameworkInstance().updateOTAProgress((int) ((fileLineNos / totalLines) * 100));
        }
        if (fileStatus == 2) {
            Log.d("BLEService", "结束");
        }
    }

    private boolean checkProgramRowCommandToSend(int totalSize) {
        if (totalSize <= BootLoaderCommands.MAX_DATA_SIZE) {
            return true;
        } else {
            return false;
        }
    }

    private void onOtaExitBootloaderComplete(int status) {
        Bundle bundle = new Bundle();
        bundle.putByteArray(Constants.EXTRA_BYTE_VALUE, new byte[]{(byte)status});
        Intent intentOTA = new Intent(OTAParams.ACTION_OTA_DATA_AVAILABLE);
        intentOTA.putExtras(bundle);
        OTAService.this.sendBroadcast(intentOTA);
    }

    private boolean secondFileUpdatedNeeded() {
        String secondFilePath = Utils.getStringSharedPreference(OTAService.this, Constants.PREF_OTA_FILE_TWO_PATH);
        Log.e("BLEService", "secondFilePath-->" + secondFilePath);
        return mBluetoothDeviceAddress.equalsIgnoreCase(saveDeviceAddress())
                && (!secondFilePath.equalsIgnoreCase("Default")
                && (!secondFilePath.equalsIgnoreCase("")));
    }

    /**
     * Returns saved device adress
     *
     * @return
     */
    private String saveDeviceAddress() {
        Utils.setStringSharedPreference(OTAService.this, Constants.PREF_DEV_ADDRESS, mBluetoothDeviceAddress);
        return Utils.getStringSharedPreference(OTAService.this, Constants.PREF_DEV_ADDRESS);
    }

    @Override
    public void onFileReadProgressUpdate(int fileLine) {
        if (this.mTotalLines <= 0 || fileLine > 0) {

        }
        if (this.mTotalLines == fileLine && mOTACharacteristic != null) {
            Log.d("BLEService", "文件读取成功");
            Utils.setStringSharedPreference(OTAService.this, Constants.PREF_BOOTLOADER_STATE, "56");
            OTAParams.mFileupgradeStarted = true;
            otaFirmwareWrite.OTAEnterBootLoaderCmd(mCheckSumType);
            Log.d("BLEService", "执行进入bootloader方法");
        }
    }
}
