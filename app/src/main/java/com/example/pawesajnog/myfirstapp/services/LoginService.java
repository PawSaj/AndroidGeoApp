package com.example.pawesajnog.myfirstapp.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.example.pawesajnog.myfirstapp.MySingleton;
import com.example.pawesajnog.myfirstapp.Person;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.example.pawesajnog.myfirstapp.StaticValues.LOGIN_INFO;
import static com.example.pawesajnog.myfirstapp.StaticValues.LOGIN_URL;
import static com.example.pawesajnog.myfirstapp.StaticValues.MSG_SAY_HELLO;
import static com.example.pawesajnog.myfirstapp.StaticValues.PERSON;


public class LoginService extends Service {
    Messenger replyMessanger;

    private Person person;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    //Toast.makeText(getApplicationContext(), "Login started", Toast.LENGTH_SHORT).show();
                    replyMessanger = msg.replyTo; //init reply messenger
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new LoginService.IncomingHandler());

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        person = (Person) intent.getSerializableExtra(PERSON);

        login();


        return super.onStartCommand(intent, flags, startId);
    }

    private void login() {
        //final String[] results = new String[1];

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL, new Response.Listener<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(String response) {
                //results[0] = response;

                if ("login successful".equals(response)) {
                    Log.i("login_info", response);
                    saveFile("userData", person.toString());

                    if(replyMessanger!=null) {
                        Message message = Message.obtain(null, LOGIN_INFO, "success");
                        try {
                            replyMessanger.send(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }


                } else {
                    Log.wtf("login_unsuccessful", "Data don't saved!");
                    if(replyMessanger!=null) {
                        Message message = Message.obtain(null, LOGIN_INFO, "fail");
                        try {
                            replyMessanger.send(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.wtf("error", error.getMessage());
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                Map<String, String> responseHeaders = response.headers;

                try {
                    if (!"error".equals(new String(response.data, "UTF-8"))) {
                        saveFile("cookie", responseHeaders.get("Set-Cookie"));
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if ("".equals(response.toString())) {
                    return Response.success(
                            null,
                            HttpHeaderParser.parseCacheHeaders(response));
                } else {
                    return super.parseNetworkResponse(response);
                }
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", person.getName());
                params.put("password", person.getPassword());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void saveFile(String filename, String data) {

        try {
            FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileInputStream in = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            Log.i("readed_file", sb.toString());
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
