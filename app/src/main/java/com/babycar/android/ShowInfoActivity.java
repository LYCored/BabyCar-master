package com.babycar.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.ssl.CertPathTrustManagerParameters;

public class ShowInfoActivity extends Activity {
    private boolean permissionflag;
    private boolean connect_success;
    private Socket socket;
    private OutputStream out;
    private InputStream is;
    private ApplicationUtil appUtil;

    private double latitude;
    private double longtitude;
    private String temperature;
    private String humidity;
    //private byte[] send;

    /**
     *
     * UI组件
     */

    private TextView distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_info);

        distance = (TextView) findViewById(R.id.distance_text);

        try{
            appUtil = (ApplicationUtil)ShowInfoActivity.this.getApplication();
            socket = appUtil.getSocket();
            out = appUtil.getOut();
            is = socket.getInputStream();
        }catch (Exception e1){
            e1.printStackTrace();
        }

        Thread t3 = new Thread(CheckNetWork);
        t3.start();

        Intent intent = getIntent();
        permissionflag = intent.getBundleExtra("Data").getBoolean("permissionflag",false);
        getBestLocation();

        /*Thread t2 = new Thread(calculateLocation);
        t2.start();*/
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

    /**
     * 采用最好的方式获取定位信息
     */
    private void getBestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(ShowInfoActivity.this,"No permission to get the location!",Toast.LENGTH_LONG).show();
            return;
        }
        Criteria c = new Criteria();//Criteria类是设置定位的标准信息（系统会根据你的要求，匹配最适合你的定位供应商），一个定位的辅助信息的类
        c.setPowerRequirement(Criteria.POWER_LOW);//设置低耗电
        c.setAltitudeRequired(true);//设置需要海拔
        c.setBearingAccuracy(Criteria.ACCURACY_COARSE);//设置COARSE精度标准
        c.setAccuracy(Criteria.ACCURACY_LOW);//设置低精度
        //... Criteria 还有其他属性，就不一一介绍了
        Location best = LocationUtils.getBestLocation(this, c);
        if (best == null) {
            Toast.makeText(this, " best location is null", Toast.LENGTH_SHORT).show();
        } else {
            char GPS = 'g';
            SendMessage sendMessage = new SendMessage();
            sendMessage.setMess(GPS);
            Thread t1 = new Thread(sendMessage);
            t1.start();

            latitude = best.getLatitude();
            longtitude = best.getLongitude();
            //send = String.valueOf(latitude).getBytes();
            //out.writeDouble(latitude);
            Toast.makeText(this, "best location: lat==" + best.getLatitude() + " lng==" + best.getLongitude(), Toast.LENGTH_SHORT).show();
        }
    }

    private Runnable calculateLocation = new Runnable() {
        @Override
        public void run() {
            try {
                /**
                 * 发送位置
                 */
                /*String sendLocation = Double.toString(latitude);
                out.write('e');
                for (int i = 0; i < sendLocation.length(); i++){
                    out.write(sendLocation.charAt(i));
                }
                out.write('r');
                out.write('e');
                sendLocation = Double.toString(longtitude);
                for (int i = 0; i < sendLocation.length(); i++){
                    out.write(sendLocation.charAt(i));
                }
                out.write('r');
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String serverMsg = br.readLine();*/
                /**
                 * 接收位置
                 */
            }catch(Exception e1){
                Log.e("error","Error!!!!!!!!!!!!!!");
                e1.printStackTrace();
            }
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
                Log.e("test","sendMessage's running!!!!!");
                out.flush();
                //out.close();
            }catch(Exception e1) {
                Toast.makeText(ShowInfoActivity.this, "网络连接错误，返回上一级", Toast.LENGTH_LONG).show();
                connect_success = false;
                Intent intent = new Intent();
                intent.putExtra("data_return", connect_success);
                setResult(RESULT_OK, intent);
                //finish();
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
                    Intent intent = new Intent(ShowInfoActivity.this,LoginActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(ShowInfoActivity.this,0,intent,0);
                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    Notification notification;
                    //socket setting
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String serverMsg = br.readLine();

                    switch (serverMsg){
                        case "119":
                            notification = new NotificationCompat.Builder(ShowInfoActivity.this)
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
                            notification = new NotificationCompat.Builder(ShowInfoActivity.this)
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
                            notification = new NotificationCompat.Builder(ShowInfoActivity.this)
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
                            notification = new NotificationCompat.Builder(ShowInfoActivity.this)
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
                        case"115":
                            float[] results = new float[1];
                            notification = new NotificationCompat.Builder(ShowInfoActivity.this)
                                    .setContentTitle("BabyCar Warning")
                                    .setContentText("115警报")
                                    .setWhen(System.currentTimeMillis())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                                    .setContentIntent(pi)
                                    .setSound(Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,"6"))
                                    .setVibrate(vir)
                                    .setAutoCancel(true)
                                    .build();
                            manager.notify(1,notification);

                            char ack = '0';
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setMess(ack);
                            Thread th1 = new Thread(sendMessage);
                            th1.start();

                            double serverLatitude = Double.parseDouble(br.readLine());
                            Log.e("Latitude",Double.toString(serverLatitude));

                            double serverLongtitude;
                            if (serverLatitude == 0){
                                //distance.setText(R.string.GPSSIGNAL);
                            }
                            else {
                                Thread th2 = new Thread(sendMessage);
                                th2.start();

                                serverLongtitude = Double.parseDouble(br.readLine());
                                Log.i("Longtitude",Double.toString(serverLongtitude));
                                Location.distanceBetween(latitude,longtitude,serverLatitude,serverLongtitude,results);
                                Log.i("distance",Double.toString(results[0]));
                                distance.setText("" + results[0]);
                            }
                            Thread th3 = new Thread(sendMessage);
                            th3.start();
                            temperature = br.readLine();
                            Log.i("Temperature",temperature);

                            Thread th4 = new Thread(sendMessage);
                            th4.start();
                            humidity = br.readLine();
                            Log.i("Humidity",humidity);

                            break;
                    }
                    //  Thread.sleep(1*1000);
                }
            }catch (Exception e){
                connect_success = false;
            }
        }
    };
}
