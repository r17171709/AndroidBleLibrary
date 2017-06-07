package com.renyu.blelibrary.impl;

import java.util.UUID;

/**
 * Created by renyu on 2017/6/7.
 */

public interface BLEReadResponseListener {
    void getResponseValues(UUID CharacUUID, byte[] value);
}
