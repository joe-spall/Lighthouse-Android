package com.example.michaelaki.safespot;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.*;
import com.android.volley.Response;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.view.View.OnClickListener;

import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class MainActivity extends Activity implements OnItemClickListener, OnClickListener {

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
    private AutoCompleteTextView autoCompView;
    private String id;
    private static ArrayList<String> latlong = new ArrayList();
    private double latitude,longitude;
    private CheckBox check2014,check2015, check2016;
    private static int year;
    private boolean done = false;
    private double danger;
    private int [] crimes;
    private static double radius = 0.5;
    private MapView mapView;
    private Settings settings;
    private int zoom;
    private RelativeLayout loadingIcon;
    private boolean currentLocation = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            zoom = 14;
//            final LocationSource locationSource = new LocationSource(this);
//            Location location;
//            locationSource.activate();
//        locationSource.requestLocationUpdates();


        Mapbox.getInstance(this, getString(R.string.access_token));
        //creates instance of mapbox

        setContentView(R.layout.activity_main);

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);
        ImageButton currentLocation = (ImageButton) findViewById(R.id.currentLocation);
        currentLocation.setOnClickListener(this);
        ImageButton settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(this);
        ImageButton search = (ImageButton) findViewById(R.id.search);
        search.setOnClickListener(this);

        mapView = (MapView) findViewById(R.id.mapview);
        //gets elements on screen and sets up click listeners

        settings = new Settings();


        mapView.onCreate(savedInstanceState);
        mapView.setStyleUrl(settings.getMapType());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    2);
        } else {
//            try {
//
//                location = locationSource.getLastLocation();
//
//
//            } catch (SecurityException e) {
//                Log.e("Not given permission", "Request Permission from the user"); // lets the user know there is a problem with the gps
//            }
            // If permissions have already been given
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    mapboxMap.setMyLocationEnabled(true);

                    Location location = mapboxMap.getMyLocation();

                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                    final CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(latitude, longitude)) // Sets the center of the map to location
                            .zoom(zoom)                            // Sets the zoom
                            .build();
                    mapboxMap.setCameraPosition(cameraPosition);
                    drawCircle();
                    addMarkers();
                }
            });

        }



    }

    public void updateLocation() {
        mapView.setStyleUrl(settings.getMapType());
        // We can now safely use the API we requested access to
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                final CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(latitude, longitude))
                        .zoom(zoom)                            // Sets the zoom
                        .build();
                mapboxMap.setCameraPosition(cameraPosition);
                // Interact with the map using mapboxMap here

            }
        });
    }


    /*
        Runs if the Location Permissions have been granted upon request
    */
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 2) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                final LocationSource locationSource = new LocationSource(this);
                // We can now safely use the API we requested access to
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        mapboxMap.setMyLocationEnabled(true);
                        Location location = null;
                        try {
                            location = locationSource.getLastLocation();
                        } catch (SecurityException e) {
                            Log.e("Not given permission", "Request Permission from the user"); // lets the user know there is a problem with the gps
                        }
                        final CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(location.getLatitude(), location.getLongitude())) // Sets the center of the map to current location
                                .zoom(zoom)                            // Sets the zoom
                                .build();
                        mapboxMap.setCameraPosition(cameraPosition);
                        // Interact with the map using mapboxMap here

                    }
                });

            } else {
                // Permission was denied or request was cancelled
            }
        }
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

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        addMarkers();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.button) {
//            if (check2014.isChecked()) {
//                year = 2014;
//            } else if (check2015.isChecked()) {
//                year = 2015;
//            } else {
//                year = 2016;
//            }
//            EditText radiusFinder = (EditText) findViewById(R.id.editText);
//            radius = Double.parseDouble(radiusFinder.getText().toString());
        if (v.getId() == R.id.currentLocation) {
            final LocationSource locationSource = new LocationSource(this);
            // We can now safely use the API we requested access to
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    mapboxMap.setMyLocationEnabled(true);
                    Location location = null;

                    try {
                        location = locationSource.getLastLocation();
                        currentLocation = true;
                    } catch (SecurityException e) {
                        Log.e("Not given permission", "Request Permission from the user"); // lets the user know there is a problem with the gps
                    }
                    final CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude())) // Sets the center of the map to current location
                            .zoom(15)                            // Sets the zoom
                            .build();
                    mapboxMap.setCameraPosition(cameraPosition);
                    drawCircle();
                    // Interact with the map using mapboxMap here

                }
            });
        } else if (v.getId() == R.id.settingsButton) {
            Intent intent = new Intent(this, SettingsActivity.class);
//            intent.putExtra("MapType", settings.getMapType());
//            intent.putExtra("Year", settings.getYear());
//            intent.putExtra("Radius", settings.getRadius());
            intent.putExtra("Settings", settings);
            startActivityForResult(intent, 1);

        } else if (v.getId() == R.id.cancelButton) {
            setContentView(R.layout.activity_main);
            updateLocation();
        } else if (v.getId() == R.id.search) {
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        findCrime(latitude, longitude);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();


        } else if (v.getId() == R.id.mapview) {
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    currentLocation = false;
                    mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng point) {
                            latitude = point.getLatitude();
                            longitude = point.getLongitude();
                        }
                    });
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                    );
                    final CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(latitude, longitude))
                            .zoom(15)                            // Sets the zoom
                            .build();
                    mapboxMap.setCameraPosition(cameraPosition);

                }
            });
        }

    }

    public void drawCircle() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                ArrayList<LatLng> polygon = new ArrayList<>();
                for (int k = 0; k < 45; k++) {
                    LatLng latLng = new LatLng();
//                    latLng.setLatitude(latitude + (radius / 69) * sin(k * (2 * PI) / 360));
//                    latLng.setLongitude(longitude + (radius / 69) * cos(k * (2 * PI) / 360));
                    int degrees = k * 8;
                    double degreeRadians = degrees * PI / 180;
                    double distRadians = settings.getRadius() /1609.34;
                    double pointLatRadians = asin(sin(latitude * PI / 180) * cos(distRadians) + cos(latitude * PI / 180) * sin(distRadians) * cos(degreeRadians));
                    double pointLonRadians = longitude * PI / 180 + atan2(sin(degreeRadians) * sin(distRadians) * cos(latitude * PI / 180), cos(distRadians) - sin(latitude * PI / 180) * sin(pointLatRadians));
                    double pointLat = pointLatRadians * 180 / PI;
                    double pointLon = pointLonRadians * 180 / PI;
                    latLng.setLatitude(pointLat);
                    latLng.setLongitude(pointLon);
                    polygon.add(latLng);
                }
                mapboxMap.addPolygon(new PolygonOptions()
                        .addAll(polygon)
                        .fillColor(Color.parseColor("#4D00008B")));
            }
        });
    }

    public void addMarkers() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    addMarkersCall();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void addMarkersCall() {
        String url = CRIME_PULL_API_BASE;
        double latlongRadius = radius / 69.0;
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
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        try {
                            JSONObject dataPoints = new JSONObject(response.toString());
                            for (int k = 0; k < Integer.parseInt(dataPoints.getString("result_num")); k++) {
                                JSONObject crime = dataPoints.getJSONArray("results").getJSONObject(k);
                                String crimeType = crime.getString("typeCrime");
                                String style = settings.getMapType();
//                                if (crimeType.equals("HOMICIDE")) {
//                                    mapboxMap.addMarker(new MarkerViewOptions()
//                                            .position(new LatLng(Double.parseDouble(crime.getString("lat")), Double.parseDouble(crime.getString("long"))))
//                                            .title(crimeType));
//                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        currentLocation = false;
                        mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng point) {
                                latitude = point.getLatitude();
                                longitude = point.getLongitude();
                            }
                        });
                        final CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(latitude, longitude))
                                .zoom(15)                            // Sets the zoom
                                .build();
                        mapboxMap.setCameraPosition(cameraPosition);

                    }
                });
            }
        };
    }
