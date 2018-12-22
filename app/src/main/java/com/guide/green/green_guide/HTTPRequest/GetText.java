package com.guide.green.green_guide.HTTPRequest;

import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

/**
 * Provides a way to run a GET request and get a String output.
 */
public class GetText extends AbstractGETRequest {
    private char[] mChrBuffer;
    private CharsetDecoder mDecoder;
    private StringBuilder mSBuilder = null;

    /**
     * Constructor which sets the default readBuffer size to 4096 bytes.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url   the url to send the request to.
     */
    public GetText(String url) {
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
    public GetText(String url, int bufferLength) {
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
    public GetText(String url, int bufferLength, int connectionTimeout, int readTimeout) {
        super(url, bufferLength, connectionTimeout, readTimeout);
        mChrBuffer = new char[getDesiredBufferSize()];
    }

    /**
     * Takes in a raw "Content-Type" data and extracts the charset.
     * If none is specified, it returns the UTF-8 charset.
     *
     * @parameter rawField  the value following the "Content-Type" line.
     * @return charset the {@code rawField}, else UTF-8 charset on any error.
     */
    private static final Charset getCharsetFromContentType(String rawField) {
        Charset charset = null;
        if (rawField != null) {
            final String charsetAttr = "charset=";
            int cStart = rawField.toLowerCase().indexOf(charsetAttr);
            if (cStart != -1) {
                cStart += charsetAttr.length();
                int cEnd = rawField.indexOf(";", cStart);
                if (cEnd == -1) { cEnd = rawField.length(); }
                rawField = rawField.substring(cStart, cEnd);
                try {
                    charset = Charset.forName(rawField);
                } catch (Exception e) { /* Do Nothing */ }
            }
        }

        return (charset == null ? Charset.forName("UTF-8") : charset);
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
        Integer statusCode = recvArgs.getResponseCode();
        if (statusCode != null && (int) (statusCode / 100) == 2) {
            String contentType = recvArgs.getContentType();
            if (contentType == null) { contentType = "utf-8"; }
            mDecoder = getCharsetFromContentType(contentType).newDecoder();
            mSBuilder = new StringBuilder();
        } else {
            stop();
            onError(new Exception("Bad status code: " + 
                          (statusCode == null ? "NULL" : statusCode)));
        }
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
        CharBuffer cb = CharBuffer.wrap(mChrBuffer, 0, dataLen);
        ByteBuffer bb = ByteBuffer.wrap(recvArgs.recvBuffer, 0, dataLen);
        mDecoder.reset();
        mDecoder.decode(bb, cb, false);
        mSBuilder.append(mChrBuffer, 0, dataLen - cb.remaining());

        recvArgs.recvBufferOffset = bb.remaining();
        if (recvArgs.recvBufferOffset == mChrBuffer.length) {
            recvArgs.recvBufferOffset -= 1;
        }
        for (int i = 0; i < recvArgs.recvBufferOffset; i++) {
            recvArgs.recvBuffer[i] = recvArgs.recvBuffer[dataLen - recvArgs.recvBufferOffset + i];
        }
    }

    /**
     * @return the string builder which has been populated with the text sent by the remote host.
     */
    public StringBuilder getResult() {
        return mSBuilder;
    }

    /**
     * Called once when an error occurs during the request.
     *
     * @param e an exception
     */
    @Override
    public void onError(Exception e) {
        Log.e("SimpleTextGet", e.toString());
        e.printStackTrace();
    }

    @Override
    public String toString() {
      if (mSBuilder != null) {
        return mSBuilder.toString();
      }
      return "NULL";
    }

    /**
     * Asynchronously runs a POST request and calls the appropriate callbacks to return the result.
     */
    public static class AsyncGetText extends AsyncRequest<StringBuilder> {
        /**
         * Constructor which sets all of the member variables.
         *
         * @param callback  the object to return results to.
         */
        public AsyncGetText(@NonNull OnRequestResultsListener<StringBuilder> callback) {
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
        protected StringBuilder doInBackground(String... strings) {
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
            request.setHttpHeaders(mHttpHeaders);
            request.send();
            return request.getResult();
        }
    }
}
