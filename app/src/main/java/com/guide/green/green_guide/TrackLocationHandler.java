package com.guide.green.green_guide;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class TrackLocationHandler implements View.OnClickListener, LocationListener {
    private android.support.design.widget.FloatingActionButton btnToggleLocation;
    private LocationManager locationManager;
    private boolean isTraking = false;
    private Location bestGuess;
    private BaiduMap mBaiduMap;
    private Activity act;

    // True if the first time a location has be obtained after the button was pressed.
    private boolean moveToMyLocation = true;

    public TrackLocationHandler(Activity act, FloatingActionButton location, BaiduMap bMap) {
        btnToggleLocation = location;
        this.act = act;
        mBaiduMap = bMap;
    }

    @Override
    public void onClick(View view) {
        // Register the listener with the Location Manager to receive location updates
        if (ContextCompat.checkSelfPermission(act,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(act,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);

            return;
        }

        if (locationManager == null) {
            // Acquire a reference to the system Location Manager
            locationManager = (LocationManager) act.getSystemService(Context.LOCATION_SERVICE);
        }

        // Define a listener that responds to location updates
        if (isTraking) {
            moveToMyLocation = true;
            locationManager.removeUpdates(this);
            setTracking(false);
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, this);
            setTracking(true);
        }
    }

    private boolean betterThanBestGuess(Location newer) {
        if (bestGuess == null) return true;

        final int maxMilliSeconds = 10000;
        long timeDelta = newer.getTime() - bestGuess.getTime();

        if (timeDelta > maxMilliSeconds) return true;
        else if (timeDelta < maxMilliSeconds / 2) return false;

        boolean isNewer = timeDelta > 0;
        int accuracyDelta = (int) (newer.getAccuracy() - bestGuess.getAccuracy());
        boolean haveSameProviders = newer.getProvider() != null &&
                newer.getProvider().equals(bestGuess.getProvider());

        if (isNewer && haveSameProviders) {
            return true;
        } else if (isNewer && accuracyDelta < 20) {
            return true;
        }
        return false;
    }

    private void setTracking(boolean mode) {
        if (mode) {
            int[][] states = new int[][] { new int[] { android.R.attr.state_enabled} };
            int[] colors = new int[] {Color.RED};
            btnToggleLocation.setImageTintList(new ColorStateList(states, colors));
            mBaiduMap.setMyLocationEnabled(true);
        } else {
            btnToggleLocation.setImageTintList(null);
            mBaiduMap.setMyLocationEnabled(false);
        }
        isTraking = mode;
    }

    public void onLocationChanged(Location location) {
        if (betterThanBestGuess(location)) {
            bestGuess = location;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getAccuracy())
                    .direction(100)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();

            mBaiduMap.setMyLocationData(locData);
        }
        if (moveToMyLocation) {
            moveToMyLocation = false;
            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(pos, 15));
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) { /* Do Nothing */ }
    public void onProviderEnabled(String provider) { /* Do Nothing */ }
    public void onProviderDisabled(String provider) { /* Do Nothing */ }
}