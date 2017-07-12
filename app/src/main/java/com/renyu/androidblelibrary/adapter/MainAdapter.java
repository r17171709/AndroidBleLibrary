package com.renyu.androidblelibrary.adapter;

import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.renyu.androidblelibrary.R;
import com.renyu.androidblelibrary.params.Params;
import com.renyu.androidblelibrary.service.BLEService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/6/16.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    ArrayList<String> models;
    Context context;

    public MainAdapter(ArrayList<String> models, Context context) {
        this.models = models;
        this.context = context;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.adapter_main, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        holder.tv_adapter_main.setText(models.get(position));
        holder.tv_adapter_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position==0) {
                    BLEService.sendReadCommand(Params.UUID_SERVICE_BATTERY, Params.UUID_SERVICE_BATTERY_READ, context);
                }
                if (position==1) {
                    BLEService.sendReadCommand(Params.UUID_SERVICE_DEVICEINFO, Params.UUID_SERVICE_DEVICEINFO_NAME, context);
                }
                if (position==2) {
                    BLEService.sendReadCommand(Params.UUID_SERVICE_DEVICEINFO, Params.UUID_SERVICE_DEVICEINFO_ID, context);
                }
                if (position==3) {
                    BLEService.sendReadCommand(Params.UUID_SERVICE_DEVICEINFO, Params.UUID_SERVICE_DEVICEINFO_VERSION, context);
                }
                if (position==4) {
                    BLEService.sendReadCommand(Params.UUID_SERVICE_DEVICEINFO, Params.UUID_SERVICE_DEVICEINFO_CPUID, context);
                }
                if (position==5) {
                    HashMap<String, String> params=new HashMap<>();
                    Calendar calendar= Calendar.getInstance();
                    SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    params.put("time", format.format(calendar.getTime()));
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_TIME, params, context);
                }
                if (position==6) {
                    HashMap<String, String> params=new HashMap<>();
                    params.put("hand", "1");
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_HAND, params, context);
                }
                if (position==7) {
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_BINDTEETH, null, context);
                }
                if (position==8) {
                    HashMap<String, String> params=new HashMap<>();
                    params.put("userid", "11");
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_SETUSERID, params, context);
                }
                if (position==9) {
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_GETCURRENTTEETHINFO, null, context);
                }
                if (position==10) {
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_GETALLCOUNT, null, context);
                }
                if (position==11) {
                    HashMap<String, String> params=new HashMap<>();
                    params.put("serial", "0");
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_GETONEINFO, params, context);
                }
                if (position==12) {
                    HashMap<String, String> params=new HashMap<>();
                    params.put("duration", "120");
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_GAMESTART, params, context);
                }
                if (position==13) {
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_CLEANTEETH, null, context);
                }
                if (position==14) {
                    HashMap<String, String> params=new HashMap<>();
                    params.put("pos", "1");
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_SENDTEETHPOSTION, params, context);
                }
                if (position==15) {
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_GETUID, null, context);
                }
                if (position==16) {
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_UPDATE, null, context);
                }
                if (position==17) {
                    BLEService.ota(context, Environment.getExternalStorageDirectory().getPath()+"/smartbrush/file/BLE_OTA_Bootloadable.cyacd");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {

        TextView tv_adapter_main;

        public MainViewHolder(View itemView) {
            super(itemView);

            tv_adapter_main=itemView.findViewById(R.id.tv_adapter_main);
        }
    }
}
