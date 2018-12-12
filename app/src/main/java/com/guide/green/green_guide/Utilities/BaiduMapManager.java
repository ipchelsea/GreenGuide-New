package com.guide.green.green_guide.Utilities;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest;
import com.guide.green.green_guide.HTTPRequest.AsyncRequest;
import com.guide.green.green_guide.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This class stores the map-related variables who's lifetime is closely related to that of
 * the main Activity. For example, the {@code poiSearch} variable needs to be destroyed at the
 * end of the main activities life, the {@code MAP_ACTIVITY} need to be paused when the activity is
 * pause, and so forth. This class acts as a way for other objects to interact with the map-related
 * variables it stores.
 */
public class BaiduMapManager {
    public final MapView mapView;
    public final BaiduMap baiduMap;
    public final PoiSearch poiSearch;
    public final SuggestionSearch SUGGESTION_SEARCH;
    public OnLocationClickListener mLocationClickListener;
    private GreenGuideMarkers mGreenGuideMarkers;
    public interface OnLocationClickListener {
        void onLocationClick(BaiduSuggestion.Location location);
    }

    public void setOnLocationClickListener(OnLocationClickListener listener) {
        mLocationClickListener = listener;
    }

    /**
     * Takes in a the map object ans uses it to create all of the other objects.
     * @param map the baidu map view.
     */
    public BaiduMapManager(@NonNull MapView map) {
        mapView = map;
        baiduMap = mapView.getMap();
        poiSearch = PoiSearch.newInstance();
        SUGGESTION_SEARCH = SuggestionSearch.newInstance();
        getGreenGuidePoints();
    }

    /**
     * Acts like an enum by instantiating singletons for each type. These singletons known
     * the integer values of the Baidu type's they correspond to and can be compared by
     * their addresses.
     */
    public static class MapType {
        public final int BAIDU_TYPE;
        private MapType(int type) { BAIDU_TYPE = type; }
        public static final MapType NORMAL = new MapType(BaiduMap.MAP_TYPE_NORMAL);
        public static final MapType SATELLITE = new MapType(BaiduMap.MAP_TYPE_SATELLITE);
    }

    /**
     * Sets the map type to an satellite view or a roads view.
     *
     * @param type  Specifies the type.
     * @return  true if the type was set, false otherwise.
     */
    public boolean setMapType(MapType type) {
        if (type != null) {
            baiduMap.setMapType(type.BAIDU_TYPE);
            return true;
        }
        return false;
    }

    /**
     * Adds an a marker to the map and sets it icon to the resource id provided.
     * @param options The markers options which should at least specify the location.
     * @param resource A resource ID for a drawable.
     * @return The created marker.
     */
    public Overlay addMarker(@NonNull MarkerOptions options, @DrawableRes int resource) {
        return baiduMap.addOverlay(options.icon(BitmapDescriptorFactory.fromResource(resource)));
    }

