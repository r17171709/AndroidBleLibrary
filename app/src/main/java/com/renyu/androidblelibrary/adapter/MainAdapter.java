package com.renyu.androidblelibrary.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
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
import java.util.Locale;

/**
 * Created by Administrator on 2017/6/16.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private ArrayList<String> models;
    private Context context;

    public MainAdapter(ArrayList<String> models, Context context) {
        this.models = models;
        this.context = context;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.adapter_main, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MainViewHolder holder, int position) {
        holder.tv_adapter_main.setText(models.get(position));
        holder.tv_adapter_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getLayoutPosition()==0) {
                    BLEService.sendReadCommand(Params.UUID_SERVICE_BATTERY, Params.UUID_SERVICE_BATTERY_READ, context);
                }
                if (holder.getLayoutPosition()==1) {
                    BLEService.sendReadCommand(Params.UUID_SERVICE_DEVICEINFO, Params.UUID_SERVICE_DEVICEINFO_NAME, context);
                }
                if (holder.getLayoutPosition()==2) {
                    BLEService.sendReadCommand(Params.UUID_SERVICE_DEVICEINFO, Params.UUID_SERVICE_DEVICEINFO_ID, context);
                }
                if (holder.getLayoutPosition()==3) {
                    BLEService.sendReadCommand(Params.UUID_SERVICE_DEVICEINFO, Params.UUID_SERVICE_DEVICEINFO_VERSION, context);
                }
                if (holder.getLayoutPosition()==4) {
                    BLEService.sendReadCommand(Params.UUID_SERVICE_DEVICEINFO, Params.UUID_SERVICE_DEVICEINFO_CPUID, context);
                }
                if (holder.getLayoutPosition()==5) {
                    HashMap<String, String> params=new HashMap<>();
                    Calendar calendar= Calendar.getInstance();
                    SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    params.put("time", format.format(calendar.getTime()));
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_TIME, params, context);
                }
                if (holder.getLayoutPosition()==6) {
                    HashMap<String, String> params=new HashMap<>();
                    params.put("hand", "1");
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_HAND, params, context);
                }
                if (holder.getLayoutPosition()==7) {
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_BINDTEETH, null, context);
                }
                if (holder.getLayoutPosition()==8) {
                    HashMap<String, String> params=new HashMap<>();
                    params.put("userid", "11");
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_SETUSERID, params, context);
                }
                if (holder.getLayoutPosition()==9) {
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_GETCURRENTTEETHINFO, null, context);
                }
                if (holder.getLayoutPosition()==10) {
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_GETALLCOUNT, null, context);
                }
                if (holder.getLayoutPosition()==11) {
                    HashMap<String, String> params=new HashMap<>();
                    params.put("serial", "0");
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_GETONEINFO, params, context);
                }
                if (holder.getLayoutPosition()==12) {
                    HashMap<String, String> params=new HashMap<>();
                    params.put("duration", "120");
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_GAMESTART, params, context);
                }
                if (holder.getLayoutPosition()==13) {
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_CLEANTEETH, null, context);
                }
                if (holder.getLayoutPosition()==14) {
                    HashMap<String, String> params=new HashMap<>();
                    params.put("pos", "1");
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_SENDTEETHPOSTION, params, context);
                }
                if (holder.getLayoutPosition()==15) {
                    BLEService.sendWriteCommand(Params.BLE_COMMAND_GETUID, null, context);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView tv_adapter_main;

        MainViewHolder(View itemView) {
            super(itemView);

            tv_adapter_main=itemView.findViewById(R.id.tv_adapter_main);
        }
    }
}
