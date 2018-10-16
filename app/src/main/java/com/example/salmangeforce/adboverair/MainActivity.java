package com.example.salmangeforce.adboverair;
/*
Author: Salman Yousaf
Dated: 10 June 2018
*/

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //Global variables
    private TextView textViewRoot;
    private TextView textViewError;
    private RelativeLayout relativeView;
    private Button btnAdb;
    private TextView textviewAdbStatus;
    private TextView textViewInfo;
    private ScrollView mainView;

    private NotificationManager notificationManager;
    private Notification.Builder builder;

    private ShellExecuter shellExecuter;

    private Boolean isOncePressed = false;
    private String isRunning;
    private String[] bootText= {"# Checking root...\n", "# Root not available...\n", "# Exiting..."};



    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainView = (ScrollView) findViewById(R.id.main);
        textViewRoot = findViewById(R.id.checkRoot);
        textViewError = findViewById(R.id.errorLarge);
        textViewRoot.setText("");
        relativeView = findViewById(R.id.container);
        btnAdb = findViewById(R.id.btnAdb);
        textviewAdbStatus = findViewById(R.id.textviewAdbStatusResult);
        shellExecuter = new ShellExecuter();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        textViewInfo = findViewById(R.id.textviewInfo);


        getSupportActionBar().setSubtitle("Developed with <3");

        //checking root
        if(!CheckRoot.isRooted())
        {
            setTimer(false);
            return;
        }
        else
        {
            setTimer(true);
        }

        //adding onclicklistener to views
        btnAdb.setOnClickListener(this);

        //adding notification
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.logo)
                    .setContentText("Click to go back to Appplication")
                    .setContentIntent(pendingIntent);
        }

        //starting adb
        startAdb();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId)
        {
            case R.id.about:
                showAlertDialog("About Developer", "Name : Salman Yousaf\n\nAndroid Developer\n\nBSCS Student\n\n" +
                        "Email : salmanyousaf240@gmail.com\n\nContact: +92 323 3480625\n\nCountry: Pakistan", "CLOSE");
                break;

            case R.id.versionInfo:
                showAlertDialog("Version 1.01", "#1. Fixed Bug: Notifcation was opening new Activity instead of the active one.\n\n" +
                        "#2. Added Feature: Animation that you are seeing here in the Alert Dilaog Box.\n\n" +
                        "#3. Added Feature: Refresh Menu in ActionBar for refreshing application status.\n\n" +
                        "#4. Added Feature: IpAddress is displayed on which adb connection is created.\n\n" +
                        "#5. Fixed Bug: After restarting mobile, the adb debug process was not starting.\n\n" +
                        "#6. Starting adb service at startup of app.\n\n" +
                        "#7. Fix some issues with starting and stopping adb\n", "GREAT");
                break;

//            case R.id.contact:
//                showToast("Email: salmanyousaf240@gmail.com");
//                break;

            case R.id.refresh:
                if(CheckRoot.isRooted())
                {
                    showToast("Refreshing...");
                    adbDebugging();
                }
                break;

            default:
                showToast("Menu Error");
                break;
        }

        return super.onOptionsItemSelected(item);
    }



    //OnClickListener
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        //Applying adbd commands based on view id
        if(v.getId() == R.id.btnAdb)
        {
            if(isRunning.equals("running"))
            {
                stopAdb();
            }
            else if(isRunning.equals("stopped"))
            {
                startAdb();
            }
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBackPressed() {
        if(isOncePressed)
        {
            stopAdb();
            super.onBackPressed();
            return;
        }

        isOncePressed = true;
        showToast("Click again to exit");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isOncePressed = false;
            }
        }, 2000);
    }



    //Helper Methods
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void startAdb()
    {
        shellExecuter.Executer("su\nsu -c setprop service.adb.tcp.port 5555", MainActivity.this);
        shellExecuter.Executer("su\nsu -c start adbd", MainActivity.this);
        start("Adb is started!");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void stopAdb()
    {
        shellExecuter.Executer("su\nsu -c stop adbd", MainActivity.this);
        stop("Adb is stopped!");
    }



    private void setTimer(final Boolean isRooted)
    {
        if(isRooted)
        {
            bootText[1] = "# Root available...\n";
            bootText[2] = "# Checking adb status...";
        }

        final int[] step = {0};
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void run() {
                        if( step[0] < bootText.length) {
                            textViewRoot.append(bootText[step[0]]);
                            step[0]++;
                        }
                        else
                        {
                            timer.cancel();
                            if(!isRooted)
                            {
                                textViewError.animate().alpha(1).setDuration(3000);
                                showToast("Error:- Device has no Root");
                            }
                            else
                            {
                                relativeView.animate().alpha(1.0f).setDuration(2000);

                                //checking whether adb is running already
                                //adbDebugging();
                            }

                        }
                    }
                });

            }
        }, 500, 500);
    }



    private void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    //Checks adb status and then set views according to that
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void adbDebugging()
    {
        try
        {
            isRunning = ShellExecuter.execCmd("getprop init.svc.adbd");

            if(isRunning.equals("running\n"))
            {
                start("Adb is running!");
            }
            else if(isRunning.equals("stopped\n"))
            {
                stop("Adb is stopped!");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            isRunning = e.getMessage();
        }
    }



    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void start(String title)
    {
        isRunning = "running";
        textviewAdbStatus.setText("Running");
        textviewAdbStatus.setTextColor(Color.argb(255, 0, 125, 0));
        btnAdb.setText("STOP ADB");
        btnAdb.setAlpha(0);
        btnAdb.animate().alpha(1.0f).setDuration(1000);
        btnAdb.setTextColor(Color.argb(255, 175, 0, 0));
        textViewInfo.setVisibility(VISIBLE);

        if(getIpAddress().equals("0.0.0.0"))
        {
            Snackbar.make(mainView, "Please turn on Wifi to connect", Snackbar.LENGTH_LONG).show();
            textViewInfo.setText("Please turn on Wifi to connect");
            textViewInfo.setTextColor(Color.argb(255, 150, 0, 0));
        }
        else
        {
            Snackbar.make(mainView, "adb connect " + getIpAddress(), Snackbar.LENGTH_LONG).show();
            textViewInfo.setText("adb connect " + getIpAddress());
            textViewInfo.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            title = "Adb running at " + getIpAddress();
        }

        builder.setContentTitle(title);
        notificationManager.notify(1, builder.build());
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void stop(String title)
    {
        isRunning = "stopped";
        textviewAdbStatus.setText("Stopped");
        textviewAdbStatus.setTextColor(Color.argb(255, 175, 0, 0));
        btnAdb.setText("START ADB");
        btnAdb.setAlpha(0);
        btnAdb.animate().alpha(1.0f).setDuration(1000);
        btnAdb.setTextColor(Color.argb(255, 0, 125, 0));
        textViewInfo.setVisibility(View.INVISIBLE);
        //Toast.makeText(this, getIpAddress(), Toast.LENGTH_SHORT).show();
        builder.setContentTitle(title);
        notificationManager.cancel(1);
    }



    private String getIpAddress()
    {
        WifiManager wifiManager= (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }



    private void showAlertDialog(String title, String message, String buttonText)
    {
        final AlertDialog builder = new AlertDialog.Builder(this).create();
        builder.setTitle(title);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setPadding(50,50,50,50);
        TextView textView = new TextView(this);
        textView.setTextSize(16);
        textView.setText(message);
        scrollView.addView(textView);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.main);
        scrollView.setAnimation(animation);

        builder.setView(scrollView);
        builder.setIcon(R.drawable.logo);
        builder.setButton(-1, buttonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                builder.cancel();
            }
        });
        builder.show();
    }



}//class ends.

