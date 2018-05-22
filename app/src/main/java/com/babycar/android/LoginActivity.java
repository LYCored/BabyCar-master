package com.babycar.android;

import android.app.PendingIntent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Handler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    private AutoCompleteTextView GetIP;
    private EditText GetPort;
    private Button ConnectSocket;

    private Socket socket;
    private boolean connect_success = false;
    private final NotLeakHandler mHandler = new NotLeakHandler(this);
    private DataOutputStream out;
    private DataInputStream in;
    private NotificationManager notificationManager;
    private Notification notification;
    private int messageNotificationID = 0;
    private ApplicationUtil appUtil;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GetIP = (AutoCompleteTextView) findViewById(R.id.Edit_Ip);
        GetPort = (EditText) findViewById(R.id.Edit_Port);

        InitHandler();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch (requestCode){
            case 1:
                if(resultCode == RESULT_OK){
                    data.getBooleanExtra("data_return",connect_success);
                }
                break;
        }
    }

    private static class NotLeakHandler extends Handler{
        private WeakReference<LoginActivity> weakReference;
        public  NotLeakHandler(LoginActivity reference) {
            weakReference = new WeakReference<LoginActivity>(reference);
        }
        @Override
        public  void handleMessage(Message msg){
            super.handleMessage(msg);
            LoginActivity reference = (LoginActivity)weakReference.get();
            if(reference == null)
                return;
            Bundle data = msg.getData();
            String val = data.getString("value");
            if(val==""){
                ///////
            }
        }

    }

    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            while (connect_success) {
                socket = appUtil.getSocket();
                in = appUtil.getIn();
                try {
                    int len = in.available();
                    while (len == 0)
                        len = in.available();
                    byte[] msg = new byte[len];
                    in.read(msg);
                    String content = new String(msg, "utf-8");
                    Bundle data = new Bundle();
                    Message message = new Message();
                    data.putString("value",content);
                    message.setData(data);
                    mHandler.sendMessage(message);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    };

    Runnable CheckNetWork = new Runnable() {
        @Override
        public void run() {
            try{
                while(true){
                    socket.sendUrgentData(0xff);
                    Thread.sleep(3*1000);
                }
            }catch (Exception e){
                connect_success = false;
                mynoti();
            }
        }
    };

    public void mynoti(){
        long[] vir = {100,200,300,400};
        Intent intent = new Intent(this,LoginActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("BabyCar Warning")
                .setContentText("与婴儿车失去连接！")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .setSound(Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,"6"))
                .setVibrate(vir)
                .setAutoCancel(true)
                .build();
        manager.notify(1,notification);
    }

    private View.OnClickListener connectHandler=new View.OnClickListener() {
        public void onClick(View v) {
            String HostIP = GetIP.getText().toString();
            int HostPort =Integer.parseInt( GetPort.getText().toString());
            appUtil = (ApplicationUtil)LoginActivity.this.getApplication();
            try{
                appUtil.init(HostIP,HostPort);
                out = appUtil.getOut();
                connect_success = true;
                new Thread(networkTask).start();
                new Thread(CheckNetWork).start();
            }catch (IOException e1){
                connect_success = false;
                e1.printStackTrace();
            }catch (Exception e1){
                connect_success = false;
                e1.printStackTrace();
            }
        }

    };

    private View.OnClickListener controlHandler=new View.OnClickListener() {
        public void onClick(View v) {
            if(connect_success){
                Intent intent = new Intent(LoginActivity.this,ControlActivty.class);
                intent.putExtra("connect_success",connect_success);
                startActivityForResult(intent,1);
            }
            else{
                Toast.makeText(LoginActivity.this,"还未连接或连接已断开，请连接后再使用该功能",Toast.LENGTH_LONG).show();
            }
        }
    };

    private View.OnClickListener showinfoHandler=new View.OnClickListener() {
        public void onClick(View v) {
            if(connect_success){
                Intent intent = new Intent(LoginActivity.this,ShowInfoActivity.class);
                intent.putExtra("connect_success",connect_success);
                startActivityForResult(intent,1);
            }
            else{
                Toast.makeText(LoginActivity.this,"还未连接或连接已断开，请连接后再使用该功能",Toast.LENGTH_LONG).show();
            }
        }
    };

    private View.OnClickListener monitorHandler=new View.OnClickListener() {
        public void onClick(View v) {
            if(connect_success){
                Intent intent = new Intent(LoginActivity.this,MonitorActivity.class);
                intent.putExtra("connect_success",connect_success);
                startActivityForResult(intent,1);
            }
            else{
                Toast.makeText(LoginActivity.this,"还未连接或连接已断开，请连接后再使用该功能",Toast.LENGTH_LONG).show();
            }
        }
    };

    private View.OnClickListener videoHandler=new View.OnClickListener() {
        public void onClick(View v) {
            if(connect_success){
                Intent intent = new Intent(LoginActivity.this,VideoActivity.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(LoginActivity.this,"还未连接或连接已断开，请连接后再使用该功能",Toast.LENGTH_LONG).show();
            }
        }
    };

    public void InitHandler(){
        ((Button)findViewById(R.id.Button_Connect)).setOnClickListener(connectHandler);
        ((Button)findViewById(R.id.Button_Control)).setOnClickListener(controlHandler);
        ((Button)findViewById(R.id.Button_ShowInfo)).setOnClickListener(showinfoHandler);
        ((Button)findViewById(R.id.Button_Monitor)).setOnClickListener(monitorHandler);
        ((Button)findViewById(R.id.Button_Video)).setOnClickListener(videoHandler);
    }
}

