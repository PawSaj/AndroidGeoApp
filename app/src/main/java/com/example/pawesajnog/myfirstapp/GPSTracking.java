package com.example.pawesajnog.myfirstapp;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Paweł Sajnóg .
 */

public class GPSTracking extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = GPSTracking.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private String filename;
    private FileOutputStream outputStreamForInternalStorage;
    private FileOutputStream outputStreamForExternalStorage;

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
        //readFile();
        openFiles();
    }

    private void openFiles() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd HH_mm_ss", Locale.ENGLISH);
        Date date = new Date();
        filename = dateFormat.format(date) +".txt";
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);

        try {
            outputStreamForInternalStorage = openFileOutput(filename, MODE_APPEND);
            outputStreamForInternalStorage.write(dateFormat.format(date).getBytes());
        } catch (IOException e) {
            Log.d(TAG, "Error while creating or open internal storage file");
            Toast.makeText(getApplicationContext(), "Error while creating or open internal storage file! The data will not be saved!", Toast.LENGTH_LONG).show();
        }

        if(isExternalStorageWritable()) {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/saved_location");
            myDir.mkdirs();
            File file = new File(myDir, filename);
            try {
                outputStreamForExternalStorage = new FileOutputStream(file, true);
                outputStreamForExternalStorage.write(dateFormat.format(date).getBytes());
            } catch (IOException e) {
                Log.d(TAG, "Error while creating or open external storage file");
                Toast.makeText(getApplicationContext(), "Error while creating or open external storage file! The data will not be saved!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private void readFile() {
        try {
            FileInputStream in = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            Toast.makeText(getApplicationContext(), sb.toString(), Toast.LENGTH_LONG).show();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Your need of location update is done. So you have to stop the apiClient.
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Tracking stoped!", Toast.LENGTH_SHORT).show();
        this.mGoogleApiClient.disconnect();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
        Date date = new Date();
        try {
            if (outputStreamForInternalStorage != null) {
                outputStreamForInternalStorage.write(("\n" + dateFormat.format(date) + "\n").getBytes());
                outputStreamForInternalStorage.close();
            }
        } catch (IOException e) {
            Log.d(TAG, "Error while closing internal storage file");
            Toast.makeText(getApplicationContext(), "Error while closing internal storage file! The data will not be saved!", Toast.LENGTH_LONG).show();
        }
        try {
            if (outputStreamForExternalStorage != null) {
                outputStreamForExternalStorage.write(("\n" + dateFormat.format(date) + "\n").getBytes());
                outputStreamForExternalStorage.close();
            }
        } catch (IOException e) {
            Log.d(TAG, "Error while closing external storage file");
            Toast.makeText(getApplicationContext(), "Error while closing external storage file! The data will not be saved!", Toast.LENGTH_LONG).show();
        }
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
        saveLocationToInternalStorage(location);
        saveLocationToExternalStorage(location);
        sendLocation(location);
    }

    private void saveLocationToExternalStorage(Location location) {

        String locationData = "\n" + location.getLatitude() + " " + location.getLongitude();
        try {
            if (outputStreamForExternalStorage != null)
                outputStreamForExternalStorage.write(locationData.getBytes());
        } catch (IOException e) {
            Log.d(TAG, "Error while writting external storage file");
            Toast.makeText(getApplicationContext(), "Error while writing external storage file! The data will not be saved!", Toast.LENGTH_LONG).show();
        }
    }

    private void saveLocationToInternalStorage(Location location) {
        String locationData = "\n" + location.getLatitude() + " " + location.getLongitude();
        try {
            if (outputStreamForInternalStorage != null)
                outputStreamForInternalStorage.write(locationData.getBytes());
        } catch (IOException e) {
            Log.d(TAG, "Error while writting internal storage file");
            Toast.makeText(getApplicationContext(), "Error while writing internal storage file! The data will not be saved!", Toast.LENGTH_LONG).show();
        }
    }

    private void sendLocation(Location location) {
        if (replyMessanger != null)
            try {
                Message message = Message.obtain(null, MainActivity.MSG_LOCATION_DATA, location);
                replyMessanger.send(message);//replying / sending msg to activity
            } catch (RemoteException e) {
                e.printStackTrace();
            }
    }
}
