package com.guide.green.green_guide.HTTPRequest;

import android.content.Context;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import static com.guide.green.green_guide.Utilities.Misc.getFileNameFromUri;
import static com.guide.green.green_guide.Utilities.Misc.getMimeTypeFromUri;
import static com.guide.green.green_guide.Utilities.Misc.readAllBytesFromFileUri;

/**
 * Describes a form item which would be found in HTML code as a <input>.
 */
public abstract class AbstractFormItem {
    private static final byte[] CONTENT_DISPOSITION = "Content-Disposition: form-data; name=\""
            .getBytes(StandardCharsets.US_ASCII);
    private static final byte[] FILE_NAME = "\"; filename=\"".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] CONTENT_TYPE = "Content-Type: ".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] NEW_LINE = "\r\n".getBytes(StandardCharsets.US_ASCII);
    private static final byte QUOTE = (byte) '\"';
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


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////Form Item Variations Bellow///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Describes a <input type="text"> tag.
     */
    public static class TextFormItem extends AbstractFormItem {
        public static final byte[] CONTENT_TYPE_UTF8_TEXT =
                "text/plain; charset=UTF-8".getBytes(StandardCharsets.US_ASCII);
        private final byte[] mValue;
        private final byte[] mName;

        /**
         * Constructor which sets the <input type="text" name="{@code name}" value="{@code value}">.
         * The value be encoded using UTF8 so unicode characters present in the value will be
         * preserved.
         *
         * @param name the name of the input.
         * @param value the text value of the input.
         */
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

    /**
     * Describes a <input type="file"> tag.
     */
    public static abstract class FileFormItem extends AbstractFormItem {
        public final byte[] mMimeType;
        private final byte[] mFileName;
        private final byte[] mName;
        protected byte[] mValue;

        /**
         * Constructor which sets the <input type="file" name="@{code inputName}">, the file's name
         * as reported to the server (should not be the full path), and stores a third value which
         * can be used freely by subclasses.
         *
         * @param inputName the name of the input.
         * @param fileName the name of the file.
         * @param value user defined storage.
         */
        public FileFormItem(String inputName, String fileName, byte[] value) {
            this(inputName, fileName, null, value);
        }

        public FileFormItem(String inputName, String fileName, String mimeType,
                            byte[] value) {
            mFileName = fileName.getBytes(StandardCharsets.UTF_8);
            mName = inputName.getBytes(StandardCharsets.UTF_8);

            if (mimeType == null) {
                mMimeType = null;
            } else {
                mMimeType = mimeType.getBytes(StandardCharsets.UTF_8);
            }

            mValue = value;
        }

        @Override
        public byte[] getName() { return mName; }

        @Override
        public byte[] getFileName() { return mFileName; }

        @Override
        public byte[] getContentType() { return mMimeType; }

        @Override
        public byte[] getOther() { return null; }

        /**
         * @return the contents of the item. <input value="..." />
         */
        @Override
        public byte[] getValue() {
            return mValue;
        }
    }

    /**
     * Describes a <input type="file"> tag.
     */
    public static class UriFileFormItem extends FileFormItem {
        /**
         * Constructor which sets the <input type="file" name="@{code inputName}">, the location on
         * the device to retrieve the file from, and a Context to use to read the file.
         *
         * @param inputName the filed name.
         * @param uri the location on this device where the file is located.
         * @param ctx a Context used to read the file.
         */
        public UriFileFormItem(String inputName, Uri uri, Context ctx) {
            super(inputName, getFileNameFromUri(ctx, uri), getMimeTypeFromUri(ctx, uri),
                    readAllBytesFromFileUri(ctx, uri));
        }

        /**
         * Constructor which sets the <input type="file" name="@{code inputName}">, the location on
         * the device to retrieve the file from, and a Context to use to read the file.
         *
         * @param inputName the filed name.
         * @param uri the location on this device where the file is located.
         * @param data the bytes of this file.
         * @param ctx a Context used to read the file.
         */
        public UriFileFormItem(String inputName, Uri uri, byte[] data, Context ctx) {
            super(inputName, getFileNameFromUri(ctx, uri), getMimeTypeFromUri(ctx, uri), data);
        }
    }
}
