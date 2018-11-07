package com.guide.green.green_guide.Utilities;

import android.os.AsyncTask;
import java.util.ArrayList;

/**
 * Retrieved JSON data from the provided URLs.
 */
public class AsyncGetRequest extends AsyncTask<String, Long, ArrayList<StringBuilder>> {
    private OnResultListener resultListener;
    private ArrayList<Exception> errors = new ArrayList<>();

    /**
     * Callback interface for supplying the results of the request.
     */
    public interface OnResultListener {
        /**
         * Called on the UI thread when the results are ready.
         *
         * @param result the data obtained from the lookup.
         * @param exceptions non-null array with the the errors that were encountered during
         *                   the background processes.
         */
        void onFinish(ArrayList<StringBuilder> result, ArrayList<Exception> exceptions);

        /**
         * Called on the UI thread when the task is canceled.
         *
         * @param result the data obtained from the lookup.
         * @param exceptions non-null array with the the errors that were encountered during
         *                   the background processes.
         */
        void onCanceled(ArrayList<StringBuilder> result, ArrayList<Exception> exceptions);
    }

    /**
     * Default constructor which specified where to return resulting data.
     *
     * @param callback  non-null value which which receive the results and errors of the processing.
     */
    public AsyncGetRequest(OnResultListener callback) {
        super();
        if (callback == null)
            throw new IllegalArgumentException("Callback can't be null");

        resultListener = callback;
    }

    /**
     * Called in the UI thread if {@cdoe cancel(true)} was called on the object.
     *
     * @param result the output from {@code doInBackground}. Probably only partially complete.
     */
    @Override
    public void onCancelled(ArrayList<StringBuilder> result) {
        resultListener.onCanceled(result, errors);
    }

    /**
     * Runs the background process on a non-UI thread.
     *
     * @param strings List of URL's to query.
     * @return The list of result objects with where index of the supplied URL strings corresponds
     *         to the index of the returned value.
     */
    @Override
    protected final ArrayList<StringBuilder> doInBackground(String... strings) {
        ArrayList<StringBuilder> results = new ArrayList<>();

        for (String strUrl : strings) {
            SimpleGETRequest getRequest = new SimpleGETRequest(strUrl) {
                @Override
                public void onError(Exception e) {
                    errors.add(e);
                }

                @Override
                public void onUpdate(StringBuilder sb, long current, long total) {
                    if (isCancelled()) {
                        stop();
                    } else {
                        AsyncGetRequest.this.onProgressUpdate(current, total);
                    }
                }
            };

            StringBuilder sb = getRequest.send();

            if (isCancelled()) {
                break;
            }

            if (sb == null) {
                results.add(null);
            } else {
                results.add(sb);
            }
        }

        return results;
    }

    /**
     * Sends the results and errors to the user supplied callback in to the UI thread.
     *
     * @param results results from {@code doInBackground}.
     */
    @Override
    public final void onPostExecute(ArrayList<StringBuilder> results) {
        resultListener.onFinish(results, errors);
     }
}