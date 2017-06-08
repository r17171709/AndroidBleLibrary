package com.renyu.androidblelibrary.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.renyu.androidblelibrary.bean.BLECommandModel;
import com.renyu.androidblelibrary.params.Params;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.impl.BLEConnectListener;
import com.renyu.blelibrary.impl.BLEOTAListener;
import com.renyu.blelibrary.impl.BLERSSIListener;
import com.renyu.blelibrary.impl.BLEReadResponseListener;
import com.renyu.blelibrary.impl.BLEStateChangeListener;
import com.renyu.blelibrary.impl.BLEWriteResponseListener;
import com.renyu.blelibrary.params.CommonParams;
import com.renyu.blelibrary.utils.BLEFramework;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by renyu on 2017/6/6.
 */

public class BLEService extends Service {

    BLEFramework bleFramework;

    private final static int PACKET_LENGTH = 20;
    private final static int PACKET_PAYLOAD = 17;

    //收到的数据列
    private static HashMap<String, LinkedList<byte[]>> receiverCommandMaps;

    @Override
    public void onCreate() {
        super.onCreate();

        receiverCommandMaps=new HashMap<>();

        bleFramework=BLEFramework.getBleFrameworkInstance();
        bleFramework.setParams(this.getApplicationContext(),
                Params.UUID_SERVICE_MILI,
                Params.UUID_SERVICE_OTASERVICE,
                Params.UUID_SERVICE_WRITE,
                Params.UUID_SERVICE_READ,
                Params.UUID_SERVICE_OTA,
                Params.UUID_DESCRIPTOR,
                Params.UUID_DESCRIPTOR_OTA);
        bleFramework.setBleConnectListener(new BLEConnectListener() {
            @Override
            public void getAllScanDevice(BLEDevice bleDevice) {
                Log.d("BLEService", bleDevice.getDevice().getName() + " " + bleDevice.getDevice().getAddress());
                if (bleDevice.getDevice().getName()!=null && bleDevice.getDevice().getName().startsWith("iite")) {
                    EventBus.getDefault().post(bleDevice);
                }
            }
        });
        bleFramework.setBleStateChangeListener(new BLEStateChangeListener() {
            @Override
            public void getCurrentState(int currentState) {
                Log.d("BLEService", "currentState:" + currentState);
            }
        });
        bleFramework.setBLEWriteResponseListener(new BLEWriteResponseListener() {
            @Override
            public void getResponseValues(byte[] value) {
                putCommand(value);
            }
        });
        bleFramework.setBleReadResponseListener(new BLEReadResponseListener() {
            @Override
            public void getResponseValues(UUID CharacUUID, byte[] value) {
                putReadCommand(CharacUUID, value);
            }
        });
        bleFramework.setBlerssiListener(new BLERSSIListener() {
            @Override
            public void getRssi(int rssi) {
                Log.d("BLEService", "rssi:" + rssi);
            }
        });
        bleFramework.setBleotaListener(new BLEOTAListener() {
            @Override
            public void showProgress(int progress) {

            }
        });
        bleFramework.initBLE();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null && intent.getStringExtra(CommonParams.COMMAND)!=null) {
            if (intent.getStringExtra(CommonParams.COMMAND).equals(CommonParams.SCAN)) {
                bleFramework.startScan();
            }
            if (intent.getStringExtra(CommonParams.COMMAND).equals(CommonParams.CONN)) {
                bleFramework.startConn((BluetoothDevice) intent.getParcelableExtra(CommonParams.DEVICE));
            }
            if (intent.getStringExtra(CommonParams.COMMAND).equals(CommonParams.SCANCONN)) {
                bleFramework.startScanAndConn(intent.getStringExtra(CommonParams.DEVICE));
            }
            if (intent.getStringExtra(CommonParams.COMMAND).equals(CommonParams.WRITE)) {
                bleFramework.addWriteCommand(intent.getByteArrayExtra(CommonParams.BYTECODE));
            }
            if (intent.getStringExtra(CommonParams.COMMAND).equals(CommonParams.READ)) {
                bleFramework.addReadCommand((UUID) intent.getSerializableExtra(CommonParams.SERVICEUUID), (UUID) intent.getSerializableExtra(CommonParams.CHARACUUID));
            }
            if (intent.getStringExtra(CommonParams.COMMAND).equals(CommonParams.RSSI)) {
                bleFramework.readRSSI();
            }
            if (intent.getStringExtra(CommonParams.COMMAND).equals(CommonParams.DISCONN)) {
                bleFramework.disconnect();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 发送写指令
     * @param command
     * @param params
     * @param context
     */
    public static void sendWriteCommand(int command, HashMap<String, String> params, Context context) {
        String values=getBLECommand(command, params);
        byte[][] bytes_send = getDivided_data(values, command);
        for (byte[] bytes : bytes_send) {
            Intent intent=new Intent(context, BLEService.class);
            intent.putExtra(CommonParams.COMMAND, CommonParams.WRITE);
            intent.putExtra(CommonParams.BYTECODE, bytes);
            context.startService(intent);
        }
    }

    /**
     * 发送读指令
     * @param serviceUUID
     * @param CharacUUID
     * @param context
     */
    public static void sendReadCommand(final UUID serviceUUID, final UUID CharacUUID, Context context) {
        Intent intent=new Intent(context, BLEService.class);
        intent.putExtra(CommonParams.COMMAND, CommonParams.READ);
        intent.putExtra(CommonParams.SERVICEUUID, serviceUUID);
        intent.putExtra(CommonParams.CHARACUUID, CharacUUID);
        context.startService(intent);
    }

    /**
     * 连接设备
     * @param bluetoothDevice
     * @param context
     */
    public static void conn(BluetoothDevice bluetoothDevice, Context context) {
        Intent intent=new Intent(context, BLEService.class);
        intent.putExtra(CommonParams.COMMAND, CommonParams.CONN);
        intent.putExtra(CommonParams.DEVICE, bluetoothDevice);
        context.startService(intent);
    }

    /**
     * 断开连接
     * @param context
     */
    public static void disconn(Context context) {
        Intent intent=new Intent(context, BLEService.class);
        intent.putExtra(CommonParams.COMMAND, CommonParams.DISCONN);
        context.startService(intent);
    }

    /**
     * 扫描设备
     * @param context
     */
    public static void scan(Context context) {
        Intent intent=new Intent(context, BLEService.class);
        intent.putExtra(CommonParams.COMMAND, CommonParams.SCAN);
        context.startService(intent);
    }

    /**
     * 扫描并连接指定设备
     * @param context
     */
    public static void scanAndConn(Context context, String deviceName) {
        Intent intent=new Intent(context, BLEService.class);
        intent.putExtra(CommonParams.COMMAND, CommonParams.SCANCONN);
        intent.putExtra(CommonParams.DEVICE, deviceName);
        context.startService(intent);
    }

    /**
     * 读取RSSI
     * @param context
     */
    public static void readRSSI(Context context) {
        Intent intent=new Intent(context, BLEService.class);
        intent.putExtra(CommonParams.COMMAND, CommonParams.RSSI);
        context.startService(intent);
    }

    /**
     * 指令封装
     * @param command
     * @param params
     * @return
     */
    private static String getBLECommand(int command, HashMap<String, String> params) {
        try {
            JSONObject object=new JSONObject();
            object.put("command", command);
            JSONObject param=new JSONObject();
            if (params!=null && params.size()>0) {
                Iterator<Map.Entry<String, String>> iterator=params.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry=iterator.next();
                    try {
                        param.put(entry.getKey(), Integer.parseInt(entry.getValue()));
                    } catch (NumberFormatException e) {
                        param.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            else {
                param.put("NULL", "NULL");
            }
            object.put("param", param);
            Log.d("BLEService", object.toString());
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static byte[][] getDivided_data(String json_str, int command) {
        byte[] bytes = json_str.getBytes();
        int total=bytes.length%PACKET_PAYLOAD==0?bytes.length/PACKET_PAYLOAD:bytes.length/PACKET_PAYLOAD+1;
        byte[][] divided_byte = new byte[total][];
        for (int i=0;i<total;i++) {
            if (total-1!=i) {
                divided_byte[i]=new byte[PACKET_LENGTH];
                for (int j=0;j<PACKET_LENGTH;j++) {
                    if (j==0) {
                        divided_byte[i][0]= (byte) (i+1);
                    }
                    else if (j==1) {
                        divided_byte[i][1]= (byte) total;
                    }
                    else if (j==2) {
                        divided_byte[i][2]= (byte) command;
                    }
                    else {
                        divided_byte[i][j]= bytes[(i*PACKET_PAYLOAD+(j-3))];
                    }
                }
            }
            else {
                divided_byte[i]=new byte[bytes.length-PACKET_PAYLOAD*(total-1)+3];
                for (int j=0;j<(bytes.length-PACKET_PAYLOAD*(total-1)+3);j++) {
                    if (j==0) {
                        divided_byte[i][0]= (byte) (i+1);
                    }
                    else if (j==1) {
                        divided_byte[i][1]= (byte) total;
                    }
                    else if (j==2) {
                        divided_byte[i][2]= (byte) command;
                    }
                    else {
                        divided_byte[i][j]= bytes[(i*PACKET_PAYLOAD+(j-3))];
                    }
                }
            }
        }
        return divided_byte;
    }

    private static String getOrigin_str(byte[][] byte_str) {
        String origin_str;
        int number=0;
        for (int i=0;i<byte_str.length;i++) {
            for (int j=3;j<byte_str[i].length;j++) {
                if (byte_str[i][j]!=0) {
                    number+=1;
                }
            }
        }
        byte[] bytes=new byte[number];
        int index=0;
        for (int i=0;i<byte_str.length;i++) {
            for (int j=3;j<byte_str[i].length;j++) {
                if (byte_str[i][j]!=0) {
                    bytes[index]=byte_str[i][j];
                    index++;
                }
            }
        }
        try {
            origin_str=new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            origin_str="";
        }
        return origin_str;
    }

    /**
     * 解析写指令
     * @param bytes
     */
    private static synchronized void putCommand(byte[] bytes) {
        int command= com.renyu.blelibrary.utils.Utils.convert2To10(com.renyu.blelibrary.utils.Utils.sign2nosign(bytes[2]));
        //最后一条数据
        if (bytes[0]==bytes[1]) {
            //判断指令是否在集合中，如果不在则忽略本条指令
            if (receiverCommandMaps.containsKey(""+command)) {
                LinkedList<byte[]> list=receiverCommandMaps.get(""+command);
                list.add(bytes);
                //如果集合中数量跟指令条数一直，则进一步判断，否则则忽略本条指令
                if (list.size()==bytes[1]) {
                    byte[][] bytes1=new byte[list.size()][];
                    for (int i=0;i<bytes1.length;i++) {
                        bytes1[i]=list.get(i);
                    }
                    String result=getOrigin_str(bytes1);
                    //符合json数据格式，则认为是有效指令，并将其转发
                    try {
                        // 测试指令是否正常
                        new JSONObject(result);

                        BLECommandModel model=new BLECommandModel();
                        model.setCommand(command);
                        model.setValue(result);
                        EventBus.getDefault().post(model);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("BLEService", "指令错误");
                    } finally {
                        //清除指令
                        receiverCommandMaps.remove(""+command);
                    }
                }
            }
        }
        else {
            LinkedList<byte[]> list;
            if (receiverCommandMaps.containsKey(""+command)) {
                list=receiverCommandMaps.get(""+command);
            }
            else {
                list=new LinkedList<>();
            }
            list.add(bytes);
            receiverCommandMaps.put(""+command, list);
        }
    }

    /**
     * 解析特定的读指令
     * @param bytes
     * @param uuid
     */
    public void putReadCommand(UUID uuid, byte[] bytes) {
        BLECommandModel model=new BLECommandModel();
        if (uuid.toString().equals(Params.UUID_SERVICE_BATTERY_READ.toString())) {
            model.setCommand(Params.BLE_COMMAND_BATTERY);
            model.setValue(""+bytes[0]);
        }
        else if (uuid.toString().equals(Params.UUID_SERVICE_DEVICEINFO_NAME.toString())) {
            model.setCommand(Params.BLE_COMMAND_INFONAME);
            model.setValue(new String(bytes));
        }
        else if (uuid.toString().equals(Params.UUID_SERVICE_DEVICEINFO_ID.toString())) {
            model.setCommand(Params.BLE_COMMAND_INFOID);
            model.setValue(new String(bytes));
        }
        else if (uuid.toString().equals(Params.UUID_SERVICE_DEVICEINFO_VERSION.toString())) {
            model.setCommand(Params.BLE_COMMAND_INFOVERSION);
            model.setValue(new String(bytes));
        }
        else if (uuid.toString().equals(Params.UUID_SERVICE_DEVICEINFO_CPUID.toString())) {
            model.setCommand(Params.BLE_COMMAND_CPUID);
            model.setValue(new String(bytes));
        }
        EventBus.getDefault().post(model);
    }
}
