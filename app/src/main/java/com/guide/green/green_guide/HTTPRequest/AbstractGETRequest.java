package com.guide.green.green_guide.HTTPRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

/**
 * Provides a way to run a single GET request.
 */
public abstract class AbstractGETRequest extends AbstractRequest {
    public final int connectionTimeout;
    public final int readTimeout;

    /**
     * Constructor which sets the default readBuffer size to 4096 bytes.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url   the url to send the request to.
     */
    public AbstractGETRequest(String url) {
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
    protected AbstractGETRequest(String url, int bufferLength) {
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
    protected AbstractGETRequest(String url, int bufferLength, int connectionTimeout,
                                 int readTimeout) {
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
    protected void send(byte[] recvBuffer) {
        SendRecvHandler recvArgs;
        try {
            recvArgs = getSendRecvHandler(recvBuffer);
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
        } catch (IOException e) {
            onError(e);
        } finally {
            recvArgs.connection.disconnect();
        }
    }
}
