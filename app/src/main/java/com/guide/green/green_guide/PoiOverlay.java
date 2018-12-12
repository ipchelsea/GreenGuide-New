package com.guide.green.green_guide;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiResult;
import com.guide.green.green_guide.Utilities.BaiduSuggestion;

import java.util.ArrayList;
import java.util.List;

public abstract class PoiOverlay extends OverlayManager {

    private static final int MAX_POI_SIZE = 10;

    private Object mProvidedData;
    private List<LatLng> mPoiLocations;

    public PoiOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }

    public void setData(@NonNull List<? extends BaiduSuggestion.Location> poiLocations) {
        mProvidedData = poiLocations;
        mPoiLocations = new ArrayList<>();
        for (BaiduSuggestion.Location location : poiLocations) {
            mPoiLocations.add(location.point);
        }
    }

    public void setData(@NonNull PoiResult poiResult) {
        mProvidedData = poiResult;
        if (poiResult.getAllPoi() != null) {
            mPoiLocations = new ArrayList<>();
            for (PoiInfo poiInfo : poiResult.getAllPoi()) {
                mPoiLocations.add(poiInfo.location);
            }
        }
    }

    @Override
    public final List<OverlayOptions> getOverlayOptions() {
        if (mPoiLocations == null) {
            return null;
        }
        List<OverlayOptions> markerList = new ArrayList<OverlayOptions>();
        int markerSize = 0;
        for (int i = 0; i < mPoiLocations.size()
                && markerSize < MAX_POI_SIZE; i++) {
            if (mPoiLocations.get(i) == null) {
                continue;
            }
            markerSize++;
            Bundle bundle = new Bundle();
            bundle.putInt("index", i);
            markerList.add(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromAssetWithDpi("Icon_mark"
                            + markerSize + ".png")).extraInfo(bundle)
                    .position(mPoiLocations.get(i)));

        }
        return markerList;
    }

    public Object getSuppliedData() {
        return mProvidedData;
    }

    public abstract boolean onPoiClick(int i);

    @Override
    public final boolean onMarkerClick(Marker marker) {
        if (!mOverlayList.contains(marker)) {
            return false;
        }
        if (marker.getExtraInfo() != null) {
            return onPoiClick(marker.getExtraInfo().getInt("index"));
        }
        return false;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        // TODO Auto-generated method stub
        return false;
    }
}