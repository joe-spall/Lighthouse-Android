package com.example.michaelaki.lighthouseandroid.controller;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.location.Location;
import android.media.Image;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.michaelaki.lighthouseandroid.model.Crime;
import com.example.michaelaki.lighthouseandroid.model.CrimeWeightSettings;
import com.example.michaelaki.lighthouseandroid.model.CustomClusterRenderer;
import com.example.michaelaki.lighthouseandroid.model.CustomRequest;
import com.example.michaelaki.lighthouseandroid.R;
import com.example.michaelaki.lighthouseandroid.model.MyClusterItemRenderer;
import com.example.michaelaki.lighthouseandroid.model.ObjectRequest;
import com.example.michaelaki.lighthouseandroid.model.Settings;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.MarkerManager;
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
import java.util.Arrays;
import java.util.Collection;
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
    private static final String CRIME_PULL_API_BASE = "http://app-lighthouse.herokuapp.com/api";
    private static final String ROUTE_PULL = "/route_crimepull";
    private static final String POINT_PULL = "/point_crimepull";
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
    private View hiddenPanel;
    private boolean changedDrawerData = false;
    private boolean isRoute = false;
    private boolean showHomicide = true, showAssault = true, showRape = true, showPedTheft = true, showCarTheft = true;
    private ArrayList<Crime>[] crimesList = new ArrayList[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        settings = new Settings();

        for (int k = 0; k < 5; k++) {
            crimesList[k] = new ArrayList<Crime>();
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setUpInteractivity();

    }

    public void slideUpDown(final View view) {
        ImageButton button = (ImageButton) findViewById(R.id.upButton);
        if (!isPanelShown()) {
            // Show the panel
            changedDrawerData = false;
            Animation bottomUp = AnimationUtils.loadAnimation(this,
                    R.anim.bottom_up);

            hiddenPanel.startAnimation(bottomUp);
            hiddenPanel.setVisibility(View.VISIBLE);



        }
        else {

            // Hide the Panel
            if (changedDrawerData) {
                mClusterManager.clearItems();
                setCameraLocation();
            }
            Animation bottomDown = AnimationUtils.loadAnimation(this,
                    R.anim.bottom_down);

            hiddenPanel.startAnimation(bottomDown);
            hiddenPanel.setVisibility(View.GONE);
        }
    }

    private boolean isPanelShown() {
        return hiddenPanel.getVisibility() == View.VISIBLE;
    }

    public void setUpInteractivity() {
        ImageButton settings = (ImageButton) findViewById(R.id.settingsButton);
        Button search = (Button) findViewById(R.id.searchButton);
        autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);
        hiddenPanel = (findViewById(R.id.hidden_panel));
        hiddenPanel.setVisibility(View.GONE);

//        ImageButton directions = (ImageButton) findViewById(R.id.directionButton);
//        directions.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setUpDirectionsDialog();
//            }
//        });
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDetailsPage();
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
                        requestDirections();

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

    public void requestDirections() {
        ObjectRequest directionsRequest = new ObjectRequest("https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + latitude + "," + longitude + "&destination=place_id:" + endID + "&mode=walking&key="+DIRECTIONS_API_KEY,
                null, directionsSuccessResponse(), directionsErrorResponse());
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(directionsRequest);
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
                            //drawWaypointCircle(lat1,lng1);
                            LatLng latLng1 = new LatLng(lat1, lng1);
                            waypointArray.add(latLng1);
                        }
                        JSONObject endLocation = waypoint.getJSONObject("end_location");
                        String latStr = endLocation.getString("lat");
                        String lngStr = endLocation.getString("lng");
                        double lat = Double.parseDouble(latStr);
                        double lng = Double.parseDouble(lngStr);
                        //drawWaypointCircle(lat,lng);
                        LatLng latLng = new LatLng(lat, lng);
                        waypointArray.add(latLng);
                        //mMap.addMarker(new MarkerOptions().position(latLng).title("This is a waypoint")
                          //      .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_crime)));
                    }
                    JSONObject route = response.getJSONArray("routes").getJSONObject(0);
                    String polylineJSON = route.getJSONObject("overview_polyline").getString("points");
                    List<LatLng> polyline = PolyUtil.decode(polylineJSON);
                    PolylineOptions polylineOptions = new PolylineOptions().width(20);
                    for (int k = 0; k < polyline.size(); k++) {
                        polylineOptions.add(polyline.get(k));
                    }

                    mMap.addPolyline(polylineOptions);
                    findCrimesOnRoute(response);
                    isRoute = true;
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
            JSONArray legs = route.getJSONArray("legs").getJSONObject(0).getJSONArray("steps");

            Map<String, String> params = new HashMap<>();
            JSONObject southwestLatLng = route.getJSONObject("bounds").getJSONObject("southwest");
            double southwestLat = southwestLatLng.getDouble("lat");
            double southwestLng = southwestLatLng.getDouble("lng");
            JSONObject northeastLatLng = route.getJSONObject("bounds").getJSONObject("northeast");
            double northeastLat = northeastLatLng.getDouble("lat");
            double northeastLng = northeastLatLng.getDouble("lng");
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(new LatLng(southwestLat,southwestLng),new LatLng(
            northeastLat,northeastLng)), 0));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 1));
            double[] latlngs = new double[(legs.length() + 1) * 2];
            int count = 0;
            JSONObject firstlatlng = legs.getJSONObject(0).getJSONObject("start_location");
            latlngs[count] = firstlatlng.getDouble("lat");
            latlngs[count + 1] = firstlatlng.getDouble("lng");
            count += 2;
            for (int k = 0; k < legs.length(); k++) {
                JSONObject step = legs.getJSONObject(k).getJSONObject("end_location");
                latlngs[count] = step.getDouble("lat");
                latlngs[count + 1] = step.getDouble("lng");
                count += 2;

            }
            params.put("radius", Integer.toString(settings.getRadius()));
            params.put("points", Arrays.toString(latlngs));
            System.out.println(Arrays.toString(latlngs));
            params.put("year", Integer.toString(settings.getYear()));
            String url = CRIME_PULL_API_BASE + ROUTE_PULL;
            CustomRequest routeRequest = new CustomRequest(Request.Method.POST, url, params, routeSuccessResponse(legs), routeErrorResponse());

            routeRequest.setRetryPolicy((new DefaultRetryPolicy(15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            requestQueue.add(routeRequest);
        } catch(JSONException e) {
            e.printStackTrace();
            Log.e("JSON Parse Error", "No Gouda");
        }
    }

    public Response.Listener<JSONArray> routeSuccessResponse(final JSONArray steps) {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                    calculateDanger(response);
                    mMap.clear();
                    try {
                        for (int k = 0; k < 5; k++) {
                            crimesList[k].clear();
                        }
                        for (int k = 0; k < response.length(); k++) {

                            JSONObject crime = response.getJSONObject(k);
                            boolean show = true;
                            String crimeType = crime.getString("Crime");
                            JSONArray coordinates = crime.getJSONArray("Coordinates");
                            Crime crimeToAdd = new Crime(crimeType, crime.getString("_id"), crime.getString("Timestamp"), new LatLng(coordinates.getDouble(1), coordinates.getDouble(0)));
                            if (crimeType.equals("HOMICIDE")) {
                                if (!showHomicide) show = false;
                                crimesList[0].add(crimeToAdd);
                            } else if (crimeType.equals("AGGRAVATED ASSAULT")) {
                                if (!showAssault) show = false;
                                crimesList[1].add(crimeToAdd);

                            } else if (crimeType.equals("RAPE")) {
                                if (!showRape) show = false;
                                crimesList[2].add(crimeToAdd);

                            } else if (crimeType.equals("ROBBERY")) {
                                if (!showPedTheft) show = false;
                                crimesList[3].add(crimeToAdd);

                            } else if (crimeType.equals("LARCENY")) {
                                if (!showPedTheft) show = false;
                                crimesList[3].add(crimeToAdd);

                            } else if (crimeType.equals("BURGLARY FROM VEHICLE")) {
                                if (!showCarTheft) show = false;
                                crimesList[4].add(crimeToAdd);

                            } else if (crimeType.equals("AUTO THEFT")) {
                                if (!showCarTheft) show = false;
                                crimesList[4].add(crimeToAdd);

                            } else if (crimeType.equals("BURGLARY")) {
                                if (!showPedTheft) show = false;
                                crimesList[3].add(crimeToAdd);

                            } else if (crimeType.equals("LARCENY FROM VEHICLE")) {
                                if (!showCarTheft) show = false;
                                crimesList[4].add(crimeToAdd);

                            } else if (crimeType.equals("THEFT")) {
                                if (!showPedTheft) show = false;
                                crimesList[3].add(crimeToAdd);

                            }


                            if (show) {

                                //TODO: remove from cluster instead of pulling again
                                mClusterManager.addItem(crimeToAdd);
                            }




                        }
                        isRoute = true;
                        mClusterManager.cluster();

                        CrimeWeightSettings crimeWeights = settings.getCrimeWeights();
                        double homicide = crimeWeights.getHomicide();
                        double rape = crimeWeights.getRape();
                        double assault = crimeWeights.getAssault();
                        double pedestrianTheft = crimeWeights.getPedestrianTheft();
                        double vehicularTheft = crimeWeights.getVehicularTheft();
//            gets all of the weights from the settings

                        double[] polylineCrimes = new double[steps.length()];
                        for (int j = 0; j < response.length(); j++) {
                            JSONObject crime = response.getJSONObject(j);
                            boolean show = true;
                            String crimeType = crime.getString("Crime");
                            JSONArray coordinates = crime.getJSONArray("Coordinates");
                            Crime currCrime = new Crime(crimeType, crime.getString("_id"), crime.getString("Timestamp"), new LatLng(coordinates.getDouble(1), coordinates.getDouble(0)));
                            for (int k = 0; k < steps.length(); k++) {
                                JSONObject waypoint = steps.getJSONObject(k);

                                JSONObject polyline = waypoint.getJSONObject("polyline");
                                String polylineString = polyline.getString("points");
                                List<LatLng> decodedPolyline = PolyUtil.decode(polylineString);

                                if (pathCheck(currCrime.getPosition(), decodedPolyline)) {

                                    if (crimeType.equals("HOMICIDE")) {
                                        polylineCrimes[k] += homicide;

                                    } else if (crimeType.equals("AGGRAVATED ASSAULT")) {
                                        polylineCrimes[k] += assault;
                                    } else if (crimeType.equals("RAPE")) {
                                        polylineCrimes[k] += rape;
                                    } else if (crimeType.equals("ROBBERY")) {
                                        polylineCrimes[k] += pedestrianTheft;
                                    } else if (crimeType.equals("LARCENY")) {
                                        polylineCrimes[k] += pedestrianTheft;
                                    } else if (crimeType.equals("BURGLARY FROM VEHICLE")) {
                                        polylineCrimes[k] += vehicularTheft;
                                    } else if (crimeType.equals("AUTO THEFT")) {
                                        polylineCrimes[k] += vehicularTheft;
                                    } else if (crimeType.equals("BURGLARY")) {
                                        polylineCrimes[k] += pedestrianTheft;
                                    } else if (crimeType.equals("LARCENY FROM VEHICLE")) {
                                        polylineCrimes[k] += vehicularTheft;
                                    } else if (crimeType.equals("THEFT")) {
                                        polylineCrimes[k] += pedestrianTheft;
                                    }

                                }
                            }
                        }
                        for (int k = 0; k < steps.length(); k++) {
                            polylineCrimes[k] /= Math.pow(steps.getJSONObject(k).getJSONObject("distance").getInt("value"), .5);
                            String color = getDangerColor(polylineCrimes[k] * settings.getRadius() / 210);
                            String polyline = steps.getJSONObject(k).getJSONObject("polyline").getString("points");
                            List<LatLng> decodedPolyline = PolyUtil.decode(polyline);
                            PolylineOptions polylineOptions1 = new PolylineOptions().width(25);

                            polylineOptions1.addAll(decodedPolyline);
                            PolylineOptions polylineOptions = new PolylineOptions().color(Color.parseColor(color)).width(15);

                            polylineOptions.addAll(decodedPolyline);

                            mMap.addPolyline(polylineOptions1);
                            mMap.addPolyline(polylineOptions);

                        }







                        //mClusterManager.setOnClusterInfoWindowClickListener();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }


        };

    }

    public boolean pathCheck(LatLng latLng, List<LatLng> polyline) {
        return PolyUtil.isLocationOnPath(latLng, polyline, true, settings.getRadius());
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
        changedDrawerData = true;
        startActivityForResult(intent, 1);
    }

    public void findCrime(double latitude, double longitude) throws Exception {
        String url = CRIME_PULL_API_BASE;
        double latlongRadius = settings.getRadius();
        Map<String, String> params = new HashMap<>();
        params.put("curlatitude", Double.toString(latitude));
        params.put("curlongitude", Double.toString(longitude));
        params.put("radius", Double.toString(latlongRadius));
        params.put("year", Integer.toString(settings.getYear()));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, url, params, this.findSuccessResponse(), this.findErrorResponse());
        requestQueue.add(jsObjRequest);
    }

    public Response.Listener<JSONArray> findSuccessResponse() {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                calculateDanger(response);
//                createAlert();
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

    public void calculateDanger(JSONArray response) {
        try {

            crimes = new int[9];
            danger = 0.0;
            CrimeWeightSettings crimeWeights = settings.getCrimeWeights();
            double homicide = crimeWeights.getHomicide();
            double rape = crimeWeights.getRape();
            double assault = crimeWeights.getAssault();
            double pedestrianTheft = crimeWeights.getPedestrianTheft();
            double vehicularTheft = crimeWeights.getVehicularTheft();
//            gets all of the weights from the settings

            for (int k = 0; k < response.length(); k++) {
                String crimeType = response.getJSONObject(k).getString("Crime");
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
                } else if (crimeType.equals("THEFT")) {
                    danger += pedestrianTheft;
                    crimes[7]++;
                }
            }

            if (settings.getYear() == 2014) {
                danger /= 6;
            } else if (settings.getYear() == 2015) {
                danger /= 4;
            }
            danger /= (settings.getRadius());
            Button button = (Button) findViewById(R.id.searchButton);
            DecimalFormat df = new DecimalFormat(".##");
            String dangerString = df.format(danger);
            button.setText(dangerString);
            int color = Color.parseColor(getDangerColor(danger));
            button.setTextColor(color);
            int circleColor = Color.parseColor(getCircleDangerColor(danger));
            drawCircle(circleColor);

            StateListDrawable drawable = (StateListDrawable) button.getBackground();
            DrawableContainer.DrawableContainerState dcs = (DrawableContainer.DrawableContainerState)drawable.getConstantState();
            Drawable[] drawableItems = dcs.getChildren();
            GradientDrawable gradientDrawableChecked = (GradientDrawable)drawableItems[0];
            GradientDrawable gradientDrawableUnChecked = (GradientDrawable)drawableItems[1];
            gradientDrawableChecked.setStroke(10, color);
            gradientDrawableUnChecked.setStroke(10, color);

        } catch (JSONException error) {
            Log.e("JSON Error", "Error parsing the JSON");
            error.printStackTrace();
        }
    }

    public String getDangerColor(double danger) {
        if (danger < 3.0) return "#288F00";
        else if (danger < 6.0) return "#E6E600";
        else return "#E01100";
    }

    public String getCircleDangerColor(double danger) {
        if (danger < 3.0) return "#22288F00";
        else if (danger < 6.0) return "#22E6E600";
        else return "#22E01100";
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

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mClusterManager = new ClusterManager<Crime>(this, mMap);
        final CustomClusterRenderer renderer = new CustomClusterRenderer(this, mMap, mClusterManager);

        mClusterManager.setRenderer(renderer);
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

        mMap.clear();
        mClusterManager.clearItems();




        // Point the map's listeners at the listeners implemented by the cluster
        // manager.


        if (isRoute) {
            requestDirections();

        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17));

            addMarkers();
        }

    }

    public void drawCircle(int color) {
        mMap.addCircle(new CircleOptions()
            .center(new LatLng(latitude, longitude))
            .radius(settings.getRadius())
            .fillColor(color)
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
        String url = CRIME_PULL_API_BASE + POINT_PULL;
        double meterRadius = settings.getRadius();
        Map<String, String> params = new HashMap<>();
        params.put("lat", Double.toString(latitude));
        params.put("lng", Double.toString(longitude));
        params.put("radius", Double.toString(meterRadius));
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
                error.printStackTrace();
                Log.e("Marker Error", "Error adding markers");
            }
        };
    }

    public Response.Listener<JSONArray> addMarkerSuccessResponse() {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(final JSONArray response) {
                        calculateDanger(response);

                        try {
                            for (int k = 0; k < 5; k++) {
                                crimesList[k].clear();
                            }
                            for (int k = 0; k < response.length(); k++) {

                                JSONObject crime = response.getJSONObject(k);
                                boolean show = true;
                                String crimeType = crime.getString("Crime");
                                JSONArray coordinates = crime.getJSONArray("Coordinates");
                                Crime crimeToAdd = new Crime(crimeType, crime.getString("_id"), crime.getString("Timestamp"), new LatLng(coordinates.getDouble(1), coordinates.getDouble(0)));
                                if (crimeType.equals("HOMICIDE")) {
                                    if (!showHomicide) show = false;
                                    crimesList[0].add(crimeToAdd);
                                } else if (crimeType.equals("AGGRAVATED ASSAULT")) {
                                    if (!showAssault) show = false;
                                    crimesList[1].add(crimeToAdd);

                                } else if (crimeType.equals("RAPE")) {
                                    if (!showRape) show = false;
                                    crimesList[2].add(crimeToAdd);

                                } else if (crimeType.equals("ROBBERY")) {
                                    if (!showPedTheft) show = false;
                                    crimesList[3].add(crimeToAdd);

                                } else if (crimeType.equals("LARCENY")) {
                                    if (!showPedTheft) show = false;
                                    crimesList[3].add(crimeToAdd);

                                } else if (crimeType.equals("BURGLARY FROM VEHICLE")) {
                                    if (!showCarTheft) show = false;
                                    crimesList[4].add(crimeToAdd);

                                } else if (crimeType.equals("AUTO THEFT")) {
                                    if (!showCarTheft) show = false;
                                    crimesList[4].add(crimeToAdd);

                                } else if (crimeType.equals("BURGLARY")) {
                                    if (!showPedTheft) show = false;
                                    crimesList[3].add(crimeToAdd);

                                } else if (crimeType.equals("LARCENY FROM VEHICLE")) {
                                    if (!showCarTheft) show = false;
                                    crimesList[4].add(crimeToAdd);

                                } else if (crimeType.equals("THEFT")) {
                                    if (!showPedTheft) show = false;
                                    crimesList[3].add(crimeToAdd);

                                }


                                if (show) {

                                    //TODO: remove from cluster instead of pulling again
                                    mClusterManager.addItem(crimeToAdd);
                                }
                                isRoute = false;
                            }
                            mClusterManager.cluster();
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
                                isRoute = false;
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

    public void toggleHomicide(final View view) {
        ImageButton homicide = (ImageButton) findViewById(R.id.homicideButton);
        if (showHomicide) {
            for (int k = 0; k < crimesList[0].size(); k++) {
                mClusterManager.removeItem(crimesList[0].get(k));
            }
            homicide.setBackground(getResources().getDrawable(R.drawable.layout_bg_white, getTheme()));
            showHomicide = false;
        } else {
            for (int k = 0; k < crimesList[0].size(); k++) {
                mClusterManager.addItem(crimesList[0].get(k));
            }
            homicide.setBackground(getResources().getDrawable(R.drawable.layout_bg_blue, getTheme()));
            showHomicide = true;
        }
        mClusterManager.cluster();
    }

    public void toggleAssault(final View view) {
        ImageButton assault = (ImageButton) findViewById(R.id.assaultButton);
        if (showAssault) {
            for (int k = 0; k < crimesList[1].size(); k++) {
                mClusterManager.removeItem(crimesList[1].get(k));
            }

            assault.setBackground(getResources().getDrawable(R.drawable.layout_bg_white, getTheme()));
            showAssault = false;
        } else {
            for (int k = 0; k < crimesList[1].size(); k++) {
                mClusterManager.addItem(crimesList[1].get(k));
            }
            assault.setBackground(getResources().getDrawable(R.drawable.layout_bg_blue, getTheme()));
            showAssault = true;
        }
        mClusterManager.cluster();
    }

    public void toggleRape(final View view) {
        ImageButton rape = (ImageButton) findViewById(R.id.rapeButton);
        if (showRape) {
            for (int k = 0; k < crimesList[2].size(); k++) {
                mClusterManager.removeItem(crimesList[2].get(k));
            }
            rape.setBackground(getResources().getDrawable(R.drawable.layout_bg_white, getTheme()));
            showRape = false;
        } else {
            for (int k = 0; k < crimesList[2].size(); k++) {
                mClusterManager.addItem(crimesList[2].get(k));
            }
            rape.setBackground(getResources().getDrawable(R.drawable.layout_bg_blue, getTheme()));
            showRape = true;
        }
        mClusterManager.cluster();
    }

    public void togglePedTheft(final View view) {
        ImageButton pedTheft = (ImageButton) findViewById(R.id.pedestrianTheftButton);
        if (showPedTheft) {
            for (int k = 0; k < crimesList[3].size(); k++) {
                mClusterManager.removeItem(crimesList[3].get(k));
            }
            pedTheft.setBackground(getResources().getDrawable(R.drawable.layout_bg_white, getTheme()));
            showPedTheft = false;
        } else {
            for (int k = 0; k < crimesList[3].size(); k++) {
                mClusterManager.addItem(crimesList[3].get(k));
            }
            pedTheft.setBackground(getResources().getDrawable(R.drawable.layout_bg_blue, getTheme()));
            showPedTheft = true;
        }
        mClusterManager.cluster();
    }

    public void toggleCarTheft(final View view) {
        ImageButton carTheft = (ImageButton) findViewById(R.id.autoTheftButton);
        if (showCarTheft) {
            for (int k = 0; k < crimesList[4].size(); k++) {
                mClusterManager.removeItem(crimesList[4].get(k));
            }
            carTheft.setBackground(getResources().getDrawable(R.drawable.layout_bg_white, getTheme()));
            showCarTheft = false;
        } else {
            for (int k = 0; k < crimesList[4].size(); k++) {
                mClusterManager.addItem(crimesList[4].get(k));
            }
            carTheft.setBackground(getResources().getDrawable(R.drawable.layout_bg_blue, getTheme()));
            showCarTheft = true;
        }
        mClusterManager.cluster();
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
        endID = this.id;
        isRoute = true;
        setCameraLocation();
        //search(this.id);
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
            ObjectRequest jsObjRequest = new ObjectRequest(Request.Method.POST, url, params, this.searchSuccessResponse(), this.searchErrorResponse());
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
        if (zoom > 17) {
//
            setUpClusterCrimeDialog(item.getItems());

        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(item.getPosition(), zoom + 1));
        }


        return true;
    }

    public void setUpClusterCrimeDialog(Collection<Crime> items) {
        LayoutInflater inflater = getLayoutInflater();
        View promptView = inflater.inflate(R.layout.dialog_cluster_crimes, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(promptView).setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        // Add action buttons
        builder.show();
        TextView crimes = (TextView) promptView.findViewById(R.id.crimesText);
        crimes.setMovementMethod(new ScrollingMovementMethod());
        crimes.setText("");
        for (Crime item: items) {

            crimes.append(item.getTitle() + " on " + item.getSnippet() + "\n\n");

        }
        // Get the layout inflater

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
