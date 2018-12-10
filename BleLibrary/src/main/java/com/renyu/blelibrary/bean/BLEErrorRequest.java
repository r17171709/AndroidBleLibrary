package com.renyu.blelibrary.bean;

import java.util.UUID;

public class BLEErrorRequest {
    UUID serviceUUID;
    UUID characUUID;
    byte[] bytes;

    public UUID getServiceUUID() {
        return serviceUUID;
    }

    public void setServiceUUID(UUID serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    public UUID getCharacUUID() {
        return characUUID;
    }

    public void setCharacUUID(UUID characUUID) {
        this.characUUID = characUUID;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
