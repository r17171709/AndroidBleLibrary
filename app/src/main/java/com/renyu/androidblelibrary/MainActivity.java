package com.renyu.androidblelibrary;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.renyu.blelibrary.params.CommonParams;
import com.renyu.blelibrary.utils.BLEService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent=new Intent(this, BLEService.class);
        intent.putExtra(CommonParams.COMMAND, CommonParams.SCAN);
        intent.putExtra("UUID_SERVICE", Params.UUID_SERVICE_MILI);
        intent.putExtra("UUID_OTASERVICE", Params.UUID_SERVICE_OTASERVICE);
        intent.putExtra("UUID_Characteristic_WRITE", Params.UUID_SERVICE_WRITE);
        intent.putExtra("UUID_Characteristic_READ", Params.UUID_SERVICE_READ);
        intent.putExtra("UUID_OTACharacteristic", Params.UUID_SERVICE_OTA);
        intent.putExtra("UUID_DESCRIPTOR", Params.UUID_DESCRIPTOR);
        intent.putExtra("UUID_OTADESCRIPTOR", Params.UUID_DESCRIPTOR_OTA);
        startService(intent);
    }
}
