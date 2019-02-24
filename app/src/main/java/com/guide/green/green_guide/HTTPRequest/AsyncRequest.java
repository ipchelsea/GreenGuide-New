package com.guide.green.green_guide.HTTPRequest;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.guide.green.green_guide.HTTPRequest.AbstractRequest.OnRequestResultsListener;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest.RequestProgress;
import com.guide.green.green_guide.HTTPRequest.GetText.AsyncGetText;
import com.guide.green.green_guide.HTTPRequest.ImageGETRequest.AsyncGetImage;
import com.guide.green.green_guide.HTTPRequest.JSON.AsyncGetJsonArray;
import com.guide.green.green_guide.HTTPRequest.JSON.AsyncGetJsonObject;
import com.guide.green.green_guide.HTTPRequest.POSTMultipartData.AsyncPostData;
import com.guide.green.green_guide.Utilities.Review;
import com.guide.green.green_guide.Utilities.AsyncGetReview;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Asynchronously runs a request and calls the appropriate callbacks.
 */
public abstract class AsyncRequest<Result> extends AsyncTask<String, RequestProgress, Result> {
    private OnRequestResultsListener<Result> mCallback;
    protected Exception mException;
    protected AbstractRequest mRequest;
    protected List<Pair<String, String>> mHttpHeaders;

    /**
     * Constructor which sets all of the member variables.
     *
     * @param callback the object to return results to.
     */
    public AsyncRequest(@NonNull OnRequestResultsListener<Result> callback) {
        mCallback = callback;
    }

    /**
     * Sets the headers. The headers are stored as a key-value pair.
     *
     * @param headers the key value pair containing the HTTP header name and its value as the second
     *                parameter.
     */
    public void setHttpHeaders(List<Pair<String, String>> headers) {
        mHttpHeaders = headers;
    }

    /**
     * Returns a request object. This is to allow {@code stop()} to be called as needed.
     */
    public AbstractRequest getRequest() {
        return mRequest;
    }

    /**
     * Worker thread which runs the request in the background.
     *
     * @param strings an array with 1 element. That one element should be the URL pointing
     *                to the location of the image.
     * @return a result of the request.
     */
    @Override
    protected abstract Result doInBackground(String... strings);

    @Override
    protected void onCancelled(Result bmpResult) {
        if (mException != null) {
            mCallback.onError(mException);
        }
        mCallback.onCanceled();
    }

    @Override
    protected void onProgressUpdate(RequestProgress... values) {
        mCallback.onProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Result result) {
        if (mException != null) {
            mCallback.onError(mException);
        } else if (mRequest == null || mRequest.wasStopped()) {
            mCallback.onCanceled();
        } else {
            mCallback.onSuccess(result);
        }
    }

