package com.guide.green.green_guide.Utilities;

import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;

/**
 * Retrieved JSON data from the provided URLs.
 */
public class AsyncJSONArray extends AsyncTask<String, Long, ArrayList<JSONArray>> {
    private OnAsyncJSONArrayResultListener resultListener;
    private ArrayList<Exception> errors = new ArrayList<>();

    /**
     * Callback interface for supplying the results of the lookup.
     */
    public interface OnAsyncJSONArrayResultListener {
        /**
         * Called on the UI thread when the results are ready.
         *
         * @param jArray    non-null array with the data where each index of jArray[position] matches to
         *                  the supplied URL in the same index.
         * @param exceptions    non-null array with the the errors that were encountered during
         *                      the background processes.
         */
        void onFinish(ArrayList<JSONArray> jArray, ArrayList<Exception> exceptions);

        /**
         * Called on the UI thread when the task is canceled.
         *
         * @param jArray    non-null array with the data where each index of jArray[position] matches to
         *                  the supplied URL in the same index.
         * @param exceptions    non-null array with the the errors that were encountered during
         *                      the background processes.
         */
        void onCanceled(ArrayList<JSONArray> jArray, ArrayList<Exception> exceptions);
    }

    /**
     * Default constructor which specified where to return resulting data.
     *
     * @param callback  non-null value which which receive the results and errors of the processing.
     */
    public AsyncJSONArray(OnAsyncJSONArrayResultListener callback) {
        super();
        if (callback == null)
            throw new IllegalArgumentException("Callback can't be null");

        resultListener = callback;
    }

    /**
     * Called in the UI thread if {@cdoe cancel(true)} was called on the object.
     *
     * @param result    the output from {@code doInBackground}. Probably only partially complete.
     */
    @Override
    public void onCancelled(ArrayList<JSONArray> result) {
        resultListener.onCanceled(result, errors);
    }

    /**
     * Runs the background process on a non-UI thread.
     *
     * @param strings   List of URL's which return a JSON array e.g., "[...]".
     * @return  The list of JSON objects with where {@code strings[position]} => {@return[i]}
     */
    @Override
    protected final ArrayList<JSONArray> doInBackground(String... strings) {
        ArrayList<JSONArray> result = new ArrayList<>();

        for (String strUrl : strings) {
            SimpleTextGETRequest getRequest = new SimpleTextGETRequest(strUrl) {
                @Override
                public void onError(Exception e) {
                    errors.add(e);
                }

                @Override
                public void onReadUpdate(long current, long total) {
                    if (isCancelled()) {
                        stop();
                    } else {
                        AsyncJSONArray.this.onProgressUpdate(current, total);
                    }
                }
            };

            getRequest.send();
            StringBuilder sb = getRequest.getResult();

            if (isCancelled()) {
                break;
            }

            if (sb == null) {
                result.add(null);
            } else {
                try {
                    String s = sb.toString();
                    result.add(new JSONArray(s));
                } catch (JSONException e) {
                    errors.add(e);
                }
            }
        }

        return result;
    }

    /**
     * Sends the results and errors to the user supplied callback in to the UI thread.
     *
     * @param json  results from {@code doInBackground}.
     */
    @Override
    public final void onPostExecute(ArrayList<JSONArray> json) {
        resultListener.onFinish(json, errors);
     }
}