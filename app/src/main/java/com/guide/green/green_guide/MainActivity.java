package com.guide.green.green_guide;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.Stroke;
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
import com.guide.green.green_guide.Dialogs.CityPickerDialog;
import com.guide.green.green_guide.Fragments.AboutFragment;
import com.guide.green.green_guide.Fragments.GuidelinesFragment;
import com.guide.green.green_guide.Fragments.LogInOutFragment;
import com.guide.green.green_guide.Fragments.MyReviewsFragment;
import com.guide.green.green_guide.Fragments.SignUpFragment;
import com.guide.green.green_guide.Fragments.UserGuideFragment;
import com.guide.green.green_guide.Utilities.Drawing;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements OnGetPoiSearchResultListener, OnGetSuggestionResultListener, NavigationView.OnNavigationItemSelectedListener {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private PoiSearch mPoiSearch;
    SuggestionSearch mSuggestionSearch;
    private List<BaiduSuggestion> suggest;
    private PoiOverlay mOverlay;

    private Button citySelectionView = null;
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
    private TextView mBtmSheetCompanyName;
    private BottomSheetBehavior mBtmSheet;
    private android.support.v4.widget.NestedScrollView btmSheetView;

    public static class BaiduSuggestion {
        public final String name;
        public final LatLng point;
        public BaiduSuggestion(@NonNull SuggestionResult.SuggestionInfo info) {
            this.name = info.key;
            this.point = info.pt;
        }
        public BaiduSuggestion(@NonNull PoiDetailResult info) {
            this.name = info.name;
            this.point = info.location;
        }
        @Override
        public String toString() {
            if (point == null) {
                return " " + name;
            } else {
                return "\uD83D\uDCCD" + name;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        // Stan Additions
        mBtmSheetCompanyName = (TextView) findViewById(R.id.previewCompanyName);
        btmSheetView = (android.support.v4.widget.NestedScrollView) findViewById(R.id.btmSheet);
        mBtmSheet = BottomSheetBehavior.from(btmSheetView);
        mBtmSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBtmSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                ViewGroup.LayoutParams layoutParams = mBtmSheetCompanyName.getLayoutParams();
                LinearLayout.LayoutParams linearL = new LinearLayout.LayoutParams (layoutParams);
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBtmSheetCompanyName.setSingleLine(true);
                    mBtmSheetCompanyName.setPadding(0, 0, 0, 0);
                } else {
                    int px = (int) Drawing.convertDpToPx(MainActivity.this, 10);
                    mBtmSheetCompanyName.setSingleLine(false);
                    linearL.setMargins (0, px,0, px);
                    if (newState == BottomSheetBehavior.STATE_HIDDEN && oldMarker != null) {
                        oldMarker.remove();
                        oldMarker = null;
                    }
                }
                mBtmSheetCompanyName.setLayoutParams(linearL);
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

        mMapView = (MapView) findViewById(R.id.map);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mBtmSheet.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                    mBtmSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                if (mBtmSheet.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                    mBtmSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                return false;
            }
        });

        mBaiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (mBtmSheet.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                    mBtmSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });

        mBaiduMap.setOnMyLocationClickListener(new BaiduMap.OnMyLocationClickListener() {
            @Override
            public boolean onMyLocationClick() {
                Log.i("MyLocation", "Not too sure what to dow now?");
                return false;
            }
        });

        initToolsAndWidgets();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFabActions(view);
            }
        });

        FloatingActionButton btnMyLocation = (FloatingActionButton) findViewById(R.id.myLocation);
        btnMyLocation.setOnClickListener(new TrackLocationHandler(this, btnMyLocation, mBaiduMap));
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
                    setMapType(1);
                    hideFabItems();
                }
            });
            satelliteMapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setMapType(2);
                    hideFabItems();
                }
            });
        }
    }

    private void setMapType(int id) {
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
        MenuItem item;

        item = menu.findItem(R.id.cityItem);
        citySelectionView = (Button) item.getActionView().findViewById(R.id.city);
        citySelectionView.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CityPickerDialog cpd = new CityPickerDialog();
                cpd.setSelectedCityButton(citySelectionView);
                cpd.show(getSupportFragmentManager(), "");
            }
        });

        item = menu.findItem(R.id.searchItem);
        keyWorldsView = (AutoCompleteTextView) item.getActionView().findViewById(R.id.search);
        sugAdapter = new ArrayAdapter<BaiduSuggestion>(this,
                android.R.layout.simple_dropdown_item_1line);
        keyWorldsView.setAdapter(sugAdapter);
        keyWorldsView.setThreshold(1);
        keyWorldsView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean gaineFocus) {
                AutoCompleteTextView tv = (AutoCompleteTextView) view;
                if (!gaineFocus) {
                    if (mOverlay != null) {
                        // Should be a function
                        mOverlay.removeFromMap();
                        mOverlay = null;
                    }
                    tv.setText("");
                }
            }
        });
        keyWorldsView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    mPoiSearch.searchInCity(new PoiCitySearchOption()
                            .city(citySelectionView.getText().toString())
                            .keyword(keyWorldsView.getText().toString()));

                    Drawing.hideKeyboard(keyWorldsView, getApplicationContext());
                }
                return false;
            }
        });
        keyWorldsView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) { /* Do Nothign */ }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {  /* Do Nothign */ }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                                      int arg3) {
                if (cs.length() <= 0) {
                    return;
                }

//                BitmapDescriptor bitmap =
//                        BitmapDescriptorFactory.fromResource(R.drawable.icon_markg_aqua);
//                OverlayOptions option;
//                option = new MarkerOptions().position(southWest).icon(bitmap); mBaiduMap.addOverlay(option);
//                option = new MarkerOptions().position(northEast).icon(bitmap); mBaiduMap.addOverlay(option);

//                mSuggestionSearch .requestSuggestion((new SuggestionSearchOption())
//                        .city("\t合肥").keyword(cs.toString()));

//*

                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                        .city(citySelectionView.getText().toString()).keyword(cs.toString()));
