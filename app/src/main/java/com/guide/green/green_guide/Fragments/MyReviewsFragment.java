package com.guide.green.green_guide.Fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.AsyncGetImage;

public class MyReviewsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.fragment_my_reviews, null);


        // https:sallysbakingaddiction.com/wp-content/uploads/2017/06/american-flag-pie.jpg
        String scheme = "https";
        String athority = "sallysbakingaddiction.com";
        String path = "/wp-content/uploads/2017/06/american-flag-pie.jpg";
        String url = scheme + "://" + athority + path;

        final ImageView imgV = v.findViewById(R.id.abcTest);
        (new AsyncGetImage() {
            @Override
            public final void onPostExecute(Bitmap bmp) {
                imgV.setImageBitmap(bmp);
            }
        }).execute(url);

        // Throws a file not found exception, network paths don't work
//        final ImageView imgV2 = v.findViewById(R.id.abcTest2);
//        Uri uri = new Uri.Builder().scheme(scheme).authority(athority).path(path).build();
//        imgV2.setImageURI(uri);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
