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
                    BLEService2.verifyUID("654321");
                }
                else if (holder.getLayoutPosition()==1) {
                    BLEService2.verifyCode("8888");
                }
                else if (holder.getLayoutPosition()==2) {
                    BLEService2.hourTotalStep();
                }
                else if (holder.getLayoutPosition()==3) {
                    BLEService2.setTime();
                }
                else if (holder.getLayoutPosition()==4) {
                    BLEService2.readEBC();
                }
                else if (holder.getLayoutPosition()==5) {
                    BLEService2.setEBC(1, 0, 2, 0, 3, 0, 0, 0, 0, 0, 400);
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
