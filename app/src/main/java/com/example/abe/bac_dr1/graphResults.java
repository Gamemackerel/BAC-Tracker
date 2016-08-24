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

//        double y,x;
//        x = -5.0;
//        GraphView graph = (GraphView) findViewById(R.id.graph);
//        series = new LineGraphSeries<DataPoint>();
//        for(int i = 0; i < 500; i++) {
//            x = x + 0.1;
//            y = Math.sin(x);
//            series.appendData(new DataPoint(x, y), true, 500);
//        }
//        graph.addSeries(series);

    }

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "com.mamlambo.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra(BACIntentService.PARAM_OUT_MSG);
            Log.d("console", "graph onReceive: " + text);
            GraphView graph = (GraphView) findViewById(R.id.graph);

            if(text.contains(" # ")) {
                text = text.substring(4);
                String[] data = text.split(" # ");
                String[] history = data[0].trim().split(" ");
                String[] prediction = data[1].trim().split(" ");
                Log.d("console", "Graph activity: " + data[0] + " //// " + data[1]);

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


                seriesPre = new LineGraphSeries<DataPoint>();
                seriesPost = new LineGraphSeries<DataPoint>();
                currentPointSeries = new PointsGraphSeries<DataPoint>();
//                warning = new LineGraphSeries<DataPoint>();
//                warning.appendData(new DataPoint(0, .08), true, 2);
//                warning.appendData(new DataPoint(((history.length + prediction.length) * (1.0 / 360)) * 2, .08), true, 2);
//                graph.addSeries(warning);

                Paint paint1 = new Paint();
                paint1.setStyle(Paint.Style.STROKE);
                paint1.setStrokeWidth(3);
                paint1.setARGB(100, 170, 150, 0);
                seriesPost.setCustomPaint(paint1);


                for(int i = 0; i < history.length; i++) {
                    seriesPre.appendData(new DataPoint(x, Double.parseDouble(history[i])), true, history.length);
                    x += 1.0 / 360;
                }
                currentPoint = new DataPoint(x, Double.parseDouble(prediction[0]));
                currentPointSeries.appendData(currentPoint, true, 1);
                currentX = x;
                for(int i = 1; i < prediction.length; i++) {
                    x += 1.0 / 360;
                    seriesPost.appendData(new DataPoint(x, Double.parseDouble(prediction[i])), true, prediction.length);
                }

                graph.addSeries(seriesPre);
                graph.addSeries(currentPointSeries);
                graph.addSeries(seriesPost);

            } else {
                double nextData = Double.parseDouble(text);
                currentX += 1.0 / 360;
                DataPoint newCurrent = new DataPoint(currentX, nextData);
                DataPoint[] arbitraryArray = {newCurrent};
                currentPointSeries.resetData(arbitraryArray);
                seriesPre.appendData(newCurrent, true, 1000);
            }

        }
    }
}
