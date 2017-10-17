package com.example.michaelaki.lighthouseandroid.controller;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.michaelaki.lighthouseandroid.model.Crime;
import com.example.michaelaki.lighthouseandroid.model.CustomRequest;
import com.example.michaelaki.lighthouseandroid.R;
import com.example.michaelaki.lighthouseandroid.model.Settings;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by michaelaki on 10/7/17.
 */

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, AdapterView.OnItemClickListener, GoogleMap.OnMyLocationButtonClickListener, ClusterManager.OnClusterItemClickListener{

    private static final String LOG_TAG = "Google Places";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_FIND = "/details";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyAtonyO8kbRMgzGLhGWR0O7Zb513qzBmGQ";
    private static final String CRIME_PULL_API_BASE = "https://www.app-lighthouse.com/app/crimepullcirc.php";
    private static final String SERVER_NAME = "localhost";
    private static final String USER_NAME = "applight_LHUser";
    private static final String PASSWORD = "mikelikesbirds1!";
    private static final String DATABASE_NAME = "applight_lighthouse";
    private static ArrayList<ArrayList<String>> resultList;
    private double latitude, longitude;
    private AutoCompleteTextView autoCompView;
    private String id;
    private GoogleMap mMap;
    private Settings settings;
    private FusedLocationProviderClient mFusedLocationClient;
    private ClusterManager<Crime> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        settings = new Settings();



        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setUpInteractivity();

    }

    public void setUpInteractivity() {
        ImageButton settings = (ImageButton) findViewById(R.id.settingsButton);
        autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToSettingsMenu();
            }
        });
    }

    public void switchToSettingsMenu() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("Settings", settings);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK || resultCode == RESULT_FIRST_USER) {
                if (intent.getSerializableExtra("Settings") != null) {
                    settings = (Settings) intent.getSerializableExtra("Settings");
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mClusterManager = new ClusterManager<Crime>(this, mMap);
        mClusterManager.setOnClusterItemClickListener(this);

        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);


        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    2);
        } else {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                setCameraLocation();
                                // Logic to handle location object
                            }
                        }
                    });


        }

    }

    public void setCameraLocation() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
        mMap.clear();


        // Point the map's listeners at the listeners implemented by the cluster
        // manager.

        drawCircle();
        addMarkers();
    }

    public void drawCircle() {
        mMap.addCircle(new CircleOptions()
            .center(new LatLng(latitude, longitude))
            .radius(settings.getRadius() * 1000)
            .fillColor(Color.parseColor("#4D00008B"))
            .strokeWidth(1));
    }

    public void addMarkers() {
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    addMarkersCall();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
        addMarkersCall();
    }

    public void addMarkersCall() {
        String url = CRIME_PULL_API_BASE;
        double latlongRadius = settings.getRadius() / 110.9472;
        Map<String, String> params = new HashMap<>();
        params.put("curlatitude", Double.toString(latitude));
        params.put("curlongitude", Double.toString(longitude));
        params.put("radius", Double.toString(latlongRadius));
        params.put("year", Integer.toString(settings.getYear()));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, url, params, this.addMarkerSuccessResponse(),
                this.addMarkerErrorResponse());
        requestQueue.add(jsObjRequest);
    }

    public Response.ErrorListener addMarkerErrorResponse() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Marker Error", "Error adding markers");
            }
        };
    }

    public Response.Listener<JSONObject> addMarkerSuccessResponse() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject response) {

                        try {
                            JSONObject dataPoints = new JSONObject(response.toString());
                            for (int k = 0; k < Integer.parseInt(dataPoints.getString("result_num")); k++) {
                                JSONObject crime = dataPoints.getJSONArray("results").getJSONObject(k);
                                String crimeType = crime.getString("typeCrime");
                                Crime crimeToAdd = new Crime(crimeType, crime.getString("id"), crime.getString("date"), new LatLng(Double.parseDouble(crime.getString("lat")), Double.parseDouble(crime.getString("long"))));
                                mClusterManager.addItem(crimeToAdd);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
            }
        };
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                setCameraLocation();
                                // Logic to handle location object
                            }
                        }
                    });
        }
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }



    public void onItemClick(AdapterView adapterView, View view, int position, long id) {

        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        this.id = resultList.get(1).get(position);
        View view2 = this.getCurrentFocus();
        if (view2 != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        search(this.id);
    }

    public void search(String input) {
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append(TYPE_FIND);
            sb.append(OUT_JSON);
            sb.append("?sensor=false");
            sb.append("&key=" + API_KEY);
            sb.append("&reference=" + URLEncoder.encode(input, "utf8"));
            String url = sb.toString();
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            Map<String, String> params = new HashMap<>();
            params.put("sensor", "false");
            params.put("key", API_KEY);
            params.put("reference", URLEncoder.encode(input, "utf8"));
            CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, url, params, this.searchSuccessResponse(), this.searchErrorResponse());
            requestQueue.add(jsObjRequest);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return;
        }

    }

    public Response.Listener<JSONObject> searchSuccessResponse() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONObject jsonObj = response;
                    JSONObject predsJsonArray = jsonObj.getJSONObject("result").getJSONObject("geometry");
                    JSONObject location = predsJsonArray.getJSONObject("location");
                    latitude = Double.parseDouble(location.getString("lat"));
                    longitude = Double.parseDouble(location.getString("lng"));
                    setCameraLocation();

                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error processing JSON results", e);
                }
            }
        };
    }


    public boolean onClusterItemClick(ClusterItem item) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(item.getPosition()));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        return true;
    }

    public Response.ErrorListener searchErrorResponse() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", "Error connecting to Places API");
            }
        };

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
//
//                        // Assign the data to the FilterResults
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
