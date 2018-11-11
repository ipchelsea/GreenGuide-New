package com.guide.green.green_guide.Utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Provides a way to run a request.
 */
public abstract class SimpleRequest {
    private final int mBufferSize;
    private final String mStrUlr;

    /**
     * Constructor which sets the default readBuffer size to 4096 bytes.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url   the url to send the request to.
     */
    public SimpleRequest(String url) {
        this(url, 4096);
    }

    /**
     * Constructor which allows for modification of the readBuffer size.
     *
     * @param url   the url to send the request to.
     * @param bufferLength  the size of the readBuffer which will be holding the binary data
     *                      from the remote host.
     */
    public SimpleRequest(String url, int bufferLength) {
        mStrUlr = url;
        mBufferSize = bufferLength;
        if (bufferLength < 3) {
            throw new IllegalArgumentException("The supplied readBuffer length \"" + bufferLength
                    + "\" is too small. Must be at least 3.");
        }
    }

    /**
     * Struct which encapsulates the variables used for sending and receiving data so they can
     * more easily be shared with child classes.
     */
    public static class SendRecvHandler {
        public final HttpURLConnection connection;
        private OutputStream mOut;
        private InputStream mIn;

        public byte[] recvBuffer;
        
        // a value greater than zero but less than {@code recvBuffer.length}.
        public int recvBufferLen;

         // a value greater than zero which represents the beginning number of bytes in
         // the array {@code readBuffer}  to not modify on the next call to {@code read}.
        public int recvBufferOffset;

        /**
         * Constructor which sets the receiving buffer.
         *
         * @param recvBuffer the receiving buffer, if null is initialized to a buffer with the size
         *                   {@code readBufferSize}.
         * @param readBufferSize a value greater than zero but less than {@code recvBuffer.length}
         * @param url the url to open the connection for.
         * @throws IOException thrown if an error is encountered opening a HttpURLConnection.
         */
        public SendRecvHandler(byte[] recvBuffer, int readBufferSize, URL url) throws IOException {
            connection = (HttpURLConnection) url.openConnection();
            if (recvBuffer == null) {
                this.recvBuffer = new byte[readBufferSize];
            } else {
                this.recvBuffer = recvBuffer;
            }
            this.recvBufferLen = readBufferSize;
        }
        
        public OutputStream getOutputStream() throws IOException {
            return mOut;
        }

        public int recv() throws IOException {
            return mIn.read(recvBuffer, recvBufferOffset, recvBufferLen - recvBufferOffset);
        }

        /**
         * Must be called called before {@code getOutputStream}
         *
         * @throws IOException thrown by {@code getOutputStream}
         */
        public void openOutputStream() throws IOException {
            mOut = new BufferedOutputStream(connection.getOutputStream());
        }
        
        /**
         * Must be called called before {@code recv} or {@code send}
         *
         * @throws IOException thrown by {@code getInputStream}
         */
        public void openInputStream() throws IOException {
            mIn = new BufferedInputStream(connection.getInputStream());
        }
    
        /**
         * @return null on error else the value of the content-encoding header field.
         */
        public String getContentEncoding() {
            if (connection != null) {
                return connection.getContentEncoding();
            }
            return null;
        }
        
        /**
         * @return null on error else the value of the content-type header field.
         */
        public String getContentType() {
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
        public Integer getResponseCode() {
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

    public SendRecvHandler getSendRecvHandler(byte[] recvBuffer) throws IOException {
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
    public abstract void send(byte[] recvBuffer);

    public void recv(SendRecvHandler recvArgs) throws IOException {
        long contentLength = -1, contentCount = 0;
        String contentLen = recvArgs.connection.getHeaderField("content-length");
        if (contentLen != null) {
            contentLength = Long.parseLong(contentLen);
        }

        onResponseHeaders(recvArgs, contentLength);
        
        if (!isStopped()) {
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
                    && recvArgs.recvBufferOffset < recvArgs.recvBufferLen && !isStopped());
        }
    }
    
    /**
     * @return the desired size of the byte array storing the data received from the remote host.
     */
    public int getDesiredBufferSize() {
      return mBufferSize;
    }

    /**
     * Called when a read of data has been completed.
     *
     * @param current the total amount of bytes read so far from the host.
     * @param total the expected total number of bytes that will be read from the host.
     *              if unknown, this value is set to -1.
     */
    public void onReadUpdate(long current, long total) { /* Do nothing */ }
    
    /**
     * Called when the connection has been established and passes the response headers.
     * If {@code stop} is invoked here, it is guaranteed that {@code onRead} will not be called.
     *
     * @param recvArgs an object containing the HTTPConnection object to the remote host.
     * @param dataLength the expected total number of bytes that will be read from the server, -1
     *                   if unknown.
     */
    public abstract void onResponseHeaders(SendRecvHandler recvArgs, long dataLength);
    
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
    public abstract void onRead(SendRecvHandler recvArgs, int rtnLen);
    
    /**
     * Called once when an error occurs during the request.
     *
     * @param e an exception
     */
    public abstract void onError(Exception e);

    /**
     * Stops any pending request.
     */
    public abstract void stop();

    /**
     * @return true if a call to {@code stop} was made.
     */
    public abstract boolean isStopped();
}
