package com.renyu.blelibrary.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by renyu on 2017/1/16.
 */

public class RequestQueue {

    private volatile static RequestQueue queue;

    private BLEFramework bleFramework;
    private ExecutorService service;
    // 单信号量
    private Semaphore semaphore;
    // 循环队列
    private Thread looperThread;
    private Handler looperHandler;
    // 超时继续发射
    private Disposable delayDisposable;

    private RequestQueue(BLEFramework bleFramework) {
        this.bleFramework=bleFramework;

        service= Executors.newSingleThreadExecutor();
        semaphore=new Semaphore(1);
        looperThread=new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looperHandler=new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        // 添加
                        if (msg.what==0x111) {
                            service.submit((Runnable) msg.obj);
                        }
                        // 释放
                        else if (msg.what==0x112) {
                            if (delayDisposable!=null && !delayDisposable.isDisposed()) {
                                delayDisposable.dispose();
                                delayDisposable=null;
                            }
                            Log.d("RequestQueue", "已释放");
                            semaphore.release();
                        }
                    }
                };
                Looper.loop();
            }
        });
        looperThread.start();
    }

    public static RequestQueue getQueueInstance(BLEFramework bleFramework) {
        if (queue==null) {
            synchronized (RequestQueue.class) {
                if (queue==null) {
                    queue=new RequestQueue(bleFramework);
                }
            }
        }
        return queue;
    }

    /**
     * 加入队列
     * @param bytes
     */
    public void add(final byte[] bytes) {
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("RequestQueue", "添加");
                Observable.timer(5, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        delayDisposable = d;
                    }

                    @Override
                    public void onNext(Long value) {
                        release();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
                bleFramework.writeCharacteristic(bytes);
            }
        };
        Message message=new Message();
        message.what=0x111;
        message.obj=runnable;
        looperHandler.sendMessage(message);
    }

    /**
     * 释放信号量
     */
    public void release() {
        looperHandler.sendEmptyMessage(0x112);
    }
}
