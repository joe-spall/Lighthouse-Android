package com.example.michaelaki.lighthouseandroid.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;

/**
 * Created by michaelaki on 12/5/17.
 */

public class MyClusterItemRenderer extends DefaultClusterRenderer<Crime> {

    private final IconGenerator mIconGenerator;
    private final ShapeDrawable mColoredCircleBackground;
    private final float mDensity;

    private SparseArray<BitmapDescriptor> mIcons = new SparseArray();

    public MyClusterItemRenderer(Context context, GoogleMap map, ClusterManager<Crime> clusterManager) {
        super(context, map, clusterManager);
        mIconGenerator = new IconGenerator(context);
        mColoredCircleBackground = new ShapeDrawable(new OvalShape());
        mDensity = context.getResources().getDisplayMetrics().density;

        SquareTextView squareTextView = new SquareTextView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -2);
        squareTextView.setLayoutParams(layoutParams);
//        squareTextView.setId(com.google.maps.android.R.id.text);
        int twelveDpi = (int)(12.0F * this.mDensity);
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi);
        this.mIconGenerator.setContentView(squareTextView);

        //mIconGenerator.setTextAppearance(com.google.maps.android.R.style.ClusterIcon_TextAppearance);

        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        outline.getPaint().setColor(Color.parseColor("#FFFFFF"));
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, this.mColoredCircleBackground});
        int strokeWidth = (int) (this.mDensity * 3.0F);
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        mIconGenerator.setBackground(background);

    }



    @Override
    protected void onBeforeClusterRendered(Cluster<Crime> cluster, MarkerOptions markerOptions) {
        int bucket = this.getBucket(cluster);
        BitmapDescriptor descriptor = (BitmapDescriptor)this.mIcons.get(bucket);
        if(descriptor == null) {
            this.mColoredCircleBackground.getPaint().setColor(this.getColor(bucket));
            descriptor = BitmapDescriptorFactory.fromBitmap(this.mIconGenerator.makeIcon(String.valueOf(cluster.getSize())));
            this.mIcons.put(bucket, descriptor);
        }

        markerOptions.icon(descriptor);
    }
}