    /**
     * Moves the center of the map to the specified location.
     *
     * @param location the location to move the map to.
     */
    public void moveTo(LatLng location) {
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(location));
    }

    // TODO: Figure out who the zoom level works (Fill in the ??)
    /**
     * Zooms in the map to the specified level.
     *
     * @param zoomLevel A number between [??], inclusive of both, where a higher number results
     *                  in a greater magnification.
     */
    public void zoomTo(int zoomLevel) {
        baiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(zoomLevel));
    }

    // TODO: Figure out who the zoom level works
    /**
     * Moves the center of the map to the specified location while zooming to the specified level.
     *
     * @param location the location to move the map to.
     * @param zoomLevel A number between [??], inclusive of both, where a higher number results
     *                  in a greater magnification.
     */
    public void moveAndZoomTo(LatLng location, int zoomLevel) {
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(location, zoomLevel));
    }

    /**
     * Hides or shows the green guide marker points.
     *
     * @param isVisible true will show the markers, false will hide the markers.
     */
    public void setPinsVisible(boolean isVisible) {
        if (mGreenGuideMarkers != null) {
            mGreenGuideMarkers.setVisible(isVisible);
        }
    }

    public void onResume() {
        mapView.onResume();
    }

    public void onPause() {
        mapView.onPause();
    }

    /**
     * Must be called when the object is no longer in use.
     */
    public void onDestroy() {
        poiSearch.destroy();
        mapView.onDestroy();
        SUGGESTION_SEARCH.destroy();
    }


    private void getGreenGuidePoints() {
        String url = "http://www.lovegreenguide.com/map_point_app.php?lng=112.578658&lat=28.247855";
        AsyncRequest.getJsonArray(url, new AbstractRequest.OnRequestResultsListener<JSONArray>() {
                @Override
                public void onError(Exception error) {
                    Log.e("--onError_GetPoints", error.toString());
                    error.printStackTrace();
                }

                @Override
                public void onSuccess(JSONArray jsonArray) {
                    final ArrayList<GreenGuideLocation> locations = new ArrayList<>();
                    if (jsonArray == null) {
                        return;
                    }
                    try {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jObj = jsonArray.getJSONObject(i);
                            String lng = jObj.getString("lng");
                            String lat = jObj.getString("lat");
                            String avrg = jObj.getString("avg_r");
                            String company = jObj.getString("company");
                            String address = jObj.getString("address");
                            String city = jObj.getString("city");
                            if (!lng.equals("") && !lat.equals("") && !avrg.equals("")) {
                                GreenGuideLocation gL = new GreenGuideLocation();
                                gL.averageRating = Float.parseFloat(avrg);
                                gL.point = new LatLng(Double.parseDouble(lat),
                                        Double.parseDouble(lng));
                                gL.companyName = company;
                                gL.address = address;
                                gL.city = city;
                                locations.add(gL);
                            }
                        }
                    } catch (JSONException e) { /* Do Nothing */ }

                    mGreenGuideMarkers = new GreenGuideMarkers() {
                        @Override
                        public boolean onPoiClick(int index) {
                            GreenGuideLocation ggLocation = locations.get(index);
                            if (mLocationClickListener != null) {
                                BaiduSuggestion.Location pos = new BaiduSuggestion.Location(
                                        ggLocation.companyName, ggLocation.point,
                                        ggLocation.address, ggLocation.city, null);
                                mLocationClickListener.onLocationClick(pos);
                            }
                            return false;
                        }
                    };
                    mGreenGuideMarkers.setData(locations);
                }
            });
    }

    /**
     * Class which
     */
    private abstract class GreenGuideMarkers implements BaiduMap.OnMarkerClickListener {
        private ArrayList<Overlay> mOverlayList;

        public GreenGuideMarkers() {
            baiduMap.setOnMarkerClickListener(this);
        }

        public abstract boolean onPoiClick(int index);

        @Override
        public boolean onMarkerClick(Marker marker) {
            if (!mOverlayList.contains(marker)) {
                return false;
            }
            if (marker.getExtraInfo() != null) {
                return onPoiClick(marker.getExtraInfo().getInt("index"));
            }
            return false;
        }

        public void setData(ArrayList<GreenGuideLocation> greenGuideLocations) {
            mOverlayList = new ArrayList<>();
            for (int i = 0; i < greenGuideLocations.size(); i++) {
                GreenGuideLocation location = greenGuideLocations.get(i);
                Bundle bundle = new Bundle();
                bundle.putInt("index", i);
                MarkerOptions option = new MarkerOptions()
                        .position(location.point)
                        .extraInfo(bundle);
                mOverlayList.add(addMarker(option,
                        getColoredMarkerFromRating(location.averageRating)));
            }
        }

        public void setVisible(boolean isVisible) {
            for (Overlay overlay : mOverlayList) {
                overlay.setVisible(isVisible);
            }
        }
    }

    /**
     * Struct to store information about a specific location obtained when creating
     * the initial markers of all the places with a review.
     */
    private static class GreenGuideLocation {
        public float averageRating;
        public String companyName;
        public String address;
        public String city;
        public LatLng point;
    }

    /**
     *
     * @param rating
     * @return
     */
    private static int roundRating(float rating) {
        if (rating < 0) {
            return  -Math.round(-rating);
        } else {
            return Math.round(rating);
        }
    }

    /***
     * Returns a resource ID for the appropriately colored marker for the supplied rating.
     *
     * @param averageRating A number in the set [-3,3] inclusive of both.
     * @return A resource Id.
     */
    private static int getColoredMarkerFromRating(float averageRating) {
        int drawableId;
        switch (roundRating(averageRating)) {
            case -3: drawableId = R.drawable.icon_markg_red; break;
            case -2: drawableId = R.drawable.icon_markg_orange; break;
            case -1: drawableId = R.drawable.icon_markg_yellow; break;
            case 0: drawableId = R.drawable.icon_markg_white; break;
            case 1: drawableId = R.drawable.icon_markg_aqua; break;
            case 2: drawableId = R.drawable.icon_markg_lime; break;
            default: drawableId = R.drawable.icon_markg_green; break;
        }
        return drawableId;
    }
}
