package com.example.abe.bac_dr1;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class BACUpdateSchedulerIntentService extends IntentService {
    private String TAG = "console";
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";


    long delay = 10*1000; // 10 second delay
    LoopTask task = new LoopTask();
    Timer timer = new Timer("TaskName");


    public BACUpdateSchedulerIntentService() {
        super("SimpleIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String msg = intent.getStringExtra(PARAM_IN_MSG);
        start();

    }



    public void start() {

        timer.cancel();
        timer = new Timer("TaskName");
        Date executionDate = new Date(); // no params = now
        timer.scheduleAtFixedRate(task, executionDate, delay);
    }

    public void sendMsg() {
        Intent msgIntent = new Intent(this, BACIntentService.class);
        msgIntent.putExtra(BACIntentService.PARAM_IN_MSG, "m");
        startService(msgIntent);
    }

    private class LoopTask extends TimerTask {
        public void run() {
            Log.d(TAG, "UpdateScheduler: Trying to send a work order to BACIntentService. This message will print every 60 seconds.");
            sendMsg();
        }
    }
}