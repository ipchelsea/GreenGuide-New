package com.guide.green.green_guide.HTTPRequest;

import java.io.IOException;
import java.util.List;
import static com.guide.green.green_guide.Utilities.Misc.getRndInt;

/**
 * Provides a way to run a single GET request.
 */
public abstract class AbstractPOSTRequest extends GetText {
    protected final List<AbstractFormItem> mPostData;

    /**
     * Constructor which sets the default readBuffer size to 4096 bytes.
     * Defaults the connection timeout to 15 seconds and read timeout to 10 seconds.
     *
     * @param url the url to send the request to.
     * @param postData the HTML form data.
     */
    public AbstractPOSTRequest(String url, List<AbstractFormItem> postData) {
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
    public AbstractPOSTRequest(String url, List<AbstractFormItem> postData, int bufferLength) {
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
    public AbstractPOSTRequest(String url, List<AbstractFormItem> postData, int bufferLength,
                               int connectionTimeout, int readTimeout) {
        super(url, bufferLength, connectionTimeout, readTimeout);
        mPostData = postData;
    }

    /**
     * Method responsible for sending the {@code mPostData} values to the remote host.
     *
     * @param recvArgs an object used to send & receive data.
     * @throws IOException due to HttpConnection.send being called.
     */
    protected abstract void sendPostData(SendRecvHandler recvArgs)
            throws IOException;

    /**
     * Method responsible for setting the content type.
     *
     * @param recvArgs an object used to send & receive data.
     */
    protected abstract void setContentType(SendRecvHandler recvArgs);

    /**
     * Runs the request and reads the data returned by the remote host.
     *
     * @param recvBuffer the readBuffer to use if not null, else a readBuffer of the supplied
     *               {@code bufferLength} will be created.
     */
    @Override
    public void send(byte[] recvBuffer) {
        SendRecvHandler recvArgs;
        try {
            recvArgs = getSendRecvHandler(recvBuffer);
        } catch (IOException e) {
            mRequestState = RequestState.FINISHED;
            onError(e);
            return;
        }

        mRequestState = RequestState.PRE_OPEN_CONNECTION;
        recvArgs.connection.setInstanceFollowRedirects(true);
        recvArgs.connection.setConnectTimeout(connectionTimeout);
        recvArgs.connection.setReadTimeout(readTimeout);

        try {
            recvArgs.connection.setRequestMethod("POST");
            setContentType(recvArgs);
            assignHttpHeaders(recvArgs.connection);
            recvArgs.connection.setDoOutput(true);
            recvArgs.connection.setDoInput(true);
            mRequestState = RequestState.OPENING_CONNECTION;
            recvArgs.connection.connect();
            recvArgs.openOutputStream();
            sendPostData(recvArgs);
            recvArgs.openInputStream();
            recv(recvArgs);
        } catch (IOException e) {
            onError(e);
        } finally {
            mRequestState = RequestState.FINISHED;
            recvArgs.connection.disconnect();
        }
    }

    /**
     * Shrinks the supplied byte array or increases its size. In a shirk, only the data from the
     * removed indexes are lost. In a size increase, all of the data form inputted array are
     * preserved and the new indexs are filled with randomly generated variables.
     * The random data conforms to the regex [0-9a-zA-Z].
     *
     * @param boundary the original boundary to extend or shrink.
     * @param newBoundaryLen a non-negative value which will be the length of the returned array.
     * @return a resized version of the supplied array.
     */
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

    /**
     * Creates an array describing the length of over lapping substrings. For example, the string
     * A = 'abadabc' would result in the array B = [0,0,1,0,1,2,0]. The resulting array says that
     * Python Notation: if B[i] != 0 then A[0:B[i]] == A[i-B[i]+1:i+1] must be true.
     * So the the substring A[0:B[i]] equals the substring of length B[i] ending at the index i.
     * This method runs in O(n) time.
     *
     * @param pattern the array to check the substring of
     * @return an array describing the location and size of overlapping substrings.
     */
    private static int[] getKMPJmpTable(byte[] pattern) {
        int[] result = new int[pattern.length];
        for (int i = 1, j = 0; i < pattern.length; i++) {
            if (pattern[i] == pattern[j]) {
                j += 1;
                result[i] = j;
            } else {
                j = 0;
            }
        }
        return result;
    }

    /**
     * <p>
     *   * Searches the supplied {@code data} array for the sub-sequence {@code pattern}. If it is not
     * found then -1 is returned. If it is found, then the index to start the search from again is
     * returned. This method runs in O(n) time.
     * </p>
     * <p>
     * The returned index assumes that a search will be run with the same {@code pattern} which has
     * had data appended to its end.
     * </p>
     *
     *
     * @param data the data to search for the sub-sequence.
     * @param dataOffset the non-negative index to start the search from.
     * @param pattern the sub-sequence to search for.
     * @param kmpJmpTable the value returned by {@code getKMPJmpTable(pattern)}.
     * @return -1 if the sub-sequence was not found, else the value to set {@code dataOffset} if
     *         the same search is run with a {@code pattern} that has had data append to it.
     */
    private static int kmpIndexOfSubstring(byte[] data, int dataOffset, byte[] pattern,
                                           int[] kmpJmpTable) {
        int iPattern = 0;

        while (dataOffset < data.length) {
            if (data[dataOffset] != pattern[iPattern]) {
                if (iPattern == 0) {
                    dataOffset += 1;
                } else {
                    iPattern = kmpJmpTable[iPattern - 1];
                }
            } else {
                dataOffset += 1;
                iPattern += 1;
                if (iPattern >= pattern.length) {
                    return dataOffset - pattern.length;
                }
            }
        }

        return -1;
    }

    /**
     * Returns a unique byte sequence not contained in a the data of any of the supplied
     * {@code formItems}. This method runs in O(n) time.
     *
     * @param formItems the forms whose data to check.
     * @return a unique byte sequence not found in the supplied data.
     */
    protected static byte[] getBoundary(List<AbstractFormItem> formItems) {
//        int contentLength = 0;
        byte[] boundary = getUniqueBoundary(null, 30);
        int[] kmpJmpTable = getKMPJmpTable(boundary);

        for (AbstractFormItem item : formItems) {
            for (byte[] part : new byte[][] {item.getHeader(), item.getValue()}) {
//                contentLength += part.length;

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
//        contentLength += (2 + boundary.length) * (1 + formItems.size()) + 4;
        return boundary;
    }
}
