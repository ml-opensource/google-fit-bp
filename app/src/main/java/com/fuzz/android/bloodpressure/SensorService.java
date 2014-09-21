package com.fuzz.android.bloodpressure;

import android.os.Handler;
import android.os.RemoteException;

import com.google.android.gms.fitness.DataPoint;
import com.google.android.gms.fitness.DataSet;
import com.google.android.gms.fitness.DataSource;
import com.google.android.gms.fitness.DataType;
import com.google.android.gms.fitness.DataTypes;
import com.google.android.gms.fitness.service.ApplicationSensorRequest;
import com.google.android.gms.fitness.service.ApplicationSensorService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by cesaraguilar on 9/15/14.
 *
 */
public class SensorService extends ApplicationSensorService {

    HashMap<DataType,ApplicationSensorRequest> requests;
    HashMap<DataType,DataSource> dataSources;
    Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        // 1. Initialize your software sensor(s).
        // 2. Create DataSource representations of your software sensor(s).
        // 3. Initialize some data structure to keep track of a registration for each sensor.
        requests = new HashMap<DataType, ApplicationSensorRequest>();
        dataSources = new HashMap<DataType, DataSource>();
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(DataTypeHelper.CUSTOM_DATA)
                .setName("fuzz-bp-sensor")
                .setType(DataSource.TYPE_RAW)
                .build();
        dataSources.put(DataTypeHelper.CUSTOM_DATA,dataSource);
    }

    @Override
    protected List<DataSource> findDataSources(List<DataType> dataTypes) {
        // 1. Find which of your software sensors provide the data types requested.
        // 2. Return those as a list of DataSource objects.
        ArrayList<DataSource> retList = new ArrayList<DataSource>();
        for (DataType data : dataTypes) {
            if (dataSources.containsKey(data)) {
                retList.add(dataSources.get(data));
            }
        }
        return retList;
    }

    @Override
    protected boolean register(ApplicationSensorRequest request) {
        // 1. Determine which sensor to register with request.getDataSource().
        // 2. If a registration for this sensor already exists, replace it with this one.
        // 3. Keep (or update) a reference to the request object.
        // 4. Configure your sensor according to the request parameters.
        // 5. When the sensor has new data, deliver it to the platform by calling
        //    request.getDispatcher().publish(List<DataPoint> dataPoints)
        if (dataSources.containsKey(request.getDataSource().getDataType())) {
            requests.put(request.getDataSource().getDataType(),request);
            if (runnable != null) {
                handler.removeCallbacks(runnable);
            }
            handler.post(runnable = new DispatchRunnable(request));
            return true;
        }
        return false;
    }

    @Override
    protected boolean unregister(DataSource dataSource) {
        // 1. Configure this sensor to stop delivering data to the platform
        // 2. Discard the reference to the registration request object
        if (requests.containsKey(dataSource.getDataType())) {
            requests.remove(dataSource.getDataType());
            handler.removeCallbacks(runnable);
            runnable = null;
            return true;
        }
        return false;
    }

    private DispatchRunnable runnable;
    private class DispatchRunnable implements Runnable {

        ApplicationSensorRequest mRequest;

        public DispatchRunnable(ApplicationSensorRequest request) {
            super();
            mRequest = request;
        }

        @Override
        public void run() {
            DataType dateType = mRequest.getDataSource().getDataType();
            DataSource dsApp = dataSources.get(dateType);

            DataSet dataSet = DataSet.create(dsApp);
            DataPoint point = dataSet.createDataPoint()
                    .setTimestamp(new Date().getTime(), TimeUnit.MILLISECONDS);
            point.getValue(dateType.getFields().get(0)).setInt(getRandomSys());
            point.getValue(dateType.getFields().get(1)).setInt(getRandomDia());
            point.getValue(DataTypes.Fields.BPM).setFloat(getRandomBPM());

            try {
                mRequest.getDispatcher().publish(point);
            } catch (RemoteException e) {
                //TODO what kind of exception
                e.printStackTrace();
            }
            handler.postDelayed(runnable,1000);
        }

        private int getRandomSys() {
            return (int) (Math.random()*200);
        }

        private int getRandomDia() {
            return (int) (Math.random()*130);
        }

        private float getRandomBPM() {
            return (int) (Math.random()*200);
        }
    }
}
