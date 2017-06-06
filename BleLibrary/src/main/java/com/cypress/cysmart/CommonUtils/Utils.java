package com.cypress.cysmart.CommonUtils;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class Utils {
    private static final String SHARED_PREF_NAME = "CySmart Shared Preference";

    public static String ByteArraytoHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int len$ = bytes.length;
        for (int i$ = 0; i$ < len$; i$++) {
            sb.append(String.format("%02X ", new Object[]{Byte.valueOf(bytes[i$])}));
        }
        return sb.toString();
    }

    public static String getMSB(String string) {
        StringBuilder msbString = new StringBuilder();
        for (int i = string.length(); i > 0; i -= 2) {
            msbString.append(string.substring(i - 2, i));
        }
        return msbString.toString();
    }

    private static int convertstringtobyte(String string) {
        return Integer.parseInt(string, 16);
    }

    public static final void setStringSharedPreference(Context context, String key, String value) {
        Editor editor = context.getSharedPreferences(SHARED_PREF_NAME, 0).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static final String getStringSharedPreference(Context context, String key) {
        if (context != null) {
            return context.getSharedPreferences(SHARED_PREF_NAME, 0).getString(key, "");
        }
        return "";
    }

    public static final void setIntSharedPreference(Context context, String key, int value) {
        Editor editor = context.getSharedPreferences(SHARED_PREF_NAME, 0).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static final int getIntSharedPreference(Context context, String key) {
        if (context != null) {
            return context.getSharedPreferences(SHARED_PREF_NAME, 0).getInt(key, 0);
        }
        return 0;
    }
}