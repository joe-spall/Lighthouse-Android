package com.example.michaelaki.lighthouseandroid.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.michaelaki.lighthouseandroid.model.CrimeWeightSettings;
import com.example.michaelaki.lighthouseandroid.R;
import com.example.michaelaki.lighthouseandroid.model.Settings;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;

/**
 * Created by michaelaki on 8/10/17.
 */

public class SettingsActivity extends Activity implements View.OnClickListener {
    private Settings settings;
    private TextView seekBarValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings();
        Intent intent = getIntent();
        if (intent.getSerializableExtra("Settings") != null) {
            settings = (Settings) intent.getSerializableExtra("Settings");
        }
        setContentView(R.layout.activity_settings);
        Button done = (Button) findViewById(R.id.doneButton);
        done.setOnClickListener(this);
        Button cancel = (Button) findViewById(R.id.cancelButton);
        cancel.setOnClickListener(this);
        Button crimeWeights = (Button) findViewById(R.id.crimeWeights);
        crimeWeights.setOnClickListener(this);
        cancel.setOnClickListener(this);
        SeekBar seekBar = (SeekBar)findViewById(R.id.radiusBar);
        seekBar.setProgress(settings.getRadius()/50 - 1);

        seekBar.setMax(4);
        seekBarValue = (TextView)findViewById(R.id.radiusText);
        seekBarValue.setText(Integer.toString(settings.getRadius()));


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = progress * 50 + 50;
                settings.setRadius(value);
                seekBarValue.setText(String.valueOf(value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Spinner spinner = (Spinner) findViewById(R.id.yearSpinner);
        ArrayList<Integer> years = new ArrayList<>();
        for (int k = 2008; k <= 2017; k++) {
            years.add(k);
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        spinner.setAdapter(adapter);
        int spinnerPosition = adapter.getPosition(settings.getYear());
        spinner.setSelection(spinnerPosition);
        if (settings.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            RadioButton street = (RadioButton) findViewById(R.id.Street);
            street.setChecked(true);
        } else if (settings.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            RadioButton satellite = (RadioButton) findViewById(R.id.Satellite);
            satellite.setChecked(true);
        } else {
            RadioButton hybrid = (RadioButton) findViewById(R.id.Hybrid);
            hybrid.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.doneButton) {

            Spinner spinner = (Spinner) findViewById(R.id.yearSpinner);
            int year = Integer.parseInt(spinner.getSelectedItem().toString());

            settings.setYear(year);
            RadioButton street = (RadioButton) findViewById(R.id.Street);
            RadioButton satellite = (RadioButton) findViewById(R.id.Satellite);
            RadioButton hybrid = (RadioButton) findViewById(R.id.Hybrid);
            int mapType = 0;
            if (street.isChecked()) {
                mapType = GoogleMap.MAP_TYPE_NORMAL;
            } else if (satellite.isChecked()) {
                mapType = GoogleMap.MAP_TYPE_SATELLITE;
            } else if (hybrid.isChecked()) {
                mapType = GoogleMap.MAP_TYPE_HYBRID;
            }
            settings.setMapType(mapType);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("Settings", settings);
            setResult(1, intent);
            finish();
        } else if (v.getId() == R.id.crimeWeights) {
            Intent intent = new Intent(this, CrimeWeightSettingsActivity.class);
            intent.putExtra("Weights", settings.getCrimeWeights());
            startActivityForResult(intent, 1);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK || resultCode == RESULT_FIRST_USER) {
                if (intent.getSerializableExtra("Weights") != null) {
//                    settings.setYear(intent.getIntExtra("Year", 2014));
//                    settings.setMapType(intent.getStringExtra("MapType"));
//                    settings.setRadius(intent.getDoubleExtra("Radius", 0.5));
                    settings.setCrimeWeights((CrimeWeightSettings) intent.getSerializableExtra("Weights"));
                }

            }
        }
    }
}
