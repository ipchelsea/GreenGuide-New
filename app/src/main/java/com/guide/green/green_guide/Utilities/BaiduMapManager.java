package com.guide.green.green_guide.Utilities;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.SuggestionSearch;

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

    /**
     * Takes in a the map object ans uses it to create all of the other objects.
     * @param map the baidu map view.
     */
    public BaiduMapManager(@NonNull MapView map) {
        mapView = map;
        baiduMap = mapView.getMap();
        poiSearch = PoiSearch.newInstance();
        SUGGESTION_SEARCH = SuggestionSearch.newInstance();
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
}
