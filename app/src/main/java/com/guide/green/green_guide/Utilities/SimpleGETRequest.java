package com.guide.green.green_guide.Utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Provides a way to run a single GET request.
 */
public abstract class SimpleGETRequest extends SimpleRequest {
    private boolean isCanceled = false;
    public final int connectionTimeout;
    public final int readTimeout;

    /**
     * Constructor which sets the default readBuffer size to 4096 bytes.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url   the url to send the request to.
     */
    public SimpleGETRequest(String url) {
        this(url, 4096);
    }

    /**
     * Constructor which allows for modification of the readBuffer size.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url   the url to send the request to.
     * @param bufferLength  the size of the readBuffer which will be holding the binary data
     *                      from the remote host.
     */
    public SimpleGETRequest(String url, int bufferLength) {
        this(url, bufferLength, 15000, 10000);
    }

    /**
     * Constructor which allows for modification the timeouts.
     *
     * @param url   the url to send the request to.
     * @param bufferLength  the size of the readBuffer which will be holding the binary data
     *                      from the remote host.
     * @param connectionTimeout the time in milliseconds to wait to connect.
     * @param readTimeout   the time in milliseconds to wait to receive data.
     */
    public SimpleGETRequest(String url, int bufferLength, int connectionTimeout, int readTimeout) {
        super(url, bufferLength);
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * Runs the request and reads the data returned by the remote host.
     *
     * @param recvBuffer the readBuffer to use if not null, else a readBuffer of the supplied
     *               {@code bufferLength} will be created.
     */
    public void send(byte[] recvBuffer) {
        SimpleRequest.SendRecvHandler recvArgs;
        try {
            recvArgs = getSendRecvHandler(recvBuffer);
        } catch (MalformedURLException e) {
            onError(e);
            return;
        } catch (IOException e) {
            onError(e);
            return;
        }

        recvArgs.connection.setInstanceFollowRedirects(true);
        recvArgs.connection.setConnectTimeout(connectionTimeout);
        recvArgs.connection.setReadTimeout(readTimeout);

        try {
            recvArgs.connection.setRequestMethod("GET");
            recvArgs.connection.connect();
            recvArgs.openInputStream();
            recv(recvArgs);
        } catch (ProtocolException e) {
            onError(e);
        } catch (IOException e) {
            onError(e);
        } finally {
            recvArgs.connection.disconnect();
        }
    }

    /**
     * Stops any pending request.
     */
    public void stop() { isCanceled = true; }

    /**
     * @return true if a call to {@code stop} was made.
     */
    public boolean isStopped() { return isCanceled; };
}
