package com.example.michaelaki.lighthouseandroid.controller;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.michaelaki.lighthouseandroid.model.Crime;
import com.example.michaelaki.lighthouseandroid.model.CrimeWeightSettings;
import com.example.michaelaki.lighthouseandroid.model.CustomRequest;
import com.example.michaelaki.lighthouseandroid.R;
import com.example.michaelaki.lighthouseandroid.model.Settings;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.clustering.Cluster;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by michaelaki on 10/7/17.
 */

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, AdapterView.OnItemClickListener, GoogleMap.OnMyLocationButtonClickListener, ClusterManager.OnClusterClickListener{

    private static final String LOG_TAG = "Google Places";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String DIRECTIONS_API_BASE = "https://maps.googleapis.com/maps/api/directions";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_FIND = "/details";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyAtonyO8kbRMgzGLhGWR0O7Zb513qzBmGQ";
    private static final String DIRECTIONS_API_KEY = "AIzaSyAUEwJCK5K09YzqGI99sZMIA1JDXVv1G54";
    private static final String CRIME_PULL_API_BASE = "https://www.app-lighthouse.com/app/crimepullcirc.php";
    private static final String SERVER_NAME = "localhost";
    private static final String USER_NAME = "applight_LHUser";
    private static final String PASSWORD = "mikelikesbirds1!";
    private static final String DATABASE_NAME = "applight_lighthouse";
    private static ArrayList<ArrayList<String>> resultList;
    private double latitude, longitude;
    private int[] crimes;
    private double danger;
    private AutoCompleteTextView autoCompView;
    private String id, startID, endID;
    private GoogleMap mMap;
    private Settings settings;
    private FusedLocationProviderClient mFusedLocationClient;
    private ClusterManager<Crime> mClusterManager;
    private ArrayList<LatLng> waypointArray;

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
        ImageButton search = (ImageButton) findViewById(R.id.searchButton);
        autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);

        ImageButton directions = (ImageButton) findViewById(R.id.directionButton);
        directions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpDirectionsDialog();
            }
        });
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToSettingsMenu();
            }
        });
    }

    public void setUpDirectionsDialog() {

        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        View promptView = inflater.inflate(R.layout.dialog_directions_search, null);
        startID = "";
        endID = "";
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(promptView)
                // Add action buttons
                .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
//                        String url = DIRECTIONS_API_BASE + OUT_JSON;
//                        Map<String, String> params = new HashMap<>();
//                        params.put("origin", "place_id:" + startID);
//                        params.put("destination", "place_id:" + endID);
//                        params.put("mode", "walking");
//                        params.put("key", DIRECTIONS_API_KEY);
//                        CustomRequest directionsRequest = new CustomRequest(Request.Method.GET, url, params, directionsSuccessResponse(), directionsErrorResponse());
//                        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
//                        requestQueue.add(directionsRequest);

//                        LatLng origin = new LatLng(37.7849569, -122.4068855);
//                        LatLng destination = new LatLng(37.7814432, -122.4460177);
//                        GoogleDirection.withServerKey(DIRECTIONS_API_KEY)
//                                .from(origin)
//                                .to(destination)
//                                .execute(new DirectionCallback() {
//                                    @Override
//                                    public void onDirectionSuccess(Direction direction, String rawBody) {
//                                        // Do something here
//                                    }
//
//                                    @Override
//                                    public void onDirectionFailure(Throwable t) {
//                                        // Do something here
//                                    }
//                                });
                        CustomRequest directionsRequest = new CustomRequest("https://maps.googleapis.com/maps/api/directions/json?" +
                                "origin=place_id:" + startID + "&destination=place_id:" + endID + "&mode=walking&key="+DIRECTIONS_API_KEY,
                                null, directionsSuccessResponse(), directionsErrorResponse());
                        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                        requestQueue.add(directionsRequest);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.show();



        final AutoCompleteTextView startLocation = (AutoCompleteTextView) promptView.findViewById(R.id.direction_start);
        final AutoCompleteTextView destination = (AutoCompleteTextView) promptView.findViewById(R.id.direction_destination);

        startLocation.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        startLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) parent.getItemAtPosition(position);
                startID = resultList.get(1).get(position);
                startLocation.setText(str);
            }
        });

        destination.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        destination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) parent.getItemAtPosition(position);
                endID = resultList.get(1).get(position);
                destination.setText(str);
            }
        });

    }

    public Response.Listener<JSONObject> directionsSuccessResponse() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    waypointArray = new ArrayList<>();
                    JSONArray waypoints = response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
                    for (int k = 0; k < waypoints.length(); k++) {
                        JSONObject waypoint = waypoints.getJSONObject(k);
                        if (k == 0) {
                            JSONObject startLocation = waypoint.getJSONObject("start_location");
                            String latStr1 = startLocation.getString("lat");
                            String lngStr1 = startLocation.getString("lng");
                            double lat1 = Double.parseDouble(latStr1);
                            double lng1 = Double.parseDouble(lngStr1);
                            drawWaypointCircle(lat1,lng1);
                            LatLng latLng1 = new LatLng(lat1, lng1);
                            waypointArray.add(latLng1);
                        }
                        JSONObject endLocation = waypoint.getJSONObject("end_location");
                        String latStr = endLocation.getString("lat");
                        String lngStr = endLocation.getString("lng");
                        double lat = Double.parseDouble(latStr);
                        double lng = Double.parseDouble(lngStr);
                        drawWaypointCircle(lat,lng);
                        LatLng latLng = new LatLng(lat, lng);
                        waypointArray.add(latLng);
                        mMap.addMarker(new MarkerOptions().position(latLng).title("This is a waypoint"));
                    }
                    JSONObject route = response.getJSONArray("routes").getJSONObject(0);
                    String polylineJSON = route.getJSONObject("overview_polyline").getString("points");
                    List<LatLng> polyline = PolyUtil.decode(polylineJSON);
                    PolylineOptions polylineOptions = new PolylineOptions();
                    for (int k = 0; k < polyline.size(); k++) {
                        polylineOptions.add(polyline.get(k));
                    }
                    mMap.clear();
                    mClusterManager.clearItems();
                    mMap.addPolyline(polylineOptions);
                    findCrimesOnRoute(response);

                } catch(JSONException e) {
                    e.printStackTrace();
                    Log.e("JSON Parse Error", "No Gouda");
                }

                System.out.println();
            }
        };
    }

    public void findCrimesOnRoute(JSONObject response) {
        try {
            JSONObject route = response.getJSONArray("routes").getJSONObject(0);
            String polylineJSON = route.getJSONObject("overview_polyline").getString("points");
            List<LatLng> polyline = PolyUtil.decode(polylineJSON);
            Map<String, String> params = new HashMap<>();
            JSONObject southwestLatLng = route.getJSONObject("bounds").getJSONObject("southwest");
            double southwestLat = southwestLatLng.getDouble("lat");
            double southwestLng = southwestLatLng.getDouble("lng");
            JSONObject northeastLatLng = route.getJSONObject("bounds").getJSONObject("northeast");
            double northeastLat = northeastLatLng.getDouble("lat");
            double northeastLng = northeastLatLng.getDouble("lng");
            params.put("aLat", Double.toString(southwestLat));
            params.put("aLng", Double.toString(southwestLng));
            params.put("bLat", Double.toString(northeastLat));
            params.put("bLng", Double.toString(northeastLng));
            params.put("year", Integer.toString(settings.getYear()));
            String url = "https://www.app-lighthouse.com/app/crimepullfromab.php";
            CustomRequest routeRequest = new CustomRequest(Request.Method.POST, url, params, routeSuccessResponse(polyline), routeErrorResponse());
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(routeRequest);
        } catch(JSONException e) {
            e.printStackTrace();
            Log.e("JSON Parse Error", "No Gouda");
        }
    }

    public Response.Listener<JSONObject> routeSuccessResponse(final List<LatLng> polyline) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject dataPoints = new JSONObject(response.toString());
                    for (int k = 0; k < Integer.parseInt(dataPoints.getString("result_num")); k++) {
                        JSONObject crime = dataPoints.getJSONArray("results").getJSONObject(k);
                        LatLng latLng = new LatLng(Double.parseDouble(crime.getString("lat")), Double.parseDouble(crime.getString("long")));
                        if (PolyUtil.isLocationOnPath(latLng, polyline, true, settings.getRadius() * 1609)) {
                            String crimeType = crime.getString("typeCrime");
                            Crime crimeToAdd = new Crime(crimeType, crime.getString("id"), crime.getString("date"), new LatLng(Double.parseDouble(crime.getString("lat")), Double.parseDouble(crime.getString("long"))));
                            mClusterManager.addItem(crimeToAdd);
                        }


                    }

                } catch(JSONException e) {
                    e.printStackTrace();
                    Log.e("JSON Parse Error", "No Gouda");
                }
            }


        };

    }

    public boolean pathCheck(LatLng latLng, List<LatLng> polyline) {
        return PolyUtil.isLocationOnPath(latLng, polyline, true, 500);
    }

    public Response.ErrorListener routeErrorResponse() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Route error", "Heck");
                error.printStackTrace();
            }
        };
    }

