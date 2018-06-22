package com.babycar.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import static android.content.ContentValues.TAG;

public class ControlActivty extends Activity {

    //private SurfaceHolder holder;
    private SurfaceView surface;
    private Thread mythread;
    private Thread checkThread;

    private int width,height;
    private Socket socket;
    private OutputStream out;
    private OutputStream videoOut;
    private Boolean connect_success;
    public static String url;
    private SurfaceHolder holder;
    private Canvas canvas;
    private ApplicationUtil appUtil;

    private Socket videoSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        ((Button)findViewById(R.id.Button_stop)).setOnClickListener(stopHandler);
        ((Button)findViewById(R.id.Button_up)).setOnClickListener(upHandler);
        ((Button)findViewById(R.id.Button_down)).setOnClickListener(downHandler);
        ((Button)findViewById(R.id.Button_left)).setOnClickListener(leftHandler);
        ((Button)findViewById(R.id.Button_right)).setOnClickListener(rightHandler);
        ((Button)findViewById(R.id.Button_model)).setOnClickListener(modelHandler);
        appUtil = (ApplicationUtil)ControlActivty.this.getApplication();
        socket = appUtil.getSocket();
        out = appUtil.getOut();
        //InitHandler();
        /*try{
            appUtil.initVSocket("192.168.43.206",8080);
            videoOut = appUtil.getVOut();
            Log.i("second socket","Running!");
        }catch (Exception e1){
            connect_success = false;
            e1.printStackTrace();
            Log.getStackTraceString(e1);
            Log.e("second socket","Error!");
        }*/

        (checkThread = new Thread(CheckNetWork)).start();

        surface = (SurfaceView)findViewById(R.id.C_sufaceview);
        surface.setKeepScreenOn(true);

