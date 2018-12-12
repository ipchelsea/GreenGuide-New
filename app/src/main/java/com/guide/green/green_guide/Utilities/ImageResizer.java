package com.guide.green.green_guide.Utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import com.guide.green.green_guide.HTTPRequest.AbstractFormItem;
import com.guide.green.green_guide.HTTPRequest.AbstractFormItem.FileFormItem;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reduces file size by reducing the image size of the supplied {@code Uri}s.
 * The output will be either a JPEG or PNG.
 * If a PNG is supplied then a PNG will be returned, else a JPEG will be returned.
 */
public class ImageResizer extends AsyncTask<Uri, Object, FileFormItem[]> {
    private Context mCtx;
    private String mFieldName;
    private final int MAX_FILE_SIZE;
    private static final int BYTES_PER_PIXEL = 4;

    /**
     * Default constructor which initializes all needed variables.
     *
     * @param ctx a context object
     * @param maxSize the maximum file size in bytes
     * @param inputName the name of the input: <input name="<THIS>" ...
     */
    public ImageResizer(Context ctx, int maxSize, String inputName) {
        mCtx = ctx;
        MAX_FILE_SIZE = maxSize;
        mFieldName = inputName;
    }

    private InputStream getFileStream(Uri filePath) {
        try {
            return mCtx.getContentResolver().openInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method sequentially handles each uri. Parallelism is not used to avoid exhausting
     * memory.
     *
     * @param uris a list of uris to load.
     * @return FileFormItem where the index of the parameter uri matched the index of the returned
     *         form item.
     */
    @Override
    protected FileFormItem[] doInBackground(Uri... uris) {
        FileFormItem[] results = new FileFormItem[uris.length];
        for (int i = 0; i < uris.length; i++) {
            long fileSize = Misc.getFileSize(mCtx.getApplicationContext(), uris[i]);
            InputStream iStream = getFileStream(uris[i]);
            if (iStream == null) return null;

            if (fileSize <= MAX_FILE_SIZE) {
                try {
                    // If all bytes are not read, no bytes are read
                    byte[] fileData = new byte[(int) fileSize];
                    if (iStream.read(fileData) == fileData.length) {
                        results[i] = new AbstractFormItem.UriFileFormItem(mFieldName, uris[i],
                                fileData, mCtx);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(iStream, null, options);
                int width = options.outWidth;
                int height = options.outHeight;
                Bitmap.CompressFormat compressionFormat = options.outMimeType.contains("png") ?
                        Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;

                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inJustDecodeBounds = false;
                options.inSampleSize = 1;

                while (width * height * BYTES_PER_PIXEL > MAX_FILE_SIZE) {
                    width /= 2;
                    height /= 2;
                    options.inSampleSize *= 2;
                }

                iStream = getFileStream(uris[i]);
                ByteArrayOutputStream bArrayOutStream = new ByteArrayOutputStream();
                Bitmap bmp = BitmapFactory.decodeStream(iStream, null, options);
                bmp.compress(compressionFormat, 100, bArrayOutStream);
                results[i] = new AbstractFormItem.UriFileFormItem(mFieldName, uris[i],
                        bArrayOutStream.toByteArray(), mCtx);
            }
        }

        return results;
    }
}
