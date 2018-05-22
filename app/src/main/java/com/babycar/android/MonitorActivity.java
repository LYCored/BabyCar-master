package com.babycar.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class MonitorActivity extends Activity implements Runnable{

    private SurfaceHolder holder;
    private Thread mythread;

    private int width,height;
    public static String url;
    private Socket socket;
    private BufferedWriter out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        ApplicationUtil apputil = (ApplicationUtil)MonitorActivity.this.getApplication();
        out = new BufferedWriter(new OutputStreamWriter(apputil.getOut()));

        final SurfaceView surface = (SurfaceView)findViewById(R.id.surface_monitor);
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
           Toast.makeText(MonitorActivity.this,"视频传输出现错误",Toast.LENGTH_LONG).show();
       }finally {
           Toast.makeText(MonitorActivity.this,"请检查是否因为网络问题导致视频传输错误",Toast.LENGTH_LONG).show();
       }
    }

    public void run(){
        while (true){
            draw();
        }
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("是否返回上一级？")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“确认”后的操作
                        MonitorActivity.this.finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“返回”后的操作,这里不设置没有任何操作
                    }
                }).show();
    }

}
