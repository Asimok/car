package com.example.laundryrack;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.laundryrack.tools.bluetooth_Pref;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Set;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class Bluetooth_band extends Activity implements OnItemClickListener {
    private String address;

    // 获取到蓝牙适配器
    public BluetoothAdapter mBluetoothAdapter;
    // 用来保存搜索到的设备信息
    public List<String> bluetoothDevices = new ArrayList<String>();
    // ListView组件
    public ListView lvDevices;
    // ListView的字符串数组适配器
    public ArrayAdapter<String> arrayAdapter;
    public TextView select;
    private bluetooth_Pref blue_sp;

    // 注册广播接收者
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            // 获取到广播的action
            String action = intent.getAction();
            // 判断广播是搜索到设备还是搜索完成
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                // 找到设备后获取其设备
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 判断这个设备是否是之前已经绑定过了，如果是则不需要添加，在程序初始化的时候已经添加了
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 设备没有绑定过，则将其保持到arrayList集合中
                    Log.d("aa", "设备没有绑定过   " + device.getName() + ":"
                            + device.getAddress() + "\n");
                    short rssi = intent.getExtras().getShort(
                            BluetoothDevice.EXTRA_RSSI);
                    int iRssi = abs(rssi);
                    // 将蓝牙信号强度换算为距离
                    double power = (iRssi - 59) / 25.0;
                    String mm = new Formatter().format("%.2f", pow(10, power)).toString();
                    Log.d("aa", "距离    " + mm);
                    bluetoothDevices.add(device.getName() + ":  "
                            + device.getAddress() + "  距离:  " + mm + "m" + "\n");
                    // 更新字符串数组适配器，将内容显示在listView中
                    arrayAdapter.notifyDataSetChanged();

                } else {
                    Log.d("aa", "设备绑定过   " + device.getName() + ":"
                            + device.getAddress() + "\n");
                    short rssi = intent.getExtras().getShort(
                            BluetoothDevice.EXTRA_RSSI);
                    int iRssi = abs(rssi);
                    // 将蓝牙信号强度换算为距离
                    double power = (iRssi - 59) / 25.0;
                    String mm = new Formatter().format("%.2f", pow(10, power)).toString();

                    Log.d("aa", "距离    " + mm);


                    bluetoothDevices.add(device.getName() + ":  "
                            + device.getAddress() + "  距离:  " + mm + "m   已配对" + "\n");
                    // 更新字符串数组适配器，将内容显示在listView中
                    arrayAdapter.notifyDataSetChanged();
                }
            } else if (action
                    .equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {

                Toast.makeText(Bluetooth_band.this, "搜索完成", Toast.LENGTH_SHORT).show();
                Log.d("aa", "搜索完成");
                select.setText("选择蓝牙设备绑定   搜索完成");
            }

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_buletooth_band);
        blue_sp = bluetooth_Pref.getInstance(this);

        select = (TextView) findViewById(R.id.select);
        initBluetooth();
        getPremession();//获取虚拟定位权限
        searchBluetooth();
    }

    private void searchBluetooth() {
        bluetoothDevices.clear();
        // 搜索周边设备，如果正在搜索，则暂停搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d("aa", "暂停搜索");
            select.setText("选择蓝牙设备绑定   暂停搜索");
        }

        mBluetoothAdapter.startDiscovery();
        Log.d("aa", "正在扫描...");
        select.setText("选择蓝牙设备绑定   正在扫描...");
    }

    private void initBluetooth() {
        // 获取到蓝牙默认的适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 获取到ListView组件
        lvDevices = (ListView) findViewById(R.id.lvDevices);
        // 为listview设置字符换数组适配器
        arrayAdapter = new ArrayAdapter<String>(this,
                R.layout.simple_list_item, android.R.id.text1,
                bluetoothDevices);
        // 为listView绑定适配器
        lvDevices.setAdapter(arrayAdapter);
        // 为listView设置item点击事件侦听
        lvDevices.setOnItemClickListener(this);

        // 用Set集合保持已绑定的设备   将绑定的设备添加到Set集合。
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();

        // 因为蓝牙搜索到设备和完成搜索都是通过广播来告诉其他应用的
        // 这里注册找到设备和完成搜索广播
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }


    // 点击listView中的设备，传送数据
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 获取到这个设备的信息
        String s = arrayAdapter.getItem(position);
        // 对其进行分割，获取到这个设备的地址
        address = s.substring((s.indexOf(":") + 1), s.indexOf("距")).trim();
        String name = s.substring(0, s.indexOf(":")).trim();
        select.setText(String.format("选择设备 ：%s  %s", name, address));
        Log.d("aa", "名称  " + name + "地址   " + address);
        blue_sp.setBluetoothAd(address);
        blue_sp.setBluetoothName(name);
        // 判断当前是否还是正在搜索周边设备，如果是则暂停搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

    }

    public void getPremession() {
        //判断是否有权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("aa", "模糊定位");
//请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    0x114);
//判断是否需要 向用户解释，为什么要申请该权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                Log.d("aa", "判断是否需要 向用户解释，为什么要申请该权限");
            }
        }
        // 判断手机是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 判断是否打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            // 弹出对话框提示用户是后打开
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
            // 不做提示，强行打开
            // mBluetoothAdapter.enable();
        } else {
            // 不做提示，强行打开
            mBluetoothAdapter.enable();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean grantedLocation = true;
        if (requestCode == 0x114) {
            Log.d("aa", "允许获取权限");
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    grantedLocation = false;
                }
            }
        }

        if (!grantedLocation) {
            Log.d("aa", "Permission error !!!");
            Toast.makeText(this, "定位权限已拒绝，请手动打开权限!", Toast.LENGTH_LONG).show();

        }
    }

}
