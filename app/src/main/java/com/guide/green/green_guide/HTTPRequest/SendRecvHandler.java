package com.guide.green.green_guide.HTTPRequest;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Struct which encapsulates the process used for sending and receiving data. This is used to share
 * the process which child classes by {@code AbstractRequest}.
 */
class SendRecvHandler {
    final HttpURLConnection connection;
    private OutputStream mOut;
    private InputStream mIn;

    byte[] recvBuffer;

    // a value greater than zero but less than {@code recvBuffer.length}.
    int recvBufferLen;

    // a value greater than zero which represents the beginning number of bytes in
    // the array {@code readBuffer}  to not modify on the next call to {@code read}.
    int recvBufferOffset;

    /**
     * Constructor which sets the receiving buffer.
     *
     * @param recvBuffer the receiving buffer, if null is initialized to a buffer with the size
     *                   {@code readBufferSize}.
     * @param readBufferSize a value greater than zero but less than {@code recvBuffer.length}
     * @param url the url to open the connection for.
     * @throws IOException thrown if an error is encountered opening a HttpURLConnection.
     */
    SendRecvHandler(byte[] recvBuffer, int readBufferSize, URL url) throws IOException {
        connection = (HttpURLConnection) url.openConnection();
        if (recvBuffer == null) {
            this.recvBuffer = new byte[readBufferSize];
        } else {
            this.recvBuffer = recvBuffer;
        }
        this.recvBufferLen = readBufferSize;
    }

    OutputStream getOutputStream() throws IOException {
        return mOut;
    }

    int recv() throws IOException {
        return mIn.read(recvBuffer, recvBufferOffset, recvBufferLen - recvBufferOffset);
    }

    /**
     * Must be called called before {@code getOutputStream}
     *
     * @throws IOException thrown by {@code getOutputStream}
     */
    void openOutputStream() throws IOException {
        mOut = new BufferedOutputStream(connection.getOutputStream());
    }

    /**
     * Must be called called before {@code recv} or {@code send}
     *
     * @throws IOException thrown by {@code getInputStream}
     */
    void openInputStream() throws IOException {
        mIn = new BufferedInputStream(connection.getInputStream());
    }

    /**
     * @return null on error else the value of the content-encoding header field.
     */
    String getContentEncoding() {
        if (connection != null) {
            return connection.getContentEncoding();
        }
        return null;
    }

    /**
     * @return null on error else the value of the content-type header field.
     */
    String getContentType() {
        if (connection != null) {
            return connection.getContentType();
        }
        return null;
    }

    /**
     * @return null on error else the status code from an HTTP response message.
     *         If an integer is returned, it will match with on of the constants matching
     *         java.net.HttpURLConnection.HTTP_*.
     */
    Integer getResponseCode() {
        if (connection != null) {
            try {
                return connection.getResponseCode();
            } catch (IOException e) {
                /* Do nothing */
            }
        }
        return null;
    }
}
