package com.fuzz.android.bloodpressure;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.DataInsertRequest;
import com.google.android.gms.fitness.DataPoint;
import com.google.android.gms.fitness.DataSet;
import com.google.android.gms.fitness.DataSource;
import com.google.android.gms.fitness.DataSourceListener;
import com.google.android.gms.fitness.DataSourcesRequest;
import com.google.android.gms.fitness.DataSourcesResult;
import com.google.android.gms.fitness.DataType;
import com.google.android.gms.fitness.DataTypes;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.SensorRequest;
import com.google.android.gms.fitness.Value;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by cesaraguilar on 9/14/14.
 *
 */
public class RecorderActivity extends BPActivity implements View.OnClickListener {

    private static final String TAG = RecorderActivity.class.getSimpleName();
    DataType dateType;
    private DataSourceListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        findViewById(R.id.sensorButton).setOnClickListener(this);
    }

    @Override
    public void onDataTypeAvailabe(DataType customType) {
        dateType = customType;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,1,0,"Add").setIcon(android.R.drawable.ic_menu_add).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            doAdd();
        }
        return super.onOptionsItemSelected(item);
    }

    private void doAdd() {
        try {
            int sys, dia, bpm;
            sys = Integer.valueOf(((TextView)findViewById(R.id.systolic)).getText().toString());
            dia = Integer.valueOf(((TextView)findViewById(R.id.diastolic)).getText().toString());
            bpm = Integer.valueOf(((TextView)findViewById(R.id.heartrate)).getText().toString());
            if (sys > 0 && dia > 0 && bpm > 0) {
                recordVitals(sys,dia,bpm);
            } else {
                throw new Exception("Need valid vitals");
            }
        } catch (Throwable t) {
            t.printStackTrace();
            //TODO add error handling
        }
    }

    private void recordVitals(int sys, int dia, int bpm) {
        if (dateType != null) {
            // 1. Create a data source
            DataSource dsApp = new DataSource.Builder()
                    .setAppPackageName(this)
                    .setDataType(dateType)
                    .setName("fuzz-bp-recorder")
                    .setType(DataSource.TYPE_RAW)
                    .build();

            // 2. Create a data set
            DataSet dataSet = DataSet.create(dsApp);
            // for each data point (startTime, endTime, stepDeltaValue):
            DataPoint point = dataSet.createDataPoint()
                    .setTimestamp(new Date().getTime(), TimeUnit.MILLISECONDS);
            //TODO mentioned in presentation
//            point.setIntValues(sys,dia,bpm);
            point.getValue(dateType.getFields().get(0)).setInt(sys);
            point.getValue(dateType.getFields().get(1)).setInt(dia);
            point.getValue(DataTypes.Fields.BPM).setFloat(bpm);
            dataSet.add(point);

            // 3. Build a data insert request
            DataInsertRequest insreq = new DataInsertRequest.Builder()
                    .setDataSet(dataSet)
                    .build();

            // 4. Invoke the History API with:
            // - The Google API client object
            // - The insert data request
            PendingResult<Status> pendingResult =
                    Fitness.HistoryApi.insert(mClient, insreq);

            // 5. Check the result asynchronously
            // (The result is not immediately available)
            pendingResult.setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                ((TextView)findViewById(R.id.systolic)).setText("");
                                ((TextView)findViewById(R.id.diastolic)).setText("");
                                ((TextView)findViewById(R.id.heartrate)).setText("");
                            } else {
                                //TODO handle failure
                            }
                        }
                    }
            );
        } else {
            //TODO handle failure
        }
    }

    @Override
    public void onClick(View view) {
        //get a value from the sensor
        if (mListener == null) {
            ((Button)view).setText("STOP SENSOR");
            startRegistration();
        } else {
            ((Button)view).setText("READ FROM SENSOR");
            unregisterFitnessDataListener();
            mListener = null;
        }
    }

    private void startRegistration() {
//        DataSourcesRequest request = new DataSourcesRequest.Builder()
//                // At least one datatype must be specified.
//                .setDataTypes(dateType)
//                .setDataSourceTypes(DataSource.TYPE_RAW)
//                .build();
//        PendingResult<DataSourcesResult> result =
//                Fitness.SensorsApi.findDataSources(mClient, request);
//        result.setResultCallback(new ResultCallback<DataSourcesResult>() {
//            @Override
//            public void onResult(DataSourcesResult dataSourcesResult) {
//                Log.i(TAG, "Result: " + dataSourcesResult.getStatus().toString());
//                for (DataSource dataSource : dataSourcesResult.getDataSources()) {
//                    Log.i(TAG, "Data source found: " + dataSource.toString());
//                    Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());
//
//                    //Let's register a listener to receive Activity data!
//                    if (dataSource.getDataType().equals(dateType)
//                            && mListener == null) {
//                        registerFitnessDataListener(dataSource, dateType);
//                    }
//                }
//            }
//        });


        DataSourcesRequest request = new DataSourcesRequest.Builder()
                // At least one datatype must be specified.
                .setDataTypes(DataTypes.HEART_RATE_BPM)
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build();
        PendingResult<DataSourcesResult> result =
                Fitness.SensorsApi.findDataSources(mClient, request);

        result.setResultCallback(new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                Log.i(TAG, "Result: " + dataSourcesResult.getStatus().toString());
                for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                    Log.i(TAG, "Data source found: " + dataSource.toString());
                    Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());

                    //Let's register a listener to receive Activity data!
                    if (dataSource.getDataType().equals(DataTypes.HEART_RATE_BPM)
                            && mListener == null) {
                        registerFitnessDataListener(dataSource, DataTypes.HEART_RATE_BPM);
                    }
                }
            }
        });
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        mListener = new DataSourceListener() {
            @Override
            public void onEvent(final DataPoint dataPoint) {
                for (DataType.Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    Log.i(TAG, "Detected DataPoint field: " + field.getName());
                    Log.i(TAG, "Detected DataPoint value: " + val);
                }

                //TODO mention onEvent runs on the background thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        ((EditText)findViewById(R.id.systolic)).setText(dataPoint.getValue(dateType.getFields().get(0)).asInt()+"");
//                        ((EditText)findViewById(R.id.diastolic)).setText(dataPoint.getValue(dateType.getFields().get(1)).asInt()+"");
                        ((EditText)findViewById(R.id.heartrate)).setText(dataPoint.getValue(DataTypes.Fields.BPM).asFloat()+"");
                    }
                });
            }
        };

        SensorRequest request = new SensorRequest.Builder()
                .setDataSource(dataSource) // Optional but recommended for custom data sets.
                .setDataType(dataType) // Can't be omitted.
                .setSamplingRate(10, TimeUnit.SECONDS)
                .build();
        PendingResult<Status> result =
                Fitness.SensorsApi.register(mClient, request, mListener);

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.i(TAG, "Listener registered!");
                } else {
                    Log.i(TAG, "Listener not registered.");
                }
            }
        });
    }

    private void unregisterFitnessDataListener() {
        if(mListener == null) {
            // This code only activates one listener at a time.  If there's no listener, there's
            // nothing to unregister.
            return;
        }

        // Waiting isn't actually necessary as the unregister call will complete regardless,
        // even if called from within onStop, but a callback can still be added in order to
        // inspect the results.
        Fitness.SensorsApi.unregister(
                mClient,
                mListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Listener was removed!");
                        } else {
                            Log.i(TAG, "Listener was not removed.");
                        }
                    }
                });
    }
}
