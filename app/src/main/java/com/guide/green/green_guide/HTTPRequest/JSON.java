package com.guide.green.green_guide.HTTPRequest;

import android.support.annotation.NonNull;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest.OnRequestResultsListener;
import org.json.JSONArray;
import org.json.JSONObject;


public class JSON {
    /**
     * Asynchronously runs a POST request and calls the appropriate callbacks to return the result.
     */
    public static class AsyncGetJsonArray extends AsyncRequest<JSONArray> {
        /**
         * Constructor which sets all of the member variables.
         *
         * @param callback  the object to return results to.
         */
        public AsyncGetJsonArray(@NonNull OnRequestResultsListener<JSONArray> callback) {
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
        protected JSONArray doInBackground(String... strings) {
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

            JSONArray jArray = null;
            try {
                jArray = new JSONArray(sb.toString());
            } catch (Exception e) {
                mException = e;
            }
            return jArray;
        }
    }

    /**
     * Asynchronously runs a POST request and calls the appropriate callbacks to return the result.
     */
    public static class AsyncGetJsonObject extends AsyncRequest<JSONObject> {
        /**
         * Constructor which sets all of the member variables.
         *
         * @param callback  the object to return results to.
         */
        public AsyncGetJsonObject(@NonNull OnRequestResultsListener<JSONObject> callback) {
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
        protected JSONObject doInBackground(String... strings) {
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

            JSONObject jObject = null;
            try {
                jObject = new JSONObject(sb.toString());
            } catch (Exception e) {
                mException = e;
            }
            return jObject;
        }
    }
}
