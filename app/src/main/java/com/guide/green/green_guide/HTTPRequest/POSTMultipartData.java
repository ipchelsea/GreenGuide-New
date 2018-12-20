package com.guide.green.green_guide.HTTPRequest;

import android.support.annotation.NonNull;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Provides a way to run a single GET request.
 */
public class POSTMultipartData extends AbstractPOSTRequest {
    /**
     * Constructor which sets the default readBuffer size to 4096 bytes.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url the url to send the request to.
     * @param postData the HTML form data.
     */
    public POSTMultipartData(String url, List<AbstractFormItem> postData) {
        this(url, postData, 4096);
    }

    /**
     * Constructor which allows for modification of the readBuffer size.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url the url to send the request to.
     * @param bufferLength the size of the readBuffer which will be holding the binary data
     *                      from the remote host.
     */
    public POSTMultipartData(String url, List<AbstractFormItem> postData, int bufferLength) {
        this(url, postData, bufferLength, 15000, 10000);
    }

    /**
     * Constructor which allows for modification the timeouts.
     *
     * @param url the url to send the request to.
     * @param bufferLength the size of the readBuffer which will be holding the binary data
     *                      from the remote host.
     * @param connectionTimeout the time in milliseconds to wait to connect.
     * @param readTimeout   the time in milliseconds to wait to receive data.
     */
    public POSTMultipartData(String url, List<AbstractFormItem> postData, int bufferLength,
                               int connectionTimeout, int readTimeout) {
        super(url, postData, bufferLength, connectionTimeout, readTimeout);
    }
    private byte[] mBoundary;

    /**
     * Sends the multi part data to the remote host.
     *
     * @param recvArgs an object used to send & receive data.
     * @throws IOException due to HttpConnection.send being called.
     */
    @Override
    protected void sendPostData(SendRecvHandler recvArgs) throws IOException {
        OutputStream out = recvArgs.getOutputStream();

        ByteArrayOutputStream bArray = new ByteArrayOutputStream(mBoundary.length + 6);
        bArray.write((byte) '-');
        bArray.write((byte) '-');
        bArray.write(mBoundary, 0, mBoundary.length);

        for (AbstractFormItem item : mPostData) {
            bArray.writeTo(out);
            out.write(AbstractFormItem.NEW_LINE);
            out.write(item.getHeader());
            out.write(item.getValue());
            out.write(AbstractFormItem.NEW_LINE);
        }

        bArray.write((byte) '-');
        bArray.write((byte) '-');
        bArray.writeTo(out);
        out.write(AbstractFormItem.NEW_LINE);
        out.flush();
        out.close();
    }

    /**
     * Method responsible for setting the content type.
     *
     * @param recvArgs an object used to send & receive data.
     */
    @Override
    protected void setContentType(SendRecvHandler recvArgs) {
        mBoundary = getBoundary(mPostData);
        recvArgs.connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="
                + new String(mBoundary, StandardCharsets.US_ASCII));
    }

    /**
     * Asynchronously runs a POST request and calls the appropriate callbacks to return the result.
     */
    public static class AsyncPostData extends AsyncRequest<StringBuilder> {
        private List<AbstractFormItem> mPostData;

        /**
         * Constructor which sets all of the member variables.
         *
         * @param callback  the object to return results to.
         */
        public AsyncPostData(@NonNull List<AbstractFormItem> postData,
                             @NonNull OnRequestResultsListener<StringBuilder> callback) {
            super(callback);
            mPostData = postData;
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
        protected StringBuilder doInBackground(String... strings) {
            POSTMultipartData request = new POSTMultipartData(strings[0], mPostData) {
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
            request.setHttpHeaders(mHttpHeaders);
            request.send();
            return request.getResult();
        }
    }
}
