package com.guide.green.green_guide.Utilities;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
import java.util.Map;

/**
 * Provides a way to run a GET request and get a Bitmap output.
 */
public class SimpleImageGETRequest extends SimpleGETRequest {
    public byte[] mImageData;

    /**
     * Constructor which sets the default readBuffer size to 4096 bytes.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url   the url to send the request to.
     */
    public SimpleImageGETRequest(String url) {
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
    public SimpleImageGETRequest(String url, int bufferLength) {
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
    public SimpleImageGETRequest(String url, int bufferLength, int connectionTimeout, int readTimeout) {
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

  public static void main(String[] args) {
    SimpleImageGETRequest simpleGet = new SimpleImageGETRequest("http://localhost/eve.jpg");
    simpleGet.send();
    try {
      java.io.FileOutputStream fsOut = new java.io.FileOutputStream("out.jpg");
      fsOut.write(simpleGet.mImageData);
      fsOut.close();
    } catch (java.io.IOException e) { System.out.println("ERROR WRITING TO FILE"); }
  }
}
