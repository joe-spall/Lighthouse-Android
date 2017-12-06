package com.example.michaelaki.lighthouseandroid.model;

import android.content.Context;

import com.example.michaelaki.lighthouseandroid.R;
import com.example.michaelaki.lighthouseandroid.controller.MainActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by michaelaki on 12/3/17.
 */

public class CustomClusterRenderer extends DefaultClusterRenderer<Crime> {

    private final Context mContext;

    public CustomClusterRenderer(Context context, GoogleMap map,
                                 ClusterManager<Crime> clusterManager) {
        super(context, map, clusterManager);

        mContext = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(Crime item,
                                                         MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_crime));
    }

    @Override
    protected String getClusterText(int bucket) {
        return bucket + "";
    }

    @Override
    public int getBucket(Cluster<Crime> cluster) {
        return cluster.getSize();


    }
}