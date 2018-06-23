package com.babycar.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private ThreadPoolExecutor poolExecutor;
    private float[] results;
    private int distance;
    //private byte[] send;

    /**
     *
     * UI组件
     */

    private TextView distText;
    private TextView tempText;
    private TextView humiText;
    private Button freshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_info);

        poolExecutor = new ThreadPoolExecutor(3, 5,
                1, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(128));

        distText = (TextView) findViewById(R.id.distance_text);
        tempText = (TextView) findViewById(R.id.temperature_text);
        humiText = (TextView) findViewById(R.id.humidity_text);
        freshButton = (Button) findViewById(R.id.fresh_button);
        freshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(appUtil.getTemperature()) && !TextUtils.isEmpty(appUtil.getHumidity())){
                    Random rand = new Random();
                    try{
                        double temprature = Double.parseDouble(appUtil.getTemperature()) + rand.nextInt(3) - 1;
                        double humidity = Double.parseDouble(appUtil.getHumidity()) + rand.nextInt(3) - 1;
                        BigDecimal bg1 = new BigDecimal(temprature);
                        temprature = bg1.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                        BigDecimal bg2 = new BigDecimal(humidity);
                        humidity = bg2.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                        String temp = String.valueOf(temprature + "℃");
                        String humi = String.valueOf(humidity + "RH");
                        tempText.setText(temp);
                        humiText.setText(humi);
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        try{
            appUtil = (ApplicationUtil)ShowInfoActivity.this.getApplication();
            socket = appUtil.getSocket();
            out = appUtil.getOut();
            is = socket.getInputStream();
        }catch (Exception e1){
            e1.printStackTrace();
        }

        CheckNetWork checkNetWork = new CheckNetWork(this);
        poolExecutor.execute(checkNetWork);
        /*Thread t3 = new Thread(CheckNetWork);
        t3.start();*/

        Intent intent = getIntent();
        permissionflag = intent.getBundleExtra("Data").getBoolean("permissionflag",false);
        getBestLocation();

        /*Thread t2 = new Thread(calculateLocation);
        t2.start();*/
    }

    @Override
    public void onResume(){
        super.onResume();
       /* while (true) {*/
            SystemClock.sleep(1000);
            if (appUtil.geiStatu() && distance == -1 && !TextUtils.isEmpty(appUtil.getTemperature()) && !TextUtils.isEmpty(appUtil.getHumidity())) {
                String temp =appUtil.getTemperature() + "℃";
                String humi = appUtil.getHumidity() + "RH";
                tempText.setText(temp);
                humiText.setText(humi);
                distText.setText(R.string.GPSSIGNAL);
                appUtil.setStatu(false);
              /*  break;
            }*/
        }
        else if (!appUtil.geiStatu()){
                Random rand = new Random();
                try{
                    double temprature = Double.parseDouble(appUtil.getTemperature()) + rand.nextInt(3) - 1;
                    double humidity = Double.parseDouble(appUtil.getHumidity()) + rand.nextInt(3) - 1;
                    BigDecimal bg1 = new BigDecimal(temprature);
                    temprature = bg1.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                    BigDecimal bg2 = new BigDecimal(humidity);
                    humidity = bg2.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                    String temp = String.valueOf(temprature + "℃");
                    String humi = String.valueOf(humidity + "RH");
                    tempText.setText(temp);
                    humiText.setText(humi);
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
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
            if (appUtil.geiStatu()){
                char GPS = 'g';
                SendMessage sendMessage = new SendMessage();
                sendMessage.setMess(GPS);
                /*Thread t1 = new Thread(sendMessage);
                t1.start();*/
                poolExecutor.execute(sendMessage);
            }

            latitude = best.getLatitude();
            longtitude = best.getLongitude();
            //send = String.valueOf(latitude).getBytes();
            //out.writeDouble(latitude);
            //Toast.makeText(this, "best location: lat==" + best.getLatitude() + " lng==" + best.getLongitude(), Toast.LENGTH_SHORT).show();
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
    /*Runnable CheckNetWork = new Runnable() {*/
    class CheckNetWork implements Runnable {
        Context context;
        public CheckNetWork(Context context){
            this.context = context;
        }

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
                            results = new float[1];
                            /*notification = new NotificationCompat.Builder(ShowInfoActivity.this)
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
                            manager.notify(1,notification);*/

                            //Toast.makeText(context,"11111",Toast.LENGTH_LONG).show();

                            /*AlertDialog.Builder builder = new AlertDialog.Builder(ShowInfoActivity.this);
                            builder.setView(View.inflate(ShowInfoActivity.this,R.layout.activity_show_info,null));
                            AlertDialog dialog = builder.create();
                            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                            dialog.setCancelable(true);
                            dialog.setTitle("信息显示");
                            dialog.setMessage("TEST");
                            dialog.show();*/
                            /*if (dialog.isShowing())
                                Log.e("TEST","AlertisShowing");*/

                            char ack = '0';
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setMess(ack);
                            /*Thread th1 = new Thread(sendMessage);
                            th1.start();*/
                            poolExecutor.execute(sendMessage);

                            double serverLatitude;
                            //if (br.ready())
                                serverLatitude = Double.parseDouble(br.readLine());

                            Log.e("Latitude",Double.toString(serverLatitude));

                            double serverLongtitude;
                            if (serverLatitude == 0){
                                //distance.setText(R.string.GPSSIGNAL);
                                distance = -1;
                            }
                            else {
                                /*Thread th2 = new Thread(sendMessage);
                                th2.start();*/
                                poolExecutor.execute(sendMessage);
                                //if (br.ready())
                                    //serverLatitude = Double.parseDouble(br.readLine());
                                serverLongtitude = Double.parseDouble(br.readLine());
                                Log.i("Longtitude",Double.toString(serverLongtitude));
                                Location.distanceBetween(latitude,longtitude,serverLatitude,serverLongtitude,results);

                                Log.i("distance",Double.toString(results[0]));
                                //distText.setText("" + results[0]);
                            }
                            /*Thread th3 = new Thread(sendMessage);
                            th3.start();*/
                            poolExecutor.execute(sendMessage);
                            //if (br.ready())
                                //serverLatitude = Double.parseDouble(br.readLine());
                            appUtil.setTemperature(br.readLine());
                            Log.i("Temperature",appUtil.getTemperature());

                            /*Thread th4 = new Thread(sendMessage);
                            th4.start();*/
                            poolExecutor.execute(sendMessage);
                            //if (br.ready())
                                //serverLatitude = Double.parseDouble(br.readLine());
                            appUtil.setHumidity(br.readLine());
                            Log.i("Humidity",appUtil.getHumidity());

                            //setInfor("1",temperature);

                            /*builder= new AlertDialog.Builder(ShowInfoActivity.this);
                            builder.setTitle("信息显示");
                            builder.setMessage("温度:" + temperature + "\n湿度:" + humidity + "\n距离:" + results[0]);
                            builder.show();*/

                            //Toast.makeText(ShowInfoActivity.this,"distance:" + distance + "  temperature:" + temperature + "  humidity:" + humidity,Toast.LENGTH_SHORT).show();
                            /*appUtil.init("192.168.43.206",8888);
                            socket = appUtil.getSocket();
                            out = appUtil.getOut();
                            is = socket.getInputStream();*/

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
