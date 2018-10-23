package com.guide.green.green_guide.Utilities;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;

public class BaiduMapManager {
    public final MapView MAP_VIEW;
    public final BaiduMap BAIDU_MAP;
    public final PoiSearch POI_SEARCH;
    public final SuggestionSearch SUGGESTION_SEARCH;

    public BaiduMapManager(@NonNull MapView map) {
        MAP_VIEW = map;
        BAIDU_MAP = MAP_VIEW.getMap();
        POI_SEARCH = PoiSearch.newInstance();
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
            BAIDU_MAP.setMapType(type.BAIDU_TYPE);
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
        return BAIDU_MAP.addOverlay(options.icon(BitmapDescriptorFactory.fromResource(resource)));
    }

    public void moveTo(LatLng location) {
        BAIDU_MAP.animateMapStatus(MapStatusUpdateFactory.newLatLng(location));
    }

    public void zoomTo(int zoomLevel) {
        BAIDU_MAP.animateMapStatus(MapStatusUpdateFactory.zoomTo(zoomLevel));
    }

    public void moveAndZoomTo(LatLng location, int zoomLevel) {
        BAIDU_MAP.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(location, zoomLevel));
    }

    public void onResume() {
        MAP_VIEW.onResume();
    }

    public void onPause() {
        MAP_VIEW.onPause();
    }

    /**
     * Must be called when the object is no longer in use.
     */
    public void onDestroy() {
        POI_SEARCH.destroy();
        SUGGESTION_SEARCH.destroy();
        MAP_VIEW.onDestroy();
    }

    public static class BaiduSuggestion {
        public final @NonNull String name;
        public final LatLng point;

        public BaiduSuggestion(@NonNull String name) {
            this.name = name;
            this.point = null;
        }
        public BaiduSuggestion(@NonNull MapPoi mapPoi) {
            name = mapPoi.getName();
            point = mapPoi.getPosition();
        }
        public BaiduSuggestion(@NonNull SuggestionResult.SuggestionInfo info) {
            this.name = info.key;
            this.point = info.pt;
        }
        public BaiduSuggestion(@NonNull PoiInfo info) {
            this.name = info.name;
            this.point = info.location;
        }
        @Override
        public String toString() {
            if (point == null) {
                return " " + name;
            } else {
                return "\uD83D\uDCCD" + name;
            }
        }
    }
}
