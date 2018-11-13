package com.guide.green.green_guide.Utilities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.sug.SuggestionResult;

/**
 * Abstract class describing a suggestion and providing a way to know what its child class is.
 * Two types of suggestions exist, TextSuggestion and Location suggestion.
 */
public abstract class BaiduSuggestion {
    public enum Type {
        TEXT_SUGGESTION, LOCATION
    }

    /**
     * Provides a way to know what type the child class is.
     *
     * @return one of the two types
     */
    public abstract Type getType();

    /**
     * Concrete class describing a single auto-complete text.
     */
    public static class TextSuggestion extends BaiduSuggestion {
        public final String suggestion;

        public TextSuggestion(String suggestion) {
            this.suggestion = suggestion;
        }

        @Override
        public Type getType() {
            return Type.TEXT_SUGGESTION;
        }
    }

    /**
     * Concrete class describing a suggested location.
     */
    public static class Location extends BaiduSuggestion {
        //TODO: Make actually non-null
        public final String uid;        // Non-null value

        public final String name;       // Non-null value
        public final LatLng point;      // Non-null value
        public final String address;    // Null-able value
        public final String city;       // Null-able value

        public static BaiduSuggestion.Location merge(BaiduSuggestion.Location a, BaiduSuggestion.Location b) {
            return new BaiduSuggestion.Location(
                    a.name == null ? b.name : a.name,
                    a.point == null ? b.point : a.point,
                    a.address == null ? b.address : a.address,
                    a.city == null ? b.city : a.city,
                    a.uid == null ? b.uid : a.uid);
        }

        public Location(@NonNull String name, @NonNull LatLng point, @Nullable String address,
                        @Nullable String city, @Nullable String uid) {
            this.name = name;
            this.point = point;
            this.address = address;
            this.city = city;
            this.uid = uid;
        }

        public Location(@NonNull MapPoi mapPoi) {
            this(mapPoi.getName(), mapPoi.getPosition(), null, null, mapPoi.getUid());
        }

        public Location(@NonNull SuggestionResult.SuggestionInfo info) {
            this(info.key, info.pt, info.address, info.city, info.uid);
        }

        public Location(@NonNull PoiInfo info) {
            this(info.name, info.location, info.address, info.city, info.uid);
        }

        public Location(@NonNull PoiDetailResult info) {
            this(info.name, info.location, info.address, null, info.uid);
        }
        @Override
        public Type getType() {
            return Type.LOCATION;
        }
    }
}
