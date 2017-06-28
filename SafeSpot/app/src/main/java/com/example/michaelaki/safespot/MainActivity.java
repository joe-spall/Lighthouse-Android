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
import android.support.v7.app.AppCompatActivity;
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

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity implements OnItemClickListener, OnClickListener {

    private static final String LOG_TAG = "Google Places";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_FIND = "/details";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyAtonyO8kbRMgzGLhGWR0O7Zb513qzBmGQ";
    private static final String CRIME_PULL_API_BASE = "http://www.app-lighthouse.com/app/crimepullcirc.php";
    private static ArrayList<ArrayList<String>> resultList;
    private AutoCompleteTextView autoCompView;
    private String id;
    private static ArrayList<String> latlong = new ArrayList();
    private double latitude,longitude;
    //TODO make a dropdown instead of checkbox
    private static int year;
    private boolean done = false;
    private double danger;
    private ArrayList<Crime> crimeCollection = new ArrayList();
    private static double radius;
    private MapView mapView;

    //TODO make settings store years and radius

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        Button settings = (Button) findViewById(R.id.settings);
        settings.setOnClickListener(this);

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



        if (v.getId() == R.id.settings) {
            setContentView(R.layout.preferences);

         /*
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

            //TODO convert to an on completion
            while (!done) {

            }
            setContentView(R.layout.scores);
            DecimalFormat df = new DecimalFormat("####0.00");
            */
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
        //TODO add error handling for incorrect response
        //TODO making single response buffer
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
        for (int k = 0; k < Integer.parseInt(dataPoints.getString("result_num")); k++) {
            JSONObject currentCrime = dataPoints.getJSONObject(Integer.toString(k));
            String id = currentCrime.getString("id");

            if (!containsId(crimeCollection, id)) {
                String date = currentCrime.getString("date");
                String typeCrime = currentCrime.getString("typeCrime");
                double lat = currentCrime.getDouble("lat");
                double lon = currentCrime.getDouble("long");
                Crime crimeObject = new Crime(id, date, typeCrime, lat, lon);
                crimeCollection.add(crimeObject);
            }
        }
        danger = calculateDanger(crimeCollection);
        System.out.println(danger);
    }

    public double calculateDanger(ArrayList<Crime> crimes) {
        double dangerLevel = 0;
        for(Crime exampleCrime:crimes) {
            String currentTypeCrime = exampleCrime.getTypeCrime();
            String currentDate = exampleCrime.getDate();
            double currentDangerLevel = 0;
            if (currentTypeCrime.equals("HOMICIDE")) {
                currentDangerLevel = 1.0;
            } else if (currentTypeCrime.equals("AGG ASSAULT")) {
                currentDangerLevel = 1.0;
            } else if (currentTypeCrime.equals("RAPE")) {
                currentDangerLevel = 1.0;
            } else if (currentTypeCrime.equals("ROBBERY-PEDESTRIAN")) {
                currentDangerLevel = 1.0;
            } else if (currentTypeCrime.equals("ROBBERY-RESIDENCE")) {
                currentDangerLevel = 1.0;
            } else if (currentTypeCrime.equals("BURGLARY-RESIDENCE")) {
                currentDangerLevel = 0.5;
            } else if (currentTypeCrime.equals("LARCENY-NON VEHICLE")) {
                currentDangerLevel = 0.5;
            } else if (currentTypeCrime.equals("AUTO THEFT")) {
                currentDangerLevel = 0.25;
            } else if (currentTypeCrime.equals("ROBBERY-COMMERICAL")) {
                currentDangerLevel = 0.25;
            } else if (currentTypeCrime.equals("BURGLARY-NONRES")) {
                currentDangerLevel = 0.25;
            } else if (currentTypeCrime.equals("LARCENY-FROM VEHICLE")) {
                currentDangerLevel = 0.25;
            }

            String currentYear = currentDate.substring(0,3);
            if (currentYear.equals("2014")) {
                currentDangerLevel /= 4;
            } else if (currentYear.equals("2015")) {
                currentDangerLevel /= 3;
            } else if (currentYear.equals("2016")) {
                currentDangerLevel /= 2;
            }

            dangerLevel += currentDangerLevel;
        }
        return dangerLevel;

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

    public static boolean containsId(ArrayList<Crime> list, String id) {
        for (Crime object : list) {
            if (object.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }
}