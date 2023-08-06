package com.NaN.dji_vjoy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.NaN.dji_vjoy.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import dji.common.Stick;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.remotecontroller.HardwareState;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.remotecontroller.RemoteController;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.thirdparty.afinal.core.AsyncTask;
import utils.Utils;

public class MainActivity extends AppCompatActivity {
    private TextView tv_registration = null;
    private TextView tv_rc_connection = null;
    private EditText et_serverIP = null;
    private Button btn_start = null;
    private ProgressBar pro_right_right, pro_right_left, pro_right_up, pro_right_down;
    private ProgressBar pro_left_right, pro_left_left, pro_left_up, pro_left_down;

    private Handler mHandler = null;
    private Aircraft mProduct = null;
    private DatagramSocket socket = null;
    private InetAddress serverAddress = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mHandler = new Handler(Looper.getMainLooper());
        tv_registration = findViewById(R.id.tv_registration);
        tv_rc_connection = findViewById(R.id.tv_rc_connection);
        et_serverIP = findViewById(R.id.et_serverIP);
        btn_start = findViewById(R.id.btn_start);
        pro_right_right = findViewById(R.id.pro_right_right);
        pro_right_left = findViewById(R.id.pro_right_left);
        pro_right_up = findViewById(R.id.pro_right_up);
        pro_right_down = findViewById(R.id.pro_right_down);
        pro_left_right = findViewById(R.id.pro_left_right);
        pro_left_left = findViewById(R.id.pro_left_left);
        pro_left_up = findViewById(R.id.pro_left_up);
        pro_left_down = findViewById(R.id.pro_left_down);


        //获取所需权限
        Utils.checkAndRequestPermissions(this);
        //注册SDK
        startSDKRegistration();
        //绑定开始模拟按钮事件
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverIPStr = String.valueOf(et_serverIP.getText());//获取pc端ip
                if(Utils.isValidIP(serverIPStr) && mProduct!=null){//ip合法且sdk注册完毕
                    //创建socket通信
                    try {
                        socket = new DatagramSocket(8456);
                        serverAddress = InetAddress.getByName(serverIPStr);
                    } catch (SocketException | UnknownHostException e) {
                        e.printStackTrace();
                    }
                    //注册遥控器状态变更事件
                    RemoteController mController = mProduct.getRemoteController();
                    mController.setHardwareStateCallback(new HardwareState.HardwareStateCallback() {
                        @Override
                        public void onUpdate(@NonNull HardwareState hardwareState) {
                            //获取左右摇杆数值
                            Stick leftStick = hardwareState.getLeftStick();
                            Stick rightStick = hardwareState.getRightStick();
                            int[] left = new int[]{
                                    leftStick.getHorizontalPosition(),//摇杆取值[-660, 660]
                                    leftStick.getVerticalPosition()
                            };
                            int[] right = new int[]{
                                    rightStick.getHorizontalPosition(),
                                    rightStick.getVerticalPosition()
                            };
                            byte[] leftByte = Utils.ByteArrayConcat(Utils.Int16ToByte(left[0]), Utils.Int16ToByte(left[1]));
                            byte[] rightByte = Utils.ByteArrayConcat(Utils.Int16ToByte(right[0]), Utils.Int16ToByte(right[1]));
                            byte[] stickData = Utils.ByteArrayConcat(leftByte, rightByte);

                            //获取左侧滚轮数值
                            byte[] leftDialData = Utils.Int16ToByte(hardwareState.getLeftDial());

                            //获取C1(Fn)键
                            byte[] C1Data = {0};
                            if(hardwareState.getC1Button().isClicked()){
                                C1Data[0] = 1;
                            }

                            //获取返航/暂停键
                            byte[] goHomeData = {0};
                            if(hardwareState.getGoHomeButton().isClicked()){
                                goHomeData[0] = 1;
                            }

                            byte[] sendData = Utils.ByteArrayConcat(Utils.ByteArrayConcat(stickData, leftDialData),
                                                                    Utils.ByteArrayConcat(C1Data, goHomeData));
                            //打包通过udp发送
                            DatagramPacket packet = new DatagramPacket(sendData, 0, sendData.length, serverAddress, 8456);
                            try {
                                socket.send(packet);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(left[0] > 0){
                                        pro_left_right.setProgress(left[0]);
                                        pro_left_left.setProgress(0);
                                    }else{
                                        pro_left_right.setProgress(0);
                                        pro_left_left.setProgress(-left[0]);
                                    }
                                    if(left[1] > 0){
                                        pro_left_up.setProgress(left[1]);
                                        pro_left_down.setProgress(0);
                                    }else{
                                        pro_left_up.setProgress(0);
                                        pro_left_down.setProgress(-left[1]);
                                    }
                                    if(right[0] > 0){
                                        pro_right_right.setProgress(right[0]);
                                        pro_right_left.setProgress(0);
                                    }else{
                                        pro_right_right.setProgress(0);
                                        pro_right_left.setProgress(-right[0]);
                                    }
                                    if(right[1] > 0){
                                        pro_right_up.setProgress(right[1]);
                                        pro_right_down.setProgress(0);
                                    }else{
                                        pro_right_up.setProgress(0);
                                        pro_right_down.setProgress(-right[1]);
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }


    private void startSDKRegistration(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                showToast("registering, pls wait...");
                DJISDKManager.getInstance().registerApp(MainActivity.this.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                    @Override
                    public void onRegister(DJIError djiError) {
                        if(djiError == DJISDKError.REGISTRATION_SUCCESS){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tv_registration.setText("已注册");
                                }
                            });
                        }
                    }

                    @Override
                    public void onProductDisconnect() {
                        showToast("设备已断开连接");
                        mProduct = null;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tv_rc_connection.setText("设备未连接");
                                tv_rc_connection.setBackgroundColor(getColor(R.color.error));
                            }
                        });
                        notifyStatusChange();
                    }

                    @Override
                    public void onProductConnect(BaseProduct baseProduct) {
                        showToast("设备已连接");
                        mProduct = (Aircraft)baseProduct;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tv_rc_connection.setText("设备已连接");
                                tv_rc_connection.setBackgroundColor(getColor(R.color.ok));
                            }
                        });
                        notifyStatusChange();
                    }

                    @Override
                    public void onProductChanged(BaseProduct baseProduct) {

                    }

                    @Override
                    public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent baseComponent, BaseComponent baseComponent1) {

                    }

                    @Override
                    public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

                    }

                    @Override
                    public void onDatabaseDownloadProgress(long l, long l1) {

                    }
                });
            }
        }).start();
    }

    private void showToast(String toast){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(Utils.FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };
    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }
}