package com.guide.green.green_guide.Utilities;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * Provides a way to run a get request.
 */
public abstract class SimpleGETRequest {
    private boolean isCanceled = false;
    private int mConnectionTimeout;
    private final int mBufferSize;
    private final String mStrUlr;
    private int mReadTimeout;

    /**
     * Constructor which sets the default buffer size to 4096 bytes.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url   the url to send the request to.
     */
    public SimpleGETRequest(String url) {
        this(url, 4096);
    }

    /**
     * Constructor which allows for modification of the buffer size.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url   the url to send the request to.
     * @param bufferLength  the size of the buffer which will be holding the binary data
     *                      and the buffer which will hold the converted character data.
     */
    public SimpleGETRequest(String url, int bufferLength) {
        this(url, bufferLength, 15000, 10000);
    }

    /**
     * Constructor which allows for modification the timeouts.
     *
     * @param url   the url to send the request to.
     * @param bufferLength  the size of the buffer which will be holding the binary data
     *                      and the buffer which will hold the converted character data.
     * @param connectionTimeout the time in milliseconds to wait to connect.
     * @param readTimeout   the time in milliseconds to wait to receive data.
     */
    public SimpleGETRequest(String url, int bufferLength, int connectionTimeout, int readTimeout) {
        mStrUlr = url;
        mBufferSize = bufferLength;
        mConnectionTimeout = connectionTimeout;
        mReadTimeout = readTimeout;
        if (bufferLength < 3) {
            throw new IllegalArgumentException("The supplied buffer length \"" + bufferLength
                    + "\" is too small. Must be at least 3.");
        }
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
     * Runs the request and reads the returned value as a string.
     * @return The string builder containg the data returned by the request.
     *         Note: Might have been updated on the call to {@code onUpdate}
     */
    public final StringBuilder send() {
        URL url = null;
        HttpURLConnection conn = null;
        StringBuilder sb = null;

        try {
            url = new URL(mStrUlr);
            conn = (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            onError(e);
            return sb;
        } catch (IOException e) {
            onError(e);
            return sb;
        }

        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(mConnectionTimeout);
        conn.setReadTimeout(mReadTimeout);

        byte[] bBuffer = new byte[mBufferSize];
        char[] cBuffer = new char[mBufferSize];

        try {
            long contentLength = -1, contentCount = 0;
            conn.setRequestMethod("GET");
            conn.connect();

            String contentLen = conn.getHeaderField("content-length");
            if (contentLen != null) {
                contentLength = Long.parseLong(contentLen);
            }

            sb = new StringBuilder();
            InputStream in = new BufferedInputStream(conn.getInputStream());
            CharsetDecoder decoder = getCharsetFromContentType(conn.getContentType()).newDecoder();

            int writeOffset = 0, rtnLen = 0, dataLen = 0;
            do {
                rtnLen = in.read(bBuffer, writeOffset, bBuffer.length - writeOffset);

                if (rtnLen == -1) { break; }
                contentCount += rtnLen;
                dataLen = rtnLen + writeOffset;

                CharBuffer cb = CharBuffer.wrap(cBuffer, 0, dataLen);
                ByteBuffer bb = ByteBuffer.wrap(bBuffer, 0, dataLen);
                decoder.reset();
                CoderResult cr = decoder.decode(bb, cb, false);

                sb.append(cBuffer, 0, dataLen - cb.remaining());
                onUpdate(sb, contentCount, contentLength);

                writeOffset = bb.remaining();
                for (int i = 0; i < writeOffset; i++) {
                    bBuffer[i] = bBuffer[rtnLen - writeOffset + i];
                }
            } while ((contentLength == -1 || contentCount < contentLength)
                        && writeOffset != mBufferSize && !isCanceled);
        } catch (ProtocolException e) {
            onError(e);
        } catch (IOException e) {
            onError(e);
        } finally {
            conn.disconnect();
        }

        return sb;
    }

    /**
     * Called once when an error occurs during the request.
     *
     * @param e an exception
     */
    public abstract void onError(Exception e);

    /**
     * Called when part of the read binary buffer is converted to a string.
     *
     * @param sb    A buffer with the decoded string.
     * @param current   the total amount of bytes read so far from the server.
     * @param total the expected total number of bytes that will be read from the server.
     *              if unknown, this value is set to -1.
     */
    public void onUpdate(StringBuilder sb, long current, long total) {};

    /**
     * Stops any pending request.
     */
    public void stop() { isCanceled = true; }
}
