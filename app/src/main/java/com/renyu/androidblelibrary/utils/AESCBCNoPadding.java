package com.renyu.androidblelibrary.utils;

import org.bouncycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCBCNoPadding {
    private static byte[] sessionkey = new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF, (byte) 0xFF, (byte) 0xEE, (byte) 0xDD, (byte) 0xCC, (byte) 0xBB, (byte) 0xAA, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD};

    public static byte[] encryption(String value) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();
            byte[] dataBytes = value.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }
            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
            SecretKeySpec keyspec = new SecretKeySpec(sessionkey, "AES");
            IvParameterSpec ivspec = new IvParameterSpec(sessionkey);
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decryption(byte[] data) {
        try {
            byte[] encrypted1 = Base64.decode((new String(Base64.encode(data))));
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(sessionkey, "AES");
            IvParameterSpec ivspec = new IvParameterSpec(sessionkey);
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            return cipher.doFinal(encrypted1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
