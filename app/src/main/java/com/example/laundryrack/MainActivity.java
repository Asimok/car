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

import com.example.laundryrack.tools.LongClickButton;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private boolean shortPress = false;
    private final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private OutputStream os;
    private ConnectedThread thread;
    boolean connected = true;
    private TextView  tvBandBluetooth;
    private bluetooth_Pref blue_sp;
    // 获取到蓝牙适配器
    public BluetoothAdapter mBluetoothAdapter;

    private TextView tvLeftClothState,tvRightClothState,tvLeftClothWeight,tvRightClothWeight;
    private ImageView imgLeftClothState,imgRightClothState;

    private TextToSpeech texttospeech;
    private Button startgetweight;
    private Button initweightleft;
    private Button bandleft,startbandleft;
    private Button bandright,startbandright;
private LongClickButton downLundary,upLaundary;
    BluetoothDevice lvDevice = null;

    BluetoothSocket lvSocket = null;
private boolean isleft=false,isright=false;

    private boolean connectLundary = true,leftspeak=true,rightspeak=true;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("智能晾衣架");
        Button initweightright = findViewById(R.id.initweightright);
        tvRightClothState=findViewById(R.id.rightclothstate);
        tvLeftClothState=findViewById(R.id.leftclothstate);
        imgLeftClothState=findViewById(R.id.imgleftcloth);
        imgRightClothState=findViewById(R.id.imgrightcloth);
        tvLeftClothWeight = findViewById(R.id.tvleftclothweight);
        tvRightClothWeight = findViewById(R.id.tvrightclothweight);
        tvBandBluetooth = (TextView) findViewById(R.id.tvBandBluetooth);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        blue_sp = bluetooth_Pref.getInstance(this);
        tvBandBluetooth.setText(String.format("已绑定设备：  %s  %s", blue_sp.getBluetoothName(), blue_sp.getBluetoothAd()));
        startgetweight = (Button) findViewById(R.id.startgetweight);
        initweightleft = (Button) findViewById(R.id.initweightleft);
        bandleft = findViewById(R.id.bandleft);
        bandright = findViewById(R.id.bandright);
        startbandleft = findViewById(R.id.startbandleft);
        startbandright = findViewById(R.id.startbandright);
          downLundary = (LongClickButton ) findViewById(R.id.downlaundary);
          upLaundary =  (LongClickButton )findViewById(R.id.uplaundary);

