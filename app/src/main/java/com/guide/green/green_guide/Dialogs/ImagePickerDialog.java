package com.guide.green.green_guide.Dialogs;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.guide.green.green_guide.Fragments.SingleImageFragment;
import com.guide.green.green_guide.R;

/**
 * Show a dialog which allows a user to select cancel or to click outside of the dialog to cancel.
 * The dialog shows a progress bar. This bar initially shown an indefinite status but if the
 * progress is known, this class offers a way to set the progress.
 */
public class ImagePickerDialog<T extends ImagePickerDialog.ImageTitlePair> extends DialogFragment
        implements ViewPager.OnPageChangeListener {
    private OnImagePickedListener<T> mOnImagePickedListener;
    private T mData[];
    private ImageListAdapter imgListAdapter;
    private RadioGroup scrollProgress;
    private int mCurrentPage;
    private TextView mTitle;
    private ViewPager mImagesPager;

    public static class ImageTitlePair {
        public String title;
        public Drawable image;
        public ImageTitlePair(String title, Drawable image) {
            this.title = title;
            this.image = image;
        }
    }

    public interface OnImagePickedListener<V> {
        void onImagePicked(V titleAndImage);
        void onCancel();
    }

    public void setOnImagePickedListener(OnImagePickedListener<T> listener) {
        mOnImagePickedListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstState) {
        View rootView = inflater.inflate(R.layout.dialog_image_picker, container);
        mImagesPager = rootView.findViewById(R.id.dialog_image_picker_picker);
        scrollProgress = rootView.findViewById(R.id.dialog_image_current);
        Button selectTime = rootView.findViewById(R.id.dialog_image_picker_select);
        Button cancel = rootView.findViewById(R.id.dialog_image_picker_cancel);
        mTitle = rootView.findViewById(R.id.dialog_image_title);

        imgListAdapter = new ImageListAdapter(getChildFragmentManager());
        mImagesPager.setAdapter(imgListAdapter);
        mImagesPager.addOnPageChangeListener(this);

        selectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnImagePickedListener != null) {
                    mOnImagePickedListener.onImagePicked(mData[mCurrentPage]);
                    mOnImagePickedListener = null;
                    dismiss();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        scrollProgress.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                int childCount = radioGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    if (radioGroup.getChildAt(i).getId() == checkedId) {
                        mImagesPager.setCurrentItem(i);
                        break;
                    }
                }
            }
        });
        setData(mData);
        return rootView;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mOnImagePickedListener != null) {
            mOnImagePickedListener.onCancel();
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        if (mOnImagePickedListener != null) {
            mOnImagePickedListener.onCancel();
        }
    }

    public void setData(T data[]) {
        mData = data;
        if (mData != null && scrollProgress != null) {
            mCurrentPage = -1;
            scrollProgress.removeAllViews();
            for (int i = 0; i < mData.length; i++) {
                scrollProgress.addView(new RadioButton(getContext()));
            }
            if (mData.length > 0) {
                onPageSelected(0);
            }
        }
        if (imgListAdapter != null) {
            imgListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        /* Do nothing */
    }

    /**
     * This method will be invoked when a new page becomes selected. Animation is not
     * necessarily complete.
     *
     * @param position Position index of the new selected page.
     */
    @Override
    public void onPageSelected(int position) {
        if (position == mCurrentPage) return;

        mCurrentPage = position;
        RadioButton button = (RadioButton) scrollProgress.getChildAt(position);
        button.setChecked(true);
        mTitle.setText(mData[position].title);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        /* Do nothing */
    }

    class ImageListAdapter extends FragmentPagerAdapter {
        public ImageListAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return the Fragment associated with a specified position.
         *
         * @param position
         */
        @Override
        public Fragment getItem(int position) {
            SingleImageFragment frag = new SingleImageFragment();
            frag.setImage(mData[position].image);
            return frag;
        }

        /**
         * Return the number of views available.
         */
        @Override
        public int getCount() {
            return mData.length;
        }
    }
}
