package com.example.pawesajnog.myfirstapp.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pawesajnog.myfirstapp.services.LoginService;
import com.example.pawesajnog.myfirstapp.Person;
import com.example.pawesajnog.myfirstapp.R;
import com.example.pawesajnog.myfirstapp.services.RegistrationService;

import static com.example.pawesajnog.myfirstapp.StaticValues.MSG_SAY_HELLO;
import static com.example.pawesajnog.myfirstapp.StaticValues.PERSON;
import static com.example.pawesajnog.myfirstapp.StaticValues.REGISTER_INFO;

/**dasa
 * Created by Paweł Sajnóg on 2017-08-01.
 */

public class RegistrationActivity extends Activity {
    Context mContext;
    Messenger mService = null;
    /** Flag indicating whether we have called bind on the service.*/
    boolean mBound;

    EditText username;
    EditText password;
    EditText passwordConfirm;

    Button registerButton;
    Button cancelButton;

    /** Class for interacting with the main interface of the service.*/
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

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REGISTER_INFO:
                    String registerInfo = (String) msg.obj;

                    if ("success".equals(registerInfo)){
                        Log.i("hura", "hura");
                        Intent intent = new Intent(getApplicationContext(), LoginService.class);
                        Person person = new Person(username.getText().toString(), password.getText().toString());
                        intent.putExtra(PERSON, person);
                        startService(intent);
                        finish();
                    } else {
                        Log.d("problem", "with register");
                        Toast.makeText(getApplicationContext(), R.string.registration_filed, Toast.LENGTH_LONG).show();
                    }

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new RegistrationActivity.IncomingHandler());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.registration_window);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int) (width * 0.9), (int) (height * 0.7));

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        passwordConfirm = findViewById(R.id.passwordConfirm);
        registerButton = findViewById(R.id.registerButton);
        cancelButton = findViewById(R.id.cancelButton);

        Intent intent = getIntent();
        Person person = (Person) intent.getSerializableExtra(PERSON);
        username.setText(person.getName());
        password.setText(person.getPassword());
    }

    @Override
    protected void onStart() {
        super.onStart();

        mContext = this;
        // Bind to the service
        bindService(new Intent(this, RegistrationService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    public void register(View view) {
        if(isConnected()) {
            if(!"".equals(password.getText().toString()) & password.getText().toString().equals(passwordConfirm.getText().toString())) {
                Intent intent = new Intent(this, RegistrationService.class);
                Person person = new Person(username.getText().toString(), password.getText().toString());
                intent.putExtra(PERSON, person);
                startService(intent);
            } else {
                Toast.makeText(getApplicationContext(), R.string.passwords_dont_match, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
        }
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void cancelActivity(View view) {
        finish();
    }
}