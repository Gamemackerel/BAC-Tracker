package com.example.abe.bac_dr1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    boolean pounds;
    String TAG = "console";
    //TO DO - add alcohol metabolization rate modifier as another user specified variable
    //use 5 options like bodyType? EZ copy and paste

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            //if succsessful in finding an existing config file, populate the settings with the
            //preferences found within. otherwise, the user must fill out the form himself.
            openFileInput("config.txt");
            Log.d(TAG, "there is a file that exists already, let me populate the settings");

            String[] userData = readFromFile().split(" ");
            Log.d(TAG, "userData[0]: " );

            int bodyTypeFromFile = Integer.parseInt(userData[0]) + 2;

            ArrayList<RadioButton> l = new ArrayList<RadioButton>();
            l.add(((RadioButton) findViewById(R.id.radioButtonNeg2)));
            l.add(((RadioButton) findViewById(R.id.radioButtonNeg1)));
            l.add(((RadioButton) findViewById(R.id.radioButton0)));
            l.add(((RadioButton) findViewById(R.id.radioButton1)));
            l.add(((RadioButton) findViewById(R.id.radioButton2)));

            l.get(bodyTypeFromFile).setChecked(true);

            if(Boolean.parseBoolean(userData[1])){
                ((RadioButton) findViewById(R.id.rBMale)).setChecked(true);
            } else {
                ((RadioButton) findViewById(R.id.rBFemale)).setChecked(true);
            }

            int kgWeightFromFile = (int) Math.round(Integer.parseInt(userData[2]) / 1000.0);
            ((EditText) findViewById(R.id.userWeight)).setText("" + kgWeightFromFile);

        } catch (FileNotFoundException e) {
            Log.d(TAG, "no file available; user must put in settings for first time");
        }
    }


    public void toggleKgsPds(View v) {
        if(pounds) {
            ((TextView) findViewById(R.id.weightType)).setText("Kilograms");
            ((TextView) findViewById(R.id.changeWeight)).setText("I prefer Imperial");
        } else { // currently accepting kgs
            ((TextView) findViewById(R.id.weightType)).setText("Pounds");
            ((TextView) findViewById(R.id.changeWeight)).setText("I prefer Metric");
        }
        pounds = !pounds;
    }


    //submit button, evalutates all the submitted fields and saves them.
    public void submitData(View v){
        if(((EditText) findViewById(R.id.userWeight)).getText().toString().isEmpty() || !(((RadioButton) findViewById(R.id.rBMale)).isChecked() || ((RadioButton) findViewById(R.id.rBFemale)).isChecked())) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Please fill out the settings form");
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

            //KILL BAC OBJECT WHEN SAVING NEW THING
            Intent msgIntent = new Intent(this, BACIntentService.class);
            msgIntent.putExtra(BACIntentService.PARAM_IN_MSG, "x");
            startService(msgIntent);



            //evaluating gender from radioButtons
            int genderChoice = ((RadioGroup) findViewById(R.id.gender)).getCheckedRadioButtonId();
            Log.d(TAG, "gender choice is " + genderChoice);
            boolean male = (genderChoice == ((RadioButton)findViewById(R.id.rBMale)).getId());
            Log.d(TAG, "are you a male? : " + male);
            //***********************************************************//

            int bodyTypeChoice = this.getBodyType();
            Log.d(TAG, "analyzed bodyType choice, you chose: " + bodyTypeChoice);






            Log.d(TAG, "BodyType int selection is : " + bodyTypeChoice);
            //userWeight
            double userWeightKg = Double.parseDouble(((EditText) findViewById(R.id.userWeight)).getText().toString());
            Log.d(TAG, "bodyType:");
            Log.d(TAG, "are you male?: " + male);
            //Write to user data file using the following user specified fields:
            //userWeight, boolean pounds, boolean gender, int bodyType
            //if weight was entered in pounds, convert to kilograms before writing
            Log.d(TAG, "userWeight: " + userWeightKg);
            Log.d(TAG, "weight is in pounds?: " + pounds);

            //if the weight was entered in pounds, convert to kilograms
            if (pounds) {
                userWeightKg *= 0.453592;
            }
            // multiply kilograms by 1000 to get the integer weight in grams
            int userWeightG = (int) (userWeightKg * 1000);
            String toStore = bodyTypeChoice + " " + male + " " + userWeightG;
            // wrote in following Syntax:
            //      bodyType male userWeight
            //          1    true   135
            //  to read from this file split on whitespace
            writeToFile(toStore);
            Log.d(TAG, "on Write in MainActivity, file contains :" + readFromFile());


            startActivity(new Intent(MainActivity.this, Main2Activity.class));
        }
    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    private int getBodyType() {
        ArrayList<RadioButton> l = new ArrayList<RadioButton>();
        l.add(((RadioButton) findViewById(R.id.radioButtonNeg2)));
        l.add(((RadioButton) findViewById(R.id.radioButtonNeg1)));
        l.add(((RadioButton) findViewById(R.id.radioButton0)));
        l.add(((RadioButton) findViewById(R.id.radioButton1)));
        l.add(((RadioButton) findViewById(R.id.radioButton2)));
        for(int i = 0; i < 5; i++) {
            if(l.get(i).isChecked()) {
                return i - 2;
            }
        }
        return 0;
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

}
