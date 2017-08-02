package com.example.pawesajnog.myfirstapp.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.pawesajnog.myfirstapp.volleyConfig.MySingleton;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static com.example.pawesajnog.myfirstapp.StaticValues.LOGOUT_URL;
import static com.example.pawesajnog.myfirstapp.StaticValues.MY_APP;

/**
 * Created by Paweł Sajnóg on 2017-08-01.
 */

public class LogoutService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logout();
        return super.onStartCommand(intent, flags, startId);
    }

    private void logout() {
        //final String[] results = new String[1];

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGOUT_URL, new Response.Listener<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(String response) {
                stopSelf();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                stopSelf();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Cookie", readFile("cookie"));
                return params;
            }
        };

        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    public String readFile(String filename) {
        try {
            FileInputStream in = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            return sb.toString();
        } catch (IOException e) {
            Log.d(MY_APP, "No user data files or no access to them");
        }
        return null;
    }
}
