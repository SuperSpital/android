package com.example.lab7;

import androidx.fragment.app.FragmentActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapLongClickListener,
        SensorEventListener {

    private GoogleMap mMap;
    List<Marker> markerList;
    boolean showingAccelerometer = false;
    private SensorManager sensorManager;
    Sensor accelerometer;
    TextView textView;
    JSONObject jsonMarkers;
    JSONArray jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        markerList = new ArrayList<>();

        final FloatingActionButton button = findViewById(R.id.hide_accelerometer_button);
        final FloatingActionButton button2 = findViewById(R.id.show_accelerometer_button);
        textView = findViewById(R.id.accelerometer);
        button.setVisibility(View.INVISIBLE);
        button2.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MapsActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        jsonMarkers = new JSONObject();
        jsonArray = new JSONArray();
        StringBuilder stringBuilder;
        String response = "";
        File file = new File(getFilesDir(), "jsonArray");
        if(file.exists())
        {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                stringBuilder = new StringBuilder();
                String line = bufferedReader.readLine();

                while(line != null)
                {
                    stringBuilder.append(line).append("\n");
                    line = bufferedReader.readLine();
                }

                bufferedReader.close();
                response = stringBuilder.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                jsonMarkers = new JSONObject(response);
                jsonArray = jsonMarkers.getJSONArray("jsonArray");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);

        for(int i = 0; i<jsonArray.length(); i=i+2) {
            LatLng latLng = null;
            try {
                latLng = new LatLng(jsonArray.getDouble(i), jsonArray.getDouble(i + 1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(String.format("Position: (%.2f; %.2f)", latLng.latitude, latLng.longitude)));
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng)
    {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latLng.latitude,latLng.longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .alpha(0.8f)
                .title(String.format("Position: (%.2f; %.2f)", latLng.latitude, latLng.longitude)));

        markerList.add(marker);

        try {
            jsonArray.put(latLng.latitude);
            jsonArray.put(latLng.longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        CameraPosition cameraPos = mMap.getCameraPosition();
        showButtons(findViewById(R.id.mainView));
        mMap.getUiSettings().setMapToolbarEnabled(false);

        return false;
    }

    public void zoomInClick(View w)
    {
        mMap.moveCamera(CameraUpdateFactory.zoomIn());
    }

    public void zoomOutClick(View w)
    {
        mMap.moveCamera(CameraUpdateFactory.zoomOut());
    }

    public void clearMemory(View w)
    {
        hideButtons(w);
        markerList.clear();
        mMap.clear();
        jsonArray = new JSONArray();
        try {
            save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void hideButtons(View w)
    {
        textView = findViewById(R.id.accelerometer);
        textView.setVisibility(View.INVISIBLE);
        showingAccelerometer = false;

        final FloatingActionButton button = findViewById(R.id.hide_accelerometer_button);
        final FloatingActionButton button2 = findViewById(R.id.show_accelerometer_button);

        button.animate()
                .translationY(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        button.setVisibility(View.INVISIBLE);
                    }
                });


        button2.animate()
                .translationY(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        button2.setVisibility(View.INVISIBLE);
                    }
                });
    }

    public void showButtons(View w)
    {
        final FloatingActionButton button = findViewById(R.id.hide_accelerometer_button);
        final FloatingActionButton button2 = findViewById(R.id.show_accelerometer_button);

        button.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);

        button.animate()
                .translationY(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });

        button2.animate()
                .translationY(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
    }

    public void showAccelerometer(View w)
    {
        textView = findViewById(R.id.accelerometer);

        if(showingAccelerometer)
        {
            textView.setVisibility(View.INVISIBLE);
            showingAccelerometer = false;
        }
        else
        {
            textView.setVisibility(View.VISIBLE);
            showingAccelerometer = true;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        textView.setText(String.format("Acceleration: \n x: %.4f   y: %.4f", event.values[0], event.values[1]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void save() throws JSONException
    {
        jsonMarkers.put("jsonArray", jsonArray);
        File file = new File(getFilesDir(),"jsonArray");
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(jsonMarkers.toString());
            bufferedWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