        mythread = new Thread(videoRunnable);
        holder = surface.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                width = surface.getWidth();
                height = surface.getHeight();
                mythread.start();
            }
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) { }
            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) { }
        });
    }

    private void draw(){
        //getSeverIP
        String url = "http://192.168.43.206:8080/?action=snapshot";
        URL videoUrl;
        HttpURLConnection httpURLConnection;
        Bitmap bmp;
        //Paint p = new Paint();
        try{
            InputStream inputStream = null;
            videoUrl = new URL(url);
            Log.w("Test message","videoUrl is correct!");
            httpURLConnection = (HttpURLConnection)videoUrl.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            Log.w("Test message","httpURLConnection is correct!");
            inputStream = httpURLConnection.getInputStream();
            //InputStream inputStream = videoSocket.getInputStream();
            Log.w("Test message","InputStream is correct!");
            bmp = BitmapFactory.decodeStream(inputStream);
            Log.w("Test message","Bitmap is correct!");
            canvas = holder.lockCanvas();
            Log.w("Test message","Lock Canvas is correct!");
            canvas.drawColor(Color.WHITE);
            RectF rectF = new RectF(0,0,width,height);
            canvas.drawBitmap(bmp,null,rectF,null);
            Log.w("Test message","Drawer is correct!");
            holder.unlockCanvasAndPost(canvas);
            Log.w("Test message","Display is correct!");
            //httpURLConnection.disconnect();
        }catch (Exception e1){
            Toast.makeText(ControlActivty.this,"视频传输出现错误,返回上一级",Toast.LENGTH_LONG).show();
            Log.i("Error message","Get Video Error!");
            holder.unlockCanvasAndPost(canvas);
            finish();
        }/*finally {
            Toast.makeText(ControlActivty.this,"请检查是否因为网络问题导致视频传输错误",Toast.LENGTH_LONG).show();
            //finish();
        }*/
    }

    Runnable videoRunnable = new Runnable() {
        @Override
        public void run() {

            while (true){
                    draw();
            }
        }
    };

    //check server message
    Runnable CheckNetWork = new Runnable() {
        @Override
        public void run() {
            try{
                while(true){
                    //Notification setting
                    long[] vir = {100,200,300,400};
                    Intent intent = new Intent(ControlActivty.this,LoginActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(ControlActivty.this,0,intent,0);
                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    Notification notification;
                    //socket setting
                    //socket.sendUrgentData(0xff);
                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String serverMsg = br.readLine();
                    Log.e("testtool","test!!!!!");
                    switch (serverMsg){
                        case "119":
                            notification = new NotificationCompat.Builder(ControlActivty.this)
                                    .setContentTitle("BabyCar Warning")
                                    .setContentText("火焰警报")
                                    .setWhen(System.currentTimeMillis())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                                    .setContentIntent(pi)
                                    .setSound(Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,"6"))
                                    .setVibrate(vir)
                                    .setAutoCancel(true)
                                    .build();
                            manager.notify(1,notification);
                            break;
                        case"112":
                            notification = new NotificationCompat.Builder(ControlActivty.this)
                                    .setContentTitle("BabyCar Warning")
                                    .setContentText("气体警报")
                                    .setWhen(System.currentTimeMillis())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                                    .setContentIntent(pi)
                                    .setSound(Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,"6"))
                                    .setVibrate(vir)
                                    .setAutoCancel(true)
                                    .build();
                            manager.notify(1,notification);
                            break;
                        case"110":
                            notification = new NotificationCompat.Builder(ControlActivty.this)
                                    .setContentTitle("BabyCar Warning")
                                    .setContentText("声音警报")
                                    .setWhen(System.currentTimeMillis())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                                    .setContentIntent(pi)
                                    .setSound(Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,"6"))
                                    .setVibrate(vir)
                                    .setAutoCancel(true)
                                    .build();
                            manager.notify(1,notification);
                            break;
                        case"116":
                            notification = new NotificationCompat.Builder(ControlActivty.this)
                                    .setContentTitle("BabyCar Warning")
                                    .setContentText("气体警报")
                                    .setWhen(System.currentTimeMillis())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                                    .setContentIntent(pi)
                                    .setSound(Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,"6"))
                                    .setVibrate(vir)
                                    .setAutoCancel(true)
                                    .build();
                            manager.notify(1,notification);
                            break;
                            default:
                                Log.e("default", "unknown information!");
                    }
                    Thread.sleep(1*1000);
                }
            }catch (Exception e){
                Log.e("catch","catch test!");
                connect_success = false;
                //mynoti();
            }
        }
    };

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("是否返回上一级？")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“确认”后的操作
                        Intent intent = new Intent();
                        intent.putExtra("data_return",connect_success);
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“返回”后的操作,这里不设置没有任何操作
                    }
                }).show();
    }

    private OnClickListener stopHandler=new OnClickListener() {
        public void onClick(View v) {
            char STOP = 'P';
            SendMessage send = new SendMessage();
            send.setMess(STOP);
            Thread t = new Thread(send);
            t.start();
        }
    };
    private OnClickListener leftHandler=new OnClickListener() {
        public void onClick(View v) {
            char LEFT = 'a';
            SendMessage send = new SendMessage();
            send.setMess(LEFT);
            Thread t = new Thread(send);
            t.start();
        }
    };
    private OnClickListener modelHandler=new OnClickListener() {
        public void onClick(View v) {
            char TAB = 'T';
            SendMessage send = new SendMessage();
            send.setMess(TAB);
            Thread t = new Thread(send);
            t.start();
        }
    };
    private OnClickListener rightHandler=new OnClickListener() {
        public void onClick(View v) {
            char RIGHT = 'd';
            SendMessage send = new SendMessage();
            send.setMess(RIGHT);
            Thread t = new Thread(send);
            t.start();
        }
    };
    private OnClickListener upHandler=new OnClickListener() {
        public void onClick(View v) {
            char UP = 'w';
            SendMessage send = new SendMessage();
            send.setMess(UP);
            Thread t = new Thread(send);
            t.start();
        }
    };
    private OnClickListener downHandler=new OnClickListener() {
        public void onClick(View v) {
            char DOWN = 's';
            SendMessage send = new SendMessage();
            send.setMess(DOWN);
            Thread t = new Thread(send);
            t.start();
        }
    };

    public class SendMessage implements Runnable{
        private char mess;
        public void setMess(char mess)
        {
            this.mess = mess;
        }
        @Override
        public void run(){
            try{
                //PrintWriter pw = new PrintWriter(out);
                //pw.write(mess);
                //pw.flush();
                //pw.close();
                out.write(mess);
                out.flush();
                //out.close();
            }catch(Exception e1) {
                Toast.makeText(ControlActivty.this, "网络连接错误，返回上一级", Toast.LENGTH_LONG).show();
                connect_success = false;
                Intent intent = new Intent();
                intent.putExtra("data_return", connect_success);
                setResult(RESULT_OK, intent);
            //finish();
            }
        }
    };
    /*public void SendMessage(char mess){
        try{
            //PrintWriter pw = new PrintWriter(out);
            //pw.write(mess);
            //pw.flush();
            //pw.close();
            out.write(mess);
            out.flush();
            out.close();
        }catch(Exception e1) {
            Toast.makeText(ControlActivty.this, "网络连接错误，返回上一级", Toast.LENGTH_LONG).show();
            connect_success = false;
            Intent intent = new Intent();
            intent.putExtra("data_return", connect_success);
            setResult(RESULT_OK, intent);
            //finish();
        }
    }*/
    public void InitHandler(){
        //Intent intent = getIntent();
        //intent.getBooleanExtra("connect_success",connect_success);
//        ((Button)findViewById(R.id.Button_stop)).setOnClickListener(stopHandler);
//        ((Button)findViewById(R.id.Button_up)).setOnClickListener(upHandler);
//        ((Button)findViewById(R.id.Button_down)).setOnClickListener(downHandler);
//        ((Button)findViewById(R.id.Button_left)).setOnClickListener(leftHandler);
//        ((Button)findViewById(R.id.Button_right)).setOnClickListener(rightHandler);
//        ((Button)findViewById(R.id.Button_model)).setOnClickListener(modelHandler);
    }
 /*public void InitHandler() {
        // 获取wifi服务
        //**WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // 判断wifi是否开启
        //	if (!wifiManager.isWifiEnabled()) {
        //	wifiManager.setWifiEnabled(true);
        //	}
        //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        //int ipAddress = wifiInfo.getIpAddress();
        //String ip = intToIp(ipAddress);
        //	TextView txt1 = (TextView) findViewById(R.id.textView1);
        //txt1.setText(txt1.getText() + "\r\nLocal IP: " + ip);
    }*/
}
