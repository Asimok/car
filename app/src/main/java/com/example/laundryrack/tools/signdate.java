package com.example.laundryrack.tools;

import android.content.Context;
import android.content.SharedPreferences;


public class signdate {


    private static signdate instance = null;
    private final SharedPreferences p;

    private signdate(Context context) {
        p = context.getSharedPreferences("data", Context.MODE_PRIVATE);
    }

    public static signdate getInstance(Context context) {
        if (instance == null) {
            instance = new signdate(context);
        }
        return instance;
    }

    public String getSignDates() {
        return p.getString("currentDate", "null");
    }

    public void setSignDates(String name) {
        p.edit().putString("currentDate", name).apply();
    }



}

