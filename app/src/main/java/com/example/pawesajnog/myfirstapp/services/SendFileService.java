package com.example.pawesajnog.myfirstapp.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.pawesajnog.myfirstapp.Person;
import com.example.pawesajnog.myfirstapp.R;
import com.example.pawesajnog.myfirstapp.volleyConfig.MySingleton;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.pawesajnog.myfirstapp.StaticValues.ACTION_LOGGED;
import static com.example.pawesajnog.myfirstapp.StaticValues.ACTION_NEW_MSG;
import static com.example.pawesajnog.myfirstapp.StaticValues.FILE_RECEIVE_URL;
import static com.example.pawesajnog.myfirstapp.StaticValues.MSG_FIELD;
import static com.example.pawesajnog.myfirstapp.StaticValues.MY_APP;
import static com.example.pawesajnog.myfirstapp.StaticValues.PERSON;

/**
 * afsad
 * Created by Paweł Sajnóg on 2017-08-02.
 */

public class SendFileService extends Service {
    private MyReceiver myReceiver;
    private String filename;
    private Set<String> filesToSend = new HashSet<>();


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        initReceiver();
    }

    private void initReceiver() {
        myReceiver = new MyReceiver();
        registerReceiver(myReceiver, new IntentFilter(ACTION_NEW_MSG));
        registerReceiver(myReceiver, new IntentFilter(ACTION_LOGGED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    private void sendFile(final String dataToSend) {
        //final String[] results = new String[1];

        StringRequest stringRequest = new StringRequest(Request.Method.POST, FILE_RECEIVE_URL, new Response.Listener<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(String response) {
                //results[0] = response;

                if ("success".equals(response)) {
                    Log.i("dataSave_info", response);
                    if(filesToSend.contains(filename)){
                        filesToSend.remove(filename);
                    }
                    if(!filesToSend.isEmpty()) {
                        sendUnsentFile();
                    }

                } else {
                    Log.wtf("dataSave_unsuccessful", "Data don't saved!");
                    Toast.makeText(getApplicationContext(), R.string.filed_to_send_file, Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401) {
                    String username = null, password = null;
                    String mydata = readFile("userData");
                    Pattern pattern = Pattern.compile("(.*);");
                    Matcher matcher = pattern.matcher(mydata);
                    if (matcher.find()) {
                        username = matcher.group(1);
                    }
                    Pattern pattern2 = Pattern.compile(";(.*)");
                    Matcher matcher2 = pattern2.matcher(mydata);
                    if (matcher2.find()) {
                        password = matcher2.group(1);
                    }

                    filesToSend.add(filename);

                    Intent intent = new Intent(getApplicationContext(), LoginService.class);
                    Person person = new Person(username, password);
                    intent.putExtra(PERSON, person);
                    startService(intent);
                } else {
                    Log.wtf("dataSave_unsuccessful", "Data don't saved!");
                }


            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Cookie", readFile("cookie"));
                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("filename", filename);
                params.put("fileData", dataToSend);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private byte[] getBytesFromFile() {
        RandomAccessFile f;
        byte[] b = new byte[0];
        try {
            f = new RandomAccessFile(getApplicationContext().getFilesDir() + "/" + filename, "r");
            b = new byte[(int) f.length()];
            f.readFully(b);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return b;
    }

    public void sendUnsentFile() {
        Iterator<String> it = filesToSend.iterator();
        if(it.hasNext()) {
            filename = it.next();
            String value = Base64.encodeToString(getBytesFromFile(), Base64.URL_SAFE);
            sendFile(value);
        }
    }

    public String readFile(String filename) {
        try {
            FileInputStream in = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            line = bufferedReader.readLine();
            sb.append(line);
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(";");
                sb.append(line);
            }
            in.close();
            return sb.toString();
        } catch (IOException e) {
            Log.d(MY_APP, "No user data files or no access to them");
        }
        return null;
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_NEW_MSG)) {
                filename = intent.getStringExtra(MSG_FIELD);

                String value = Base64.encodeToString(getBytesFromFile(), Base64.URL_SAFE);
                sendFile(value);
            } else if (intent.getAction().equals(ACTION_LOGGED)) {
                if(!filesToSend.isEmpty()) {
                    sendUnsentFile();
                }
            }
        }
    }

}
