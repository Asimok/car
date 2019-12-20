package com.example.laundryrack;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



import com.example.laundryrack.tools.Codes;
import com.example.laundryrack.tools.bluetooth_Pref;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    private boolean shortPress = false;
    private final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private OutputStream os;
    private ConnectedThread thread;
    boolean commected = true;
    private TextView tv_recive, tvBandBluetooth;
    private bluetooth_Pref blue_sp;
    // 获取到蓝牙适配器
    public BluetoothAdapter mBluetoothAdapter;

    private TextView tvflodstate;
    private ImageView imgflodstate;

    private TextToSpeech texttospeech;
    public Button startgetweight, initweight, recycletrush, hazaroustrush;

    BluetoothDevice lvDevice = null;
    private Toast mToast;
    BluetoothSocket lvSocket = null;
    private SharedPreferences mSharedPreferences;
    private boolean mTranslateEnable = false;

    private boolean bldrytrush = true, blwettrush = true, blrecycletrush = true, blhazaroustrush = true;
    int ret = 0;// 函数调用返回值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tvflodstate=findViewById(R.id.flodstate);
        imgflodstate=findViewById(R.id.imgflodstate);

        tv_recive = findViewById(R.id.tvrecive);
        tvBandBluetooth = (TextView) findViewById(R.id.tvBandBluetooth);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        blue_sp = bluetooth_Pref.getInstance(this);
        tvBandBluetooth.setText(String.format("已绑定设备：  %s  %s", blue_sp.getBluetoothName(), blue_sp.getBluetoothAd()));

        startgetweight = (Button) findViewById(R.id.startgetweight);
        initweight = (Button) findViewById(R.id.initweight);

        startgetweight.setOnClickListener(this);
startgetweight.setOnLongClickListener(this);
        initweight.setOnClickListener(this);
        texttospeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // 如果装载TTS引擎成功
                if (status == TextToSpeech.SUCCESS) {
                    // 设置使用美式英语朗读
                    int result = texttospeech.setLanguage(Locale.US);
                    // 如果不支持所设置的语言
                    if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                            && result != TextToSpeech.LANG_AVAILABLE) {
                        Log.d("ff", "TTS暂时不支持这种语言的朗读！");
                    }
                }
            }
        });
    }









    //右上角三个点
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /***
     * 向指定的蓝牙设备发送数据
     */
    public void send(String pvsMac, byte[] pvsContent) throws IOException {

        // 如果选择设备为空则代表还没有选择设备
        if (lvDevice == null) {
            //通过地址获取到该设备
            lvDevice = mBluetoothAdapter.getRemoteDevice(pvsMac);
        }
        // 这里需要try catch一下，以防异常抛出
        try {
            // 判断客户端接口是否为空
            if (lvSocket == null) {
                // 获取到客户端接口
                lvSocket = lvDevice
                        .createRfcommSocketToServiceRecord(MY_UUID);
                // 向服务端发送连接
                lvSocket.connect();

                // 获取到输出流，向外写数据
                os = lvSocket.getOutputStream();
                if (commected) {
                    commected = false;
                    // 实例接收客户端传过来的数据线程
                    thread = new ConnectedThread(lvSocket);
                    // 线程开始
                    thread.start();
                }
            }
            // 判断是否拿到输出流
            if (os != null) {
                // 需要发送的信息
                // 以utf-8的格式发送出去
                os.write(pvsContent);
            }
            // 吐司一下，告诉用户发送成功
            Toast.makeText(this, "发送信息成功，请查收", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // 如果发生异常则告诉用户发送失败
            Toast.makeText(this, "发送信息失败", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_1:
                Toast.makeText(this, "已经断开连接", Toast.LENGTH_SHORT).show();
                thread.cancel();

                break;
            case R.id.menu_2:
                Toast.makeText(this, "绑定蓝牙", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(MainActivity.this, Bluetooth_band.class);
                startActivity(intent1);
                break;

        }
        return true;
    }

    private boolean isNeedRequestPermissions(List<String> permissions) {
        // 定位精确位置
        addPermission(permissions, Manifest.permission.ACCESS_FINE_LOCATION);
        // 存储权限
        addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // 读取手机状态
        addPermission(permissions, Manifest.permission.READ_PHONE_STATE);
        return permissions.size() > 0;
    }

    private void addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 适配android M，检查权限
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isNeedRequestPermissions(permissions)) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
        }
    }

    @Override
    protected void onResume() {
        tvBandBluetooth.setText(String.format("已绑定设备：  %s  %s", blue_sp.getBluetoothName(), blue_sp.getBluetoothAd()));
        super.onResume();
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();

        thread.cancel();
    }


    // 创建handler，因为我们接收是采用线程来接收的，在线程中无法操作UI，所以需要handler
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            Log.e("cc1","收到 "+(String)msg.obj);

