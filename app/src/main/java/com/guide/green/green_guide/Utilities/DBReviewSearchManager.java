package com.guide.green.green_guide.Utilities;


import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.model.LatLng;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest;
import com.guide.green.green_guide.HTTPRequest.AsyncRequest;
import com.guide.green.green_guide.PoiOverlay;
import com.guide.green.green_guide.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles showing and hiding the supplied {@code container} (which holds everything in the bottom
 * sheet when the results are being displayed). It also handles asynchronously searching for a
 * supplied search term. A new instance of this object does not need to be created for every search.
 */
public class DBReviewSearchManager {
    public final ViewGroup container;
    private ViewGroup mListContainer;
    private DBItemsOverlay mDBItemsOverlay;
    private BottomSheetManager mBtmSheetManager;
    private List<BaiduSuggestion.Location> mLocations = new ArrayList<>();
    public DBReviewSearchManager(@NonNull ViewGroup container, @NonNull ViewGroup listContainer) {
        this.container = container;
        mListContainer = listContainer;
    }

    public void setBtmSheetManager(@NonNull BottomSheetManager btmSheet) {
        mBtmSheetManager = btmSheet;
    }

    public void searchFor(final BaiduMapManager mapManager, String query) {
        mListContainer.removeAllViews();
        mLocations.clear();
        String url = "http://lovegreenguide.com/search-all_app.php?s_company=";
        try {
            url += URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        AsyncRequest.getJsonArray(url, new AbstractRequest.OnRequestResultsListener<JSONArray>() {
            @Override
            public void onSuccess(JSONArray jsonArray) {
                int len = jsonArray.length();
                for (int i = 0; i < len; i++) {
                    try {
                        ViewGroup child = createChildReview(jsonArray.getJSONObject(i), i + 1);
                        mListContainer.addView(child);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mDBItemsOverlay = new DBItemsOverlay(mapManager.baiduMap);
                mapManager.baiduMap.setOnMarkerClickListener(mDBItemsOverlay);
                mDBItemsOverlay.setData(mLocations);
                mDBItemsOverlay.addToMap();
                mDBItemsOverlay.zoomToSpan();
            }
        });
    }

    /**
     * Creates a list element which at the top has the icon used to show this location on the map
     * followed by the the name, address, city, industry, product, ratting, and number of reviews
     * for the company.
     *
     * @param jObj the object returned by the BAIDU REST API
     * @return the ViewGroup displaying all of the information for this location
     * @throws JSONException thrown when obtaining an item that should be contained by jObj
     */
    private ViewGroup createChildReview(JSONObject jObj, int markerNumber) throws JSONException {
        ViewGroup childRoot = (ViewGroup) LayoutInflater.from(mListContainer.getContext()).inflate(
                R.layout.bottomsheet_db_review_search_result, mListContainer, false);
        TextView companyView = childRoot.findViewById(R.id.db_search_result_company);
        TextView addressView = childRoot.findViewById(R.id.db_search_result_address);
        TextView cityView = childRoot.findViewById(R.id.db_search_result_city);
        TextView industryView = childRoot.findViewById(R.id.db_search_result_industry);
        TextView productView = childRoot.findViewById(R.id.db_search_result_product);
        TextView ratingView = childRoot.findViewById(R.id.db_search_result_rating);
        Button reviewsView = childRoot.findViewById(R.id.db_search_result_reviews);
        ImageView iconView = childRoot.findViewById(R.id.db_search_result_icon);

        String imgName = "Icon_mark" + markerNumber + ".png";
        Bitmap bmp = BitmapDescriptorFactory.fromAssetWithDpi(imgName).getBitmap();
        iconView.setImageBitmap(bmp);


        LatLng location = new LatLng(Double.parseDouble(jObj.getString("lat")),
                Double.parseDouble(jObj.getString("lng")));

        String company = jObj.getString("company");
        String city = jObj.getString("city");
        String address = jObj.getString("address");

        final BaiduSuggestion.Location suggestion = new BaiduSuggestion.Location(
                company, location, address, city, null);
        mLocations.add(suggestion);

        companyView.setText(company);
        addressView.setText(String.format("Company Address: %s", address));
        cityView.setText(String.format("City: %s", city));
        industryView.setText(String.format("Industry: %s", jObj.getString("industry")));
        productView.setText(String.format("Product: %s", jObj.getString("product")));
        ratingView.setText(String.format("Rating: %s", jObj.getString("avg_r")));
        reviewsView.setText(String.format("%s Reviews", jObj.getString("num_r")));
        reviewsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBtmSheetManager.getReviewFor(suggestion);
            }
        });
        return childRoot;
    }

    /**
     * An overlay which handles clicks to it by opening the review of the item it represents.
     */
    private class DBItemsOverlay extends PoiOverlay {
        public DBItemsOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
        @Override
        public boolean onPoiClick(int index) {
            mBtmSheetManager.getReviewFor(mLocations.get(index));
            return true;
        }
    }

    /**
     * Shows or hides all of the points added to the map by this object.
     *
     * @param isVisible
     */
    public void setVisibility(boolean isVisible) {
        if (mDBItemsOverlay != null) {
            mDBItemsOverlay.setVisibility(isVisible);
        }
    }

    /**
     * Removes all of the points added to the map by this object.
     */
    public void remove() {
        if (mDBItemsOverlay != null) {
            mDBItemsOverlay.removeFromMap();
        }
    }
}
