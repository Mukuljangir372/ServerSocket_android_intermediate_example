package com.mu.jan.app.serversocket;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView msg_textView,ip_text;
    private EditText msg_editText;
    private Button btn_send;

    private Boolean isStarted = false;

    private JobScheduler jobScheduler;
    private static final int JOB_ID = 0232;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msg_textView = (TextView) findViewById(R.id.msg_text);
        ip_text = (TextView)findViewById(R.id.ip_text);
        msg_editText = (EditText)findViewById(R.id.send_EditText);
        btn_send = (Button)findViewById(R.id.button_send);

        getIPFromDevice();

        //This is server socket
        //server socket always receive upcoming request from client socket using IP and port
        //Always remember that all networks calls are do in background threads

        //here is demonstration
        /** Using JobScheduler, we can run a service that runs on backgrounds even after app killed
        it also works on api26 and higher versions

         If you use service or intent service, it can easily killed by system or even stops after app closed in some device like Xiomi

         If you want that your service continuously running on background on every devices and every versions (like api 26+), you
         have to use JobScheduler,WorkManager or AlarmManager(outdated method).
         Using this approach, battery consumption reduce and many good approaches meet.

         JobScheduler used to schedule task. you can use any conditions where you want to start your service

         i want to start service only when device reboots,
         when network connection activites,
         when any power supply plug in device
         etc.


         */

        try{
            //Using AlarmManager
            Intent i = new Intent(this,RestartServiceBroadcast.class);
            int requestCodeInt = new Random().nextInt();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,requestCodeInt,i,PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager)getApplicationContext().getSystemService(ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),1,pendingIntent);


        }catch (Exception e){}


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });




    }
   private void getIPFromDevice(){
       try {
           for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
               NetworkInterface intf = en.nextElement();
               for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                   InetAddress inetAddress = enumIpAddr.nextElement();
                   if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                       ip_text.setText(""+inetAddress.getHostAddress());
                   }
               }
           }
       } catch (SocketException e) {
           e.printStackTrace();
       }
   }



   private void start_job_scheduler(){

        jobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);

        //apply requirements you want
       //conditions using JobInfo

       ComponentName componentName = new ComponentName(getPackageName(),ServerService.class.getName());

       /**
        * Here we are setting JobInfo.NETWORK_TYPE_ANY
        * This job scheduler activates only when network connection available
        * you can use many options here
        */
       JobInfo jobInfo;
       if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
           jobInfo = new JobInfo.Builder(JOB_ID,componentName)
                  // .setRequiredNetworkType(JobInfo.NETWORK_TYPE_CELLULAR)
                   .setRequiresCharging(true)
                   .setPersisted(true)
                   .setMinimumLatency(5000)
                   .build();
       }else{
           jobInfo = new JobInfo.Builder(JOB_ID,componentName)
                   .setRequiresCharging(true)
                   .setPersisted(true)
                   .setPeriodic(5000)
                   .build();
       }



       //schedule
      int resultCode = jobScheduler.schedule(jobInfo);

      if(resultCode == jobScheduler.RESULT_SUCCESS){
        Toast.makeText(this,"working",Toast.LENGTH_SHORT).show();
      }
      if(resultCode == jobScheduler.RESULT_FAILURE){
          Log.d("jobScheduler","failed");
      }
   }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
