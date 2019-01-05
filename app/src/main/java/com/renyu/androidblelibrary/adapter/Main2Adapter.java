package com.renyu.androidblelibrary.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.renyu.androidblelibrary.R;
import com.renyu.androidblelibrary.service.BLEService2;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/16.
 */

public class Main2Adapter extends RecyclerView.Adapter<Main2Adapter.MainViewHolder> {

    private ArrayList<String> models;
    private Context context;

    public Main2Adapter(ArrayList<String> models, Context context) {
        this.models = models;
        this.context = context;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_main, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MainViewHolder holder, int position) {
        holder.tv_adapter_main.setText(models.get(position));
        holder.tv_adapter_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getLayoutPosition() == 0) {
                    BLEService2.verifyUID("654321", holder.itemView.getContext());
                } else if (holder.getLayoutPosition() == 1) {
                    BLEService2.verifyCode("8888", holder.itemView.getContext());
                } else if (holder.getLayoutPosition() == 2) {
                    BLEService2.setTime(holder.itemView.getContext());
                } else if (holder.getLayoutPosition() == 3) {
                    BLEService2.readEBC(holder.itemView.getContext());
                } else if (holder.getLayoutPosition() == 4) {
                    BLEService2.setEBC(1, 0, 2, 0, 3, 0, 0, 0, 0, 0, 400, holder.itemView.getContext());
                } else if (holder.getLayoutPosition() == 5) {
                    BLEService2.hourTotalStep(holder.itemView.getContext());
                } else if (holder.getLayoutPosition() == 6) {
                    BLEService2.dayTotalStep(holder.itemView.getContext());
                } else if (holder.getLayoutPosition() == 7) {
                    BLEService2.heartRate(holder.itemView.getContext());
                } else if (holder.getLayoutPosition() == 8) {
                    BLEService2.sleepTime(holder.itemView.getContext());
                } else if (holder.getLayoutPosition() == 9) {
                    BLEService2.writeUserInfo(holder.itemView.getContext(), 1, 180, 85, 10000);
                } else if (holder.getLayoutPosition() == 10) {
                    BLEService2.settingSedentaryReminder(holder.itemView.getContext(), 1);
                } else if (holder.getLayoutPosition() == 11) {
                    BLEService2.sedentaryReminder(holder.itemView.getContext());
                } else if (holder.getLayoutPosition() == 12) {
                    BLEService2.allAlarmInfo(holder.itemView.getContext());
                } else if (holder.getLayoutPosition() == 13) {
                    BLEService2.eventDelete(holder.itemView.getContext(), 10);
//                    BLEService2.eventAdd(holder.itemView.getContext(), 3, System.currentTimeMillis(), 0x81, "Hello你好你叫什么名字你住哪里你今年多大");
//                    BLEService2.eventAdd(holder.itemView.getContext(), 4, System.currentTimeMillis(), 0x82, "Hello你好你叫什么名字你住哪里你今年多大");
//                    BLEService2.eventAdd(holder.itemView.getContext(), 5, System.currentTimeMillis(), 0x01, "Hello你好你叫什么名字你住哪里你今年多大");
//                    BLEService2.eventAdd(holder.itemView.getContext(), 6, System.currentTimeMillis(), 0x02, "Hello你好你叫什么名字你住哪里你今年多大");
//                    BLEService2.eventAdd(holder.itemView.getContext(), 7, System.currentTimeMillis(), 0x81 | 0x82 | 0x84 | 0x88 | 0x90 | 0xA0 | 0xC0, "Hello你好你叫什么名字你住哪里你今年多大");
//                    BLEService2.eventAdd(holder.itemView.getContext(), 8, System.currentTimeMillis(), 0xA0 | 0xC0, "Hello你好你叫什么名字你住哪里你今年多大");
                    BLEService2.eventAdd(holder.itemView.getContext(), 10, true, System.currentTimeMillis(), 0x81 | 0x84, "Hello你好你叫什么名字你住哪里你今年多大");
                } else if (holder.getLayoutPosition() == 14) {
                    BLEService2.alarmClockAdd(holder.itemView.getContext(), 76, true, System.currentTimeMillis(), 0x00, true);
                } else if (holder.getLayoutPosition() == 15) {
                    BLEService2.sedentaryByAlarmAdd(holder.itemView.getContext(), 51, true, System.currentTimeMillis(), 0x00);
                } else if (holder.getLayoutPosition() == 16) {
                    BLEService2.noDisturb(holder.itemView.getContext(), 1, 0, 22, 0, 6);
                } else if (holder.getLayoutPosition() == 17) {
                    BLEService2.readAlarm(holder.itemView.getContext(), 9);
                } else if (holder.getLayoutPosition() == 18) {
                    BLEService2.sportsRun(holder.itemView.getContext());
                } else if (holder.getLayoutPosition() == 19) {
                    BLEService2.sportsFastWalk(holder.itemView.getContext());
                } else if (holder.getLayoutPosition() == 20) {
                    BLEService2.sportsSwim(holder.itemView.getContext());
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

            tv_adapter_main = itemView.findViewById(R.id.tv_adapter_main);
        }
    }
}
