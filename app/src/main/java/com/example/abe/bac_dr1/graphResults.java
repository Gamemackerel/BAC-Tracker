package com.example.abe.bac_dr1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.Arrays;
import java.util.Date;

public class graphResults extends AppCompatActivity {

    LineGraphSeries<DataPoint> warning;
    LineGraphSeries<DataPoint> seriesPre;
    LineGraphSeries<DataPoint> seriesPost;
    PointsGraphSeries<DataPoint> currentPointSeries;
    DataPoint currentPoint;

    double currentX;

    private ResponseReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_results);


        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);


        Intent msgIntent = new Intent(this, BACIntentService.class);
        msgIntent.putExtra(BACIntentService.PARAM_IN_MSG, "g");
        startService(msgIntent);


    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    //// TODO: change reciever to accept array of doubles instead of string
    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "com.mamlambo.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra(BACIntentService.DOUBLE_ARRAY)) {
                double[] graphPoints = intent.getDoubleArrayExtra(BACIntentService.DOUBLE_ARRAY);
                GraphView graph = (GraphView) findViewById(R.id.graph);
                double x = 0;


                //TODO: modify the x label to display x + timeStarted
                graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if (isValueX) {
                            // show normal x values
                            Double add = Double.parseDouble(super.formatLabel(value, isValueX));
                            return super.formatLabel(value, isValueX);
                        } else {
                            // show currency for y values
                            return super.formatLabel(value, isValueX);
                        }
                    }
                });
                //TODO ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

                seriesPre = new LineGraphSeries<DataPoint>();
                seriesPost = new LineGraphSeries<DataPoint>();
                currentPointSeries = new PointsGraphSeries<DataPoint>();
//                                warning = new LineGraphSeries<DataPoint>();
//                                warning.appendData(new DataPoint(0, .08), true, 2);
//                                warning.appendData(new DataPoint(((graphPoints.length) * (1.0 / 360)) * 2, .08), true, 2);
//                                graph.addSeries(warning);
                Paint paint1 = new Paint();
                paint1.setStyle(Paint.Style.STROKE);
                paint1.setStrokeWidth(3);
                paint1.setARGB(100, 170, 150, 0);
                seriesPost.setCustomPaint(paint1);

                for(int i = 0; i < graphPoints.length; i++) {
                    if(graphPoints[i] == -1.0) {
                        currentPointSeries.appendData(new DataPoint((i+1) * (1.0 / 360), graphPoints[i + 1]), true, 1);
                    } else {
                        seriesPre.appendData(new DataPoint(i * (1.0 / 360), graphPoints[i]), true, graphPoints.length);
                    }
                }



                Log.d("console", "graph onReceive: length: " + graphPoints.length + " " + Arrays.toString(graphPoints));

                graph.addSeries(seriesPre);
                graph.addSeries(currentPointSeries);
//                graph.addSeries(seriesPost);
            } else {
                                //BELOW: WHEN RECIEVING TEXT INTENT, APPEND TO SERIES PRE
                String text = intent.getStringExtra(BACIntentService.PARAM_OUT_MSG);
                double nextData = Double.parseDouble(text);
                currentX += 1.0 / 360;
                DataPoint newCurrent = new DataPoint(currentX, nextData);
                DataPoint[] arbitraryArray = {newCurrent};
                currentPointSeries.resetData(arbitraryArray);
//                seriesPre.appendData(newCurrent, true, 1000);
            }
        }
    }
}
