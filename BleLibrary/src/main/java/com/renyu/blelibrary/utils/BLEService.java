package com.renyu.blelibrary.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.impl.BLEConnectListener;
import com.renyu.blelibrary.impl.BLEStateChangeListener;
import com.renyu.blelibrary.params.CommonParams;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by renyu on 2017/6/6.
 */

public class BLEService extends Service {

    ArrayList<BLEDevice> bleDevices;

    BLEFramework bleFramework;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null && intent.getStringExtra(CommonParams.COMMAND)!=null) {
            if (intent.getStringExtra(CommonParams.COMMAND).equals(CommonParams.SCAN)) {
                bleDevices=new ArrayList<>();

                UUID UUID_SERVICE= (UUID) intent.getSerializableExtra("UUID_SERVICE");
                UUID UUID_OTASERVICE= (UUID) intent.getSerializableExtra("UUID_OTASERVICE");
                UUID UUID_Characteristic_WRITE= (UUID) intent.getSerializableExtra("UUID_Characteristic_WRITE");
                UUID UUID_Characteristic_READ= (UUID) intent.getSerializableExtra("UUID_Characteristic_READ");
                UUID UUID_OTACharacteristic= (UUID) intent.getSerializableExtra("UUID_OTACharacteristic");
                UUID UUID_DESCRIPTOR= (UUID) intent.getSerializableExtra("UUID_DESCRIPTOR");
                UUID UUID_OTADESCRIPTOR= (UUID) intent.getSerializableExtra("UUID_OTADESCRIPTOR");
                bleFramework=BLEFramework.getBleFrameworkInstance();
                bleFramework.setParams(this.getApplicationContext(),
                        UUID_SERVICE,
                        UUID_OTASERVICE,
                        UUID_Characteristic_WRITE,
                        UUID_Characteristic_READ,
                        UUID_OTACharacteristic,
                        UUID_DESCRIPTOR,
                        UUID_OTADESCRIPTOR);
                bleFramework.setBleConnectListener(new BLEConnectListener() {
                    @Override
                    public void getAllScanDevice(BLEDevice bleDevice) {
                        Log.d("BLEService", bleDevice.getDevice().getName() + " " + bleDevice.getDevice().getAddress());
                        if (bleDevice.getDevice().getName()!=null && bleDevice.getDevice().getName().startsWith("iite")) {
                            bleDevices.add(bleDevice);
                        }
                    }
                });
                bleFramework.setBleStateChangeListener(new BLEStateChangeListener() {
                    @Override
                    public void getCurrentState(int currentState) {
                        Log.d("BLEService", "currentState:" + currentState);
                        if (currentState==2 && bleDevices.size()>0) {
                            bleFramework.startConn(bleDevices.get(0).getDevice());
                        }
                    }
                });
                bleFramework.initBLE();
                bleFramework.startScan();
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }
}
