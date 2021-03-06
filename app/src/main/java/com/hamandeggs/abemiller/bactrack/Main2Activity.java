package com.hamandeggs.abemiller.bactrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

public class Main2Activity extends AppCompatActivity {

    static Boolean seshActive = false;
    String TAG = "console";
    private ResponseReceiver receiver;
    public static int drinks;
    public static Calendar c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        String prefs = readFromFile();
        Log.d(TAG, "onCreateMain2: the second activity was switched to successfully");
        Log.d(TAG, "onCreateMain2: " + prefs);


        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);

        try {
            openFileInput("config.txt");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "startApp: no config file found, go to settings");


            startActivity(new Intent(Main2Activity.this, MainActivity.class));
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
        if (seshActive) {
            if(drinks > 1) {
                ((TextView) findViewById(R.id.totalDrinks)).setText("" + drinks + " drinks consumed");
            } else {
                ((TextView) findViewById(R.id.totalDrinks)).setText("" + drinks + " drink consumed");
            }
            ((TextView) findViewById(R.id.startButton)).setText("Drink");
            ((TextView) findViewById(R.id.bloodAlcoholContent)).setText(Double.toString(BACIntentService.bac.getBAC()).substring(0,6) + "%");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {

        }
    }

    //On clicking the *Start Drinking* button
    //if the drinking intentservice has not yet begun, create an intent which contains all the
    // necessary parameters for BAC, start it now, and run the BAC class from the intentservice
    // with the work intent I constructed
    // tell user they should press the button after they finish a beverage

    public void startSesh(View view) {

        if(!seshActive) {
            seshActive = true;
            ((TextView) findViewById(R.id.startButton)).setText("Drink");
            ((TextView) findViewById(R.id.startButton)).setBackgroundColor(Color.parseColor("#d1003b"));
            ((TextView) findViewById(R.id.startButton)).setTextColor(Color.WHITE);

            c = Calendar.getInstance();
            Log.d("time", "the current time is: " + c.getTime());

            String prefs = "s " + readFromFile();
            //prefs will be the param in msg of the intentService and it will
            //be used to construct a new BAC object in the Intent

            //USEFUL LINKS:
            // https://developer.android.com/training/run-background-service/create-service.html

            // http://code.tutsplus.com/tutorials/android-fundamentals-intentservice-basics--mobile-6183

            Intent msgIntent = new Intent(this, BACIntentService.class);
            msgIntent.putExtra(BACIntentService.PARAM_IN_MSG, prefs);
            startService(msgIntent);
            Toast toast = Toast.makeText(getApplicationContext(), "BAC Tracking Session Initialized", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            takeShot();
        }


    }

    public void takeShot() {
        //checks to see if the user filled in the drink data, if not, scolds them, if so, starts tracking the drink
        if(((EditText) findViewById(R.id.drinkVolume)).getText().toString().isEmpty() || ((EditText) findViewById(R.id.drinkPercent)).getText().toString().isEmpty()) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Please enter the drink volume and percentage ethanol first");
            builder1.setCancelable(true);
            builder1.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        } else {
            drinks++;
            if(drinks > 1) {
                ((TextView) findViewById(R.id.totalDrinks)).setText("" + drinks + " drinks consumed");
            } else {
                ((TextView) findViewById(R.id.totalDrinks)).setText("" + drinks + " drink consumed");
            }
            double volume = Double.parseDouble(((EditText) findViewById(R.id.drinkVolume)).getText().toString());
            double percent = Double.parseDouble(((EditText) findViewById(R.id.drinkPercent)).getText().toString());
            String drinkInfo = "t " + volume + " " + percent;
            Intent msgIntent = new Intent(this, BACIntentService.class);
            msgIntent.putExtra(BACIntentService.PARAM_IN_MSG, drinkInfo);
            startService(msgIntent);
        }
    }



    public void goToSettings(View view) {
        startActivity(new Intent(Main2Activity.this, MainActivity.class));
    }

    public void goToGraph(View view) {
        if(seshActive && BACIntentService.bac.getBAC() != 0) {
            startActivity(new Intent(Main2Activity.this, graphResults.class));
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "no data to graph", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    private String readFromFile() {

        String ret = "";

        try {
            InputStream inputStream = openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }


    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "com.mamlambo.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            TextView result = (TextView) findViewById(R.id.bloodAlcoholContent);
            if(intent.hasExtra(BACIntentService.PARAM_OUT_MSG)) {
                String text = intent.getStringExtra(BACIntentService.PARAM_OUT_MSG);
                if (text.length() > 6) {
                    text = text.substring(0, 6);
                }
                result.setText(text + "%");
            }
        }
    }
}
