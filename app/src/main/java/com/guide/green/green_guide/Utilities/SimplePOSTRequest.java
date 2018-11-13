package com.guide.green.green_guide.Utilities;

import android.content.Context;
import android.net.Uri;

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
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static com.guide.green.green_guide.Utilities.Misc.getFileNameFromUri;
import static com.guide.green.green_guide.Utilities.Misc.getMimeTypeFromUri;
import static com.guide.green.green_guide.Utilities.Misc.getRndInt;
import static com.guide.green.green_guide.Utilities.Misc.readAllBytesFromFileUri;

/**
 * Provides a way to run a single GET request.
 */
public class SimplePOSTRequest extends SimpleTextGETRequest {
    private List<FormItem> mPostData;
    private byte[] mBoundary;
    
    public static abstract class FormItem {
        public static final byte[] CONTENT_DISPOSITION = "Content-Disposition: form-data; name=\"".getBytes(StandardCharsets.US_ASCII);
        public static final byte[] FILE_NAME = "\"; filename=\"".getBytes(StandardCharsets.US_ASCII);
        public static final byte[] CONTENT_TYPE = "Content-Type: ".getBytes(StandardCharsets.US_ASCII);
        public static final byte[] NEW_LINE = "\r\n".getBytes(StandardCharsets.US_ASCII);
        public static final byte QUOTE = (byte) '\"';
        private byte[] mHeader;
        
        /**
         * @return a newly created byte array with the contents after the boundary or abstract
         *         cached version of this same information. If any of the other values such as
         *         the name are changed after this method has been invoked, then the cached
         *         version will not reflect those changes.
         */
        public final byte[] getHeader() {
            if (mHeader == null) {
                ByteArrayOutputStream bArray = new ByteArrayOutputStream();
                bArray.write(CONTENT_DISPOSITION, 0, CONTENT_DISPOSITION.length);
                
                byte[] inputName = getName();
                bArray.write(inputName, 0, inputName.length);
                
                byte[] fileName = getFileName();
                if (fileName != null) {
                    bArray.write(FILE_NAME, 0, FILE_NAME.length);
                    bArray.write(fileName, 0, fileName.length);
                }
                bArray.write(QUOTE);
                bArray.write(NEW_LINE, 0, NEW_LINE.length);
                
                byte[] contentType = getContentType();
                if (contentType != null) {
                    bArray.write(CONTENT_TYPE, 0, CONTENT_TYPE.length);
                    bArray.write(contentType, 0, contentType.length);
                    bArray.write(NEW_LINE, 0, NEW_LINE.length);
                }
                
                byte[] other = getOther();
                if (other != null) {
                    bArray.write(other, 0, other.length);
                    bArray.write(NEW_LINE, 0, NEW_LINE.length);
                }
                
                bArray.write(NEW_LINE, 0, NEW_LINE.length);
                mHeader = bArray.toByteArray();
            }
            return mHeader;
        }
        
        /**
         * @return a non-null name of the form <input>.
         */
        public abstract byte[] getName();
        
        /**
         * @return a file name if this item is a file, else null.
         */
        public abstract byte[] getFileName();
        
        /**
         * @return the content type without the prefix "Content-type:", else null.
         */
        public abstract byte[] getContentType();
        
        /**
         * @return any other data that should be added in the header of the boundary.
         */
        public abstract byte[] getOther();
        
        /**
         * @return the contents of the item. <input value="..." />
         */
        public abstract byte[] getValue();
    }
    
    public static class TextFormItem extends FormItem {
        public static final byte[] CONTENT_TYPE_UTF8_TEXT =
                "text/plain; charset=UTF-8".getBytes(StandardCharsets.US_ASCII);
        private final byte[] mValue;
        private final byte[] mName;
        
        public TextFormItem(String name, String value) {
            mName = name.getBytes(StandardCharsets.UTF_8);
            mValue = value.getBytes(StandardCharsets.UTF_8);
        }
        
        @Override
        public byte[] getName() { return mName; }
        
        @Override
        public byte[] getFileName() { return null; }
        
        @Override
        public byte[] getContentType() { return CONTENT_TYPE_UTF8_TEXT; }
        
        @Override
        public byte[] getOther() { return null; }
        
        @Override
        public byte[] getValue() { return mValue; }
    }
    
    public static abstract class FileFormItem extends FormItem {
        public final byte[] mMimeType;
        private final byte[] mFileName;
        private final byte[] mName;
        public Object value;
        
        public FileFormItem(String inputName, String fileName, Object value) {
            this(inputName, fileName, null, value);
        }
        
        public FileFormItem(String inputName, String fileName, String mimeType, Object value) {
            mFileName = fileName.getBytes(StandardCharsets.UTF_8);
            mName = inputName.getBytes(StandardCharsets.UTF_8);
            
            if (mimeType == null) {
                mMimeType = null;
            } else {
                mMimeType = mimeType.getBytes(StandardCharsets.UTF_8);
            }
            
            this.value = value;
        }
        
        @Override
        public byte[] getName() { return mName; }
        
        @Override
        public byte[] getFileName() { return mFileName; }
        
        @Override
        public byte[] getContentType() { return mMimeType; }
        
        @Override
        public byte[] getOther() { return null; }
    }

    public static class UriFileFormItem extends FileFormItem {
        private Context mCtx;
        private Uri mUri;

