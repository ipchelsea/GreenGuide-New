package com.guide.green.green_guide.Utilities;

import android.support.annotation.NonNull;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
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
        public final String name;       // Non-null value
        public final String address;    // Non-able value
        public final LatLng point;      // Non-able value

        public Location(@NonNull String name, @NonNull String address, @NonNull LatLng point) {
            this.name = name;
            this.point = point;
            this.address = address;
        }

        public Location(@NonNull MapPoi mapPoi) {
            this(mapPoi.getName(), "MAP POI NULL", mapPoi.getPosition());
        }

        public Location(@NonNull SuggestionResult.SuggestionInfo info) {
            this(info.key, info.pt == null ? null : info.pt.toString(), info.pt);
        }

        public Location(@NonNull PoiInfo info) {
            this(info.name, info.address, info.location);
        }

        @Override
        public Type getType() {
            return Type.LOCATION;
        }
    }
}
