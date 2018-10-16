package com.guide.green.green_guide.Fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiResult;
import com.guide.green.green_guide.PoiOverlay;
import com.guide.green.green_guide.R;

import java.util.Date;
import java.util.List;

public class BtmSheetPoiResultPage extends Fragment {
    public interface ViewCreated {
        void onCreateView(BtmSheetPoiResultPage fragment, int pageNumber);
    }

    public interface Clicked {
        void onClicked(PoiInfo poi);
    }

    private ViewCreated mViewCreated;
    private Clicked mViewClicked;
    private int pageNum;
    private long time;

    public void setViewCreatedHandler(ViewCreated handler) {
        mViewCreated = handler;
    }
    public void setViewClickedHandler(Clicked handler) {
        mViewClicked = handler;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mViewCreated != null) {
            mViewCreated.onCreateView(this, pageNum);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        pageNum = getArguments().getInt("pageNumber", -1);
        time = getArguments().getLong("time", -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        return inflater.inflate(R.layout.bottom_sheet_poi_result_page, container, false);
    }

    public void addResultData(final PoiResult poiResult) {
        ViewGroup container = ((ViewGroup) getView());
        if (container == null) { return; }
        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        int iconNumber = 1;
        for (PoiInfo result : poiResult.getAllPoi()) {
            LinearLayout listItem = (LinearLayout) inflater.inflate(
                    R.layout.bottom_sheet_poi_list_item, container, false);
            Bitmap bmp = BitmapDescriptorFactory.fromAssetWithDpi("Icon_mark"
                    + (iconNumber) + ".png").getBitmap();

            ((ImageView) listItem.findViewById(R.id.poi_icon)).setImageBitmap(bmp);
            ((TextView) listItem.findViewById(R.id.poi_name)).setText("Name: " + result.name);
            ((TextView) listItem.findViewById(R.id.poi_address)).setText("Address: " + result.address);
            ((TextView) listItem.findViewById(R.id.poi_city)).setText("City: " + result.city);
            ((TextView) listItem.findViewById(R.id.poi_lanAndLat)).setText("Cordinate: (" + result.location.toString() + ")");
            ((Button) listItem.findViewById(R.id.poi_select)).setOnClickListener(new ViewClickHandler(poiResult, iconNumber - 1));
            container.addView(listItem);
            iconNumber++;
        }

        ((ContentLoadingProgressBar) container.findViewById(R.id.poi_loading)).hide();
    }

    public class ViewClickHandler implements View.OnClickListener {
        private PoiResult poiResult;
        private int index;
        public ViewClickHandler(PoiResult poiResult, int index) {
            this.poiResult = poiResult;
            this.index = index;
        }
        @Override
        public void onClick(View view) {
            if (mViewClicked != null) {
                mViewClicked.onClicked(poiResult.getAllPoi().get(index));
            }
        }
    }

    public static BtmSheetPoiResultPage newInstance(int pageNumber) {
        BtmSheetPoiResultPage frag = new BtmSheetPoiResultPage();
        Bundle b = new Bundle();
        b.putInt("pageNumber", pageNumber);
        frag.setArguments(b);
        return frag;
    }
}
