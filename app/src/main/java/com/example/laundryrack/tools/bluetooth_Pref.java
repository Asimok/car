package com.example.laundryrack.tools;

import android.content.Context;
import android.content.SharedPreferences;


public class bluetooth_Pref {


    private static bluetooth_Pref instance = null;
    private final SharedPreferences p;

    private bluetooth_Pref(Context context) {
        p = context.getSharedPreferences("data", Context.MODE_PRIVATE);
    }

    public static bluetooth_Pref getInstance(Context context) {
        if (instance == null) {
            instance = new bluetooth_Pref(context);
        }
        return instance;
    }

    public String getBluetoothName() {
        return p.getString("BluetoothName", "null");
    }

    public void setBluetoothName(String name) {
        p.edit().putString("BluetoothName", name).apply();
    }


    public String getBluetoothAd() {
        return p.getString("BluetoothAd", "null");
    }

    public void setBluetoothAd(String Ad) {
        p.edit().putString("BluetoothAd", Ad).apply();
    }
}

