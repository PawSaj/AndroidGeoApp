package com.example.pawesajnog.myfirstapp.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pawesajnog.myfirstapp.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class SensorsFragment extends Fragment implements SensorEventListener {

    private Activity mActivity;
    private View view;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mMagnetometer;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater
                .inflate(R.layout.fragment_graphs, container, false);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        graph1 = (GraphView) view.findViewById(R.id.graph1);
        graph2 = (GraphView) view.findViewById(R.id.graph2);
        graph3 = (GraphView) view.findViewById(R.id.graph3);
        sensorsData = new LinkedList<>();

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
        graph1.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph1.setTitle("Accelerometer");
        graph1.setPadding(5, 5, 5, 5);
        // set manual X bounds
        graph1.getViewport().setXAxisBoundsManual(true);
        graph1.getViewport().setMinX(0.0);
        graph1.getViewport().setMaxX(60.0);

        graph2.getViewport().setScalable(true);
        graph2.getLegendRenderer().setVisible(true);
        graph2.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph2.setTitle("Gyroscope");
        graph2.setPadding(5, 5, 5, 5);
        // set manual X bounds
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0.0);
        graph2.getViewport().setMaxX(60.0);

        graph3.getViewport().setScalable(true);
        graph3.getLegendRenderer().setVisible(true);
        graph3.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph3.setTitle("Magnetometer");
        graph3.setPadding(5, 5, 5, 5);
        // set manual X bounds
        graph3.getViewport().setXAxisBoundsManual(true);
        graph3.getViewport().setMinX(0.0);
        graph3.getViewport().setMaxX(60.0);

        //add series to graphs
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

    @Override
    @SuppressWarnings("unchecked")
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        long tempDate = new Date().getTime();
        long sensorDelay;
        int readSensorDataDelay = 500;
        int maxHistorySizeOfSensorData = 720;
        boolean autoScrollingFlag;

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorDelay = tempDate - dateAcc;
            if (sensorDelay >= readSensorDataDelay) {
                autoScrollingFlag = setAutoScrollingFlag(graphLastXAccValue);
                sensorsData.get(0).appendData(new DataPoint(graphLastXAccValue, event.values[0]), autoScrollingFlag, maxHistorySizeOfSensorData);
                sensorsData.get(1).appendData(new DataPoint(graphLastXAccValue, event.values[1]), autoScrollingFlag, maxHistorySizeOfSensorData);
                sensorsData.get(2).appendData(new DataPoint(graphLastXAccValue, event.values[2]), autoScrollingFlag, maxHistorySizeOfSensorData);
                graphLastXAccValue += ((float) sensorDelay / (float) 1000);
                dateAcc = tempDate;
            }

        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            sensorDelay = tempDate - dateGyro;
            if (sensorDelay >= readSensorDataDelay) {
                autoScrollingFlag = setAutoScrollingFlag(graphLastXAccValue);
                sensorsData.get(3).appendData(new DataPoint(graphLastXGyroValue, event.values[0]), autoScrollingFlag, maxHistorySizeOfSensorData);
                sensorsData.get(4).appendData(new DataPoint(graphLastXGyroValue, event.values[1]), autoScrollingFlag, maxHistorySizeOfSensorData);
                sensorsData.get(5).appendData(new DataPoint(graphLastXGyroValue, event.values[2]), autoScrollingFlag, maxHistorySizeOfSensorData);
                graphLastXGyroValue += ((float) sensorDelay / (float) 1000);
                dateGyro = tempDate;
            }
        } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            sensorDelay = tempDate - dateMag;
            if (sensorDelay >= readSensorDataDelay) {
                autoScrollingFlag = setAutoScrollingFlag(graphLastXAccValue);
                sensorsData.get(6).appendData(new DataPoint(graphLastXMagValue, event.values[0]), autoScrollingFlag, maxHistorySizeOfSensorData);
                sensorsData.get(7).appendData(new DataPoint(graphLastXMagValue, event.values[1]), autoScrollingFlag, maxHistorySizeOfSensorData);
                sensorsData.get(8).appendData(new DataPoint(graphLastXMagValue, event.values[2]), autoScrollingFlag, maxHistorySizeOfSensorData);
                graphLastXMagValue += ((float) sensorDelay / (float) 1000);
                dateMag = tempDate;
            }

        }
    }

    private boolean setAutoScrollingFlag(float graphLastXValue) {
        return graphLastXValue >= 60;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
