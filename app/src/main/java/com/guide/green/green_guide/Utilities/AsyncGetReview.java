package com.guide.green.green_guide.Utilities;

import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;

import com.guide.green.green_guide.HTTPRequest.AbstractRequest;
import com.guide.green.green_guide.HTTPRequest.AsyncRequest;
import com.guide.green.green_guide.HTTPRequest.GetText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Asynchronously runs a POST request and calls the appropriate callbacks to return the result.
 */
public class AsyncGetReview extends AsyncRequest<ArrayList<Review>> implements Serializable {
    /**
     * Constructor which sets all of the member variables.
     *
     * @param callback  the object to return results to.
     */
    public AsyncGetReview(@NonNull AbstractRequest.OnRequestResultsListener<ArrayList<Review>> callback) {
        super(callback);
    }

    /**
     * Worker thread which runs the request in the background. Creates an POSTMultipartData
     * object and overrides some of its classes to insure that callbacks are successfully
     * run.
     *
     * @param strings   an array with 1 element. That one element should be the URL pointing
     *                  to the location of the image.
     * @return  a StringBuilder object with the returned text data.
     */
    @Override
    protected ArrayList<Review> doInBackground(String... strings) {
        GetText request = new GetText(strings[0]) {
            @Override
            protected void onReadUpdate(long current, long total) {
                publishProgress(new RequestProgress(current, total));
            }
            @Override
            public void onError(Exception e) {
                mException = e;
            }
        };
        mRequest = request;
        request.send();
        StringBuilder sb = request.getResult();

        JSONArray jArr = null;
        try {
            jArr = new JSONArray(sb.toString());
        } catch (Exception e) {
            mException = e;
            return null;
        }

        ArrayList<Review> results = new ArrayList<>();
        for (int i = jArr.length() - 1; i >= 0; i--) {
            Review review = new Review();
            try {
                JSONObject jObj = jArr.getJSONObject(i);
                review.imageCount = jObj.getInt("img_count");

                if (!jObj.isNull("review")) {
                    JSONObject subJObj = jObj.getJSONObject("review");
                    review.id = subJObj.getString("id");
                }

                getJsonValuesForObject(jObj, "review", review.location);
                getJsonValuesForObject(jObj, "water", review.waterIssue);
                getJsonValuesForObject(jObj, "solid", review.solidWaste);
                getJsonValuesForObject(jObj, "air", review.airWaste);
                results.add(review);
            } catch (JSONException e) {
                mException = e;
                return null;
            }
        }

        return results;
    }

    private static String decodeHTML(String htmlString) {
        if (Build.VERSION.SDK_INT >= 24) {
            return Html.fromHtml(htmlString , Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(htmlString).toString();
        }
    }

    /**
     * For the specified category, it goes through all of its keys. For all the once with a
     * jsonName, it retrieves stores the value of the {@code JSONObject[jsonName]} in that
     * key.
     *
     * @param jObj the json object which which contains another object corresponding to the
     *             category. E.g., jObj = { 'water': {...}, 'air': {...} }
     * @param objName the name of the category in the object. E.g., 'water'
     * @param category the category to fill the data of
     * @throws JSONException
     */
    private void getJsonValuesForObject(JSONObject jObj, String objName,
                                        Review.ReviewCategory category) throws JSONException {
        if (!jObj.isNull(objName)) {
            JSONObject subJObj = jObj.getJSONObject(objName);
            for (Review.Key key : category.allKeys()) {
                if (key.jsonName != null) {
                    category.set(key, decodeHTML(subJObj.getString(key.jsonName)));
                }
            }
        }
    }
}
