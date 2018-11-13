package com.guide.green.green_guide.Utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrieved JSON data from the provided URLs.
 */
public class AsyncPostRequest extends AsyncTask<List<SimplePOSTRequest.FormItem>, Object, StringBuilder> {
    public Bitmap bmpResult;
    private String mUrl;
    public AsyncPostRequest(String url) {
        mUrl = url;
    }

    @Override
    protected final StringBuilder doInBackground(List<SimplePOSTRequest.FormItem>... formItems) {
        SimplePOSTRequest spg = new SimplePOSTRequest(mUrl, formItems[0]);
        spg.send();
        return spg.getResult();
    }
}