//连续减
        downLundary.setLongClickRepeatListener(new LongClickButton.LongClickRepeatListener() {
            @Override
            public void repeatAction() {
                try {
                    send(blue_sp.getBluetoothAd(), Codes.downLundary);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 50);
        //连续加
        upLaundary.setLongClickRepeatListener(new LongClickButton.LongClickRepeatListener() {
            @Override
            public void repeatAction() {
                Log.d("aa","持续");
                try {
                    send(blue_sp.getBluetoothAd(), Codes.upLundary);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 50);

        //减1
        downLundary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    send(blue_sp.getBluetoothAd(), Codes.downLundary);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //加1
        upLaundary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("aa","one");
                try {

                    send(blue_sp.getBluetoothAd(), Codes.upLundary);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        startbandleft.setOnClickListener(this);
        startbandright.setOnClickListener(this);
        initweightright.setOnClickListener(this);
        startgetweight.setOnClickListener(this);
        initweightleft.setOnClickListener(this);
        bandright.setOnClickListener(this);
        bandleft.setOnClickListener(this);
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



    // 创建handler，因为我们接收是采用线程来接收的，在线程中无法操作UI，所以需要handler
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            Log.e("cc1","收到 "+(String)msg.obj);
String nowWeight=(String) msg.obj;

    if (nowWeight.contains("r") && nowWeight.contains("i") && nowWeight.contains("l") && nowWeight.contains("f")) {
        int leftWeight = Integer.parseInt(nowWeight.substring(nowWeight.indexOf("l") + 1, nowWeight.indexOf("f")));

        tvLeftClothWeight.setText(nowWeight.substring(nowWeight.indexOf("l") + 1, nowWeight.indexOf("f")) + "g");


        if(bandleft.getText().toString().contains("g")) {
            String left = bandleft.getText().toString();
        int nowbandleft = Integer.parseInt(left.substring(0, left.indexOf("g")));
            if(isleft)
        if (leftWeight - 10< nowbandleft && leftWeight+ 10 > nowbandleft ) {
            //  autoSign();
            tvLeftClothState.setText("已晾干");
            if(leftspeak){
                texttospeech.speak("1号位置的衣服晾干啦", TextToSpeech.QUEUE_ADD,
                        null);
                leftspeak=false;
            }

            imgLeftClothState.setImageDrawable(getResources().getDrawable(R.drawable.dry));
        } else {
            tvLeftClothState.setText("潮湿");
            imgLeftClothState.setImageDrawable(getResources().getDrawable(R.drawable.wet));
        }


    }
}



                if (nowWeight.contains("r") && nowWeight.contains("i") && nowWeight.contains("l") && nowWeight.contains("f")) {

                    int rightWeight = Integer.parseInt(nowWeight.substring(nowWeight.indexOf("r") + 1, nowWeight.indexOf("i")));

                    tvRightClothWeight.setText(nowWeight.substring(nowWeight.indexOf("r") + 1, nowWeight.indexOf("i")) + "g");

                    if(bandright.getText().toString().contains("g")) {

                        String right = bandright.getText().toString();
                    int nowbandright = Integer.parseInt(right.substring(0, right.indexOf("g")));
                        if(isright)
                    if (rightWeight - 10< nowbandright && rightWeight+ 10 > nowbandright ) {
                        //  autoSign();
                        tvRightClothState.setText("已晾干");

                        if(rightspeak)
                        {
                            texttospeech.speak("2号位置的衣服晾干啦", TextToSpeech.QUEUE_ADD,
                                    null);
                            rightspeak=false;
                        }
                        imgRightClothState.setImageDrawable(getResources().getDrawable(R.drawable.dry2));
                    } else {
                        tvRightClothState.setText("潮湿");
                        imgRightClothState.setImageDrawable(getResources().getDrawable(R.drawable.wet2));
                    }
                }
            }


        }
    };
    @Override
    public void onClick(View view) {
        //TODO 按钮点击
        switch (view.getId()) {
            case R.id.startgetweight:
                try {
                    if (connectLundary) {
                        if(blue_sp.getBluetoothAd().equals("null"))
                        {
                            Toast.makeText(MainActivity.this,"请先绑定蓝牙设备",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            send(blue_sp.getBluetoothAd(), Codes.openLundary);
                            startgetweight.setBackgroundResource(R.drawable.btn_close);
                            startgetweight.setText("关闭");
                            connectLundary = false;
                        }
                    } else {
                        startgetweight.setText("开启");

                        send(blue_sp.getBluetoothAd(), Codes.closeLundary);
                        startgetweight.setBackgroundResource(R.drawable.btn_open);
                        connectLundary = true;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.startbandleft:

                    if (!isleft) {

                        startbandleft.setBackgroundResource(R.drawable.btn_open);
                        startbandleft.setText("正在监测");
                        isleft = true;
                        leftspeak=true;
                    } else {
                        startbandleft.setText("开始监测");
                        startbandleft.setBackgroundResource(R.drawable.btn_updown);
                        isleft = false;
                    }


                break;
            case R.id.startbandright:

                if (!isright) {

                    startbandright.setBackgroundResource(R.drawable.btn_open);
                    startbandright.setText("正在监测");
                    isright = true;
                    rightspeak=true;
                } else {
                    startbandright.setText("开始监测");
                    startbandright.setBackgroundResource(R.drawable.btn_updown);
                    isright = false;
                }


                break;

            case R.id.initweightleft:
                try {

                    send(blue_sp.getBluetoothAd(), Codes.initLeft);
                    Toast.makeText(this,"已初始化1号位置重量",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.initweightright:
                try {

                    send(blue_sp.getBluetoothAd(), Codes.initRight);
                    Toast.makeText(this,"已初始化2号位置重量",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bandleft:
                if(!tvLeftClothWeight.getText().toString().equals(""))
                {
                    if(tvLeftClothWeight.getText().toString().contains("g"))
                    bandleft.setText(tvLeftClothWeight.getText().toString());
                    else
                        Toast.makeText(this,"未获取到1号位置重量",Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.bandright:
                if(!tvRightClothWeight.getText().toString().equals(""))
                {
                    if(tvRightClothWeight.getText().toString().contains("g"))
                    bandright.setText(tvRightClothWeight.getText().toString());
                    else
                        Toast.makeText(this,"未获取到2号位置重量",Toast.LENGTH_SHORT).show();
                }
                break;
        }

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
                if (connected) {
                    connected = false;
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
                os=null;
                lvSocket = null;
                lvDevice=null;
                connected=true;
                thread.cancel();


                break;
            case R.id.menu_3:
                try {
                    lvDevice=null;
                    os=null;
                    lvSocket = null;
                    connected=true;
                    thread.cancel();

                    send(blue_sp.getBluetoothAd(), Codes.openLundary);


                    Toast.makeText(this, "已重新连接", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }


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
        connected=false;
        thread.cancel();
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
            byte[] buffer = new byte[256];
            int bytes;


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