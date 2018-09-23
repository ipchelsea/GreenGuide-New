package com.guide.green.green_guide;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.guide.green.green_guide.Dialogs.LoadingDialog;
import com.guide.green.green_guide.Fragments.AboutFragment;
import com.guide.green.green_guide.Fragments.GuidelinesFragment;
import com.guide.green.green_guide.Fragments.LogInOutFragment;
import com.guide.green.green_guide.Fragments.MyReviewsFragment;
import com.guide.green.green_guide.Fragments.SignUpFragment;
import com.guide.green.green_guide.Fragments.UserGuideFragment;
import com.guide.green.green_guide.Utilities.AsyncJSONArray;
import com.guide.green.green_guide.Utilities.Drawing;
import com.guide.green.green_guide.Utilities.Review;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements OnGetPoiSearchResultListener, OnGetSuggestionResultListener, NavigationView.OnNavigationItemSelectedListener {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private PoiSearch mPoiSearch;
    SuggestionSearch mSuggestionSearch;
    private List<BaiduSuggestion> suggest;

    private EditText editCity = null;
    private AutoCompleteTextView keyWorldsView = null;
    private ArrayAdapter<BaiduSuggestion> sugAdapter = null;
    private int loadIndex = 0;

    LatLng center = new LatLng(39.92235, 116.380338);
    int radius = 100;
    LatLng southwest = new LatLng( 39.92235, 116.380338 );
    LatLng northeast = new LatLng( 39.947246, 116.414977);
    LatLngBounds searchbound = new LatLngBounds.Builder().include(southwest).include(northeast).build();

    int searchType = 0;  // 搜索的类型，在显示时区分

    FloatingActionButton fab;
    FloatingActionButton normalMapView;
    FloatingActionButton satelliteMapView;
    boolean fabIsOpen = false;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    SearchView searchView;

    // Stan additions
    private TextView previewCityName, previewCompanyName, btmSheetCoName;
    android.support.v4.widget.NestedScrollView btmSheetView;
    private ImageView btmSheetRatingStars;
    private BottomSheetBehavior btmSheet;

    public void doStuff(View view) {
    }

    public static class BaiduSuggestion {
        public final String name;
        public final double lat, lng;
        public  BaiduSuggestion(SuggestionResult.SuggestionInfo info) {
            this.name = info.key;
            this.lat = info.pt.latitude;
            this.lng = info.pt.longitude;
        }
        @Override
        public String toString() { return name; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        // Stan Additions
        btmSheetRatingStars = (ImageView) findViewById(R.id.btmSheetRatingStars);
        btmSheetCoName = (TextView) findViewById(R.id.previewCompanyName);

        btmSheetView = (android.support.v4.widget.NestedScrollView) findViewById(R.id.btmSheet);
        btmSheet = BottomSheetBehavior.from(btmSheetView);
//        btmSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        btmSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                ViewGroup.LayoutParams layoutParams = btmSheetCoName.getLayoutParams();
                LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams (layoutParams);

                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    btmSheetCoName.setSingleLine(true);
                    btmSheetCoName.setPadding(0, 0, 0, 0);
                } else {
                    btmSheetCoName.setSingleLine(false);
                    ll.setMargins (0,0,0,
                            (int) Drawing.convertDpToPx(MainActivity.this, 10));
                }

                btmSheetCoName.setLayoutParams(ll);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                /* Do Nothing */
            }
        });


        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);

        editCity = (EditText) findViewById(R.id.city);
        keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
        sugAdapter = new ArrayAdapter<BaiduSuggestion>(this,
                android.R.layout.simple_dropdown_item_1line);
        keyWorldsView.setAdapter(sugAdapter);
        keyWorldsView.setThreshold(1);
        mBaiduMap = ((SupportMapFragment) (getSupportFragmentManager()
                .findFragmentById(R.id.map))).getBaiduMap();


        keyWorldsView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                                      int arg3) {
                if (cs.length() <= 0) {
                    return;
                }

                mSuggestionSearch
                        .requestSuggestion((new SuggestionSearchOption())
                                .keyword(cs.toString()).city(editCity.getText().toString()));
            }
        });

        initToolsAndWidgets();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFabActions(view);
            }
        });

        keyWorldsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Drawing.hideKeyboard(keyWorldsView, getApplicationContext());

                BaiduSuggestion bSuggestion = (BaiduSuggestion) parent.getItemAtPosition(position);
                final LoadingDialog ld = new LoadingDialog();
                ld.show(getFragmentManager(), "Working?");

                final AsyncJSONArray reviewTask = Review.getReviewsForPlace(104.075155d,
                        37.198839d, new FetchedReviewsHandler(ld, bSuggestion));

                ld.setCallback(new LoadingDialog.Canceled() {
                    @Override
                    public void onCancel() {
                        reviewTask.cancel(true);
                    }
                });
            }
        });
    }

    private void initToolsAndWidgets() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        normalMapView = findViewById(R.id.normalfab);
        satelliteMapView = findViewById(R.id.satellitefab);
        fab = findViewById(R.id.fab);

        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

