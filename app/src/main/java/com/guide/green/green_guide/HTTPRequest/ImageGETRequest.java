package com.guide.green.green_guide.HTTPRequest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Provides a way to run a GET request and get a Bitmap output.
 */
public class ImageGETRequest extends AbstractGETRequest {
    private byte[] mImageData;

    /**
     * Constructor which sets the default readBuffer size to 4096 bytes.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url   the url to send the request to.
     */
    private ImageGETRequest(String url) {
        this(url, 4096);
    }

    /**
     * Constructor which allows for modification of the readBuffer size.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url   the url to send the request to.
     * @param bufferLength  the size of the readBuffer which will be holding the binary data
     *                      and the readBuffer which will hold the converted character data.
     */
    private ImageGETRequest(String url, int bufferLength) {
        this(url, bufferLength, 15000, 10000);
    }

    /**
     * Constructor which allows for modification the timeouts.
     *
     * @param url   the url to send the request to.
     * @param bufferLength  the size of the readBuffer which will be holding the binary data
     *                      and the readBuffer which will hold the converted character data.
     * @param connectionTimeout the time in milliseconds to wait to connect.
     * @param readTimeout   the time in milliseconds to wait to receive data.
     */
    public ImageGETRequest(String url, int bufferLength, int connectionTimeout, int readTimeout) {
        super(url, bufferLength, connectionTimeout, readTimeout);
    }
    
    /**
     * Called when the connection has been established and passes the response headers.
     * If {@code stop} is invoked here, it is guaranteed that {@code onRead} will not be called.
     *
     * @param recvArgs an object containing the HTTPConnection object to the remote host.
     * @param dataLength the expected total number of bytes that will be read from the server, -1 if unknown.
     */
    @Override
    public void onResponseHeaders(SendRecvHandler recvArgs, long dataLength) {
        if (dataLength > 0) {
          mImageData = new byte[(int) dataLength];
        } else {
          mImageData = new byte[getDesiredBufferSize()];
        }
        recvArgs.recvBuffer = mImageData;
        recvArgs.recvBufferLen = recvArgs.recvBuffer.length;
    }

    /**
     * Called after every read operation is performed to receive data from the remote host.
     * The last data received from the remote host will be stored in the array
     * {@code recvArgs.readBuffer} and will start at the index {@code [recvArgs.writeOffset]} and
     * end at the index {@code [recvArgs.writeOffset + rtnLen - 1]}.
     *
     * @param recvArgs an object containing the byte array with the data received from the remote
     *                 host.
     * @param rtnLen   the number of bytes that were just received.
     */
    @Override
    public void onRead(SendRecvHandler recvArgs, int rtnLen) {
        int dataLen = rtnLen + recvArgs.recvBufferOffset;

        recvArgs.recvBufferLen += getDesiredBufferSize();
        if (recvArgs.recvBufferLen > recvArgs.recvBuffer.length) {
          recvArgs.recvBufferLen = recvArgs.recvBuffer.length;
        }
        recvArgs.recvBufferOffset = dataLen;
    }

    /**
     * Called once when an error occurs during the request.
     *
     * @param e an exception
     */
    @Override
    public void onError(Exception e) {
        Log.e("SimpleImage", e.toString());
        e.printStackTrace();
    }

    /**
     * Asynchronously runs an AsyncGetImage request and calls the appropriate callbacks to return
     * the result.
     */
    public static class AsyncGetImage extends AsyncRequest<Bitmap> {
        /**
         * Constructor which sets all of the member variables.
         *
         * @param callback  the object to return results to.
         */
        public AsyncGetImage(@NonNull OnRequestResultsListener<Bitmap> callback) {
            super(callback);
        }

        /**
         * Worker thread which runs the request in the background. Creates an ImageGETRequest
         * object and overrides some of its classes to insure that callbacks are successfully
         * run.
         *
         * @param strings   an array with 1 element. That one element should be the URL pointing
         *                  to the location of the image.
         * @return  a bitmap object of the downloaded image.
         */
        @Override
        protected final Bitmap doInBackground(String... strings) {
            ImageGETRequest request = new ImageGETRequest(strings[0]) {
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
            return BitmapFactory.decodeByteArray(
                    request.mImageData, 0, request.mImageData.length);
        }
    }
}
