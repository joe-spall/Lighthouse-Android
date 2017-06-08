package com.example.michaelaki.safespot;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity implements OnItemClickListener, OnClickListener {

    private static final String LOG_TAG = "Google Places";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_FIND = "/details";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyAtonyO8kbRMgzGLhGWR0O7Zb513qzBmGQ";
    private static final String CRIME_PULL_API_BASE = "http://www.app-lighthouse.com/app/crimepullcirc.php";
    private static final String SERVER_NAME = "localhost";
    private static final String USER_NAME = "applight_LHUser";
    private static final String PASSWORD = "mikelikesbirds1!";
    private static final String DATABASE_NAME = "applight_lighthouse";
    private static ArrayList<ArrayList<String>> resultList;
    private AutoCompleteTextView autoCompView;
    private String id;
    private static ArrayList<String> latlong = new ArrayList();
    private double latitude,longitude;
    private CheckBox check2014,check2015, check2016;
    private static int year;
    private boolean done = false;
    private double danger;
    private int [] crimes;
    private static double radius;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        Button start = (Button) findViewById(R.id.button);
        start.setOnClickListener(this);
        check2015 = (CheckBox) findViewById(R.id.checkBox);
        check2014 = (CheckBox) findViewById(R.id.checkBox2);
        check2016 = (CheckBox) findViewById(R.id.checkBox3);
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        this.id = resultList.get(1).get(position);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            if (check2014.isChecked()) {
                year = 2014;
            } else if (check2015.isChecked()) {
                year = 2015;
            } else {
                year = 2016;
            }
            EditText radiusFinder = (EditText) findViewById(R.id.editText);
            radius = Double.parseDouble(radiusFinder.getText().toString());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        search(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
            while (latlong.size() < 2) {

            }

            latitude = Double.parseDouble(latlong.get(0));
            longitude = Double.parseDouble(latlong.get(1));

            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        findCrime(latitude, longitude);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });

            thread1.start();

            while (!done) {

            }
            setContentView(R.layout.scores);
            TextView dangerScore = (TextView) findViewById(R.id.textView2);
            TextView crimeText = (TextView) findViewById(R.id.textView5);
            TextView details = (TextView) findViewById(R.id.textView6);
            DecimalFormat df = new DecimalFormat("####0.00");
            dangerScore.setText("Danger Score\n" + df.format(danger));
            dangerScore.setTextColor(Color.rgb((int) danger * 50, 0, 0));
            details.setText("Crimes for " + autoCompView.getText());
            crimeText.setText("Homicides: " + crimes[0] + "\nAggregated Assault: " + crimes[1]
                    + "\nRape: " + crimes[2] + "\nPedestrian Robbery: " + crimes[3]
                    + "\nResidential Robbery: " + crimes[5] + "\nNon Vehicular Larceny: "
                    + crimes[6] + "\nResidential Burglary: " + crimes[7] + "\nCommercial Robbery: "
                    + crimes[8] + "\nAuto Theft: " + crimes[9] + "\nNon-Residential Burglary: "
                    + crimes[10] + "\nVehicular Larceny: " + crimes[11]);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void findCrime(double latitude, double longitude) throws Exception {
        String url = CRIME_PULL_API_BASE;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        double latlongRadius = radius / 69.0;
        String urlParameters = "curlatitude=" + latitude
                + "&curlongitude=" + longitude + "&radius=" + latlongRadius + "&year=" + year;
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        int read;
        char[] buff = new char[1024];
        while ((read = in.read(buff)) != -1) {
            response.append(buff, 0, read);
        }
        in.close();
        if (con != null) {
            con.disconnect();
        }

        JSONObject dataPoints = new JSONObject(response.toString());
        crimes = new int[12];
        danger = 0.0;
        boolean finish = false;
        for (int k = 0; k < Integer.parseInt(dataPoints.getString("result_num")); k++) {
            if (finish) {
                String crimeType = dataPoints.getJSONObject(Integer.toString(k)).getString("typeCrime");
                if (crimeType.equals("HOMICIDE")) {
                    danger += 1.0;
                    crimes[0]++;
                } else if (crimeType.equals("AGG ASSAULT")) {
                    danger += 1.0;
                    crimes[1]++;
                } else if (crimeType.equals("RAPE")) {
                    danger += 1.0;
                    crimes[2]++;
                } else if (crimeType.equals("ROBBERY-PEDESTRIAN")) {
                    danger += 1.0;
                    crimes[3]++;
                } else if (crimeType.equals("ROBBERY-RESIDENTIAL")) {
                    danger += 1.0;
                    crimes[5]++;
                } else if (crimeType.equals("LARCENY-NON VEHICLE")) {
                    danger += .5;
                    crimes[6]++;
                } else if (crimeType.equals("BURGLARY-RESIDENCE")) {
                    danger += .5;
                    crimes[7]++;
                } else if (crimeType.equals("ROBBERY-COMMERCIAL")) {
                    danger += .25;
                    crimes[8]++;
                } else if (crimeType.equals("AUTO THEFT")) {
                    danger += .25;
                    crimes[9]++;
                } else if (crimeType.equals("BURGLARY-NONRES")) {
                    danger += .05;
                    crimes[10]++;
                } else if (crimeType.equals("LARCENY-FROM VEHICLE")) {
                    danger += .05;
                    crimes[11]++;
                }
            } else {
                String date = dataPoints.getJSONObject(Integer.toString(k)).getString("date");
                if (Integer.parseInt(date.substring(0, 4)) >= year) {
                    String crimeType = dataPoints.getJSONObject(Integer.toString(k)).getString("typeCrime");
                    if (crimeType.equals("HOMICIDE")) {
                        danger += 1.0;
                        crimes[0]++;
                    } else if (crimeType.equals("AGG ASSAULT")) {
                        danger += 1.0;
                        crimes[1]++;
                    } else if (crimeType.equals("RAPE")) {
                        danger += 1.0;
                        crimes[2]++;
                    } else if (crimeType.equals("ROBBERY-PEDESTRIAN")) {
                        danger += 1.0;
                        crimes[3]++;
                    } else if (crimeType.equals("ROBBERY-RESIDENTIAL")) {
                        danger += 1.0;
                        crimes[5]++;
                    } else if (crimeType.equals("LARCENY-NON VEHICLE")) {
                        danger += .5;
                        crimes[6]++;
                    } else if (crimeType.equals("BURGLARY-RESIDENCE")) {
                        danger += .5;
                        crimes[7]++;
                    } else if (crimeType.equals("ROBBERY-COMMERCIAL")) {
                        danger += .25;
                        crimes[8]++;
                    } else if (crimeType.equals("AUTO THEFT")) {
                        danger += .25;
                        crimes[9]++;
                    } else if (crimeType.equals("BURGLARY-NONRES")) {
                        danger += .05;
                        crimes[10]++;
                    } else if (crimeType.equals("LARCENY-FROM VEHICLE")) {
                        danger += .05;
                        crimes[11]++;
                    }
                    finish = true;
                }
            }
        }
        if (year == 2014) {
            danger /= 6;
        } else if (year == 2015) {
            danger /= 4;
        }
        danger /= (radius * 60);
        done = true;
    }

    public static void search(String input) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append(TYPE_FIND);
            sb.append(OUT_JSON);
            sb.append("?sensor=false");
            sb.append("&key=" + API_KEY);
            sb.append("&reference=" + URLEncoder.encode(input, "utf8"));
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        try {
            JSONObject jsonObj = new JSONObject(jsonResults.toString()).getJSONObject("result");
            JSONObject predsJsonArray = jsonObj.getJSONObject("geometry");
            JSONObject location = predsJsonArray.getJSONObject("location");
            latlong.add(location.getString("lat"));
            latlong.add(location.getString("lng"));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error processing JSON results", e);
        }
    }



    public static ArrayList<ArrayList<String>> autocomplete(String input) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:us");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            return resultList;
        } catch (IOException e) {
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        resultList = new ArrayList<ArrayList<String>>();
        resultList.add(new ArrayList<String>());
        resultList.add(new ArrayList<String>());
        resultList.add(new ArrayList<String>());
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.get(0).add(predsJsonArray.getJSONObject(i).getString("description"));
                resultList.get(1).add(predsJsonArray.getJSONObject(i).getString("reference"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException");
        }

        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList<ArrayList<String>> resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.get(0).size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(0).get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList.get(0);
                        filterResults.count = resultList.get(0).size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
}