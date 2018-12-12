package com.guide.green.green_guide.Utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Random;


/**
 * Contains miscellaneous useful functions which don't have large enough number to make their own
 * class.
 */
public class Misc {
    private static Random rndObj = new Random();

    /**
     * Hides the virtual keyboard without taking away focus from the view that is using its input.
     *
     * @param view  the view which caused the keyboard to appear
     * @param ctx   the context of the view
     */
    public static void hideKeyboard(@NonNull View view, @NonNull Context ctx) {
        InputMethodManager imm = (InputMethodManager)
                ctx.getSystemService(ctx.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * @param min the smallest number to return.
     * @param max the largest number to return.
     * @return a random number between the {@code min} and {@code max} inclusive of both.
     */
    public static int getRndInt(int min,int max) {
        return rndObj.nextInt((max - min) + 1) + min;
    }

    /**
     * Given a {@code Uri} object. This method returns the name of the file described by that
     * object.
     * @param uri a uri pointing to a file on the device
     * @param context used to get a getContentResolver
     * @return the file name or null
     */
    public static String getFileNameFromUri(Context context, Uri uri) {
        String result = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
            cursor.close();
        }
        return result;
    }

    /**
     * Given a {@code Uri} object. This method returns the MIME type of the file.
     *
     * @param uri a uri pointing to a file on the device
     * @param context used to get a getContentResolver
     * @return the file name or null
     */
    public static String getMimeTypeFromUri(Context context, Uri uri) {
        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    /**
     * Given a {@code Uri} object. This method returns all of the bytes of the file.
     *
     * @param context used to get a getContentResolver
     * @param uri a uri pointing to a file on the device
     * @return the file name or null
     */
    public static byte[] readAllBytesFromFileUri(Context context, Uri uri) {
        try {
            InputStream iStream = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len;
            while ((len = iStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            iStream.close();
            return byteBuffer.toByteArray();
        } catch (IOException e) {
            /* Do Nothing */
        }
        return null;
    }

    /**
     * Gets the file size of a file on the device.
     *
     * @param context the context used to acquire a {@code getContentResolver}
     * @param uri a file on this device
     * @return the size of the file of -1 if the size could not be obtained
     */
    public static long getFileSize(Context context, Uri uri) {
        long result = -1;
        Cursor cursor =
                context.getContentResolver().query(uri, null, null, null, null);
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        if (cursor.moveToFirst()) {
            result = cursor.getLong(sizeIndex);
        }
        cursor.close();
        return result;
    }
}
