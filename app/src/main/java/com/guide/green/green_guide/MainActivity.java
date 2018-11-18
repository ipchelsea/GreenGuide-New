package com.guide.green.green_guide;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Button;
import android.widget.EditText;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;
import com.guide.green.green_guide.Dialogs.CityPickerDialog;
import com.guide.green.green_guide.Dialogs.CityPickerDialog.OnCitySelectedListener;
import com.guide.green.green_guide.Utilities.AsyncJSONArray;
import com.guide.green.green_guide.Utilities.BaiduMapManager;
import com.guide.green.green_guide.Utilities.BaiduSuggestion;
import com.guide.green.green_guide.Utilities.BottomSheetManager;
import com.guide.green.green_guide.Utilities.RomanizedLocation;
import com.guide.green.green_guide.Utilities.SuggestionSearchManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements OnCitySelectedListener,
        NavigationView.OnNavigationItemSelectedListener {
    // High level managers
    private BaiduMapManager mMapManager;
    private BottomSheetManager mBtmSheetManager;
    private Button mCitySelectionView = null;

    // Terrain selection
    FloatingActionButton normalMapView;
    FloatingActionButton satelliteMapView;
    boolean fabIsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.writeReview)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WriteReviewActivity.open(MainActivity.this);
            }
        });

        initMapManager();
        initBottomSheet();
        initToolsAndWidgets();
        initLocationTracker();
        getGreenGuidePoints();
    }

    public void initMapManager() {
        mMapManager = new BaiduMapManager((MapView) findViewById(R.id.map));
    }

    /**
     * {@code mMapManager} must be initialized before calling this.
     */
    public void initLocationTracker() {
        FloatingActionButton btnMyLocation = findViewById(R.id.myLocation);

        TrackLocationHandler locationHandler =
                new TrackLocationHandler(this, btnMyLocation, mMapManager.baiduMap);

        btnMyLocation.setOnClickListener(locationHandler);
    }

    /**
     * {@code mMapManager} must be initialized before calling this.
     */
    private void initBottomSheet() {
        // Get Relevant Bottom Sheet Views
        BottomSheetManager.Reviews reviews = new BottomSheetManager.Reviews();

        reviews.container = findViewById(R.id.btmSheetReviewsContainer);
        reviews.writeReviewButton = findViewById(R.id.btmSheetWriteReview);

        reviews.peekBar.companyName = findViewById(R.id.previewCompanyName);
        reviews.peekBar.ratingValue = findViewById(R.id.btmSheetRatingValue);
        reviews.peekBar.ratingStars = findViewById(R.id.btmSheetRatingStars);
        reviews.peekBar.ratingCount = findViewById(R.id.btmSheetRatingsCount);

        reviews.body.container = findViewById(R.id.btmSheetReviewBody);
        reviews.body.address = findViewById(R.id.btmSheetAddress);
        reviews.body.city = findViewById(R.id.btmSheetCityName);
        reviews.body.industry = findViewById(R.id.btmSheetIndustry);
        reviews.body.product = findViewById(R.id.btmSheetProduct);
        reviews.body.histogram = findViewById(R.id.btmSheetHistogram);
        reviews.body.reviews = findViewById(R.id.userReviewList);

        BottomSheetManager.PoiSearchResults poiResults = new BottomSheetManager.PoiSearchResults();
        poiResults.container = findViewById(R.id.poiResultsSwipeView);
        poiResults.swipeView = (ViewPager) poiResults.container;

        // Initialize Bottom Sheet Manager
        NestedScrollView btmSheet = findViewById(R.id.btmSheet);
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMapTypeFab();
            }
        });
    }

    private void hideMapTypeFabItems() {
        normalMapView.hide();
        satelliteMapView.hide();
        normalMapView.setClickable(false);
        satelliteMapView.setClickable(false);
        fabIsOpen = false;
    }

    private void showMapTypeFabItems() {
        normalMapView.show();
        satelliteMapView.show();
        normalMapView.setClickable(true);
        satelliteMapView.setClickable(true);
        fabIsOpen = true;
    }

    private void toggleMapTypeFab() {
        if (fabIsOpen) {
            hideMapTypeFabItems();
        } else {
            showMapTypeFabItems();
            normalMapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMapManager.setMapType(BaiduMapManager.MapType.NORMAL);
                    hideMapTypeFabItems();
                }
            });
            satelliteMapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMapManager.setMapType(BaiduMapManager.MapType.SATELLITE);
                    hideMapTypeFabItems();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mBtmSheetManager.getBottomSheetState() != BottomSheetBehavior.STATE_HIDDEN) {
            if (mBtmSheetManager.getBottomSheetState() == BottomSheetBehavior.STATE_COLLAPSED) {
                mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_bar, menu);

        MenuItem itemCity = menu.findItem(R.id.cityItem);
        mCitySelectionView = itemCity.getActionView().findViewById(R.id.city);
        mCitySelectionView.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                CityPickerDialog cityPickerDialog = new CityPickerDialog();
                cityPickerDialog.setOnCitySelectedListener(MainActivity.this);
                cityPickerDialog.show(getSupportFragmentManager(), "City Picker");
            }
        });

        MenuItem itemSearch = menu.findItem(R.id.searchItem);
        EditText searchInput = itemSearch.getActionView().findViewById(R.id.searchInput);
        RecyclerView searchDropDown = findViewById(R.id.searchDropDown);

        ViewGroup mapViewContainer = findViewById(R.id.mapViewContainer);
        ViewGroup searchDropDownContainer = findViewById(R.id.searchDropDownContainer);

        /* Not saved Intentionally */
        new SuggestionSearchManager(this, searchInput, searchDropDown, mCitySelectionView,
                mMapManager, mBtmSheetManager, itemSearch, mapViewContainer, searchDropDownContainer);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int itemId = item.getItemId();
        FragmentContainer.startActivity(this, itemId);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

    @Override
    public void onCitySelected(RomanizedLocation city) {
        mCitySelectionView.setText(city.name);
    }

    /* Temporary Method For Testing Things */
    public void doStuff(View view) {}

    public void getGreenGuidePoints() {
        new AsyncJSONArray(new AsyncJSONArray.OnAsyncJSONArrayResultListener() {
            @Override
            public void onFinish(ArrayList<JSONArray> jArrays, ArrayList<Exception> exceptions) {
                final ArrayList<GreenGuideLocation> pos = new ArrayList<>();
                JSONArray jsonArray = jArrays.get(0);
                if (jsonArray == null) {
                    return;
                }
                try {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jObj = jsonArray.getJSONObject(i);
                        String lng = jObj.getString("lng");
                        String lat = jObj.getString("lat");
                        String avrg = jObj.getString("avg_r");
                        String company = jObj.getString("company");
                        String address = jObj.getString("address");
                        String city = jObj.getString("city");
                        if (!lng.equals("") && !lat.equals("") && !avrg.equals("")) {
                            GreenGuideLocation gL = new GreenGuideLocation();
                            gL.averageRating = Float.parseFloat(avrg);
                            gL.point = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                            gL.companyName = company;
                            gL.address = address;
                            gL.city = city;
                            pos.add(gL);
                        }
                    }
                } catch (JSONException e) { /* Do Nothing */ }

                GreenGuideMarkers markers = new GreenGuideMarkers(mMapManager) {
                    @Override
                    public boolean onPoiClick(int index) {
                        GreenGuideLocation location = pos.get(index);
                        mBtmSheetManager.getReview(new BaiduSuggestion.Location(
                                location.companyName, location.point, location.address,
                                location.city, null));
                        return false;
                    }
                };
                markers.setData(pos);
            }

            @Override
            public void onCanceled(ArrayList<JSONArray> jArray, ArrayList<Exception> exceptions) {
                /* Do Nothing */
            }
        }).execute("http://www.lovegreenguide.com/map_point_app.php?lng=112.578658&lat=28.247855");
    }

    public static abstract class GreenGuideMarkers implements BaiduMap.OnMarkerClickListener {
        private ArrayList<Overlay> mOverlayList;
        private BaiduMapManager mMapManager;

        public GreenGuideMarkers(BaiduMapManager mapManager) {
            mMapManager = mapManager;
            mMapManager.baiduMap.setOnMarkerClickListener(this);
        }

        public abstract boolean onPoiClick(int index);

        @Override
        public boolean onMarkerClick(Marker marker) {
            if (!mOverlayList.contains(marker)) {
                return false;
            }
            if (marker.getExtraInfo() != null) {
                return onPoiClick(marker.getExtraInfo().getInt("index"));
            }
            return false;
        }

        public void setData(ArrayList<GreenGuideLocation> greenGuideLocations) {
            mOverlayList = new ArrayList<>();
            for (int i = 0; i < greenGuideLocations.size(); i++) {
                GreenGuideLocation location = greenGuideLocations.get(i);
                Bundle bundle = new Bundle();
                bundle.putInt("index", i);
                MarkerOptions option = new MarkerOptions()
                        .position(location.point)
                        .extraInfo(bundle);
                mOverlayList.add(mMapManager.addMarker(option,
                        getColoredMarkerFromRating(location.averageRating)));
            }
        }
    }

    public static class GreenGuideLocation {
        public float averageRating;
        public String companyName;
        public String address;
        public String city;
        public LatLng point;
    }

    private static int roundRating(float rating) {
        if (rating < 0) {
            return  -Math.round(-rating);
        } else {
            return Math.round(rating);
        }
    }

    /***
     * Returns a resource ID for the appropriately colored marker for the supplied rating.
     *
     * @param averageRating A number in the set [-3,3] inclusive of both.
     * @return A resource Id.
     */
    private static int getColoredMarkerFromRating(float averageRating) {
        int drawableId;
        switch (roundRating(averageRating)) {
            case -3: drawableId = R.drawable.icon_markg_red; break;
            case -2: drawableId = R.drawable.icon_markg_orange; break;
            case -1: drawableId = R.drawable.icon_markg_yellow; break;
            case 0: drawableId = R.drawable.icon_markg_white; break;
            case 1: drawableId = R.drawable.icon_markg_aqua; break;
            case 2: drawableId = R.drawable.icon_markg_lime; break;
            default: drawableId = R.drawable.icon_markg_green; break;
        }
        return drawableId;
    }
}