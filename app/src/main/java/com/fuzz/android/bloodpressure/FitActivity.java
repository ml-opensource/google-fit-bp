package com.fuzz.android.bloodpressure;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessScopes;
import com.google.android.gms.fitness.FitnessStatusCodes;

/**
 * Created by cesaraguilar on 9/14/14.
 *
 */
public abstract class FitActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = FitActivity.class.getSimpleName();
    protected GoogleApiClient mClient = null;
    private static final int REQUEST_OAUTH = 1000;

    @Override
    protected void onStart() {
        super.onStart();
        if (mClient == null || !mClient.isConnected()) {
            connectFitness();
        }
    }

    private void connectFitness() {
        Log.i(TAG, "Connecting...");

        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                // select the Fitness API
                .addApi(Fitness.API)
                        // specify the scopes of access
                .addScope(FitnessScopes.SCOPE_BODY_READ_WRITE)
                        // provide callbacks
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Connect the Google API client
        mClient.connect();
    }

    // Manage OAuth authentication
    @Override
    public void onConnectionFailed(ConnectionResult result) {

        // Error while connecting. Try to resolve using the pending intent returned.
        if (result.getErrorCode() == ConnectionResult.SIGN_IN_REQUIRED ||
                result.getErrorCode() == FitnessStatusCodes.NEEDS_OAUTH_PERMISSIONS) {
            try {
                // Request authentication
                result.startResolutionForResult(this, REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception connecting to the fitness service", e);
            }
        } else {
            Log.e(TAG, "Unknown connection issue. Code = " + result.getErrorCode());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            if (resultCode == RESULT_OK) {
                // If the user authenticated, try to connect again
                mClient.connect();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // If your connection gets lost at some point,
        // you'll be able to determine the reason and react to it here.
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            Log.i(TAG, "Connection lost.  Cause: Network Lost.");
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected!");
        invokeFitnessAPIs();
    }

    public abstract void invokeFitnessAPIs();
}