//    public void waypointSearch(String place_id) {
//        try {
//            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
//            sb.append(TYPE_FIND);
//            sb.append(OUT_JSON);
//            sb.append("?sensor=false");
//            sb.append("&key=" + API_KEY);
//            sb.append("&place_id=" + URLEncoder.encode(place_id, "utf8"));
//            String url = sb.toString();
//            RequestQueue requestQueue = Volley.newRequestQueue(this);
//            Map<String, String> params = new HashMap<>();
//            params.put("sensor", "false");
//            params.put("key", API_KEY);
//            params.put("place_id", URLEncoder.encode(place_id, "utf8"));
//            CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, url, params, this.waypointSearchSuccessResponse(), this.searchErrorResponse());
//            requestQueue.add(jsObjRequest);
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "Error connecting to Places API", e);
//            return;
//        }
//    }

//    public Response.Listener<JSONObject> waypointSearchSuccessResponse() {
//        return new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    JSONObject jsonObj = response;
//                    JSONObject predsJsonArray = jsonObj.getJSONObject("result").getJSONObject("geometry");
//                    JSONObject location = predsJsonArray.getJSONObject("location");
//                    double lat = Double.parseDouble(location.getString("lat"));
//                    double lng = Double.parseDouble(location.getString("lng"));
//                    LatLng latLng = new LatLng(lat, lng);
//                    mMap.addMarker(new MarkerOptions().position(latLng).title("This is a waypoint"));
//                    drawWaypointCircle(lat, lng);
//                } catch (JSONException e ) {
//                    Log.e("JSON Error", "Uh oh Spagett-O");
//                }
//            }
//        };
//    }

    public void drawWaypointRoutes() {
        PolylineOptions polyline = new PolylineOptions();
        for (int k = 0; k < waypointArray.size(); k++) {
            polyline.add(waypointArray.get(k));

        }

        mMap.addPolyline(polyline);
    }

    public void drawWaypointCircle(double latitude, double longitude) {
        mMap.addCircle(new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(250)
                .fillColor(Color.parseColor("#FF00008B"))
                .strokeWidth(1));
    }

    public Response.ErrorListener directionsErrorResponse() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Directions error", "Darn");
                error.printStackTrace();
            }
        };
    }

    public void switchToSettingsMenu() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("Settings", settings);
        startActivityForResult(intent, 1);
    }

    public void findCrime(double latitude, double longitude) throws Exception {
        String url = CRIME_PULL_API_BASE;
        double latlongRadius = settings.getRadius() / 69.0;
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
        startActivity(intent);
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

            if (settings.getYear() == 2014) {
                danger /= 6;
            } else if (settings.getYear() == 2015) {
                danger /= 4;
            }
            danger /= (settings.getRadius() * 60);

        } catch (JSONException error) {
            Log.e("JSON Error", "Error parsing the JSON");
            error.printStackTrace();
        }
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
        mClusterManager.setOnClusterClickListener(this);

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
                            //mClusterManager.setOnClusterInfoWindowClickListener();

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
            sb.append("&place_id=" + URLEncoder.encode(input, "utf8"));
            String url = sb.toString();
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            Map<String, String> params = new HashMap<>();
            params.put("sensor", "false");
            params.put("key", API_KEY);
            params.put("place_id", URLEncoder.encode(input, "utf8"));
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


    public boolean onClusterClick(Cluster item) {
//        mMap.animateCamera(CameraUpdateFactory.zoomIn());

        float zoom = mMap.getCameraPosition().zoom;
        if (zoom > 20) {
//

        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(item.getPosition(), zoom + 1));
        }


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
                resultList.get(1).add(predsJsonArray.getJSONObject(i).getString("place_id"));
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