//        clearImage = (ImageView) findViewById(R.id.clearImage);mPoiSearch = PoiSearch.newInstance();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void handleFabActions(View view) {
        if (fabIsOpen) {
            normalMapView.hide();
            satelliteMapView.hide();
            normalMapView.setClickable(false);
            satelliteMapView.setClickable(false);
            fabIsOpen = false;
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
                    setMapType(1);
                }
            });
            satelliteMapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setMapType(2);
                }
            });
        }
    }

    private void setMapType(int id) {
        mBaiduMap = ((SupportMapFragment) (getSupportFragmentManager()
                .findFragmentById(R.id.bmapView))).getBaiduMap();
        mMapView = (MapView) findViewById(R.id.bmapView);
        if (mMapView != null) {
            mMapView = (MapView) findViewById(R.id.bmapView);
            mBaiduMap = mMapView.getMap();
        }
        if (mBaiduMap != null) {
            if (id == 1)
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            else
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
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
        MenuItem item = menu.findItem(R.id.search);
        searchView = (SearchView)item.getActionView();

        // Old top searchbar
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

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
            default:
                keyWorldsView.setVisibility(View.VISIBLE);
                keyWorldsView.setText("");
        }

        if (id != R.id.home) {
            keyWorldsView.setVisibility(View.GONE);
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
    protected void onPause() { super.onPause(); }

    @Override
    protected void onResume() { super.onResume(); }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) { }

    @Override
    protected void onDestroy() {
        if (mPoiSearch != null)
            mPoiSearch.destroy();
        if (mSuggestionSearch != null)
            mSuggestionSearch.destroy();
        mPoiSearch.destroy();
        mSuggestionSearch.destroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void searchButtonProcess(View v) {
        searchType = 1;
        String citystr = editCity.getText().toString();
        String keystr = keyWorldsView.getText().toString();
        mPoiSearch.searchInCity((new PoiCitySearchOption())
                .city(citystr).keyword(keystr).pageNum(loadIndex));
    }

    public void  searchNearbyProcess(View v) {
        searchType = 2;
        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption().keyword(keyWorldsView.getText()
                .toString()).sortType(PoiSortType.distance_from_near_to_far).location(center)
                .radius(radius).pageNum(loadIndex);
        mPoiSearch.searchNearby(nearbySearchOption);
    }

    public void goToNextPage(View v) {
        loadIndex++;
        searchButtonProcess(null);
    }

    public void searchBoundProcess(View v) {
        searchType = 3;
        mPoiSearch.searchInBound(new PoiBoundSearchOption().bound(searchbound)
                .keyword(keyWorldsView.getText().toString()));

    }

    public void onGetPoiResult(PoiResult result) {
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(MainActivity.this, "未找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mBaiduMap.clear();
            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result);
            overlay.addToMap();
            overlay.zoomToSpan();

            switch( searchType ) {
                case 2:
                    showNearbyArea(center, radius);
                    break;
                case 3:
                    showBound(searchbound);
                    break;
                default:
                    break;
            }

            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(MainActivity.this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }
        suggest = new ArrayList<BaiduSuggestion>();
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null & info.pt != null) {
                suggest.add(new BaiduSuggestion(info));
            }
        }
        sugAdapter = new ArrayAdapter<BaiduSuggestion>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, suggest);
        keyWorldsView.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            // if (poi.hasCaterDetails) {
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
                    .poiUid(poi.uid));
            // }
            return true;
        }
    }

    public void showNearbyArea( LatLng center, int radius) {
        BitmapDescriptor centerBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_geo);
        MarkerOptions ooMarker = new MarkerOptions().position(center).icon(centerBitmap);
        mBaiduMap.addOverlay(ooMarker);

        OverlayOptions ooCircle = new CircleOptions().fillColor( 0xCCCCCC00 )
                .center(center).stroke(new Stroke(5, 0xFFFF00FF ))
                .radius(radius);
        mBaiduMap.addOverlay(ooCircle);
    }

    public void showBound( LatLngBounds bounds) {
        BitmapDescriptor bdGround = BitmapDescriptorFactory
                .fromResource(R.drawable.ground_overlay);

        OverlayOptions ooGround = new GroundOverlayOptions()
                .positionFromBounds(bounds).image(bdGround).transparency(0.8f);
        mBaiduMap.addOverlay(ooGround);

        MapStatusUpdate u = MapStatusUpdateFactory
                .newLatLng(bounds.getCenter());
        mBaiduMap.setMapStatus(u);

        bdGround.recycle();
    }

    class FetchedReviewsHandler implements Review.GetReviewsResult {
        private LoadingDialog ld;
        private BaiduSuggestion suggestion;

        public FetchedReviewsHandler(LoadingDialog ld, BaiduSuggestion suggestion) {
            this.ld = ld;
            this.suggestion = suggestion;
        }
        @Override
        public void onSuccess(ArrayList<Review> reviews) {
            // Hide dialog
            ld.dismiss();
            btmSheetView.requestFocus();

            // Calculate histogram values and average rating
            String address = "", city = "", industry = "", product = "";


            LinearLayout root = (LinearLayout) findViewById(R.id.userReviewList);
            root.removeAllViews();


            int total = 0;
            String[] histogramX = new String[] {"+3", "+2", "+1", "0", "-1", "-2", "-3"};
            int[] histogramY = new int[histogramX.length];
            for (int i = reviews.size() - 1; i >= 0; i--) {
                Review review = reviews.get(i);
                int rating = Integer.parseInt(review.location.get(Review.Location.Key.RATING));
                histogramY[3 - rating] += 1; // '-3' = 6, '-2' = 5, ..., '+3' = 0
                total += rating;

                if (address.equals("")) {
                    address = review.location.get(Review.Location.Key.ADDRESS);
                }
                if (city.equals("")) {
                    city = review.location.get(Review.Location.Key.CITY);
                }
                if (industry.equals("")) {
                    industry = review.location.get(Review.Location.Key.INDUSTRY);
                }
                if (product.equals("")) {
                    product = review.location.get(Review.Location.Key.PRODUCT);
                }

                // Add review
                LayoutInflater lf = LayoutInflater.from(MainActivity.this);
                LinearLayout child = (LinearLayout) lf.inflate(R.layout.review_single_comment, null, false);
                TextView ratingValue = (TextView) child.findViewById(R.id.ratingValue);
                ImageView ratingImage = (ImageView) child.findViewById(R.id.ratingImage);
                TextView reviewText = (TextView) child.findViewById(R.id.reviewText);
                TextView reviewTime = (TextView) child.findViewById(R.id.reviewTime);
                Button rawDataBtn = (Button) child.findViewById(R.id.rawDataBtn);
                Button helpfulBtn = (Button) child.findViewById(R.id.helpfulBtn);
                Button inappropriateBtn = (Button) child.findViewById(R.id.inappropriateBtn);

                ratingValue.setText("Rating: " + (rating > 0 ? "+" : "") + rating);

                String resourseName = "rate" + (rating < 0 ? "_" : "") + Math.abs(rating);
                int resoureseId = getResources().getIdentifier(resourseName, "drawable", MainActivity.this.getPackageName());
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), resoureseId);
                ratingImage.setImageBitmap(bmp);

                reviewText.setText(review.location.get(Review.Location.Key.REVIEW));

                reviewTime.setText("Time: " + review.location.get(Review.Location.Key.TIME));

                root.addView(child);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams (child.getLayoutParams());
                child.setPadding(0, (int) Drawing.convertDpToPx(MainActivity.this,
                        10), 0, 0);
                child.setLayoutParams(lp);
            }

            // Remove empty X values from histogram.
            int histLeft = 0, histRight = histogramY.length - 1;

            float ratio = (float) total / reviews.size();
            float halfBtmSheetHeight = getResources().getDimension(R.dimen.reviews_bottom_sheet_peek_height_halved);
            int w = btmSheetRatingStars.getWidth();
            int h = (int) Drawing.convertDpToPx(MainActivity.this, (int) halfBtmSheetHeight);

            int filledStarsColor, backgroundColor;
            if (Build.VERSION.SDK_INT >= 23) {
                filledStarsColor = getResources().getColor(R.color.bottom_sheet_gold, null);
                backgroundColor = getResources().getColor(R.color.bottom_sheet_blue, null);
            } else {
                filledStarsColor = getResources().getColor(R.color.bottom_sheet_gold);
                backgroundColor = getResources().getColor(R.color.bottom_sheet_blue);
            }

            // Create the rating stars
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Drawing.drawStars(0, 0, w, h, 5, ratio,
                                filledStarsColor, Color.GRAY, new Canvas(bmp));
            btmSheetRatingStars.setImageBitmap(bmp);

            if (histLeft != -1 && histRight != -1) {
                // Create the histogram
                float textSize = Drawing.convertSpToPx(MainActivity.this, 13);
                w = getResources().getDisplayMetrics().widthPixels;
                bmp = Drawing.createBarGraph(histogramX, histogramY, histLeft, histRight, w,
                        textSize, filledStarsColor, Color.WHITE, backgroundColor, 7, 7, 7);
            }

            ((ImageView) findViewById(R.id.btmSheetHistogram)).setImageBitmap(bmp);

            String sTemp;
            sTemp = (ratio > 0 ? "+" : "") + ratio;
            ((TextView) findViewById(R.id.btmSheetRatingValue)).setText(sTemp);
            sTemp = reviews.size() + " Review" + (reviews.size() > 1 ? "s" : "");
            ((TextView) findViewById(R.id.btmSheetRatingsCount)).setText(sTemp);

            ((TextView) findViewById(R.id.btmSheetAddress)).setText("Address: " + address);
            ((TextView) findViewById(R.id.btmSheetCityName)).setText("City: " + city);
            ((TextView) findViewById(R.id.btmSheetIndustry)).setText("Industry: " + industry);
            ((TextView) findViewById(R.id.btmSheetProduct)).setText("Product: " + product);
        }

        @Override
        public void onError(Exception e) {
            Log.e("Getting Review", e.toString());
            e.printStackTrace();
        }

        @Override
        public void onUpdate(long current, long total) {
            if (total != -1) {
                ld.setProgress((double) current / total);
            }
        }

        @Override
        public void onCanceled() {
            Toast.makeText(MainActivity.this, "Get Review Canceled", Toast.LENGTH_SHORT);
        }
    }
}