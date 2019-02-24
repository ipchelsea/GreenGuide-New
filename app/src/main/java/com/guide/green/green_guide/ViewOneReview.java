package com.guide.green.green_guide;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.guide.green.green_guide.Fragments.AirReviewFragment;
import com.guide.green.green_guide.Fragments.GeneralInfoFragment;
import com.guide.green.green_guide.Fragments.GeneralReviewFragment;
import com.guide.green.green_guide.Fragments.WasteReviewFragment;
import com.guide.green.green_guide.Fragments.WaterReviewFragment;
import com.guide.green.green_guide.Utilities.OneReviewFetchReviews;
import com.guide.green.green_guide.Utilities.OneReviewTabsPagerAdapter;
import com.guide.green.green_guide.Utilities.Review;

import static com.guide.green.green_guide.Utilities.Review.AirWaste.Key.PHYSICAL_PROBS;
import static com.guide.green.green_guide.Utilities.Review.AirWaste.Key.PM10;
import static com.guide.green.green_guide.Utilities.Review.AirWaste.Key.PM2_5;
import static com.guide.green.green_guide.Utilities.Review.AirWaste.Key.SMOKE_CHECK;
import static com.guide.green.green_guide.Utilities.Review.AirWaste.Key.VISIBILITY;
import static com.guide.green.green_guide.Utilities.Review.Location.Key.ADDRESS;
import static com.guide.green.green_guide.Utilities.Review.Location.Key.CITY;
import static com.guide.green.green_guide.Utilities.Review.Location.Key.COMPANY;
import static com.guide.green.green_guide.Utilities.Review.Location.Key.INDUSTRY;
import static com.guide.green.green_guide.Utilities.Review.Location.Key.PRODUCT;
import static com.guide.green.green_guide.Utilities.Review.Location.Key.RATING;
import static com.guide.green.green_guide.Utilities.Review.Location.Key.TIME;
import static com.guide.green.green_guide.Utilities.Review.Location.Key.WEATHER;
import static com.guide.green.green_guide.Utilities.Review.SolidWaste.Key.AMOUNT;
import static com.guide.green.green_guide.Utilities.Review.SolidWaste.Key.MEASUREMENTS;
import static com.guide.green.green_guide.Utilities.Review.SolidWaste.Key.WASTE_TYPE;
import static com.guide.green.green_guide.Utilities.Review.WaterIssue.Key.FLOAT_TYPE;
import static com.guide.green.green_guide.Utilities.Review.WaterIssue.Key.PH;
import static com.guide.green.green_guide.Utilities.Review.WaterIssue.Key.TURB_SCORE;
import static com.guide.green.green_guide.Utilities.Review.WaterIssue.Key.WATER_BODY;
import static com.guide.green.green_guide.Utilities.Review.WaterIssue.Key.WATER_COLOR;

