package com.guide.green.green_guide.Utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Retrieved JSON data from the provided URLs.
 */
public class AsyncGetImage extends AsyncTask<String, Object, Bitmap> {
    public Bitmap bmpResult;

    @Override
    protected final Bitmap doInBackground(String... strings) {
        SimpleImageGETRequest spg = new SimpleImageGETRequest(strings[0]);
        spg.send();
        Bitmap result = BitmapFactory.decodeByteArray(spg.mImageData, 0, spg.mImageData.length);
        return result;
    }
}