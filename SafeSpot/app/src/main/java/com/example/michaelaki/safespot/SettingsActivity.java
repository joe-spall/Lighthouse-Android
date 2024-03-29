package com.example.michaelaki.safespot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.michaelaki.safespot.CrimeWeightSettings;
import com.mapbox.mapboxsdk.constants.Style;

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
        seekBar.setProgress((int) (settings.getRadius() * 4) - 2);

        seekBar.setMax(6);
        seekBarValue = (TextView)findViewById(R.id.radiusText);
        seekBarValue.setText(Double.toString(settings.getRadius()));


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Double value = progress / 4.0 + 0.5;
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

        if (settings.getYear() == 2014) {
            RadioButton button14 = (RadioButton) findViewById(R.id.checkBox2014);
            button14.setChecked(true);
        } else if (settings.getYear() == 2015) {
            RadioButton button15 = (RadioButton) findViewById(R.id.checkBox2015);
            button15.setChecked(true);
        } else {
            RadioButton button16 = (RadioButton) findViewById(R.id.checkBox2016);
            button16.setChecked(true);
        }
        if (settings.getMapType().equals(Style.MAPBOX_STREETS)) {
            RadioButton street = (RadioButton) findViewById(R.id.Street);
            street.setChecked(true);
        } else if (settings.getMapType().equals(Style.SATELLITE)) {
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
            RadioButton button14 = (RadioButton) findViewById(R.id.checkBox2014);
            RadioButton button15 = (RadioButton) findViewById(R.id.checkBox2015);
            RadioButton button16 = (RadioButton) findViewById(R.id.checkBox2016);
            int year = 2016;
            if (button14.isChecked()) {
                year = 2014;
            } else if (button15.isChecked()) {
                year = 2015;
            }
            settings.setYear(year);
            RadioButton street = (RadioButton) findViewById(R.id.Street);
            RadioButton satellite = (RadioButton) findViewById(R.id.Satellite);
            RadioButton hybrid = (RadioButton) findViewById(R.id.Hybrid);
            String mapType = "";
            if (street.isChecked()) {
                mapType = Style.MAPBOX_STREETS;
            } else if (satellite.isChecked()) {
                mapType = Style.SATELLITE;
            } else if (hybrid.isChecked()) {
                mapType = Style.SATELLITE_STREETS;
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
