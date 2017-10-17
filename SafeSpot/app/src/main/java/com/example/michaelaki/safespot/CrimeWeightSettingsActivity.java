package com.example.michaelaki.safespot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by michaelaki on 9/11/17.
 */

public class CrimeWeightSettingsActivity extends Activity implements View.OnClickListener {
    private CrimeWeightSettings crimeWeights;
    private double homicide, rape, assault, pedestrianTheft, vehicularTheft;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_weight_settings);


        Intent intent = getIntent();
        crimeWeights = (CrimeWeightSettings) intent.getSerializableExtra("Weights");

        homicide = crimeWeights.getHomicide();
        rape = crimeWeights.getRape();
        assault = crimeWeights.getAssault();
        pedestrianTheft = crimeWeights.getPedestrianTheft();
        vehicularTheft = crimeWeights.getVehicularTheft();

        Button done = (Button) findViewById(R.id.doneButton);
        done.setOnClickListener(this);
        Button cancel = (Button) findViewById(R.id.cancelButton);
        cancel.setOnClickListener(this);

        TextView homicideWeight = (TextView) findViewById(R.id.homicideWeight);
        homicideWeight.setText("" + crimeWeights.getHomicide());
        TextView assaultWeight = (TextView) findViewById(R.id.assaultWeight);
        assaultWeight.setText("" + crimeWeights.getAssault());
        TextView rapeWeight = (TextView) findViewById(R.id.rapeWeight);
        rapeWeight.setText("" + crimeWeights.getRape());
        TextView pedestrianTheftWeight = (TextView) findViewById(R.id.pedestrianTheftWeight);
        pedestrianTheftWeight.setText("" + crimeWeights.getPedestrianTheft());
        TextView vehicularTheftWeight = (TextView) findViewById(R.id.vehicularTheftWeight);
        vehicularTheftWeight.setText("" + crimeWeights.getVehicularTheft());

        SeekBar homicideSlider = (SeekBar)findViewById(R.id.homicideSlider);
        homicideSlider.setProgress((int) (crimeWeights.getHomicide() * 100));
        homicideSlider.setMax(100);
        homicideSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                homicide = progress / 100.0;
                ((TextView) findViewById(R.id.homicideWeight)).setText("" + homicide);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar assaultSlider = (SeekBar)findViewById(R.id.assaultSlider);
        assaultSlider.setProgress((int) (crimeWeights.getAssault() * 100));
        assaultSlider.setMax(100);
        assaultSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                assault = progress / 100.0;
                ((TextView) findViewById(R.id.assaultWeight)).setText("" + assault);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar rapeSlider = (SeekBar)findViewById(R.id.rapeSlider);
        rapeSlider.setProgress((int) (crimeWeights.getRape() * 100));
        rapeSlider.setMax(100);
        rapeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rape = progress / 100.0;
                ((TextView) findViewById(R.id.rapeWeight)).setText("" + rape);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar pedestrianTheftSlider = (SeekBar)findViewById(R.id.pedestrianTheftSlider);
        pedestrianTheftSlider.setProgress((int) (crimeWeights.getPedestrianTheft() * 100));
        pedestrianTheftSlider.setMax(100);
        pedestrianTheftSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pedestrianTheft = progress / 100.0;
                ((TextView) findViewById(R.id.pedestrianTheftWeight)).setText("" + pedestrianTheft);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar vehicularTheftSlider = (SeekBar)findViewById(R.id.vehicularTheftSlider);
        vehicularTheftSlider.setProgress((int) (crimeWeights.getVehicularTheft() * 100));
        vehicularTheftSlider.setMax(100);
        vehicularTheftSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                vehicularTheft = progress / 100.0;
                ((TextView) findViewById(R.id.vehicularTheftWeight)).setText("" + vehicularTheft);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.doneButton) {
            crimeWeights.setCrimeWeights(homicide, assault, rape, pedestrianTheft, vehicularTheft);
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("Weights", crimeWeights);
            setResult(1, intent);
            finish();
        } else {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }
}
