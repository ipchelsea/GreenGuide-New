package com.guide.green.green_guide.Utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;


/**
 * Contains miscellaneous useful functions which don't have large enough number to make their own
 * class.
 */
public class Misc {
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


    private static Random rndObj;
    public static int getRndInt(int min,int max) {
        if (rndObj == null) {
            rndObj = new Random();
        }
        return rndObj.nextInt((max - min) + 1) + min;
    }

    public static String getFileNameFromUri(Uri uri, Context context) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static String getMimeTypeFromUri(Uri uri, Context context) {
        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public static byte[] readAllBytesFromFileUri(Uri fileUri, Context context) {
        try {
            InputStream iStream = context.getContentResolver().openInputStream(fileUri);
            byte[] result = getBytes(iStream);
            iStream.close();
            return result;
        } catch (IOException e) {
            /* Do Nothing */
        }

        return null;
    }

    private static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
