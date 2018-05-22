package com.babycar.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class ControlActivty extends Activity implements Runnable{

    private SurfaceHolder holder;
    private Thread mythread;

    private int width,height;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private Boolean connect_success;
    public static String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_activty);

        ApplicationUtil apputil = (ApplicationUtil)ControlActivty.this.getApplication();
        socket = apputil.getSocket();

        InitHandler();

        final SurfaceView surface = (SurfaceView)findViewById(R.id.C_sufaceview);
        surface.setKeepScreenOn(true);
        mythread = new Thread(this);
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
        String url = "";
        Canvas canvas;
        URL videoUrl;
        HttpURLConnection httpURLConnection;
        Bitmap bmp;
        try{
            InputStream inputStream = null;
            videoUrl = new URL(url);
            httpURLConnection = (HttpURLConnection)videoUrl.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();
            bmp = BitmapFactory.decodeStream(inputStream);
            canvas = holder.lockCanvas();
            canvas.drawColor(Color.WHITE);
            RectF rectF = new RectF(0,0,width,height);
            canvas.drawBitmap(bmp,null,rectF,null);
            holder.unlockCanvasAndPost(canvas);
            httpURLConnection.disconnect();
        }catch (Exception e1){
            Toast.makeText(ControlActivty.this,"视频传输出现错误,返回上一级",Toast.LENGTH_LONG).show();
            finish();
        }finally {
            Toast.makeText(ControlActivty.this,"请检查是否因为网络问题导致视频传输错误",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void run(){
        while (true){
            draw();
        }
    }

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
            String STOP = "STOP";
            SendMessage(STOP);
        }
    };
    private OnClickListener leftHandler=new OnClickListener() {
        public void onClick(View v) {
            String LEFT = "LEFT";
            SendMessage(LEFT);
        }
    };
    private OnClickListener modelHandler=new OnClickListener() {
        public void onClick(View v) {
            String TAB = "TAB";
            SendMessage(TAB);
        }
    };
    private OnClickListener rightHandler=new OnClickListener() {
        public void onClick(View v) {
            String RIGHT = "RIGHT";
            SendMessage(RIGHT);
        }
    };
    private OnClickListener upHandler=new OnClickListener() {
        public void onClick(View v) {
            String UP = "UP";
            SendMessage(UP);
        }
    };
    private OnClickListener downHandler=new OnClickListener() {
        public void onClick(View v) {
            String DOWN = "DOWN";
            SendMessage(DOWN);
        }
    };

    public void SendMessage(String mess){
        try{
            PrintWriter pw = new PrintWriter(out);
            pw.write(mess);
            pw.flush();
            pw.close();
        }catch(Exception e1){
            Toast.makeText(ControlActivty.this,"网络连接错误，返回上一级",Toast.LENGTH_LONG).show();
            connect_success = false;
            Intent intent = new Intent();
            intent.putExtra("data_return",connect_success);
            setResult(RESULT_OK,intent);
            finish();
        }
    }
    public void InitHandler(){
        Intent intent = getIntent();
        intent.getBooleanExtra("connect_success",connect_success);
        ((Button)findViewById(R.id.Button_stop)).setOnClickListener(stopHandler);
        ((Button)findViewById(R.id.Button_up)).setOnClickListener(upHandler);
        ((Button)findViewById(R.id.Button_down)).setOnClickListener(downHandler);
        ((Button)findViewById(R.id.Button_left)).setOnClickListener(leftHandler);
        ((Button)findViewById(R.id.Button_right)).setOnClickListener(rightHandler);
        ((Button)findViewById(R.id.Button_model)).setOnClickListener(modelHandler);
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
