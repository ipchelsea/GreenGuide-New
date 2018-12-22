package com.guide.green.green_guide.HTTPRequest;

import android.util.Pair;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Provides a way to run a request.
 */
public abstract class AbstractRequest {
    private final int mBufferSize; // The buffer to temporarily stored the returned data
    private final String mStrUlr; // The URL to send data to
    protected List<Pair<String, String>> mHttpHeaders;
    protected RequestState mRequestState = RequestState.NOT_STARTED;
    protected volatile boolean mStopRequested = false;
    protected volatile boolean mStopped = false;
    public int id; // Used to uniquely identify the request

    protected enum RequestState {
        NOT_STARTED,
        PRE_OPEN_CONNECTION, // Before the headers are sent
        OPENING_CONNECTION,  // After the headers are sent
        SENDING, RECEIVING, FINISHED
    }

    /**
     * Callback interface for supplying the results and updates of a request.
     *
     * @param <Result>  The value returned on a successful request.
     */
    public static abstract class OnRequestResultsListener<Result> {
        public abstract void onSuccess(Result result);
        public void onProgress(RequestProgress progress) { /* Do Nothing */ }
        public void onError(Exception error) { /* Do Nothing */ }
        public void onCanceled() { /* Do Nothing */ }
    }

    /**
     * Constructor which sets the default readBuffer size to 4096 bytes.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url   the url to send the request to.
     */
    protected AbstractRequest(String url) {
        this(url, 4096);
    }

    /**
     * Constructor which allows for modification of the readBuffer size.
     *
     * @param url   the url to send the request to.
     * @param bufferLength  the size of the readBuffer which will be holding the binary data
     *                      from the remote host.
     */
    protected AbstractRequest(String url, int bufferLength) {
        mStrUlr = url;
        mBufferSize = bufferLength;
        if (bufferLength < 3) {
            throw new IllegalArgumentException("The supplied readBuffer length \"" + bufferLength
                    + "\" is too small. Must be at least 3.");
        }
    }

    /**
     * Struct which can be used to describe the progress of a request.
     */
    public static class RequestProgress {
        public long total;      // The total number of bytes to be received. -1 when unknown.
        public long current;    // The total number of bytes read.

        /**
         * Constructor which initializes all of the member variables.
         *
         * @param current The total number of bytes read.
         * @param total The total number of bytes to be received. -1 when unknown.
         */
        public RequestProgress(long current, long total) {
            this.total = total;
            this.current = current;
        }

        /**
         * Uses the defined behaviour of the total being -1 when the total number of bytes to be
         * received is unknown.
         *
         * @return true if the amount of data remaining is unknown, else false.
         */
        public boolean remainingIsUnknown() {
            return total == -1;
        }
    }

    /**
     * Sets the headers. The headers are stored as a key-value pair.
     *
     * @param headers the key value pair containing the header name and its value as the second
     *                parameter.
     */
    public void setHttpHeaders(List<Pair<String, String>> headers) {
        if (mRequestState != RequestState.NOT_STARTED) {
            throw new IllegalStateException("The Headers Have Already Been Sent.");
        }
        mHttpHeaders = headers;
    }

    /**
     * @param connection the HTTP headers add to the provided {@code HttpURLConnection}.
     */
    protected void assignHttpHeaders(HttpURLConnection connection) {
        if (mHttpHeaders == null) return;
        for (Pair<String, String> header : mHttpHeaders) {
            connection.setRequestProperty(header.first, header.second);
        }
    }

    /**
     * Creates and returnes a SendRecvHandler.
     *
     * @param recvBuffer the receiving buffer, if null it is initialized to a buffer with the size
     *                   {@code mBufferSize}.
     * @return
     * @throws IOException
     */
    SendRecvHandler getSendRecvHandler(byte[] recvBuffer) throws IOException {
        return new SendRecvHandler(recvBuffer, mBufferSize, new URL(mStrUlr));
    }

    /**
     * Runs the request and reads the data returned by the remote host.
     */
    public final void send() {
        send(null);
    }

    /**
     * Runs the request and reads the data returned by the remote host.
     *
     * @param recvBuffer the readBuffer to use if not null, else a readBuffer of the supplied
     *               {@code bufferLength} will be created.
     */
    protected abstract void send(byte[] recvBuffer);

    /**
     * Received data from the remote host, saves data to the temporary buffer as it reads parts of
     * the sent data. Calls the appropriate callbacks to handle the data as it reads it.
     *
     * @param recvArgs an object used to send & receive data.
     * @throws IOException due to the HttpConnection object sending and receiving data.
     */
    protected void recv(SendRecvHandler recvArgs) throws IOException {
        mRequestState = RequestState.RECEIVING;
        long contentLength = -1, contentCount = 0;
        String contentLen = recvArgs.connection.getHeaderField("content-length");
        if (contentLen != null) {
            contentLength = Long.parseLong(contentLen);
        }

        onResponseHeaders(recvArgs, contentLength);
        
        if (!mStopped) {
            if (recvArgs.recvBuffer == null) {
                recvArgs.recvBuffer = new byte[getDesiredBufferSize()];
                recvArgs.recvBufferLen = recvArgs.recvBuffer.length;
            }
            
            do {
                int rtnLen = recvArgs.recv();

                if (rtnLen == -1) {
                    break;
                }
                contentCount += rtnLen;

                onRead(recvArgs, rtnLen);
                onReadUpdate(contentCount, contentLength);
            } while ((contentLength == -1 || contentCount < contentLength)
                    && recvArgs.recvBufferOffset < recvArgs.recvBufferLen && !mStopped);
        }
    }

    /**
     * Stops any pending request. Will throw if called after the request has entered a FINISHED
     * state.
     */
    public void stop() {
        if (mRequestState != RequestState.FINISHED) {
            mStopRequested = true;
        }
    }

    /**
     * @return true if a call to {@code stop()} was made before the request finished.
     */
    public boolean wasStopped() {
        return mStopped;
    }
    
    /**
     * @return the desired size of the byte array storing the data received from the remote host.
     */
    int getDesiredBufferSize() {
      return mBufferSize;
    }

    /**
     * Called when a read of data has been completed.
     *
     * @param current the total amount of bytes read so far from the host.
     * @param total the expected total number of bytes that will be read from the host.
     *              if unknown, this value is set to -1.
     */
    protected void onReadUpdate(long current, long total) { /* Do nothing */ }
    
    /**
     * Called when the connection has been established and passes the response headers.
     * If {@code stop} is invoked here, it is guaranteed that {@code onRead} will not be called.
     *
     * @param recvArgs an object containing the HTTPConnection object to the remote host.
     * @param dataLength the expected total number of bytes that will be read from the server, -1
     *                   if unknown.
     */
    protected abstract void onResponseHeaders(SendRecvHandler recvArgs, long dataLength);
    
    /**
     * Called after every read operation is performed to receive data from the remote host.
     * The last data received from the remote host will be stored in the array
     * {@code recvArgs.readBuffer} and will start at the index {@code [recvArgs.writeOffset]} and
     * end at the index {@code [recvArgs.writeOffset + rtnLen - 1]}.
     *
     * @param recvArgs an object containing the byte array with the data received from the remote
     *                 host.
     * @param rtnLen the number of bytes that were just received.
     */
    protected abstract void onRead(SendRecvHandler recvArgs, int rtnLen);
    
    /**
     * Called once when an error occurs during the request.
     *
     * @param e an exception
     */
    protected abstract void onError(Exception e);
}
