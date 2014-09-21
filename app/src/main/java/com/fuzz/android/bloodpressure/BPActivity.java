package com.fuzz.android.bloodpressure;

import android.util.Log;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.DataType;
import com.google.android.gms.fitness.DataTypeCreateRequest;
import com.google.android.gms.fitness.DataTypeCreateResult;
import com.google.android.gms.fitness.DataTypeResult;
import com.google.android.gms.fitness.DataTypes;
import com.google.android.gms.fitness.Fitness;

/**
 * Created by cesaraguilar on 9/14/14.
 *
 */
public abstract class BPActivity extends FitActivity {

    private static final String TAG = BPActivity.class.getSimpleName();
    public final static String BP = "com.fuzz.android.bloodpressure.blood_pressure";

    @Override
    public void invokeFitnessAPIs() {
        checkForDataType();
    }

    private void checkForDataType() {
        // Call the Fitness APIs here

        // 1. Invoke the History API with:
        // - The Google API client object
        // - The custom data type name
        PendingResult<DataTypeResult> pendingResult =
                Fitness.HistoryApi.readDataType(mClient, BP);

        // 2. Check the result asynchronously
        // (The result may not be immediately available)
        pendingResult.setResultCallback(
                new ResultCallback<DataTypeResult>() {
                    @Override
                    public void onResult(DataTypeResult dataTypeResult) {
                        // Retrieve the custom data type
                        if (dataTypeResult.getStatus().isSuccess()) {
                            DataType customType = dataTypeResult.getDataType();
                            // Use this custom data type to insert data in your app
                            DataTypeHelper.CUSTOM_DATA = customType;
                            onDataTypeAvailabe(customType);
                        } else {
                            createDataType();
                        }
                    }
                }
        );
    }

    private void createDataType() {
        // Call the Fitness APIs here

        // 1. Build a request to create a new data type
        DataTypeCreateRequest request = new DataTypeCreateRequest.Builder()
                // The prefix of your data type name must match your app's package name
                .setName(BP)
                        // Add some custom fields
                .addField("systolic", DataType.Field.FORMAT_INT32)
                .addField("diastolic", DataType.Field.FORMAT_INT32)
                .addField(DataTypes.Fields.BPM)
                .build();


        // 2. Invoke the History API with:
        // - The Google API client object
        // - The create data type request
        PendingResult<DataTypeCreateResult> pendingResult =
                Fitness.HistoryApi.addDataType(mClient, request);


        // 3. Check the result asynchronously
        // (The result may not be immediately available)
        pendingResult.setResultCallback(
                new ResultCallback<DataTypeCreateResult>() {
                    @Override
                    public void onResult(DataTypeCreateResult dataTypeResult) {
                        // Retrieve the created data type
                        if (dataTypeResult.getStatus().isSuccess()) {
                            DataType customType = dataTypeResult.getDataType();
                            // Use this custom data type to insert data in your app
                            DataTypeHelper.CUSTOM_DATA = customType;
                            onDataTypeAvailabe(customType);
                        } else {
                            //Serious error occurred, fix in production
                            Log.e(TAG,"error");
                        }
                    }
                }
        );
    }

    //Now we do something with the custom datatype
    public abstract void onDataTypeAvailabe(DataType customType);

}