/*/
                Point btmLeft = new Point(mMapView.getWidth(), 0);
                LatLng southWest = mBaiduMap.getProjection().fromScreenLocation(btmLeft);
                Point topRight = new Point(0, mMapView.getHeight());
                LatLng northEast = mBaiduMap.getProjection().fromScreenLocation(topRight);

                LatLngBounds searchBound = new LatLngBounds.Builder()
                        .include(southwest).include(northeast)
                        .build();

                PoiBoundSearchOption option = new PoiBoundSearchOption();
                option.keyword(cs.toString());
                option.bound(searchBound);
                mPoiSearch.searchInBound(option);
//*/


            }
        });

        keyWorldsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the suggested place that was clicked
                MainActivity.BaiduSuggestion bSuggestion =
                        (MainActivity.BaiduSuggestion) parent.getItemAtPosition(position);

                keyWorldsView.setText(bSuggestion.name);
                keyWorldsView.setSelection(bSuggestion.name.length());

                // Start the asynchronous retrieval of the GreenGuide DB data about this place
                if (bSuggestion.point != null) {
                    if (mOverlay != null) {
                        // Should be a function
                        mOverlay.removeFromMap();
                        mOverlay = null;
                    }

                    FetchedReviewsHandler.fetch(MainActivity.this, bSuggestion);
                    Drawing.hideKeyboard(keyWorldsView, getApplicationContext());

                    // Remove all markers then put a marker on the map at this places location
                    if (oldMarker != null) {
                        oldMarker.remove();
                        oldMarker = null;
                    }
                    BitmapDescriptor bitmap =
                            BitmapDescriptorFactory.fromResource(R.drawable.icon_markg_red);
                    OverlayOptions option = new MarkerOptions().position(bSuggestion.point).icon(bitmap);
                    oldMarker = mBaiduMap.addOverlay(option);

                    // Move the map to the selected point
                    moveMapViewTo(bSuggestion.point);
                }
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    Overlay oldMarker = null;
    private void moveMapViewTo(LatLng location) {
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(location, 18));
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
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) { }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPoiSearch != null)
            mPoiSearch.destroy();
        if (mSuggestionSearch != null)
            mSuggestionSearch.destroy();
        mPoiSearch.destroy();
        mSuggestionSearch.destroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void searchButtonProcess(View v) {
        searchType = 1;
        String keystr = keyWorldsView.getText().toString();


        mPoiSearch.searchInBound(new PoiBoundSearchOption().bound(mBaiduMap.getMapStatusLimit())
                .keyword(keystr).pageNum(loadIndex));

//        mPoiSearch.searchInCity((new PoiCitySearchOption())
//                .keyword(keystr).pageNum(loadIndex));
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
            if (oldMarker != null) {
                // Should be a function
                oldMarker.remove();
                oldMarker = null;
            }
            if (mOverlay != null) {
                // Should be a function
                mOverlay.removeFromMap();
                mOverlay = null;
            }
            mOverlay = new MyPoiOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(mOverlay);
            mOverlay.setData(result);
            mOverlay.addToMap();
            mOverlay.zoomToSpan();

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
            BaiduSuggestion bSuggestion = new BaiduSuggestion(result);
            // Start the asynchronous retrieval of the GreenGuide DB data about this place
            FetchedReviewsHandler.fetch(MainActivity.this, bSuggestion);
            Drawing.hideKeyboard(keyWorldsView, getApplicationContext());

            // Remove all markers then put a marker on the map at this places location
            mBaiduMap.clear();

            BitmapDescriptor bitmap =
                    BitmapDescriptorFactory.fromResource(R.drawable.icon_markg_red);
            OverlayOptions option = new MarkerOptions().position(bSuggestion.point).icon(bitmap);
            oldMarker = mBaiduMap.addOverlay(option);

            // Move the map to the selected point
            moveMapViewTo(bSuggestion.point);

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
            suggest.add(new BaiduSuggestion(info));
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

    public void doStuff(View view) {
    }
}