public class ViewOneReview extends AppCompatActivity implements
        GeneralReviewFragment.OnGeneralReviewFragmentListener,
        WaterReviewFragment.OnWaterReviewFragmentListener,
        AirReviewFragment.OnAirReviewFragmentListener,
        WasteReviewFragment.OnWasteReviewFragmentListener {

    private String location;
    private double longitude;
    private double latitude;
    private int reviewIndex;
    private Review mReview;
    private int fragmentsLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_one_review);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TabLayout tabs = (TabLayout) findViewById(R.id.one_review_tabs);

        ViewPager pager = (ViewPager) findViewById(R.id.one_review_pager);
        OneReviewTabsPagerAdapter adapter =
                new OneReviewTabsPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        tabs.setupWithViewPager(pager);

        location = getIntent().getStringExtra("location");
        longitude = getIntent().getDoubleExtra("longitude", 0);
        latitude = getIntent().getDoubleExtra("latitude", 0);
        reviewIndex = getIntent().getIntExtra("reviewIndex", 0);

        getSupportActionBar().setTitle(location);

        fragmentsLoaded = 0;

        OneReviewFetchReviews fetchReviews =
           new OneReviewFetchReviews(this, location, longitude, latitude, reviewIndex);

        fetchReviews.setReviewFetchReviewsListener(new OneReviewFetchReviews.ReviewFetchReviewsListener() {
            @Override
            public void onReviewsLoaded(Review review) {
                loadReview(review);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof GeneralReviewFragment) {
            GeneralReviewFragment frag = (GeneralReviewFragment) fragment;
            frag.setOnGeneralReviewFragmentListener(this);
        } else if (fragment instanceof WaterReviewFragment) {
            WaterReviewFragment frag = (WaterReviewFragment) fragment;
            frag.setOnWaterReviewFragmentListener(this);
        } else if (fragment instanceof AirReviewFragment) {
            AirReviewFragment frag = (AirReviewFragment) fragment;
            frag.setOnAirReviewFragmentListener(this);
        } else if (fragment instanceof WasteReviewFragment) {
            WasteReviewFragment frag = (WasteReviewFragment) fragment;
            frag.setOnWasteReviewFragmentListener(this);
        }
    }

    public void onGeneralReviewInteraction() {
        if (mReview != null) {
            initGeneralAndPhotosFragment(mReview);
        }
    }

    public void onWaterReviewInteraction() {
        if (mReview != null) {
            initWaterFragment(mReview);
        }
    }

    public void onAirReviewInteraction() {
        if (mReview != null) {
            initAirFragment(mReview);
        }
    }

    public void onWasteReviewInteraction() {
        if (mReview != null) {
            initWasteFragment(mReview);
        }
    }

    public void loadReview(Review review) {
        this.mReview = review;
        initGeneralAndPhotosFragment(mReview);
        Log.d("ViewOneReview", "Review Loaded");
    }

    public void initGeneralAndPhotosFragment(Review review) {

        TextView nameTextView = findViewById(R.id.general_review_name);
        TextView ratingTextView = findViewById(R.id.general_review_rating);
        TextView addressTextView = findViewById(R.id.general_review_address);
        TextView cityTextView = findViewById(R.id.general_review_city);
        TextView industryTextView = findViewById(R.id.general_review_industry);
        TextView productTextView = findViewById(R.id.general_review_product);
        TextView dateTextView = findViewById(R.id.general_review_date);
        TextView timeTextView = findViewById(R.id.general_review_time);
        TextView weatherTextView = findViewById(R.id.general_review_weather);

        nameTextView.setText(review.location.get(COMPANY));
        ratingTextView.setText(review.location.get(RATING));
        addressTextView.setText(review.location.get(ADDRESS));
        cityTextView.setText(review.location.get(CITY));
        industryTextView.setText(review.location.get(INDUSTRY));
        productTextView.setText(review.location.get(PRODUCT));

        String timeAndDate = review.location.get(TIME);
        dateTextView.setText(timeAndDate.substring(0,10));
        timeTextView.setText(timeAndDate.substring(12,16));

        weatherTextView.setText(review.location.get(WEATHER));
    }

    public void initWaterFragment(Review review) {

        TextView bodyTypeTextView = findViewById(R.id.water_review_body);
        TextView colorTextView = findViewById(R.id.water_review_color);
        TextView turbidityTextView = findViewById(R.id.water_review_turbidity);
        TextView odorTextView = findViewById(R.id.water_review_odor);
        TextView floatTextView = findViewById(R.id.water_review_float);
        TextView phTextView = findViewById(R.id.water_review_ph);

        bodyTypeTextView.setText(review.waterIssue.get(WATER_BODY));
        colorTextView.setText(review.waterIssue.get(WATER_COLOR));
        turbidityTextView.setText(review.waterIssue.get(TURB_SCORE));
        odorTextView.setText(review.waterIssue.get(Review.WaterIssue.Key.ODOR));
        floatTextView.setText(review.waterIssue.get(FLOAT_TYPE));
        phTextView.setText(review.waterIssue.get(PH));
    }

    public void initAirFragment(Review review) {

        //AirReviewFragment airReviewFragment = (AirReviewFragment) getSupportFragmentManager().find

        TextView visibilityTextView = findViewById(R.id.air_review_visibility);
        TextView odorTextView = findViewById(R.id.air_review_odor);
        TextView smokeTextView = findViewById(R.id.air_review_smoke);
        TextView discomfortTextView = findViewById(R.id.air_review_discomfort);
        TextView pm2_5TextView = findViewById(R.id.air_review_ph2_5);
        TextView pm10TextView = findViewById(R.id.air_review_ph10);

        visibilityTextView.setText(review.airWaste.get(VISIBILITY));
        odorTextView.setText(review.airWaste.get(Review.AirWaste.Key.ODOR));
        smokeTextView.setText(review.airWaste.get(SMOKE_CHECK));
        discomfortTextView.setText(review.airWaste.get(PHYSICAL_PROBS));
        pm2_5TextView.setText(review.airWaste.get(PM2_5));
        pm10TextView.setText(review.airWaste.get(PM10));
    }

    public void initWasteFragment(Review review) {

        TextView typeTextView = findViewById(R.id.waste_review_type);
        TextView amountTextView = findViewById(R.id.waste_review_amount);
        TextView odorTextView = findViewById(R.id.waste_review_odor);
        TextView infoTextView = findViewById(R.id.waste_review_info);

        typeTextView.setText(review.solidWaste.get(WASTE_TYPE));
        amountTextView.setText(review.solidWaste.get(AMOUNT));
        odorTextView.setText(review.solidWaste.get(Review.SolidWaste.Key.ODOR));
        infoTextView.setText(review.solidWaste.get(MEASUREMENTS));
    }
}
