package com.example.testddpush;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.ddpush.im.util.StringUtil;
import org.ddpush.im.v1.client.appuser.Message;
import org.ddpush.im.v1.client.appuser.UDPClientBase;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import androidx.core.app.NotificationCompat;

import io.objectbox.Box;

public class DDPushService extends Service{
    WakeLock wakeLock;
    MyUdpClient myUdpClient;
    private NotificationManager mNotificationManager;
    private int notifiId=100;
    private  PendingIntent pIntent;
    private String packageName;
    private ActivityManager activityManager;
    private JSONObject object;
    private JSONObject msgObject;
    private JSONObject assessObject;
    private String msgId="";
    protected PendingIntent tickPendIntent;
    protected TickAlarmReceiver tickAlarmReceiver=new TickAlarmReceiver();
    private MediaPlayer mediaPlayer=null;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        this.setTickAlarm();
        packageName = this.getPackageName();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        System.out.println("------------DDPushService???????????????");
        Box<User> box=MyApplication.getBoxStore().boxFor(User.class);
        List<User> list=box.getAll();
        User user=list.get(0);
        if(this.myUdpClient != null){
            try{myUdpClient.stop();}catch(Exception e){}
        }
        try {
          //  MyUdpClient myTcpClient = new MyUdpClient(StringUtil.md5Byte(user.getUuid()), 1, UrlUtil.DDPUSH_SERVER_IP, UrlUtil.DDPUSH_SERVER_port);
            MyUdpClient myTcpClient = new MyUdpClient(StringUtil.md5Byte("123456"), 1, UrlUtil.DDPUSH_SERVER_IP, UrlUtil.DDPUSH_SERVER_port);
            myTcpClient.setHeartbeatInterval(50);//50???????????????
            myTcpClient.start();
            Notification notification=createNofication("??????????????????","????????????????????????");
            startForeground(notifiId,notification);
            System.out.println("DDPush?????????--"+user.getUuid());
            //	Toast.makeText(getApplicationContext(), "DDPush?????????--"+MyApplication.userId, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.onCreate();
    }
    private Notification createNofication(String contentTitle,String contentText){
        NotificationCompat.Builder builder ;
        int channelId = 1 ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){        //Android 8.0??????
            NotificationChannel channel = new NotificationChannel(String.valueOf(channelId),
                    "channel_name",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this,String.valueOf(channelId));
        }else{
            builder = new NotificationCompat.Builder(this);
        }

        builder.setContentTitle(contentTitle)            //??????????????????????????????
                .setContentText(contentText)             //?????????????????????
                .setWhen(System.currentTimeMillis())                //?????????????????????
                .setSmallIcon(R.mipmap.ic_launcher)    //????????????????????????????????????alpha???????????????????????????
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL)                                      //????????????????????????
                .setPriority(Notification.PRIORITY_MAX)                                 //???????????????????????????
                .setOngoing(true)                                   //??????????????????????????????
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher_background));
        Notification notification = builder.build() ;
        return notification;
    }
    private Notification createNofication1(String contentTitle,String contentText){
        NotificationCompat.Builder builder ;
        int channelId = 2 ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){        //Android 8.0??????
            NotificationChannel channel = new NotificationChannel(String.valueOf(channelId),
                    "channel_name",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this,String.valueOf(channelId));
        }else{
            builder = new NotificationCompat.Builder(this);
        }

        builder.setContentTitle(contentTitle)            //??????????????????????????????
                .setContentText(contentText)             //?????????????????????
                .setWhen(System.currentTimeMillis())                //?????????????????????
                .setSmallIcon(R.mipmap.ic_launcher)    //????????????????????????????????????alpha???????????????????????????
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL)                                      //????????????????????????
                .setPriority(Notification.PRIORITY_HIGH)                                 //???????????????????????????
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher_background));
        Notification notification = builder.build() ;
        return notification;
    }
    protected void setTickAlarm(){
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,TickAlarmReceiver.class);
        int requestCode = 0;
        tickPendIntent = PendingIntent.getBroadcast(this,
                requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //??????2s???MIUI??????????????????????????????????????????5???????????????5?????????alarm?????????5??????????????????
        long triggerAtTime = System.currentTimeMillis();
        int interval = 300 * 1000;
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, tickPendIntent);
    }
    @Override
    public int onStartCommand(Intent param, int flags, int startId) {
        // TODO Auto-generated method stub
        if(param == null){
            return START_STICKY;
        }
        String cmd = param.getStringExtra("CMD");
        if(cmd == null){
            cmd = "";
        }
        if(cmd.equals("TICK")){
            if(wakeLock != null && wakeLock.isHeld() == false){
                wakeLock.acquire();
            }
        }
        mediaPlayer=MediaPlayer.create(getApplicationContext(), R.raw.no_voice);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer arg0) {
                // TODO Auto-generated method stub
                mediaPlayer.start();
            }
        });
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        //this.cancelTickAlarm();
        this.tryReleaseWakeLock();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }
    public class MyUdpClient extends UDPClientBase {

        public MyUdpClient(byte[] uuid, int appid, String serverAddr, int serverPort)
                throws Exception {
            super(uuid, appid, serverAddr, serverPort);

        }

        @Override
        public boolean hasNetworkConnection() {
            return Util.hasNetwork(DDPushService.this);
        }


        @Override
        public void trySystemSleep() {
            tryReleaseWakeLock();
        }

        @Override
        public void onPushMessage(Message message) {
            if(message == null){
                return;
            }
            String msg="";
            try {
                msg = new String(message.getData(),5,message.getContentLength(), "UTF-8");
                System.out.println("msg:"+msg);
                try {
                  Notification notification=createNofication1("??????????????????",msg);
                  mNotificationManager.notify(notifiId++,notification);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //msg1=msg.split("-");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    protected void tryReleaseWakeLock(){
        if(wakeLock != null && wakeLock.isHeld() == true){
            wakeLock.release();
        }
    }
}
