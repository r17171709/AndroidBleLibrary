package com.renyu.androidblelibrary.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.renyu.androidblelibrary.R;
import com.renyu.androidblelibrary.params.Params;
import com.renyu.androidblelibrary.utils.DataUtils;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.impl.BLEOTAListener;
import com.renyu.blelibrary.impl.BLEScanCallBackListener;
import com.renyu.blelibrary.impl.BLEStateChangeListener;
import com.renyu.blelibrary.utils.BLEFramework;

/**
 * Created by renyu on 2017/2/19.
 */

public class OTAActivity extends AppCompatActivity {

    BLEFramework bleFramework;

    TextView tv_progress;

    Handler handlerConnState=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what== BLEFramework.STATE_SERVICES_DISCOVERED
                    || msg.what==BLEFramework.STATE_SERVICES_OTA_DISCOVERED) {
                if (msg.what== BLEFramework.STATE_SERVICES_DISCOVERED) {
                    Toast.makeText(OTAActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DataUtils.enterOta(bleFramework, Params.UUID_SERVICE_OTA, Params.UUID_SERVICE_OTA_WRITE);
                        }
                    }, 1000);
                }
                else {
                    Toast.makeText(OTAActivity.this, "开始ota升级", Toast.LENGTH_SHORT).show();
                    bleFramework.startOTA();
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota);

        tv_progress = findViewById(R.id.tv_progress);
        findViewById(R.id.button_startota).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleFramework.startScan();
            }
        });
        findViewById(R.id.button_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLEFramework.isOTA=true;
                bleFramework.startScan();
            }
        });

        bleFramework=BLEFramework.getBleFrameworkInstance();
        bleFramework.setParams(this.getApplicationContext());
        bleFramework.setTimeSeconds(35000);
        bleFramework.setBleScanCallbackListener(new BLEScanCallBackListener() {
            @Override
            public void getAllScanDevice(BLEDevice bleDevice) {
                Log.d("OTAActivity", bleDevice.getDevice().getName()+" "+bleDevice.getDevice().getAddress());
                byte[] scanRecord=bleDevice.getScanRecord();
                int a=(int) scanRecord[5]&0xff;
                int b=(int) scanRecord[6]&0xff;
                if (a==0xaa && b==0xfe) {
                    bleFramework.stopScan(false);
                    bleFramework.startConn(bleDevice.getDevice());
                }
            }
        });
        bleFramework.setBleStateChangeListener(new BLEStateChangeListener() {
            @Override
            public void getCurrentState(int currentState) {
                Message message=new Message();
                message.what=currentState;
                handlerConnState.sendMessage(message);
            }
        });
        bleFramework.setBleotaListener(new BLEOTAListener() {
            @Override
            public void showProgress(int progress) {
                if (progress==-1) {
                    Log.d("OTAActivity", "升级失败");
                }
                else if (progress==101) {
                    Log.d("OTAActivity", "升级成功");
                }
                else {
                    Log.d("OTAActivity", "升级完成"+progress+"%");
                }
            }
        });
        bleFramework.initBLE();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleFramework.disconnect();
        BLEFramework.isOTA=false;
    }
}
