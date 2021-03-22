package com.example.testddpush;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.List;
import java.util.UUID;

import io.objectbox.Box;

public class MainActivity extends AppCompatActivity {
    private Switch switch1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switch1=findViewById(R.id.switch1);
        saveUUid();
        if(isServiceRunning("com.example.testddpush.DDPushService",MainActivity.this)){
            switch1.setChecked(true);
        }else{
            switch1.setChecked(false);
        }
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Intent intent=new Intent(MainActivity.this,DDPushService.class);
                    startService(intent);
                }else{
                    Intent intent=new Intent(MainActivity.this,DDPushService.class);
                    stopService(intent);
                }
            }
        });

    }
    private void saveUUid(){
        Box<User> box=MyApplication.getBoxStore().boxFor(User.class);
        List<User> list=box.getAll();
        String uuid="";
        if(list.size()==0){
            uuid= UUID.randomUUID().toString();
            User user=new User();
            user.setUuid(uuid);
            box.put(user);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    public  boolean isServiceRunning(String serviceName, Context context) {
        //活动管理器
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(100); //获取运行的服务,参数表示最多返回的数量

        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            String className = runningServiceInfo.service.getClassName();
            if (className.equals(serviceName)) {
                return true; //判断服务是否运行
            }
        }

        return false;
    }
}