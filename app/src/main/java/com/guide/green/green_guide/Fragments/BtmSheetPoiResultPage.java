package com.guide.green.green_guide.Fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiResult;
import com.guide.green.green_guide.R;

/**
 * A fragment which represents one page of results for a POI search. It manages the items
 * listed on its page.
 */
public class BtmSheetPoiResultPage extends Fragment {
    private OnRetrySearchListener mRetrySearchListener;
    private ViewCreatedListener mViewCreatedListener;
    private OnItemClickedListener mViewClickedListener;
    private PoiResult mPoiResult;
    private int mPageNum;

    /**
     * Callback interface invoked when the Fragments onCreateView method has been called.
     * Used to check know when {@code addResultData} can be called.
     */
    public interface ViewCreatedListener {
        /**
         * A callback called when the fragment's {@code onActivityCreated} method is invoked.
         * @param fragment A reference to {@code this} object.
         * @param pageNumber the # of the results page this object was created to represent.
         */
        void onCreateView(BtmSheetPoiResultPage fragment, int pageNumber);
    }

    /**
     * Callback interface invoked when the retry button is pressed. Used because this object
     * does not have a way for getting BAIDU map search results.
     */
    public interface OnRetrySearchListener {
        /**
         * Is called when the users clicked the retry button. The retry button is available when
         * an error is encountered and {@code onErrorEncountered} is called with the appropriate
         * arguments.
         *
         * @param pageNumber The page that the the clicked button is in.
         */
        void onRetrySearch(int pageNumber);
    }

    /**
     * Callback interface called when the "Select" button of one of the items in the list is
     * clicked.
     */
    public interface OnItemClickedListener {
        /**
         * A callback called when one of the items of this page is clicked.
         * @param poi Information about the clicked point of interest.
         */
        void onClicked(PoiInfo poi);
    }

    // Setters
    public void setOnRetrySearchListener(OnRetrySearchListener listener) {
        mRetrySearchListener = listener;
    }
    public void setOnViewCreatedListener(ViewCreatedListener listener) {
        mViewCreatedListener = listener;
    }
    public void setOnItemClickedListener(OnItemClickedListener listener) {
        mViewClickedListener = listener;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mViewCreatedListener != null) {
            mViewCreatedListener.onCreateView(this, mPageNum);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        mPageNum = getArguments().getInt("pageNumber", -1);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.bottom_sheet_poi_result_page,
                container, false);

        root.findViewById(R.id.poi_results_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRetrySearchListener != null) {
                    mRetrySearchListener.onRetrySearch(mPageNum);
                }
            }
        });

        return root;
    }

    /***
     * Must be called after {@code onCreateView} has already been called because it uses the
     * layout inflated by that method.
     *
     * @param poiResult an object returned by running a POI search.
     */
    public void addResultData(@NonNull final PoiResult poiResult) {
        ViewGroup container = ((ViewGroup) getView());
        if (container == null) { return; }
        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        mPoiResult = poiResult;

        int iconNumber = 1;
        for (PoiInfo result : mPoiResult.getAllPoi()) {
            LinearLayout listItem = (LinearLayout) inflater.inflate(
                    R.layout.bottom_sheet_poi_list_item, container, false);
            Bitmap bmp = BitmapDescriptorFactory.fromAssetWithDpi(String.format("Icon_mark%d.png",
                    iconNumber)).getBitmap();

            ((ImageView) listItem.findViewById(R.id.poi_icon)).setImageBitmap(bmp);
            ((TextView) listItem.findViewById(R.id.poi_name))
                    .setText(String.format("Name: %s", result.name));
            ((TextView) listItem.findViewById(R.id.poi_address))
                    .setText(String.format("Address: %s", result.address));
            ((TextView) listItem.findViewById(R.id.poi_city))
                    .setText(String.format("City: %s", result.city));
            ((TextView) listItem.findViewById(R.id.poi_lanAndLat))
                    .setText(String.format("Coordinate: (%s, %s)", result.location.longitude, result.location.latitude));

            listItem.findViewById(R.id.poi_select)
                    .setOnClickListener(new ListItemClickHandler(iconNumber - 1));
            container.addView(listItem);
            iconNumber++;
        }

        ((ContentLoadingProgressBar) container.findViewById(R.id.poi_results_loading)).hide();
        container.findViewById(R.id.poi_results_message).setVisibility(View.GONE);
        container.findViewById(R.id.poi_results_retry).setVisibility(View.GONE);
    }

    /**
     * Hides the loading bar and shows a message to the user explaining why there are no results.
     * @param message a description of the problem.
     */
    public void onErrorEncountered(@Nullable String message, boolean showRetryButton) {
        if (message == null) {
            message = "Unknown error encountered";
        }

        ViewGroup container = ((ViewGroup) getView());
        ((ContentLoadingProgressBar) container.findViewById(R.id.poi_results_loading)).hide();

        TextView msgView = container.findViewById(R.id.poi_results_message);
        msgView.setText(message);
        msgView.setVisibility(View.VISIBLE);

        if (showRetryButton) {
            container.findViewById(R.id.poi_results_retry).setVisibility(View.VISIBLE);
        } else {
            container.findViewById(R.id.poi_results_retry).setVisibility(View.GONE);
        }
    }

    /**
     * Class that calls the POI marker clicked callback when the corresponding item in the
     * list view is clicked.
     */
    public class ListItemClickHandler implements View.OnClickListener {
        private int mIndex;
        public ListItemClickHandler(int index) {
            this.mIndex = index;
        }
        @Override
        public void onClick(View view) {
            if (mViewClickedListener != null) {
                mViewClickedListener.onClicked(mPoiResult.getAllPoi().get(mIndex));
            }
        }
    }

    /**
     * Creates a result page fragment associated with the specified page number.
     * This page number should correspond to in index in the value supplied to
     * {@code addResultData}.
     *
     * @param pageNumber A number equal to or greater than 0.
     * @return New fragment representing the specified {@code pageNumber}.
     */
    public static BtmSheetPoiResultPage newInstance(int pageNumber) {
        BtmSheetPoiResultPage frag = new BtmSheetPoiResultPage();
        Bundle b = new Bundle();
        b.putInt("pageNumber", pageNumber);
        frag.setArguments(b);
        return frag;
    }
}
