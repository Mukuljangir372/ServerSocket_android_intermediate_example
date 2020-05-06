package com.mu.jan.app.serversocket;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmManagerServerService extends Service {

    private String CHANNEL_ID = "xxxxxxx";
    private int notification_id;

    public AlarmManagerServerService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notification_id = new Random().nextInt();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {


                    ServerSocket serverSocket = new ServerSocket(8080);

                    //server is waiting for client
                    Socket s = serverSocket.accept();

                    //receiving sms from client and show notification
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));


                    //show a notification
                    Intent i = new Intent(AlarmManagerServerService.this,MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    i.putExtra("message",bufferedReader.readLine());

                    show_notification(i);



                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    private void create_channel(){

        //in api 26+ (oreo and higher version)
        //before creating any notification, you have to register notification with channel
        //only in api 26+
        //create channel for notification
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            CharSequence name = getString(R.string.notification_name);
            String des = getString(R.string.notification_des);

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,name, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(des);

            NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }
    private void show_notification(Intent intent){

        //create notification channel
        create_channel();

        PendingIntent pendingIntent = PendingIntent.getActivity(AlarmManagerServerService.this,0,intent,0);

        //make a builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(AlarmManagerServerService.this,CHANNEL_ID);
        builder.setContentTitle("Message received from client")
                .setContentText(intent.getStringExtra("message"))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true);

        //show
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notification_id,builder.build());
    }
}
