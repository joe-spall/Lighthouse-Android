package com.example.michaelaki.safespot;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * Created by michaelaki on 8/14/17.
 */

public class DetailActivity extends Activity implements View.OnClickListener{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scores);

        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        Intent intent = getIntent();
        Double danger = intent.getDoubleExtra("Danger", 0.0);
        String title = intent.getStringExtra("Address");
        int[] crimes = intent.getIntArrayExtra("CrimeArray");

        TextView dangerScore = (TextView) findViewById(R.id.textView2);
        TextView crimeText = (TextView) findViewById(R.id.textView5);
        TextView details = (TextView) findViewById(R.id.textView6);

        DecimalFormat df = new DecimalFormat("####0.00");

        dangerScore.setText("Danger Score\n" + df.format(danger));
        dangerScore.setTextColor(Color.rgb((int) (danger * 50), 0, 0));
        details.setText("Crimes for " + title);
        crimeText.setText("Homicides: " + crimes[0] + "\nAggregated Assault: " + crimes[1]
                + "\nRape: " + crimes[2] + "\nRobbery: " + crimes[3]
                + "\nNon Vehicular Larceny: "
                + crimes[4] + "\nVehicle Burglary: " + crimes[5]
                + "\nAuto Theft: " + crimes[6] + "\nPedestrian Burglary: "
                + crimes[7] + "\nVehicular Larceny: " + crimes[8]);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backButton) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }
}
