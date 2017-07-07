package com.example.pawesajnog.myfirstapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {


    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private final int readSensorDataDelay = 500; // in milliseconds
    private final int maxHistorySizeOfSensorData = 40; //number of measurement
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mMagnetometer;

    private TextView textViewAccelerometer;
    private TextView textViewGyroscope;
    private TextView textViewMagnetometer;

    private GraphView graph1;
    private GraphView graph2;
    private GraphView graph3;
    private List<LineGraphSeries> sensorsData;

    private float graphLastXAccValue = 0.0f;
    private float graphLastXGyroValue = 0.0f;
    private float graphLastXMagValue = 0.0f;

    private long dateAcc = new Date().getTime();
    private long dateGyro = new Date().getTime();
    private long dateMag = new Date().getTime();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        textViewAccelerometer = (TextView) findViewById(R.id.textView3);
        textViewGyroscope = (TextView) findViewById(R.id.textView4);
        textViewMagnetometer = (TextView) findViewById(R.id.textView5);

        graph1 = (GraphView) findViewById(R.id.graph1);
        graph2 = (GraphView) findViewById(R.id.graph2);
        graph3 = (GraphView) findViewById(R.id.graph3);
        sensorsData = new LinkedList<LineGraphSeries>();

        setSeriesOptions();

        setGraphOptions();

    }

    private void setSeriesOptions() {
        for (int i = 0; i < 9; i++) {
            sensorsData.add(new LineGraphSeries<DataPoint>());
            if (i % 3 == 0) {
                sensorsData.get(i).setColor(Color.GREEN);
                sensorsData.get(i).setTitle("x");
            } else if (i % 3 == 1) {
                sensorsData.get(i).setColor(Color.BLUE);
                sensorsData.get(i).setTitle("y");
            } else {
                sensorsData.get(i).setColor(Color.RED);
                sensorsData.get(i).setTitle("z");
            }
        }
    }

    private void setGraphOptions() {
        graph1.getViewport().setScalable(true);
        graph1.getLegendRenderer().setVisible(true);

        graph2.getViewport().setScalable(true);
        graph2.getLegendRenderer().setVisible(true);

        graph3.getViewport().setScalable(true);
        graph3.getLegendRenderer().setVisible(true);

        for (int i = 0; i < 9; i++) {
            if (i < 3) {
                graph1.addSeries(sensorsData.get(i));
            } else if (i < 6) {
                graph2.addSeries(sensorsData.get(i));
            } else {
                graph3.addSeries(sensorsData.get(i));
            }
        }
    }

    //Called when the user taps Send button
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        Intent mServiceIntent = new Intent(this, BackgroundService.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        mServiceIntent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        startService(mServiceIntent);
    }

    @Override
    public synchronized void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        long tempDate = new Date().getTime();
        long sensorDelay;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorDelay = tempDate - dateAcc;
            if (sensorDelay >= readSensorDataDelay) {
                textViewAccelerometer.setText("Acc: " + String.format(java.util.Locale.US, "%.2f", event.values[0]) + " "
                        + String.format(java.util.Locale.US, "%.2f", event.values[1]) + " "
                        + String.format(java.util.Locale.US, "%.2f", event.values[2]));
                sensorsData.get(0).appendData(new DataPoint(graphLastXAccValue, event.values[0]), true, maxHistorySizeOfSensorData);
                sensorsData.get(1).appendData(new DataPoint(graphLastXAccValue, event.values[1]), true, maxHistorySizeOfSensorData);
                sensorsData.get(2).appendData(new DataPoint(graphLastXAccValue, event.values[2]), true, maxHistorySizeOfSensorData);
                graphLastXAccValue += ((float) sensorDelay / (float) 1000);
                dateAcc = tempDate;
            }

        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            sensorDelay = tempDate - dateGyro;
            if (sensorDelay >= readSensorDataDelay) {
                textViewGyroscope.setText("Gyro: " + String.format(java.util.Locale.US, "%.2f", event.values[0]) + " "
                        + String.format(java.util.Locale.US, "%.2f", event.values[1]) + " "
                        + String.format(java.util.Locale.US, "%.2f", event.values[2]));
                sensorsData.get(3).appendData(new DataPoint(graphLastXGyroValue, event.values[0]), true, maxHistorySizeOfSensorData);
                sensorsData.get(4).appendData(new DataPoint(graphLastXGyroValue, event.values[1]), true, maxHistorySizeOfSensorData);
                sensorsData.get(5).appendData(new DataPoint(graphLastXGyroValue, event.values[2]), true, maxHistorySizeOfSensorData);
                graphLastXGyroValue += ((float) sensorDelay / (float) 1000);
                dateGyro = tempDate;
            }
        } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            sensorDelay = tempDate - dateMag;
            if (sensorDelay >= readSensorDataDelay) {
                textViewMagnetometer.setText("Mag: " + String.format(java.util.Locale.US, "%.2f", event.values[0]) + " "
                        + String.format(java.util.Locale.US, "%.2f", event.values[1]) + " "
                        + String.format(java.util.Locale.US, "%.2f", event.values[2]));
                sensorsData.get(6).appendData(new DataPoint(graphLastXMagValue, event.values[0]), true, maxHistorySizeOfSensorData);
                sensorsData.get(7).appendData(new DataPoint(graphLastXMagValue, event.values[1]), true, maxHistorySizeOfSensorData);
                sensorsData.get(8).appendData(new DataPoint(graphLastXMagValue, event.values[2]), true, maxHistorySizeOfSensorData);
                graphLastXMagValue += ((float) sensorDelay / (float) 1000);
                dateMag = tempDate;
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
