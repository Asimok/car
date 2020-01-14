package com.example.car;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.car.R;
import com.example.car.tools.LongClickButton;

import com.example.car.tools.Codes;
import com.example.car.tools.bluetooth_Pref;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
    // private TextView  tvBandBluetooth;
    private TextView havaPeople, frontDistance;
    private bluetooth_Pref blue_sp;
    // 获取到蓝牙适配器
    public BluetoothAdapter mBluetoothAdapter;
    private Button openCarLamp, openCarBeep, connectCar, openCarLampLeft, openCarLampRight;
    private Button autoDrive, detectAround,one,two,three;
    private LongClickButton btn_back, btn_front, btn_Left, btn_Right;
    BluetoothDevice lvDevice = null;
    private boolean connectedCar = false;
    BluetoothSocket lvSocket = null;
    private boolean boolopenCarLampLeft = false, boolopenCarLampRight = false, boolopenCarLamp = false, boolopenCarBeep = false;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //tvBandBluetooth =   findViewById(R.id.tvBandBluetooth);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        blue_sp = bluetooth_Pref.getInstance(this);
        setTitle(String.format("已绑定设备：  %s  %s", blue_sp.getBluetoothName(), blue_sp.getBluetoothAd()));
        havaPeople = findViewById(R.id.havaPeople);
        frontDistance = findViewById(R.id.frontDistance);

        openCarLamp = findViewById(R.id.openCarLamp);
        connectCar = findViewById(R.id.connectCar);
        autoDrive = findViewById(R.id.autoDrive);
        detectAround = findViewById(R.id.detectAround);
        openCarBeep = findViewById(R.id.openCarBeep);
        openCarLampLeft = findViewById(R.id.openCarLampLeft);
        openCarLampRight = findViewById(R.id.openCarLampRight);
        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        three = findViewById(R.id.three);

        btn_back = (LongClickButton) findViewById(R.id.btn_back);
        btn_front = (LongClickButton) findViewById(R.id.btn_front);
        btn_Left = (LongClickButton) findViewById(R.id.btn_left);
        btn_Right = (LongClickButton) findViewById(R.id.btn_right);

