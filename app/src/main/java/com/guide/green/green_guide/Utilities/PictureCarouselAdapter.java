package com.guide.green.green_guide.Utilities;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.guide.green.green_guide.HTTPRequest.AbstractRequest;
import com.guide.green.green_guide.HTTPRequest.AsyncRequest;
import com.guide.green.green_guide.R;

import java.util.List;

public class PictureCarouselAdapter extends RecyclerView.Adapter<PictureCarouselAdapter.CarouselViewHolder> {
    private List<String> mImageUrls;
    private Bitmap mBmps[];
    public Context mContext;

    public static class CarouselViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public ProgressBar progress;
        public Button retry;
        public int position;
        public void showOnlyImage() {
            image.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            retry.setVisibility(View.GONE);
        }
        public void showOnlyRetry() {
            image.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            retry.setVisibility(View.VISIBLE);
        }
        public void showOnlyProgress() {
            image.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            retry.setVisibility(View.GONE);
        }
        public CarouselViewHolder(View itemView) {
            super(itemView);
        }
    }

    public PictureCarouselAdapter(Context context, @NonNull List<String> imgUrls) {
        mContext = context;
        mImageUrls = imgUrls;
        mBmps = new Bitmap[mImageUrls.size()];
    }


    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewGroup root = (ViewGroup) LayoutInflater.from(mContext).inflate(
                R.layout.fragment_carousel_picture, parent, false);
        final CarouselViewHolder viewHolder = new CarouselViewHolder(root);
        viewHolder.image = root.findViewById(R.id.carousel_img);
        viewHolder.progress = root.findViewById(R.id.carousel_img_progress);
        viewHolder.retry = root.findViewById(R.id.carousel_img_retry);
        viewHolder.retry.findViewById(R.id.carousel_img_retry).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestImageHandler(viewHolder.getAdapterPosition(), viewHolder);
                    }
                });
        return viewHolder;
    }

    private void requestImageHandler(final int imgPosition, final CarouselViewHolder holder) {
        AsyncRequest.getImage("http://www.lovegreenguide.com/" + mImageUrls.get(imgPosition),
                new AbstractRequest.OnRequestResultsListener<Bitmap>() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        mBmps[imgPosition] = bitmap;
                        if (holder.getAdapterPosition() == imgPosition) {
                            holder.image.setImageBitmap(bitmap);
                            holder.showOnlyImage();
                        }
                    }
                    @Override
                    public void onError(Exception error) {
                        holder.showOnlyRetry();
                        Log.i("--onError_CarouselPic", error.toString());
                        error.printStackTrace();
                    }
                });
    }

    @Override
    public void onBindViewHolder(final @NonNull CarouselViewHolder holder, int position) {
        if (mBmps[position] != null) {
            holder.image.setImageBitmap(mBmps[position]);
            holder.showOnlyImage();
        } else {
            holder.showOnlyProgress();
            holder.image.setImageBitmap(null);
            requestImageHandler(position, holder);
        }
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("onBindViewHolder>>>>", "CLICKED IMAGE " + holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImageUrls.size();
    }
}
