package com.example.pawesajnog.myfirstapp;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

/**
 * aa
 * Created by Paweł Sajnóg on 2017-07-10.
 */

public class GPSTracking extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = GPSTracking.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;


    Messenger replyMessanger;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MainActivity.MSG_SAY_HELLO:
                    Toast.makeText(getApplicationContext(), "GPS tracking started!", Toast.LENGTH_SHORT).show();
                    replyMessanger = msg.replyTo; //init reply messenger
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setLocationLocationRequest();
    }

    @Override
    public void onDestroy() {
        // Your need of location update is done. So you have to stop the apiClient.
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Tracking stoped!", Toast.LENGTH_SHORT).show();
        this.mGoogleApiClient.disconnect();
    }

    private void setLocationLocationRequest() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000)
                .setFastestInterval(200)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(2);
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Your API Client is connected. So can request for updates
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Play services connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        // After your desired interval This api will give you the Location Object.

        sendLocation(location);
    }

    private void sendLocation(Location location) {
        // do stuff

        if (replyMessanger != null)
            try {
                Message message = Message.obtain(null, MainActivity.MSG_LOCATION_DATA, location);
                replyMessanger.send(message);//replying / sending msg to activity
            } catch (RemoteException e) {
                e.printStackTrace();
            }
    }
}
