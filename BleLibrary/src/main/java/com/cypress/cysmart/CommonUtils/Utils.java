package com.cypress.cysmart.CommonUtils;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.cypress.cysmart.BLEConnectionServices.BluetoothLeService;

import java.util.Locale;

/**
 * Class for commonly used methods in the project
 */
public class Utils {

    // Shared preference constant
    private static final String SHARED_PREF_NAME = "CySmart Shared Preference";
    public static final Locale DATA_LOCALE = Locale.ROOT;

    /**
     * Adding the necessary INtent filters for Broadcast receivers
     *
     * @return {@link IntentFilter}
     */
    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothLeService.ACTION_OTA_STATUS);//CYACD
        filter.addAction(BluetoothLeService.ACTION_OTA_STATUS_V1);//CYACD2
        return filter;
    }

    public static String byteArrayToHex(byte[] bytes) {
        if (bytes != null) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(formatForRootLocale("%02X ", b));
            }
            return sb.toString();
        }
        return "";
    }

    public static String getMSB(String string) {
        StringBuilder msbString = new StringBuilder();
        for (int i = string.length(); i > 0; i -= 2) {
            String str = string.substring(i - 2, i);
            msbString.append(str);
        }
        return msbString.toString();
    }

    /**
     * Setting the shared preference with values provided as parameters
     *
     * @param context
     * @param key
     * @param value
     */
    public static final void setStringSharedPreference(Context context, String key, String value) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Returning the stored values in the shared preference with values provided
     * as parameters
     *
     * @param context
     * @param key
     * @return
     */
    public static final String getStringSharedPreference(Context context, String key) {
        if (context != null) {
            SharedPreferences pref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            return pref.getString(key, "");
        }
        return "";
    }

    /**
     * Setting the shared preference with values provided as parameters
     *
     * @param context
     * @param key
     * @param value
     */
    public static final void setIntSharedPreference(Context context, String key, int value) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Returning the stored values in the shared preference with values provided
     * as parameters
     *
     * @param context
     * @param key
     * @return
     */
    public static final int getIntSharedPreference(Context context, String key) {
        if (context != null) {
            SharedPreferences pref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            return pref.getInt(key, 0);
        }
        return 0;
    }

    public static final void setBooleanSharedPreference(Context context, String key, boolean value) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static final boolean getBooleanSharedPreference(Context context, String key) {
        SharedPreferences Preference = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return Preference.getBoolean(key, false);
    }

    public static String formatForRootLocale(String format, Object... args) {
        return String.format(DATA_LOCALE, format, args);
    }
}