//            if (((String)msg.obj).contains("toast"))
//            {
//                Log.e("cc","tttttttttttttttt");
//                Toast.makeText(MainActivity.this, "已为您自动签到", Toast.LENGTH_SHORT).show();
//            }
//            else
//            { tv_recive.setText((String) msg.obj);
//                if (tv_recive.getText().toString().length()>2)
//                {
//                    String nowWeight=tv_recive.getText().toString();
//                    Log.e("cc",String.valueOf(nowWeight.length()));
//                    if(nowWeight.length()<8&&nowWeight.length()>3) {
//                        Log.e("cc",nowWeight.substring(0,nowWeight.indexOf("g")-1));
//                        if (Integer.parseInt((nowWeight.substring(0,nowWeight.indexOf("g")-1)))> 1000) {
//                          //  autoSign();
//                            tvflodstate.setText("已叠被");
//                            imgflodstate.setImageDrawable(getResources().getDrawable(R.drawable.flod));
//                        }
//                        else
//                        {
//                            tvflodstate.setText("未叠被");
//                            imgflodstate.setImageDrawable(getResources().getDrawable(R.drawable.unflod));
//                        }
//
//                    }
//                }}
            tv_recive.setText((String) msg.obj);

        }
    };
    @Override
    public void onClick(View view) {
        //TODO 按钮点击
        switch (view.getId()) {
            case R.id.startgetweight:
                try {
                    if (bldrytrush) {
                        send(blue_sp.getBluetoothAd(), Codes.upLundary);
                        startgetweight.setBackgroundResource(R.drawable.btn_close);
                        startgetweight.setText("关闭");
                        bldrytrush = false;
                    } else {
                        startgetweight.setText("开启");

                        send(blue_sp.getBluetoothAd(), Codes.closeDryTrush);
                        startgetweight.setBackgroundResource(R.drawable.btn_open);
                        bldrytrush = true;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.initweight:
                try {

                        send(blue_sp.getBluetoothAd(), Codes.openwettrush);


                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

        }

    }
    //TODO 按键按下


    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_3) {
            shortPress = false;
            //长按要执行的代码
            try {
                send(blue_sp.getBluetoothAd(), Codes.upLundary);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("onKeyUp", "onKeyLongPress");

            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_3) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                event.startTracking(); //只有执行了这行代码才会调用onKeyLongPress的；
                if (event.getRepeatCount() == 0) {
                    shortPress = true;
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_3) {
            if (shortPress) {
                //短按要执行的代码

                Log.e("onKeyUp", "onKeyUp2");

            }
            shortPress = false;
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onLongClick(View view) {
        Log.e("onKeyUp", "onKeyUp1");
        return false;
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d("aa", "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d("aa", "temp sockets not created" + e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            if (Thread.interrupted()) {
                Log.d("aa", "return");
                return;
            }
            Log.d("aa", "BEGIN mConnectedThread");
            byte[] buffer = new byte[128];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                synchronized (this) {

                    try {
                        while (mmInStream.available() == 0) {
                        }
                        try {
                            Thread.sleep(100);  //当有数据流入时，线程休眠一段时间，默认100ms
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bytes = mmInStream.read(buffer);  //从字节流中读取数据填充到字节数组，返回读取数据的长度

                        Log.d("aa", "count   " + bytes);
                        // 创建Message类，向handler发送数据
                        Message msg = new Message();
                        // 发送一个String的数据，让他向上转型为obj类型
                        msg.obj = new String(buffer, 0, bytes, "utf-8");
                        // 发送数据
                        Log.d("aa", "data   " + msg.obj);

                        handler.sendMessage(msg);
                    } catch (IOException e) {
                        Log.e("aa", "disconnected", e);

                        break;
                    }
                }


            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e("aa", "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("aa", "close() of connect socket failed", e);
            }
        }
    }
}