package com.guide.green.green_guide;


import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.guide.green.green_guide.Dialogs.CityPickerDialog;
import com.guide.green.green_guide.Dialogs.CityPickerDialog2;
import com.guide.green.green_guide.Fragments.AboutFragment;
import com.guide.green.green_guide.Fragments.GuidelinesFragment;
import com.guide.green.green_guide.Fragments.LogInOutFragment;
import com.guide.green.green_guide.Fragments.MyReviewsFragment;
import com.guide.green.green_guide.Fragments.SignUpFragment;
import com.guide.green.green_guide.Fragments.UserGuideFragment;
import com.guide.green.green_guide.Utilities.BaiduMapManager;
import com.guide.green.green_guide.Utilities.BottomSheetManager;
import com.guide.green.green_guide.Utilities.SuggestionSearchManager;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {
    // High level managers
    private BaiduMapManager mMapManager;
    private BottomSheetManager mBtmSheetManager;
    private Button citySelectionView = null;

    // Terrain selection
    FloatingActionButton normalMapView;
    FloatingActionButton satelliteMapView;
    boolean fabIsOpen = false;

    private DrawerLayout mDrawer;
    private NavigationView navigationView;
    private SuggestionSearchManager mSuggestionSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        initMapManager();
        initBottomSheet();
        initToolsAndWidgets();
        initLocationTracker();
    }

    public void initMapManager() {
        mMapManager = new BaiduMapManager((MapView) findViewById(R.id.map));
    }

    /**
     * {@code mMapManager} must be initialized before calling this.
     */
    public void initLocationTracker() {
        FloatingActionButton btnMyLocation = (FloatingActionButton) findViewById(R.id.myLocation);

        TrackLocationHandler locationHandler =
                new TrackLocationHandler(this, btnMyLocation, mMapManager.BAIDU_MAP);

        btnMyLocation.setOnClickListener(locationHandler);
    }

    /**
     * {@code mMapManager} must be initialized before calling this.
     */
    private void initBottomSheet() {
        // Get Relevant Bottom Sheet Views
        BottomSheetManager.Reviews reviews = new BottomSheetManager.Reviews();

        reviews.container = (ViewGroup) findViewById(R.id.btmSheetReviewsContainer);
        reviews.firstReviewButton = (Button) findViewById(R.id.btmSheetWriteReview);

        reviews.peekBar.companyName = (TextView) findViewById(R.id.previewCompanyName);
        reviews.peekBar.ratingValue = (TextView) findViewById(R.id.btmSheetRatingValue);
        reviews.peekBar.ratingStars = (ImageView) findViewById(R.id.btmSheetRatingStars);
        reviews.peekBar.ratingCount = (TextView) findViewById(R.id.btmSheetRatingsCount);

        reviews.body.container = (ViewGroup) findViewById(R.id.btmSheetReviewBody);
        reviews.body.address = (TextView) findViewById(R.id.btmSheetAddress);
        reviews.body.city = (TextView) findViewById(R.id.btmSheetCityName);
        reviews.body.industry = (TextView) findViewById(R.id.btmSheetIndustry);
        reviews.body.product = (TextView) findViewById(R.id.btmSheetProduct);
        reviews.body.histogram = (ImageView) findViewById(R.id.btmSheetHistogram);
        reviews.body.reviews = (ViewGroup) findViewById(R.id.userReviewList);

        BottomSheetManager.PoiSearchResults poiResults = new BottomSheetManager.PoiSearchResults();
        poiResults.container = (ViewGroup) findViewById(R.id.poiResultsSwipeView);
        poiResults.swipeView = (ViewPager) findViewById(R.id.poiResultsSwipeView);

        // Initialize Bottom Sheet Manager
        NestedScrollView btmSheet = (NestedScrollView) findViewById(R.id.btmSheet);
        mBtmSheetManager = new BottomSheetManager(this, btmSheet, reviews, poiResults,
                mMapManager);
        mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
    }

    /**
     * Initializes the tool bar and the floating buttons.
     */
    private void initToolsAndWidgets() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        normalMapView = findViewById(R.id.normalfab);
        satelliteMapView = findViewById(R.id.satellitefab);
        FloatingActionButton fab = findViewById(R.id.fab);

        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFabActions(view);
            }
        });
    }

    private void hideFabItems() {
        normalMapView.hide();
        satelliteMapView.hide();
        normalMapView.setClickable(false);
        satelliteMapView.setClickable(false);
        fabIsOpen = false;
    }

    private void handleFabActions(final View view) {
        if (fabIsOpen) {
            hideFabItems();
        }
        else {
            normalMapView.show();
            satelliteMapView.show();
            normalMapView.setClickable(true);
            satelliteMapView.setClickable(true);
            fabIsOpen = true;

            normalMapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMapManager.setMapType(BaiduMapManager.MapType.NORMAL);
                    hideFabItems();
                }
            });
            satelliteMapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMapManager.setMapType(BaiduMapManager.MapType.SATELLITE);
                    hideFabItems();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.cityItem);
        citySelectionView = (Button) item.getActionView().findViewById(R.id.city);
        citySelectionView.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
//                final CityPickerDialog cpd = new CityPickerDialog();
                final CityPickerDialog2 cpd = new CityPickerDialog2();
                cpd.setSelectedCityButton(citySelectionView);
                cpd.show(getSupportFragmentManager(), "Pick a City");
            }
        });

        item = menu.findItem(R.id.searchItem);
        AutoCompleteTextView searchView =
                (AutoCompleteTextView) item.getActionView().findViewById(R.id.search);

        mSuggestionSearch = new SuggestionSearchManager(this, searchView, citySelectionView,
                mMapManager, mBtmSheetManager);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        switch (id) {
            case R.id.my_reviews:
                fragment = new MyReviewsFragment();
                break;
            case R.id.guidelines:
                fragment = new GuidelinesFragment();
                break;
            case R.id.about:
                fragment = new AboutFragment();
                break;
            case R.id.user_guide:
                fragment = new UserGuideFragment();
                break;
            case R.id.sign_up:
                fragment = new SignUpFragment();
                break;
            case R.id.log_in_out:
                fragment = new LogInOutFragment();
                break;
        }

        if (fragment != null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.drawer_layout, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapManager.onDestroy();
    }

    public void doStuff(View view) {
    }

}