package com.guide.green.green_guide.Utilities;

import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Review {
    public Location location = new Location();
    public WaterIssue waterIssue = new WaterIssue();
    public AirWaste airWaste = new AirWaste();
    public SolidWaste solidWaste = new SolidWaste();
    public int imageCount;
    public String id;

    /**
     * Parent class for all keys. Will be used to make "enums" for children.
     * Will contain static final variables and be un-instantiable in child classes through
     * private constructors.
     *
     * A null postName means that this argument should not be sent in a post request.
     */
    public static abstract class Key {
        public final String jsonName;
        public final String postName;
        /**
         * Private constructor to insure that no keys can be created outside of this
         * object. Effect: enum like structure when the enum values are the static final values.
         *
         * @param jsonName A unique key name. Uniqueness is not enforced.
         * @param postName A the value that the REST API expects this to be named.
         */
        public Key(String jsonName, String postName) {
            this.jsonName = jsonName;
            this.postName = postName;
        }

        /**
         * @return  list of the keys unique to the parent object.
         */
        public abstract List<? extends Key> getAllKeys();

        @Override
        public String toString() {
            if (jsonName == postName) {
                return jsonName;
            } else {
                return jsonName + "|" + postName;
            }
        }
    }

    /**
     * The parent of {@code Location}, {@code WaterIssue}, {@code SolidWaste}, and {@code AirWaste}.
     * Creates an object similar to a {@code map} where the key can only be from a specific
     * predefined set. Allows for setting and getting values.
     *
     * Provides a way to set the predefined set so each child can have its own keyset.
     */
    public static class ReviewCategory {
        private HashMap<Key, String> attribLookup = new HashMap<>();
        private List<? extends Key> mKeySet;

        /**
         * Default constructor uses STATIC {@code Key} variables from the realized
         * class through a call to {@code createAttribLookup} to create the
         * {@code attribLookup} map of this object.
         */
        public ReviewCategory(List<? extends Key> keySet) {
            mKeySet = keySet;
            for (Key k : mKeySet) {
                attribLookup.put(k, null);
            }
        }

        /**
         * Like a map, gets the value associated with a {@code Key}.
         *
         * @code key    The name of the attribute.
         */
         public String get(Key key) {
            return attribLookup.get(key);
        }

        /**
         * Like a map, associates a {@code Key} to a {@code value}.
         *
         * @code key    The name of the attribute.
         * @code value  The value to set it to.
         * @return      true if the value was set, else false
         */
        public boolean set(Key key, String value) {
            if (attribLookup.containsKey(key)) {
                attribLookup.put(key, value);
                return true;
            } else {
                return false;
            }
        }

        /**
         * @return all of the keys associated with {@code k}
         */
        public List<? extends Key> allKeys() {
            return mKeySet;
        }
    }

    public static class Location extends ReviewCategory {
        /**
         * Default constructor. Insures the right keyset is used by this object.
         */
        public Location() {
            super(Key.allKeys());
        }

        /**
         * Key class containing keys that only work for the Location object.
         */
        public static class Key extends Review.Key {
            public static final Key WEATHER = new Key(null, null);
            public static final Key OBSERVATION_DATE = new Key(null, null);
            public static final Key OBSERVATION_TIME = new Key(null, null);
            public static final Key OTHER_ITEM = new Key(null, "other_item");
            public static final Key SIZE = new Key("size", null);
            public static final Key REASON = new Key("reason", null);
            public static final Key STATUS = new Key("status", null);
            public static final Key MEASURE = new Key("measure", null);
            public static final Key EPA = new Key("epa", null);
            public static final Key REPORT = new Key("report", null);
            public static final Key HELP = new Key("help", null);
            public static final Key TIME = new Key("time", null);
            public static final Key PRODUCT = new Key("product");
            public static final Key INDUSTRY = new Key("industry");
            public static final Key NEWS = new Key("news");
            public static final Key OTHER = new Key("other");
            public static final Key LIVING = new Key("living");
            public static final Key LAND = new Key("land");
            public static final Key WASTE = new Key("waste");
            public static final Key AIR = new Key("air");
            public static final Key WATER = new Key("water");
            public static final Key RATING = new Key("rating");
            public static final Key LAT = new Key("lat");
            public static final Key LNG = new Key("lng");
            public static final Key REVIEW = new Key("review");
            public static final Key CITY = new Key("city");
            public static final Key ADDRESS = new Key("address");
            public static final Key COMPANY = new Key("company");
            private static ArrayList<Key> mKeys;

            /**
             * Private constructor to insure that no keys can be created outside of this
             * object. Effect: enum like structure when the enum values are the static final values.
             *
             * @param jsonName A unique key name. Uniqueness is not enforced.
             */
            private Key(String jsonName) {
                this(jsonName, jsonName);
            }

            /**
             * Private constructor to insure that no keys can be created outside of this
             * object. Effect: enum like structure when the enum values are the static final values.
             *
             * @param jsonName A unique key name. Uniqueness is not enforced.
             * @param postName A the value that the REST API expects this to be named.
             */
            public Key(String jsonName, String postName) {
                super(jsonName, postName);
                if (mKeys == null) { mKeys = new ArrayList<>(); }
                mKeys.add(this);
            }

            /**
             * @return  list of the keys unique to the parent object.
             */
            @Override
            public List<Key> getAllKeys() { return allKeys(); }

            public static List<Key> allKeys() { return Collections.unmodifiableList(mKeys); }
        }
    }


    public static class WaterIssue extends ReviewCategory {
        /**
         * Default constructor. Insures the right keyset is used by this object.
         */
        public WaterIssue() {
            super(Key.allKeys());
        }

        /**
         * Key class containing keys that only work for the  object.
         */
        public static class Key extends Review.Key {
            public static final Key NITRATE = new Key(null, null);
            public static final Key WATER_BODY_OTHER = new Key(null, null);
            public static final Key ODOR_OTHER = new Key(null, null);
            public static final Key WATER_COLOR_OTHER = new Key(null, null);
            public static final Key FLOAT_TYPE_OTHER = new Key(null, null);
            public static final Key ARSENIC = new Key("Arsenic", "As");
            public static final Key CADMIUM = new Key("Cd");
            public static final Key LEAD = new Key("Pb");
            public static final Key MERCURY = new Key("Hg");
            public static final Key PHOSPHORUS = new Key("TP");
            public static final Key AMMONIUM = new Key("NH4");
            public static final Key SOLID = new Key("TS");
            public static final Key ORGANIC_CARBON = new Key("TOC");
            public static final Key CHEM_OXYGEN_DEMAND = new Key("COD");
            public static final Key BIO_OXYGEN_DEMAND = new Key("BOD");
            public static final Key TURB_PARAMS = new Key("TurbParams", "Turbidity");
            public static final Key PH = new Key("pH");
            public static final Key DISSOLVED_OXYGEN = new Key("DO");
            public static final Key FLOAT_TYPE = new Key("Floats", "floatType");
            public static final Key CHECK_FLOAT = new Key("CheckFloat", "float");
            public static final Key TURB_SCORE = new Key("TurbScore", "turbRate");
            public static final Key ODOR = new Key("Odor", "WaterOdor");
            public static final Key WATER_COLOR = new Key("WaterColor");
            public static final Key WATER_BODY = new Key("WaterType");
            private static ArrayList<Key> mKeys;

            /**
             * Private constructor to insure that no keys can be created outside of this
             * object. Effect: enum like structure when the enum values are the static final values.
             *
             * @param jsonName A unique key name. Uniqueness is not enforced.
             */
            private Key(String jsonName) {
                this(jsonName, jsonName);
            }

            /**
             * Private constructor to insure that no keys can be created outside of this
             * object. Effect: enum like structure when the enum values are the static final values.
             *
             * @param jsonName A unique key name. Uniqueness is not enforced.
             * @param postName A the value that the REST API expects this to be named.
             */
            public Key(String jsonName, String postName) {
                super(jsonName, postName);
                if (mKeys == null) { mKeys = new ArrayList<>(); }
                mKeys.add(this);
            }

            /**
             * @return  list of the keys unique to the parent object.
             */
            @Override
            public List<Key> getAllKeys() { return allKeys(); }

            public static List<Key> allKeys() { return Collections.unmodifiableList(mKeys); }
        }
    }

    public static class AirWaste extends ReviewCategory {
        /**
         * Default constructor. Insures the right keyset is used by this object.
         */
        public AirWaste() {
            super(Key.allKeys());
        }

        /**
         * Key class containing keys that only work for the  object.
         */
        public static class Key extends Review.Key {
            public static final Key ODOR_OTHER = new Key(null, null);
            public static final Key SMOKE_COLOR_OTHER = new Key(null, null);
            public static final Key CO = new Key("CO");
            public static final Key NOX = new Key("NOx");
            public static final Key SOX = new Key("SOx");
            public static final Key O3 = new Key("O3");
            public static final Key PM10 = new Key("PM10");
            public static final Key PM2_5 = new Key("PM2_5", "PM2.5");
            public static final Key PHYSICAL_PROBS = new Key("symptomDescr");
            public static final Key SYMPTOM = new Key("Symptom");
            public static final Key SMOKE_COLOR = new Key("SmokeColor");
            public static final Key SMOKE_CHECK = new Key("Smoke_Check", "SmokeCheck");
            public static final Key ODOR = new Key("Odor", "AirOdor");
            public static final Key VISIBILITY = new Key("Visibility");
            private static ArrayList<Key> mKeys;

            /**
             * Private constructor to insure that no keys can be created outside of this
             * object. Effect: enum like structure when the enum values are the static final values.
             *
             * @param jsonName A unique key name. Uniqueness is not enforced.
             */
            private Key(String jsonName) {
                this(jsonName, jsonName);
            }

            /**
             * Private constructor to insure that no keys can be created outside of this
             * object. Effect: enum like structure when the enum values are the static final values.
             *
             * @param jsonName A unique key name. Uniqueness is not enforced.
             * @param postName A the value that the REST API expects this to be named.
             */
            public Key(String jsonName, String postName) {
                super(jsonName, postName);
                if (mKeys == null) { mKeys = new ArrayList<>(); }
                mKeys.add(this);
            }

            /**
             * @return  list of the keys unique to the parent object.
             */
            @Override
            public List<Key> getAllKeys() {
                return allKeys();
            }

            public static List<Key> allKeys() {
                return Collections.unmodifiableList(mKeys);
            }
        }
    }

    public static class SolidWaste extends ReviewCategory {
        /**
         * Default constructor. Insures the right keyset is used by this object.
         */
        public SolidWaste() {
            super(Key.allKeys());
        }

        /**
         * Key class containing keys that only work for the object.
         */
        public static class Key extends Review.Key {
            public static final Key ODOR_OTHER = new Key(null, null);
            public static final Key WASTE_TYPE_OTHER = new Key(null, null);
            public static final Key MEASUREMENTS = new Key("Measurements", "WasteMeasure");
            public static final Key ODOR = new Key("Odor", "WasteOdor");
            public static final Key AMOUNT = new Key("Amount", "WasteAmount");
            public static final Key WASTE_TYPE = new Key("WasteType");
            private static ArrayList<Key> mKeys;

            /**
             * Private constructor to insure that no keys can be created outside of this
             * object. Effect: enum like structure when the enum values are the static final values.
             *
             * @param jsonName A unique key name. Uniqueness is not enforced.
             */
            private Key(String jsonName) {
                this(jsonName, jsonName);
            }

            /**
             * Private constructor to insure that no keys can be created outside of this
             * object. Effect: enum like structure when the enum values are the static final values.
             *
             * @param jsonName A unique key name. Uniqueness is not enforced.
             * @param postName A the value that the REST API expects this to be named.
             */
            public Key(String jsonName, String postName) {
                super(jsonName, postName);
                if (mKeys == null) { mKeys = new ArrayList<>(); }
                mKeys.add(this);
            }

            /**
             * @return  list of the keys unique to the parent object.
             */
            @Override
            public List<Key> getAllKeys() { return allKeys(); }

            public static List<Key> allKeys() { return Collections.unmodifiableList(mKeys); }
        }
    }

    public interface Results {
        /**
         * Provided the returns of the query.
         *
         * @param reviews   non-null value.
         */
        public void onSuccess(ArrayList<Review> reviews);

        /**
         *  Provides the exception that messed up getting the results.
         *
         * @param e non-null value.
         */
        public void onError(Exception e);

        /**
         * Called every time more data is obtained from the webserver.
         *
         * @param current   the total amount of bytes read so far from the server.
         * @param total the expected total number of bytes that will be read from the server.
         *              if unknown, this value is set to -1.
         */
        public void onUpdate(long current, long total);

        /**
         * Called when the background task is canceled.
         */
        public void onCanceled();
    }

    /**
     * Gets the reviews stored for a specific point on the Green Guide database.
     *
     * @param lng longitude
     * @param lat latitude
     * @param callback non-null value where the results will be returned to.
     * @return  the object managing the background request.
     */
    public static AsyncJSONArray getReviewsForPlace(double lng, double lat,
                                                    @NonNull final Results callback) {

        final ReviewAsyncJSONArrayResult jsonCallback = new ReviewAsyncJSONArrayResult(callback);

        AsyncJSONArray aj = new AsyncJSONArray(jsonCallback) {
            @Override
            protected void onProgressUpdate(Long... values) {
                jsonCallback.REVIEWS_CALLBACK.onUpdate(values[0], values[1]);
            }
        };

        String url = "http://www.lovegreenguide.com/map_point_co_app.php?lng=" + lng + "&lat=" +lat;
        aj.execute(url);
        return  aj;
    }

    private static class ReviewAsyncJSONArrayResult implements AsyncJSONArray.OnAsyncJSONArrayResultListener {
        public final Results REVIEWS_CALLBACK;

        private static String decodeHTML(String htmlString) {
            if (Build.VERSION.SDK_INT >= 24) {
                return Html.fromHtml(htmlString , Html.FROM_HTML_MODE_LEGACY).toString();
            } else {
                return Html.fromHtml(htmlString).toString();
            }
        }

        ReviewAsyncJSONArrayResult(Results callback) {
            REVIEWS_CALLBACK = callback;
        }

        @Override
        public void onCanceled(ArrayList<JSONArray> jArray, ArrayList<Exception> exceptions) {
            REVIEWS_CALLBACK.onCanceled();
        }

        private void getJsonValuesForObject(JSONObject jObj, String objName,
                                            ReviewCategory category) throws JSONException {
            if (!jObj.isNull(objName)) {
                JSONObject subJObj = jObj.getJSONObject(objName);
                for (Key key : category.allKeys()) {
                    if (key.jsonName != null) {
                        category.set(key, decodeHTML(subJObj.getString(key.jsonName)));
                    }
                }
            }
        }

        @Override
        public void onFinish(ArrayList<JSONArray> jArray, ArrayList<Exception> exceptions) {
            if (!exceptions.isEmpty() || jArray.isEmpty() || jArray.get(0) == null) {
                REVIEWS_CALLBACK.onError(exceptions.get(0));
                return;
            }

            ArrayList<Review> results = new ArrayList<>();
            JSONArray jArr = jArray.get(0);
            for (int i = jArr.length() - 1; i >= 0; i--) {
                Review review = new Review();
                try {
                    JSONObject jObj = jArr.getJSONObject(i);
                    review.imageCount = jObj.getInt("img_count");
                    getJsonValuesForObject(jObj, "review", review.location);
                    getJsonValuesForObject(jObj, "water", review.waterIssue);
                    getJsonValuesForObject(jObj, "solid", review.solidWaste);
                    getJsonValuesForObject(jObj, "air", review.airWaste);
                    results.add(review);
                } catch (JSONException e) {
                    REVIEWS_CALLBACK.onError(e);
                    return;
                }
            }
            REVIEWS_CALLBACK.onSuccess(results);
        }
    }
}
