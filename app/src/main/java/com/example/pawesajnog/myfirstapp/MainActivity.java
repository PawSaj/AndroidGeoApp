package com.example.pawesajnog.myfirstapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int MSG_SAY_HELLO = 1;
    public static final int MSG_LOCATION_DATA = 2;

    MapViewFragment fragment;

    /**
     * Messenger for communicating with the service.
     */
    Messenger mService;

    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean mBound;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
            sayHello();
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mBound = false;
        }
    };
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new MainActivity.IncomingHandler());

    Switch mSwitch;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = (MapViewFragment) getSupportFragmentManager().findFragmentById(R.id.locationFragment);

        mSwitch = (Switch) findViewById(R.id.GPS_switch);
        final Activity mActivity = this;

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(new Intent(mActivity, GPSTracking.class));
                    bindService(new Intent(mActivity, GPSTracking.class), mConnection,
                            Context.BIND_AUTO_CREATE);
                    fragment.turnOnLocationUI();
                } else {
                    if (mBound) {
                        unbindService(mConnection);
                        mBound = false;
                    }
                    stopService(new Intent(mActivity, GPSTracking.class));
                    fragment.turnOffLocationUI();
                }
            }
        });
    }

    private void sayHello() {
        if (!mBound) {
            return;
        }

        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, MainActivity.MSG_SAY_HELLO, 0, 0);
        msg.replyTo = mMessenger;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MainActivity.MSG_LOCATION_DATA:
                    Location location = (Location) msg.obj;
                    //Toast.makeText(getApplicationContext(), (String.valueOf(location.getLatitude()) + "\n" + String.valueOf(location.getLongitude())), Toast.LENGTH_SHORT).show();
                    fragment.updateLocation(location);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


}
