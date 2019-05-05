package com.guide.green.green_guide.Utilities;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.Html;

import com.guide.green.green_guide.HTTPRequest.AbstractRequest;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest.OnRequestResultsListener;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest.RequestProgress;
import com.guide.green.green_guide.HTTPRequest.AsyncRequest;
import com.guide.green.green_guide.HTTPRequest.GetText;
import com.guide.green.green_guide.HTTPRequest.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Review implements Serializable {
    public Location location = new Location();
    public WaterIssue waterIssue = new WaterIssue();
    public AirWaste airWaste = new AirWaste();
    public SolidWaste solidWaste = new SolidWaste();
    public int imageCount;
    public String id;

    private ArrayList<String> mImagesUrls;

    // Unclear why this needs to be a class variable, and it made it really hard to implement Serializable
    //private JSON.AsyncGetJsonObject mGetImgUrlsRequest;
    final private List<OnRequestResultsListener<List<String>>> mImgListCallbacks = new ArrayList<>();

    /**
     * Thread safe function which retrieves a list of images associated with a review with the
     * set {@code id}.
     *
     * @param callback contains methods which will called on the data retrieval progresses.
     */
    public void getImages(OnRequestResultsListener<List<String>> callback) {
        // Probably no need because the results are always handled in the main thread.
        synchronized (mImgListCallbacks) {
            if (mImagesUrls != null) {
                callback.onSuccess(Collections.unmodifiableList(mImagesUrls));
                return;
            }
            mImgListCallbacks.add(callback);
        }
        JSON.AsyncGetJsonObject mGetImgUrlsRequest = null;
        if (mGetImgUrlsRequest == null) {
            String url = "http://www.lovegreenguide.com/view_app.php?id=" + id;
            mGetImgUrlsRequest = AsyncRequest.getJsonObject(url,
                    new OnRequestResultsListener<JSONObject>() {
                        @Override
                        public void onSuccess(JSONObject jsonObject) {
                            ArrayList<String> imageUrls = new ArrayList<>();
                            JSONArray imgUrls = null;
                            try {
                                imgUrls = jsonObject.getJSONArray("all_image");
                                for (int i = 0; i < imgUrls.length(); i++) {
                                    imageUrls.add(imgUrls.getString(i));
                                }
                                mImagesUrls = imageUrls;
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return;
                            }
                            synchronized (mImgListCallbacks) {
                                for (OnRequestResultsListener<List<String>> c : mImgListCallbacks) {
                                    c.onSuccess(mImagesUrls);
                                }
                                mImgListCallbacks.clear();
                            }
                        }

                        @Override
                        public void onProgress(RequestProgress progress) {
                            synchronized (mImgListCallbacks) {
                                for (OnRequestResultsListener<List<String>> c : mImgListCallbacks) {
                                    c.onProgress(progress);
                                }
                            }
                        }

                        @Override
                        public void onError(Exception error) {
                            synchronized (mImgListCallbacks) {
                                for (OnRequestResultsListener<List<String>> c : mImgListCallbacks) {
                                    c.onError(error);
                                }
                                mImgListCallbacks.clear();
                            }
                        }

                        @Override
                        public void onCanceled() {
                            synchronized (mImgListCallbacks) {
                                for (OnRequestResultsListener<List<String>> c : mImgListCallbacks) {
                                    c.onCanceled();
                                }
                                mImgListCallbacks.clear();
                            }
                        }
                    });
        }
    }

    /**
     * Parent class for all keys. Will be used to make "enums" for children.
     * Will contain static final variables and be un-instantiable in child classes through
     * private constructors.
     *
     * A null postName means that this argument should not be sent in a post request.
     */
    public static abstract class Key implements Serializable {
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
    public static class ReviewCategory implements Serializable {
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

    public static class Location extends ReviewCategory implements Serializable {
        /**
         * Default constructor. Insures the right keyset is used by this object.
         */
        public Location() {
            super(Key.allKeys());
        }

        /**
         * Key class containing keys that only work for the Location object.
         */
        public static class Key extends Review.Key implements Serializable {
            public static final Key WEATHER = new Key(null, null);
            public static final Key OBSERVATION_DATE = new Key(null, "observation_date");
            public static final Key OBSERVATION_TIME = new Key(null, "observation_time");
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

    public static class WaterIssue extends ReviewCategory implements Serializable {
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
            public static final Key NITRATE = new Key(null, "nitrate");
            public static final Key WATER_BODY_OTHER = new Key(null, "water_body_other");
            public static final Key ODOR_OTHER = new Key(null, "water_odor_other");
            public static final Key WATER_COLOR_OTHER = new Key(null, "water_color_other");
            public static final Key FLOAT_TYPE_OTHER = new Key(null, "float_type_other");
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

    public static class AirWaste extends ReviewCategory implements Serializable {
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
            public static final Key SMOKE_COLOR_OTHER = new Key(null, "smoke_color_other");
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
             * @param jsonPostName A unique key name. Uniqueness is not enforced.
             */
            private Key(String jsonPostName) {
                this(jsonPostName, jsonPostName);
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

    public static class SolidWaste extends ReviewCategory implements Serializable {
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
            public static final Key WASTE_TYPE_OTHER = new Key(null, "waste_type_other");
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

    // Had to remove the AsyncGetReview Class and put it in Utilities in order to implement Serializable
}