//连续点击
        btn_back.setLongClickRepeatListener(new LongClickButton.LongClickRepeatListener() {
            @Override
            public void repeatAction() {
                try {
                    send(blue_sp.getBluetoothAd(), Codes.Back);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 50);
        btn_front.setLongClickRepeatListener(new LongClickButton.LongClickRepeatListener() {
            @Override
            public void repeatAction() {
                try {
                    send(blue_sp.getBluetoothAd(), Codes.Front);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 50);

        btn_Left.setLongClickRepeatListener(new LongClickButton.LongClickRepeatListener() {
            @Override
            public void repeatAction() {
                try {
                    send(blue_sp.getBluetoothAd(), Codes.Left);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 50);
        btn_Right.setLongClickRepeatListener(new LongClickButton.LongClickRepeatListener() {
            @Override
            public void repeatAction() {
                try {
                    send(blue_sp.getBluetoothAd(), Codes.Right);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 50);


        //单次点击
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    send(blue_sp.getBluetoothAd(), Codes.Back);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_front.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    send(blue_sp.getBluetoothAd(), Codes.Front);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_Left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    send(blue_sp.getBluetoothAd(), Codes.Left);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_Right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    send(blue_sp.getBluetoothAd(), Codes.Right);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        openCarBeep.setOnClickListener(this);
        connectCar.setOnClickListener(this);
        detectAround.setOnClickListener(this);
        openCarLamp.setOnClickListener(this);
        openCarLampLeft.setOnClickListener(this);
        openCarLampRight.setOnClickListener(this);
        autoDrive.setOnClickListener(this);
        detectAround.setOnClickListener(this);
        one.setOnClickListener(this);
        two .setOnClickListener(this);
        three.setOnClickListener(this);
    }


    // 创建handler，因为我们接收是采用线程来接收的，在线程中无法操作UI，所以需要handler
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
//TODO 处理接收到的信息
            super.handleMessage(msg);
            Log.e("cc1", "收到 " + (String) msg.obj);
            String message = (String) msg.obj;
            if (message.contains("cm") && message.contains("人")) {
                frontDistance.setText("前方障碍物:" + message.substring(0, message.indexOf("c")));
                havaPeople.setText("是否有人:" + message.substring(message.indexOf("m") + 1));
            }

        }
    };

    public void Stop(View view) throws IOException {
        send(blue_sp.getBluetoothAd(), Codes.Stop);
    }

    public void toFront(View view) throws IOException {
        send(blue_sp.getBluetoothAd(), Codes.goFront);
    }

    @Override
    public void onClick(View view) {
        //TODO 按钮点击
        switch (view.getId()) {
            case R.id.connectCar:
                try {
                    if (connectCar.getText().toString().equals("连接小车")) {
                        if (blue_sp.getBluetoothAd().equals("null")) {
                            Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                        } else {
                            send(blue_sp.getBluetoothAd(), Codes.connect);
                            connectCar.setBackgroundResource(R.drawable.btn_close);
                            connectCar.setText("断开连接");
                            connectedCar = true;
                        }
                    } else {
                        connectCar.setText("连接小车");
                        send(blue_sp.getBluetoothAd(), Codes.disconnect);
                        connectCar.setBackgroundResource(R.drawable.btn_open);
                        connectedCar = false;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.openCarLampLeft:
                try {
                    if (!boolopenCarLampLeft) {
                        if (blue_sp.getBluetoothAd().equals("null")) {
                            Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                        } else {
                            send(blue_sp.getBluetoothAd(), Codes.openCarLampLeft);
                            openCarLampLeft.setBackgroundResource(R.drawable.btn_close);
                            openCarLampLeft.setText("左转向");
                            boolopenCarLampLeft = !boolopenCarLampLeft;
                        }
                    } else {
                        openCarLampLeft.setText("左转向");
                        send(blue_sp.getBluetoothAd(), Codes.closeCarLampLeft);
                        openCarLampLeft.setBackgroundResource(R.drawable.btn_open);
                        boolopenCarLampLeft = !boolopenCarLampLeft;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.openCarLampRight:
                try {
                    if (!boolopenCarLampRight) {
                        if (blue_sp.getBluetoothAd().equals("null")) {
                            Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                        } else {
                            send(blue_sp.getBluetoothAd(), Codes.openCarLampRight);
                            openCarLampRight.setBackgroundResource(R.drawable.btn_close);
                            openCarLampRight.setText("右转向");
                            boolopenCarLampRight = !boolopenCarLampRight;
                        }
                    } else {
                        openCarLampRight.setText("右转向");
                        send(blue_sp.getBluetoothAd(), Codes.closeCarLampRight);
                        openCarLampRight.setBackgroundResource(R.drawable.btn_open);
                        boolopenCarLampRight = !boolopenCarLampRight;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.openCarLamp:
                try {
                    if (!boolopenCarLamp) {
                        if (blue_sp.getBluetoothAd().equals("null")) {
                            Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                        } else {
                            send(blue_sp.getBluetoothAd(), Codes.openCarLamp);
                            openCarLamp.setBackgroundResource(R.drawable.btn_close);
                            openCarLamp.setText("车灯");
                            boolopenCarLamp = !boolopenCarLamp;
                        }
                    } else {
                        openCarLamp.setText("车灯");
                        send(blue_sp.getBluetoothAd(), Codes.closeCarLamp);
                        openCarLamp.setBackgroundResource(R.drawable.btn_open);
                        boolopenCarLamp = !boolopenCarLamp;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.openCarBeep:
                try {
                    if (!boolopenCarBeep) {
                        if (blue_sp.getBluetoothAd().equals("null")) {
                            Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                        } else {
                            send(blue_sp.getBluetoothAd(), Codes.openCarBeep);
                            openCarBeep.setBackgroundResource(R.drawable.btn_close);
                            openCarBeep.setText("喇叭");
                            boolopenCarBeep = !boolopenCarBeep;
                        }
                    } else {
                        openCarBeep.setText("喇叭");
                        send(blue_sp.getBluetoothAd(), Codes.closeCarBeep);
                        openCarBeep.setBackgroundResource(R.drawable.btn_open);
                        boolopenCarBeep = !boolopenCarBeep;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.autoDrive:
                try {
                    if (autoDrive.getText().toString().equals("自动驾驶")) {
                        if (blue_sp.getBluetoothAd().equals("null")) {
                            Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                        } else {
                            send(blue_sp.getBluetoothAd(), Codes.openAutoDrive);
                            autoDrive.setBackgroundResource(R.drawable.btn_close);
                            autoDrive.setText("手动驾驶");
                        }
                    } else {
                        autoDrive.setText("自动驾驶");

                        send(blue_sp.getBluetoothAd(), Codes.closeAutoDrive);
                        autoDrive.setBackgroundResource(R.drawable.btn_open);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.detectAround:
                try {
                    if (detectAround.getText().toString().equals("探测周边")) {
                        if (blue_sp.getBluetoothAd().equals("null")) {
                            Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                        } else {
                            send(blue_sp.getBluetoothAd(), Codes.openDetectAround);
                            detectAround.setBackgroundResource(R.drawable.btn_close);
                            detectAround.setText("关闭探测");

                        }
                    } else {
                        detectAround.setText("探测周边");

                        send(blue_sp.getBluetoothAd(), Codes.closeDetectAround);
                        detectAround.setBackgroundResource(R.drawable.btn_open);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.one:

                        if (blue_sp.getBluetoothAd().equals("null")) {
                            Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                send(blue_sp.getBluetoothAd(), Codes.one);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            one.setBackgroundResource(R.drawable.btn_close);
                           two.setBackgroundResource(R.drawable.btn_band);
                            three.setBackgroundResource(R.drawable.btn_band);
                        }
                break;
            case R.id.two:

                if (blue_sp.getBluetoothAd().equals("null")) {
                    Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        send(blue_sp.getBluetoothAd(), Codes.two);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    two.setBackgroundResource(R.drawable.btn_close);
                    one.setBackgroundResource(R.drawable.btn_band);
                    three.setBackgroundResource(R.drawable.btn_band);
                }
                break;
            case R.id.three:

                if (blue_sp.getBluetoothAd().equals("null")) {
                    Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        send(blue_sp.getBluetoothAd(), Codes.three);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    three.setBackgroundResource(R.drawable.btn_close);
                    one.setBackgroundResource(R.drawable.btn_band);
                    two.setBackgroundResource(R.drawable.btn_band);
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
                os = null;
                lvSocket = null;
                lvDevice = null;
                connected = true;
                thread.cancel();


                break;
            case R.id.menu_3:
                try {
                    lvDevice = null;
                    os = null;
                    lvSocket = null;
                    connected = true;
                    thread.cancel();

                    send(blue_sp.getBluetoothAd(), Codes.connect);

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
//        tvBandBluetooth.setText(String.format("已绑定设备：  %s  %s", blue_sp.getBluetoothName(), blue_sp.getBluetoothAd()));
        setTitle(String.format("已绑定设备：  %s  %s", blue_sp.getBluetoothName(), blue_sp.getBluetoothAd()));
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        connected = false;
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