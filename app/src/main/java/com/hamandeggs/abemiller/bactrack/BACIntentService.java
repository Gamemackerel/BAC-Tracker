package com.hamandeggs.abemiller.bactrack;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Arrays;

public class BACIntentService extends IntentService {
    private String TAG = "console";
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";
    public static final String DOUBLE_ARRAY = "dub[]";
    public static BAC bac;
    public static Intent updater;

    public BACIntentService() {
        super("SimpleIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String msg = intent.getStringExtra(PARAM_IN_MSG);
        Log.d(TAG, "BACIntentService: work order recieved, it was: " + msg);

        String[] userData = msg.split(" ");
        //3 possible instructions: start object, take shot, do metabolization calc, stop object

        //if the first char marker is s, then follow instructions for starting the BAC app
        if(msg.charAt(0) == 's') {

            Log.d(TAG, "starting BAC object with these parameters:" + Arrays.toString(userData));

            int bodyType = Integer.parseInt(userData[1]);
            boolean male = Boolean.parseBoolean(userData[2]);
            double KgramsWeight = (Double.parseDouble(userData[3]) / 1000); //change back to Kgs
            //int MetabModifier = Integer.parseInt(userData[4]);

            bac = new BAC(KgramsWeight, male, bodyType);
            Log.d(TAG, "BAC object initialized, now I'm going to start the BACUpdateSchedulerIntentService");


            //start the UpdateSchedulerIntentService that gives a scheduled work order to update bac once every 10 seconds
            updater = new Intent(this, BACUpdateSchedulerIntentService.class);
            updater.putExtra(BACUpdateSchedulerIntentService.PARAM_IN_MSG, "");
            startService(updater);
        } else if(msg.charAt(0) == 't') {
            Double volume = Double.parseDouble(userData[1]);
            Double percentEth = Double.parseDouble(userData[2]);
            Log.d(TAG, "message recieved from Main2Activity, it wants me to take a drink");
            bac.takeShot(volume, percentEth);
            bac.absorbUpdate();
            bac.metabolizeUpdate();
        } else if(msg.charAt(0) == 'm' && bac != null) {
            //this should be called on a scheduled execution once every 10 seconds
            //if i wanted to update once a second I would have to change these methods
            Log.d(TAG, "BACInentService: message recieved from updateScheduler, it wants me to update the BAC");
            bac.absorbUpdate();
            bac.metabolizeUpdate();
            Log.d(TAG, "I updated the BAC, the new value for BAC is: " + bac.getBAC());
            //Now I should use the updated BAC as a outbound message that can be read by any activity
            //which needs to display the current estimate



        } else if (msg.charAt(0) == 'x') {
            //end sesh
            //not sure if the user should ever manually do this, probably not.
            //maybe could be implemented by deleting the bac object by reassigning it to null
            bac = null;
            BACUpdateSchedulerIntentService.nuke = true;
            this.stopForeground(true);
            this.stopSelf();
        } else if(msg.charAt(0) == 'g' && bac != null) {
            // the graph activity asked me for graphing data in string form, let me get that from the BAC object
            // and broadcast it back to the graph activity
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(graphResults.ResponseReceiver.ACTION_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(DOUBLE_ARRAY, bac.getGraphData());
            sendBroadcast(broadcastIntent);
        }
        if(bac != null && msg.charAt(0) != 'g') {
            double thisBAC = bac.getBAC();
            String resultTxt = "" + thisBAC;
            Log.d(TAG, "passing back to main2activity: BAC is: " + resultTxt);
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(Main2Activity.ResponseReceiver.ACTION_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(PARAM_OUT_MSG, resultTxt);
            sendBroadcast(broadcastIntent);
        }


        //if the users BAC is estimated to be over the legal limit, give a notification warning the user to not drive
        if(bac != null && bac.getBAC() >= .08) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_stat_nodrive)
                            .setContentTitle("High Blood Alcohol - DO NOT DRIVE")
                            .setContentText("Your BAC is about " + Double.toString(bac.getBAC()).substring(0, 5) + "%");
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, Main2Activity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(Main2Activity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            int mId = 0;
            mNotificationManager.notify(mId, mBuilder.build());

        }


    }
}