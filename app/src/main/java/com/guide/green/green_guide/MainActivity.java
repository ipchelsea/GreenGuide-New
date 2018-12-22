package com.guide.green.green_guide;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.guide.green.green_guide.Dialogs.CityPickerDialog;
import com.guide.green.green_guide.Dialogs.CityPickerDialog.OnCitySelectedListener;
import com.guide.green.green_guide.Utilities.BaiduMapManager;
import com.guide.green.green_guide.Utilities.BaiduSuggestion;
import com.guide.green.green_guide.Utilities.BottomSheetManager;
import com.guide.green.green_guide.Utilities.CredentialManager;
import com.guide.green.green_guide.Utilities.DBReviewSearchManager;
import com.guide.green.green_guide.Utilities.RomanizedLocation;
import com.guide.green.green_guide.Utilities.SuggestionSearchManager;


public class MainActivity extends AppCompatActivity implements OnCitySelectedListener,
        NavigationView.OnNavigationItemSelectedListener, SuggestionSearchManager.DrawerController,
        CredentialManager.OnLoginStateChanged {
    // Main managers
    private BaiduMapManager mMapManager;
    private BottomSheetManager mBtmSheetManager;
    private SuggestionSearchManager mSearchManager;
    private Button mCitySelectionView = null;

    // Terrain selection
    private FloatingActionButton normalMapView;
    private FloatingActionButton satelliteMapView;
    private boolean fabIsOpen = false;

    // Toggle between hamburger icon and back arrow
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mActionBarToggle;
    private boolean mBackButtonDisplaied = false;

    // Logout & login state handler
    private MenuItem mLoginOut;

    // Image View
    private TextView mDrawerUsername;
    private ImageView mDrawerImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        findViewById(R.id.writeReview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WriteReviewActivity.open(MainActivity.this);
            }
        });

        initMapManager();
        initBottomSheet();
        initToolsAndWidgets();
        initLocationTracker();
        CredentialManager.addLoginStateChangedListener(this);
        CredentialManager.initialize(this);

        mMapManager.setOnLocationClickListener(new BaiduMapManager.OnLocationClickListener() {
            @Override
            public void onLocationClick(BaiduSuggestion.Location location) {
                mBtmSheetManager.getReviewFor(location);
            }
        });

        findViewById(R.id.doSomething).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doStuff(view);
            }
        });
    }

    /**
     * Insure that the icon on the toolbar is correct.
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarToggle.syncState();
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

        ViewGroup container = findViewById(R.id.db_search_results_container);
        ViewGroup childContainer = findViewById(R.id.db_search_results);
        DBReviewSearchManager dbSearchMgr = new DBReviewSearchManager(container, childContainer);

        // Initialize Bottom Sheet Manager
        NestedScrollView btmSheet = findViewById(R.id.btmSheet);
        mBtmSheetManager = new BottomSheetManager(this, btmSheet, reviews, dbSearchMgr,
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
        mActionBarToggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mActionBarToggle);
        mActionBarToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mLoginOut = navigationView.getMenu().findItem(R.id.drawable_log_in_out);

        mDrawerUsername = navigationView.getHeaderView(0).findViewById(R.id.drawer_user_name);
        mDrawerImage = navigationView.getHeaderView(0).findViewById(R.id.drawer_user_picture);
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
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else if (mBtmSheetManager.getBottomSheetState() != BottomSheetBehavior.STATE_HIDDEN) {
            if (mBtmSheetManager.getBottomSheetState() == BottomSheetBehavior.STATE_COLLAPSED) {
                mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
                mBtmSheetManager.removeMarkers();
                if (mSearchManager.hasText()) {
                    mSearchManager.showDropDownOverlay();
                }
            } else {
                mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        } else if (mSearchManager.isDropDownOverlayShowing()) {
            mSearchManager.onBackButtonClick();
        } else if (mSearchManager.hasText()) {
            mSearchManager.showDropDownOverlay();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_bar, menu);
        MenuItem menuItem = menu.findItem(R.id.searchItem);

        View root = menuItem.getActionView().getRootView();
        ViewGroup.LayoutParams lp = root.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        root.setLayoutParams(lp);

        mCitySelectionView = menuItem.getActionView().findViewById(R.id.city);
        mCitySelectionView.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                CityPickerDialog cityPickerDialog = new CityPickerDialog();
                cityPickerDialog.setOnCitySelectedListener(MainActivity.this);
                cityPickerDialog.show(getSupportFragmentManager(), "City Picker");
            }
        });

        EditText searchInput = menuItem.getActionView().findViewById(R.id.searchInput);
        RecyclerView searchDropDown = findViewById(R.id.searchDropDown);

        ViewGroup mapViewContainer = findViewById(R.id.mapViewContainer);
        ViewGroup searchDropDownContainer = findViewById(R.id.searchDropDownContainer);

        /* Not saved Intentionally */
        mSearchManager = new SuggestionSearchManager(this, searchInput, searchDropDown,
                mCitySelectionView, mMapManager, mBtmSheetManager, mapViewContainer,
                searchDropDownContainer, this);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int itemId = item.getItemId();
        if (itemId == R.id.drawable_log_in_out) {
            LogInOutSignUpActivity.startActivity(this);
        } else {
            FragmentContainerActivity.startActivity(this, itemId);
        }
        mDrawer.closeDrawer(GravityCompat.START);
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

    @Override
    public void showBackButton(boolean enabled) {
        if (mBackButtonDisplaied == enabled) return;
        if (enabled) {
            if (mDrawer.isDrawerOpen(GravityCompat.START)) {
                mDrawer.closeDrawer(GravityCompat.START);
            }
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mActionBarToggle.setDrawerIndicatorEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActionBarToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSearchManager.onBackButtonClick();
                }
            });
        } else {
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mActionBarToggle.setDrawerIndicatorEnabled(true);
            mActionBarToggle.setToolbarNavigationClickListener(null);
        }
        mBackButtonDisplaied = enabled;
    }

    /* Temporary Method For Testing Things */
    public void doStuff(View view) {

    }

    @Override
    public void onLoginStateChanged(boolean isLoggedIn) {
        if (isLoggedIn) {
            mDrawerUsername.setText(CredentialManager.getUsername());
            mLoginOut.setTitle(getResources().getString(R.string.log_out_text));
        } else {
            mDrawerUsername.setText("");
            mLoginOut.setTitle(getResources().getString(R.string.log_in_text));
        }
    }
}