package com.fuzz.android.bloodpressure;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.Bucket;
import com.google.android.gms.fitness.DataPoint;
import com.google.android.gms.fitness.DataReadRequest;
import com.google.android.gms.fitness.DataReadResult;
import com.google.android.gms.fitness.DataSet;
import com.google.android.gms.fitness.DataType;
import com.google.android.gms.fitness.DataTypes;
import com.google.android.gms.fitness.Fitness;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by cesaraguilar on 9/14/14.
 *
 */
public class VitalsActivity extends BPActivity {

    private static final String TAG = VitalsActivity.class.getSimpleName();
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitals);
        textView = (TextView) findViewById(R.id.vitals);
    }

    @Override
    public void onDataTypeAvailabe(DataType customType) {
        // 1. Obtain start and end times
        // (In this example, the start time is one week before this moment)
        long WEEK_IN_MS = 1000 * 60 * 60 * 24 * 7;
        Date now = new Date();
        long endTime = now.getTime();
        long startTime = endTime - (WEEK_IN_MS);

        // 2. Create a data request specifying data types and a time range
        // (In this example, group the data to find how many steps were walked per day)
        DataReadRequest readreq = new DataReadRequest.Builder()
                .addDefaultDataSource(customType)
                //TODO you can bucket by time or other units
                //.bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime)
                .build();

        // 3. Invoke the History API with:
        // - The Google API client object
        // - The read data request
        PendingResult<DataReadResult> pendingResult =
                Fitness.HistoryApi.readData(mClient, readreq);

        // 4. Access the results of the query asynchronously
        // (The result is not immediately available)
        pendingResult.setResultCallback(
                new ResultCallback<DataReadResult>() {
                    @Override
                    public void onResult(DataReadResult readDataResult) {
                        if (readDataResult.getStatus().isSuccess()) {
                            // If the request specified aggregated data, the data is returned as buckets
                            // that contain lists of DataSet objects
                            if (readDataResult.getBuckets() != null && readDataResult.getBuckets().size() > 0) {
                                for (Bucket bucket : readDataResult.getBuckets()) {
                                    List<DataSet> dataSets = bucket.getDataSets();
                                    for (DataSet dataSet : dataSets) {
                                        // Show the data points (see next example)
                                        dumpDataSet(dataSet);
                                    }
                                }
                                if (textView.getText().length()==0) {
                                    noResults();
                                }
                                // Otherwise, the data is returned as a list of DataSet objects
                            } else if (readDataResult.getDataSets() != null && readDataResult.getDataSets().size() > 0) {
                                for (DataSet dataSet : readDataResult.getDataSets()) {
                                    // Show the data points (see next example)
                                    dumpDataSet(dataSet);
                                }
                                if (textView.getText().length()==0) {
                                    noResults();
                                }
                            } else {
                                noResults();
                            }
                        } else {
                            noResults();
                        }
                    }
                }
        );
    }

    private void noResults() {
        textView.append(Html.fromHtml("No Results<br/>"));
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data type: " + dataSet.getDataType().getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        int Systolic=0,Diastolic=0;
        float BPM=0;

        for (DataPoint dp : dataSet.getDataPoints()) {
            // Obtain human-readable start and end times
            long dpStart = dp.getStartTimeNanos() / 1000000;
            long dpEnd = dp.getEndTimeNanos() / 1000000;
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dpStart));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dpEnd));

            String app = dp.getDataSource().getName();


            for(DataType.Field field : dp.getDataType().getFields()) {
                String fieldName = field.getName();
                Log.i(TAG, "\tField: " + fieldName +
                        " Value: " + dp.getValue(field));

                if (fieldName.equals("systolic")) {
                    Systolic = dp.getValue(field).asInt();
                } else if (fieldName.equals("diastolic")) {
                    Diastolic = dp.getValue(field).asInt();
                } else if (fieldName.equals(DataTypes.Fields.BPM.getName())) {
                    BPM = dp.getValue(DataTypes.Fields.BPM).asFloat();
                }
            }
            textView.append(Html.fromHtml(formatSys(Systolic)+"/"+formatDia(Diastolic)+":"+formatBPM(BPM)+" :"+app+"<br/>"));
        }
    }

    private String formatSys(int value) {
        if (value >= 180) {
            return "<font color='#E60008'>"+value+"</font>";
        } else if(value >= 160) {
            return "<font color='#FD4A0E'>"+value+"</font>";
        } else if (value >= 140) {
            return "<font color='#FF9315'>"+value+"</font>";
        } else if (value >= 120) {
            return "<font color='#FCFF4C'>"+value+"</font>";
        } else {
            return "<font color='#1EBC16'>"+value+"</font>";
        }
    }

    private String formatDia(int value) {
        if (value >= 110) {
            return "<font color='#E60008'>"+value+"</font>";
        } else if(value >= 100) {
            return "<font color='#FD4A0E'>"+value+"</font>";
        } else if (value >= 90) {
            return "<font color='#FF9315'>"+value+"</font>";
        } else if (value >= 80) {
            return "<font color='#FCFF4C'>"+value+"</font>";
        } else {
            return "<font color='#1EBC16'>"+value+"</font>";
        }
    }

    private String formatBPM(float value) {
        return "<b>"+value+"</b>";
    }

}