    @Override
    protected void onCancelled() {
        if (mRequest != null) {
            mRequest.stop();
        }
        super.onCancelled();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// Request Runner Methods ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Performs an asynchronous get request to retrieve an the data from.
     *
     * @param url the web address to query.
     * @param callback the object to invoke to notify about the completion of the operation.
     * @return a request object to allow for stopping the request.
     */
    public static AsyncGetJsonArray getJsonArray(String url,
                                                 OnRequestResultsListener<JSONArray> callback) {
        return getJsonArray(url, callback, null);
    }

    /**
     * Performs an asynchronous get request to retrieve an the data from.
     *
     * @param url the web address to query.
     * @param callback the object to invoke to notify about the completion of the operation.
     * @param httpHeaders the key value pair containing the HTTP header name and its value as
     *                    the second parameter.
     * @return a request object to allow for stopping the request.
     */
    public static AsyncGetJsonArray getJsonArray(String url,
                                                 OnRequestResultsListener<JSONArray> callback,
                                                 List<Pair<String, String>> httpHeaders) {
        AsyncGetJsonArray asyncTask = new AsyncGetJsonArray(callback);
        asyncTask.setHttpHeaders(httpHeaders);
        asyncTask.execute(url);
        return asyncTask;
    }

    /**
     * Performs an asynchronous get request to retrieve an the data from.
     *
     * @param url the web address to query.
     * @param callback the object to invoke to notify about the completion of the operation.
     * @return a request object to allow for stopping the request.
     */
    public static AsyncGetJsonObject getJsonObject(String url,
                                                   OnRequestResultsListener<JSONObject> callback) {
        return getJsonObject(url, callback, null);
    }

    /**
     * Performs an asynchronous get request to retrieve an the data from.
     *
     * @param url the web address to query.
     * @param callback the object to invoke to notify about the completion of the operation.
     * @param httpHeaders the key value pair containing the HTTP header name and its value as
     *                    the second parameter.
     * @return a request object to allow for stopping the request.
     */
    public static AsyncGetJsonObject getJsonObject(String url,
                                                   OnRequestResultsListener<JSONObject> callback,
                                                   List<Pair<String, String>> httpHeaders) {
        AsyncGetJsonObject asyncTask = new AsyncGetJsonObject(callback);
        asyncTask.setHttpHeaders(httpHeaders);
        asyncTask.execute(url);
        return asyncTask;
    }

    /**
     * Performs an asynchronous get request to send HTTP post data. The response is also returned
     * and interpreted as text.
     *
     * @param url the web address to send the data to.
     * @param callback the object to invoke to notify about the completion of the operation.
     * @return a request object to allow for stopping the request.
     */
    public static AsyncPostData postMultipartData(String url, List<AbstractFormItem> postData,
                                              OnRequestResultsListener<StringBuilder> callback) {
        return postMultipartData(url, postData, callback, null);
    }

    /**
     * Performs an asynchronous get request to send HTTP post data. The response is also returned
     * and interpreted as text.
     *
     * @param url the web address to send the data to.
     * @param callback the object to invoke to notify about the completion of the operation.
     * @param httpHeaders the key value pair containing the HTTP header name and its value as
     *                    the second parameter.
     * @return a request object to allow for stopping the request.
     */
    public static AsyncPostData postMultipartData(String url, List<AbstractFormItem> postData,
                                              OnRequestResultsListener<StringBuilder> callback,
                                              List<Pair<String, String>> httpHeaders) {
        AsyncPostData asyncTask = new AsyncPostData(postData, callback);
        asyncTask.setHttpHeaders(httpHeaders);
        asyncTask.execute(url);
        return asyncTask;
    }

    /**
     * Performs an asynchronous get request to retrieve text from the remote host.
     *
     * @param url the web address to get the text from.
     * @param callback the object to invoke to notify about the completion of the operation.
     * @return a request object to allow for stopping the request.
     */
    public static AsyncGetText getText(String url,
                                       OnRequestResultsListener<StringBuilder> callback) {
        return getText(url, callback, null);
    }

    /**
     * Performs an asynchronous get request to retrieve text from the remote host.
     *
     * @param url the web address to get the text from.
     * @param callback the object to invoke to notify about the completion of the operation.
     * @param httpHeaders the key value pair containing the HTTP header name and its value as
     *                    the second parameter.
     * @return a request object to allow for stopping the request.
     */
    public static AsyncGetText getText(String url,
                                       OnRequestResultsListener<StringBuilder> callback,
                                       List<Pair<String, String>> httpHeaders) {
        AsyncGetText asyncTask = new AsyncGetText(callback);
        asyncTask.setHttpHeaders(httpHeaders);
        asyncTask.execute(url);
        return asyncTask;
    }

    /**
     * Performs an asynchronous get request to retrieve an array of binary data for an image file,
     * then it converts the bytes to a bitmap object. The supported image files are those which can
     * be decoded by {@code BitmapFactory.decodeByteArray}.
     *
     * @param url   the web address to retrieve the image from.
     * @param callback  the object to invoke to notify about the completion of the operation.
     * @return a request object to allow for stopping the request.
     */
    public static AsyncGetImage getImage(String url, OnRequestResultsListener<Bitmap> callback) {
        AsyncGetImage asyncTask = new AsyncGetImage(callback);
        asyncTask.execute(url);
        return asyncTask;
    }

    /**
     * Gets the reviews stored for a specific point on the Green Guide database.
     *
     * @param lng longitude
     * @param lat latitude
     * @param callback non-null value where the results will be returned to.
     * @return an object managing the background request.
     */
    public static AsyncGetReview getReviewsForPlace(double lng, double lat,
                    @NonNull OnRequestResultsListener<ArrayList<Review>> callback) {
        AsyncGetReview asyncTask = new AsyncGetReview(callback);
        asyncTask.execute(
                "http://www.lovegreenguide.com/map_point_co_app.php?lng=" + lng + "&lat=" + lat);
        return asyncTask;
    }

    public static AsyncGetReview getMyReviews(double lng, double lat,
                                                    @NonNull OnRequestResultsListener<ArrayList<Review>> callback) {
        AsyncGetReview asyncTask = new AsyncGetReview(callback);
        asyncTask.execute(
                "http://www.lovegreenguide.com/map_point_co_app.php?lng=" + lng + "&lat=" + lat);
        return asyncTask;
    }
}