        public UriFileFormItem(String inputName, Uri uri, Context ctx) {
            super(inputName, getFileNameFromUri(uri, ctx), getMimeTypeFromUri(uri, ctx),
                    null);
            this.mCtx = ctx;
            this.mUri = uri;
        }

        /**
         * @return the contents of the item. <input value="..." />
         */
        @Override
        public byte[] getValue() {
            if (value == null) {
                value = readAllBytesFromFileUri(mUri, mCtx);
            }
            return (byte[]) value;
        }
    }
    
    private static byte[] getUniqueBoundary(byte[] boundary, int newBoundaryLen) {
        byte[] result = new byte[newBoundaryLen];
        int resultOffset = 0;
        if (boundary != null) {
            resultOffset = Math.min(result.length, boundary.length);
            for (int i = 0; i < resultOffset; i++) {
                result[i] = boundary[i];
            }
        }
        for (int i = resultOffset; i < result.length; i++) {
            int rndNum = getRndInt(0, 62);
            if (rndNum >= 52) {
                rndNum = '0' + (rndNum - 52);
            } else if (rndNum >= 26) {
                rndNum = 'a' + (rndNum - 26);
            } else {
                rndNum = 'A' + rndNum;
            }
            result[i] = (byte) rndNum;
        }
        return result;
    }

    private static int[] getKMPJmpTable(byte[] patern) {
        int[] result = new int[patern.length];
        for (int i = 1, j = 0; i < patern.length; i++) {
            if (patern[i] == patern[j]) {
                j += 1;
                result[i] = j;
            } else {
                j = 0;
            }
        }
        return result;
    }
    
    private static int kmpIndexOfSubstring(byte[] data, int dataOffset, byte[] patern, int[] kmpJmpTable) {
        int iData = dataOffset;
        int iPatern = 0;
        
        while (iData < data.length) {
            if (data[iData] != patern[iPatern]) {
                if (iPatern == 0) {
                    iData += 1;
                } else {
                    iPatern = kmpJmpTable[iPatern - 1];
                }
            } else {
                iData += 1;
                iPatern += 1;
                if (iPatern >= patern.length) {
                    return iData - patern.length;
                }
            }
        }
        
        return -1;
    }
    
    private static byte[] getBoundary(List<FormItem> formItems) {
        int contentLength = 0;
        byte[] boundary = getUniqueBoundary(null, 30);
        int[] kmpJmpTable = getKMPJmpTable(boundary);
        
        for (FormItem item : formItems) {
            for (byte[] part : new byte[][] {item.getHeader(), item.getValue()}) {
                contentLength += part.length;
                
                int matchIndex = 0;
                while (true) {
                    matchIndex = kmpIndexOfSubstring(part, matchIndex, boundary, kmpJmpTable);
                    if (matchIndex == -1) {
                        break;
                    }
                    boundary = getUniqueBoundary(boundary, boundary.length + 5);
                    kmpJmpTable = getKMPJmpTable(boundary);
                }
            }
        }
        
        // "--<boundaries>".length * (1 + <Number of boundaries needed>) + "--\r\n".length
        // "--".length because the last boundary has these at the end
        contentLength += (2 + boundary.length) * (1 + formItems.size()) + 4;
        return boundary;
    }

    /**
     * Constructor which sets the default readBuffer size to 4096 bytes.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url the url to send the request to.
     * @param postData the HTML form data.
     */
    public SimplePOSTRequest(String url, List<FormItem> postData) {
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
    public SimplePOSTRequest(String url, List<FormItem> postData, int bufferLength) {
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
    public SimplePOSTRequest(String url, List<FormItem> postData, int bufferLength, int connectionTimeout, int readTimeout) {
        super(url, bufferLength, connectionTimeout, readTimeout);
        mPostData = postData;
    }

    /**
     * Runs the request and reads the data returned by the remote host.
     *
     * @param recvBuffer the readBuffer to use if not null, else a readBuffer of the supplied
     *               {@code bufferLength} will be created.
     */
     @Override
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

        mBoundary = getBoundary(mPostData);
        try {
            recvArgs.connection.setRequestMethod("POST");
            recvArgs.connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="
                    + new String(mBoundary, StandardCharsets.US_ASCII));
            recvArgs.connection.setRequestProperty("Cookie", "PHPSESSID=5vnoc7gkjtlopob841an5g8km");
            // recvArgs.connection.setRequestProperty("Content-Length", "20");
            recvArgs.connection.setDoOutput(true);
            recvArgs.connection.setDoInput(true);
            recvArgs.connection.connect();
            recvArgs.openOutputStream();
            sendPostData(recvArgs);
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

    private void sendPostData(SimpleRequest.SendRecvHandler recvArgs) throws IOException {
        OutputStream out = recvArgs.getOutputStream();
        
        ByteArrayOutputStream bArray = new ByteArrayOutputStream(mBoundary.length + 6);
        bArray.write((byte) '-');
        bArray.write((byte) '-');
        bArray.write(mBoundary, 0, mBoundary.length);
        
        for (FormItem item : mPostData) {
            bArray.writeTo(out);
            out.write(FormItem.NEW_LINE);
            out.write(item.getHeader());
            out.write(item.getValue());
            out.write(FormItem.NEW_LINE);
        }
        
        bArray.write((byte) '-');
        bArray.write((byte) '-');
        bArray.writeTo(out);
        out.write(FormItem.NEW_LINE);
        out.flush();
        out.close();
    }
}
