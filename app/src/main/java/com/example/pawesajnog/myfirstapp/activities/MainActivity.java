package com.example.pawesajnog.myfirstapp.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.pawesajnog.myfirstapp.R;
import com.example.pawesajnog.myfirstapp.fragments.MapViewFragment;
import com.example.pawesajnog.myfirstapp.services.GPSTracking;
import com.example.pawesajnog.myfirstapp.services.LogoutService;
import com.example.pawesajnog.myfirstapp.services.SendFileService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.example.pawesajnog.myfirstapp.StaticValues.MSG_LOCATION_DATA;
import static com.example.pawesajnog.myfirstapp.StaticValues.MSG_SAY_HELLO;
import static com.example.pawesajnog.myfirstapp.StaticValues.MY_APP;
import static com.example.pawesajnog.myfirstapp.StaticValues.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    MapViewFragment fragment;

    Messenger mService;
    boolean mBound;

    Context mContext;
    Switch mLoginSwitch;
    Switch mGPSTrackingSwitch;

    final Messenger mMessenger = new Messenger(new MainActivity.IncomingHandler());

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

    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(this, SendFileService.class));

        mContext = this;

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = (MapViewFragment) getSupportFragmentManager().findFragmentById(R.id.locationFragment);

        mGPSTrackingSwitch = (Switch) findViewById(R.id.GPS_switch);
        mLoginSwitch = (Switch) findViewById(R.id.loginSwitch);
        final Activity mActivity = this;

        mGPSTrackingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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

        checkUserDataExist();
        mLoginSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startActivity(new Intent(mActivity, LoginActivity.class));
                } else {
                    startService(new Intent(mActivity, LogoutService.class));
                    deleteFile("cookie");
                    if(deleteFile("userData")) {
                        Toast.makeText(getApplicationContext(), R.string.user_logout, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserDataExist();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, GPSTracking.class));
    }

    private void checkUserDataExist() {
        File cookieFile = new File(getApplicationContext().getFilesDir() + "/cookie");
        File userDataFail = new File(getApplicationContext().getFilesDir() + "/userData");
        if (cookieFile.exists()) {
            //TODO check cookie has valid session
            mLoginSwitch.setChecked(true);
        } else if (userDataFail.exists()) {
            //TODO login for valid cookie
            mLoginSwitch.setChecked(true);
        } else {
            mLoginSwitch.setChecked(false);
        }
    }

    private void sayHello() {
        if (!mBound) {
            return;
        }

        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, MSG_SAY_HELLO, 0, 0);
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
                case MSG_LOCATION_DATA:
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
