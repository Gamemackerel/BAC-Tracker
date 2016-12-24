package com.example.abe.bac_dr1;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class BACUpdateSchedulerIntentService extends IntentService {
    private String TAG = "updater";
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";
    public static volatile boolean nuke;

    long delay = 10*1000; // 10 second delay
    LoopTask task = new LoopTask();
    Timer timer;


    public BACUpdateSchedulerIntentService() {
        super("SimpleIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String msg = intent.getStringExtra(PARAM_IN_MSG);
        start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void start() {
        timer = new Timer("TaskName");
        Date executionDate = new Date(); // no params = now
        timer.scheduleAtFixedRate(task, executionDate, delay);
    }

    public void sendMsg() {
        if(nuke) {
            Log.d(TAG, "Atomic boolean changed, updater stopping self");
            nuke = false;
            timer.cancel();
            timer = null;
            stopForeground(true);
            stopSelf();
        } else {
            Intent msgIntent = new Intent(this, BACIntentService.class);
            msgIntent.putExtra(BACIntentService.PARAM_IN_MSG, "m");
            startService(msgIntent);
        }
    }

    private class LoopTask extends TimerTask {
        public void run() {
            Log.d(TAG, "UpdateScheduler: Trying to send a work order to BACIntentService. This message will print every 10 seconds.");
            sendMsg();
        }
    }
}