//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void findCrime(double latitude, double longitude) throws Exception {
        String url = CRIME_PULL_API_BASE;
        double latlongRadius = radius / 69.0;
        Map<String, String> params = new HashMap<>();
        params.put("curlatitude", Double.toString(latitude));
        params.put("curlongitude", Double.toString(longitude));
        params.put("radius", Double.toString(latlongRadius));
        params.put("year", Integer.toString(settings.getYear()));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, url, params, this.findSuccessResponse(), this.findErrorResponse());
        requestQueue.add(jsObjRequest);
    }

    public Response.Listener<JSONObject> findSuccessResponse() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                calculateDanger(response);
                createAlert();
            }
        };
    }

    public Response.ErrorListener findErrorResponse() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                Log.e("Error", "Error connecting to Places API");
                error.printStackTrace();
            }
        };
    }

    public void createAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons

        DecimalFormat df = new DecimalFormat("####0.00");
        builder.setMessage("").setTitle("This area has a danger score of " + df.format(danger));
        builder.setPositiveButton("View Details", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                openDetailsPage();
                // Go to details page
            }
        });
        builder.setNegativeButton("Back to Map", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // Go Back to map
            }
        });

        builder.show();
        // Show the AlertDialog
    }

    public void openDetailsPage() {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("CrimeArray", crimes);
        intent.putExtra("Danger", danger);
        if (!currentLocation) {
            intent.putExtra("Address", autoCompView.getText());
        } else {
            intent.putExtra("Address", "The Current Location");
        }
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK || resultCode == RESULT_FIRST_USER) {
                if (intent.getSerializableExtra("Settings") != null) {
//                    settings.setYear(intent.getIntExtra("Year", 2014));
//                    settings.setMapType(intent.getStringExtra("MapType"));
//                    settings.setRadius(intent.getDoubleExtra("Radius", 0.5));
                    settings = (Settings) intent.getSerializableExtra("Settings");
                }
                updateLocation();

            }
        }
    }

    public void calculateDanger(JSONObject response) {
        try {
            JSONObject dataPoints = new JSONObject(response.toString());
            crimes = new int[9];
            danger = 0.0;
            CrimeWeightSettings crimeWeights = settings.getCrimeWeights();
            double homicide = crimeWeights.getHomicide();
            double rape = crimeWeights.getRape();
            double assault = crimeWeights.getAssault();
            double pedestrianTheft = crimeWeights.getPedestrianTheft();
            double vehicularTheft = crimeWeights.getVehicularTheft();
//            gets all of the weights from the settings

            for (int k = 0; k < Integer.parseInt(dataPoints.getString("result_num")); k++) {
                    String crimeType = dataPoints.getJSONArray("results").getJSONObject(k).getString("typeCrime");
                    if (crimeType.equals("HOMICIDE")) {
                        danger += homicide;
                        crimes[0]++;
                    } else if (crimeType.equals("AGGRAVATED ASSAULT")) {
                        danger += assault;
                        crimes[1]++;
                    } else if (crimeType.equals("RAPE")) {
                        danger += rape;
                        crimes[2]++;
                    } else if (crimeType.equals("ROBBERY")) {
                        danger += pedestrianTheft;
                        crimes[3]++;
                    } else if (crimeType.equals("LARCENY")) {
                        danger += pedestrianTheft;
                        crimes[4]++;
                    } else if (crimeType.equals("BURGLARY FROM VEHICLE")) {
                        danger += vehicularTheft;
                        crimes[5]++;
                    } else if (crimeType.equals("AUTO THEFT")) {
                        danger += vehicularTheft;
                        crimes[6]++;
                    } else if (crimeType.equals("BURGLARY")) {
                        danger += pedestrianTheft;
                        crimes[7]++;
                    } else if (crimeType.equals("LARCENY FROM VEHICLE")) {
                        danger += vehicularTheft;
                        crimes[8]++;
                    }
                }

            if (year == 2014) {
                danger /= 6;
            } else if (year == 2015) {
                danger /= 4;
            }
            danger /= (radius * 60);
            done = true;

        } catch (JSONException error) {
            Log.e("JSON Error", "Error parsing the JSON");
            error.printStackTrace();
        }
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
                    currentLocation = false;
                    JSONObject jsonObj = response;
                    JSONObject predsJsonArray = jsonObj.getJSONObject("result").getJSONObject("geometry");
                    JSONObject location = predsJsonArray.getJSONObject("location");
                    latitude = Double.parseDouble(location.getString("lat"));
                    longitude = Double.parseDouble(location.getString("lng"));
                    updateLocation();
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error processing JSON results", e);
                }
            }
        };
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