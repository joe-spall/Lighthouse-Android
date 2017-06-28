package com.example.michaelaki.safespot;


import android.preference.DialogPreference;
import android.content.Context;
import android.util.AttributeSet;
import android.content.res.TypedArray;





/**
 * Created by jspall16 on 6/24/17.
 */

public class DistancePreference extends DialogPreference {

    /**
     * Radius value
     */
    private float radius;

    /**
     * Resource of the dialog layout
     */
    private int mDialogLayoutResId = R.layout.distance_pref;

    public DistancePreference(Context context){
        this(context,null);
    }

    public DistancePreference(Context context, AttributeSet attrs) {
        // Delegate to other constructor
        // Use the preferenceStyle as the default style
        this(context, attrs, 0);
    }

    public DistancePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        // Delegate to other constructor
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public DistancePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        // Du custom stuff here
        // ...
        // read attributes etc.
    }

    /**
     * Gets the radius from the Shared Preferences
     *
     * @return The current preference value
     */
    public float getDistance() {
        return radius;
    }

    /**
     * Saves the time to the SharedPreferences
     *
     * @param distance The radius distance to save
     */
    public void setDistance(float distance) {
        radius = distance;

        // Save to SharedPreference
        persistFloat(distance);
    }

    /**
     * Called when a Preference is being inflated and the default value attribute needs to be read
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // The type of this preference is Float, so we read the default value from the attributes
        // as Float. Fallback value is set to 0.
        return a.getFloat(index, 0.05f);
    }

    /**
     * Returns the layout resource that is used as the content View for the dialog
     */
    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

    /**
     * Implement this to set the initial value of the Preference.
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // If the value can be restored, do it. If not, use the default value.
        setDistance(restorePersistedValue ?
                getPersistedFloat(radius) : (float) defaultValue);
    }

}
