package com.example.abe.bac_dr1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class graphResults extends AppCompatActivity {

    LineGraphSeries<DataPoint> warning;
    LineGraphSeries<DataPoint> seriesPre;
    LineGraphSeries<DataPoint> seriesPost;
    PointsGraphSeries<DataPoint> currentPointSeries;
    DataPoint currentPoint;

    double currentX;
    public static String TAG =  "console";

    private ResponseReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_results);


        Intent msgIntent = new Intent(this, BACIntentService.class);
        msgIntent.putExtra(BACIntentService.PARAM_IN_MSG, "g");
        startService(msgIntent);
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
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
        ((TextView) findViewById(R.id.graphBac)).setText("Current BAC:  " + Double.toString(BACIntentService.bac.getBAC()).substring(0,6) + "%");
        ((TextView) findViewById(R.id.gStom)).setText("Ethanol in Stomach:         " + Double.toString(BACIntentService.bac.getgStomachEthanol()).substring(0,4) + "g");
        ((TextView) findViewById(R.id.gBlood)).setText("Ethanol in Bloodstream:  " + Double.toString(BACIntentService.bac.getgBloodEthanol()).substring(0,4) + "g");
        ((TextView) findViewById(R.id.gMet)).setText("Ethanol Metabolized:       " + Double.toString(BACIntentService.bac.getgMetabolizedEthanol()).substring(0,4) + "g");
    }

    public void soberAt(double hours) {
        Double add = hours;
        Log.d("time", "hours add: " + add);
        long millisAdd = Math.round(add * 3.6 * (Math.pow(10,6)));
        Log.d("time", "start time: " + Main2Activity.c.getTime());
        long start = Main2Activity.c.getTimeInMillis();
        Log.d("time", "start time millis: " + Main2Activity.c.getTimeInMillis());
        Calendar k = Calendar.getInstance();
        k.setTimeInMillis(start + millisAdd);
        Log.d("time", "new Time: " + k.getTime());
        DateFormat df = new SimpleDateFormat("h:mm a");
        ((TextView) findViewById(R.id.soberAt)).setText("You should be fully sober by " + df.format(k.getTime()));
    }

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "com.mamlambo.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra(BACIntentService.DOUBLE_ARRAY)) {
                double[] graphPoints = intent.getDoubleArrayExtra(BACIntentService.DOUBLE_ARRAY);
                GraphView graph = (GraphView) findViewById(R.id.graph);
                GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
                gridLabel.setLabelVerticalWidth(90);

                gridLabel.setLabelFormatter(new DefaultLabelFormatter() {
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if (isValueX) {
                            // show hh:mm instead of x values
                            Double add = Double.parseDouble(super.formatLabel(value, isValueX));
                            Log.d("time", "hours add: " + add);
                            long millisAdd = Math.round(add * 3.6 * (Math.pow(10,6)));
                            Log.d("time", "start time: " + Main2Activity.c.getTime());
                            long start = Main2Activity.c.getTimeInMillis();
                            Log.d("time", "start time millis: " + Main2Activity.c.getTimeInMillis());
                            Calendar k = Calendar.getInstance();
                            k.setTimeInMillis(start + millisAdd);
                            Log.d("time", "new Time: " + k.getTime());
                            DateFormat df = new SimpleDateFormat("h:mm");
                            return df.format(k.getTime());
                        } else {
                            // show y values without the begginning 0
                            //also do not show 0% at the origin
                            if(Double.parseDouble(super.formatLabel(value,isValueX)) == 0) {
                                return null;
                            } else {
                                return super.formatLabel(value, isValueX).substring(1) + "%";
                            }
                        }
                    }
                });

                seriesPre = new LineGraphSeries<DataPoint>();
//                seriesPost = new LineGraphSeries<DataPoint>();  //This no longer exists
                currentPointSeries = new PointsGraphSeries<DataPoint>();
//                                warning = new LineGraphSeries<DataPoint>();
//                                warning.appendData(new DataPoint(0, .08), true, 2);
//                                warning.appendData(new DataPoint(((graphPoints.length) * (1.0 / 360)) * 2, .08), true, 2);
//                                graph.addSeries(warning);
                Paint paint1 = new Paint();
                paint1.setStyle(Paint.Style.STROKE);
                paint1.setStrokeWidth(5);
                paint1.setARGB(100, 255, 0, 0);
                seriesPre.setCustomPaint(paint1);
                int x = 0;
                for(int i = 0; i < graphPoints.length; i++) {
                    if(graphPoints[i] == -1.0) {
                        currentX = (i + 1) * (1.0 / 360);
                        currentPointSeries.appendData(new DataPoint(currentX, graphPoints[i + 1]), true, 1);
                        x--;
                    } else {
                        seriesPre.appendData(new DataPoint((x) * (1.0 / 360), graphPoints[i]), false, graphPoints.length);
                        x++;
                    }
                }

                x--;
                Log.d(TAG, "last value of array: " + graphPoints[graphPoints.length - 1]);
                soberAt(x * (1.0 / 360));

                Log.d("console", "graph onReceive: length: " + graphPoints.length + " " + Arrays.toString(graphPoints));

                graph.addSeries(seriesPre);
                graph.addSeries(currentPointSeries);
//                graph.addSeries(seriesPost);
            } else {
                                //BELOW: WHEN RECIEVING TEXT INTENT, USE TO UPDATE CURRENT POINT
                String text = intent.getStringExtra(BACIntentService.PARAM_OUT_MSG);
                double nextData = Double.parseDouble(text);
                DataPoint newCurrent = new DataPoint(currentX, nextData);
                DataPoint[] arbitraryArray = {newCurrent};
                currentPointSeries.resetData(arbitraryArray);
                currentX += 1.0 / 360;
//                seriesPre.appendData(newCurrent, true, 1000);


                ((TextView) findViewById(R.id.graphBac)).setText("Current BAC:  " + Double.toString(BACIntentService.bac.getBAC()).substring(0,6) + "%");
                ((TextView) findViewById(R.id.gStom)).setText("Ethanol in Stomach:         " + Double.toString(BACIntentService.bac.getgStomachEthanol()).substring(0,4) + "g");
                ((TextView) findViewById(R.id.gBlood)).setText("Ethanol in Bloodstream:  " + Double.toString(BACIntentService.bac.getgBloodEthanol()).substring(0,4) + "g");
                ((TextView) findViewById(R.id.gMet)).setText("Ethanol Metabolized:       " + Double.toString(BACIntentService.bac.getgMetabolizedEthanol()).substring(0,4) + "g");
            }
        }
    }
}
