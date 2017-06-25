package com.example.michaelaki.safespot;

import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.content.Context;
import android.util.AttributeSet;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by jspall16 on 6/24/17.
 */

public class DistancePreference extends DialogPreference {

    private SeekBar seekBar;
    private TextView textView;

    //Distance Preference Methods

    public DistancePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            persistInt(mNewValue);
        }
    }

    //Seek Bar Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeVariables();

        // Initialize the textview with '0'.
        textView.setText("Covered: " + seekBar.getProgress() + "/" + seekBar.getMax());

        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText("Covered: " + progress + "/" + seekBar.getMax());
            }
        });
    }

    // A private method to help us initialize our variables.
    private void initializeVariables() {
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        textView = (TextView) findViewById(R.id.textView1);
    }

}
