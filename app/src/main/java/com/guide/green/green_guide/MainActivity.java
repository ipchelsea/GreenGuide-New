package com.guide.green.green_guide;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonWriter;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Overlay;
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
import com.guide.green.green_guide.Fragments.AboutFragment;
import com.guide.green.green_guide.Fragments.GuidelinesFragment;
import com.guide.green.green_guide.Fragments.HomeFragment;
import com.guide.green.green_guide.Fragments.LogInOutFragment;
import com.guide.green.green_guide.Fragments.MyReviewsFragment;
import com.guide.green.green_guide.Fragments.SignUpFragment;
import com.guide.green.green_guide.Fragments.UserGuideFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;


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
    private TextView previewCityName, previewCompanyName;
    private BottomSheetBehavior btmSheet;
    private RequestQueue requestsQueue;
    public static class BaiduSuggestion {
        public final String name;
        public final double lat, lng;
        public  BaiduSuggestion(String name, double lat, double lng) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
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
        requestsQueue = Volley.newRequestQueue(this);
        android.support.v4.widget.NestedScrollView sheetView = (android.support.v4.widget.NestedScrollView) findViewById(R.id.btmSheet);
        btmSheet = BottomSheetBehavior.from(sheetView);
        btmSheet.setState(BottomSheetBehavior.STATE_HIDDEN);


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
//
        keyWorldsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                    Stan:
                     - Get Selection (Latitude/Longitude)
                     - Search GreenGuide DB for location
                     - Display it

                Old Code for starting a new intent:
                    Intent intent = new Intent(MainActivity.this, ResultDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("id", String.valueOf(position));
                    intent.putExtras(bundle);
                    startActivity(intent);
                */

                BaiduSuggestion bSuggestion = (BaiduSuggestion) parent.getItemAtPosition(position);
                String url = "http://www.lovegreenguide.com/map_point_co_app.php?lng=" +
                                bSuggestion.lng + "&lat=" + bSuggestion.lat;
                url = "http://www.lovegreenguide.com/map_point_co_app.php?lng=121.461988&lat=31.028635";

                JsonArrayRequest greenGuideDBRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Toast.makeText(MainActivity.this, "Success Querying Green Guid DB", Toast.LENGTH_SHORT).show();
                            btmSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            TextView tvJson = (TextView) findViewById(R.id.previewJSON);
                            StringBuilder result = new StringBuilder();
                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject jObj = response.getJSONObject(i);
                                    result.append("[\n");
                                    for (Iterator<String> jIter = jObj.keys(); jIter.hasNext();) {
                                        String key = jIter.next();
                                        result.append("  \"" + key + "\" {\n");

                                        JSONObject jSubObj = jObj.getJSONObject(key);
                                        for (Iterator<String> jSubIter = jSubObj.keys(); jSubIter.hasNext();) {
                                            String subKey = jSubIter.next();
                                            result.append("    \"" + subKey + "\":" + jSubObj.getString(subKey) + ",\n");
                                        }
                                        result.append("  },\n");
                                    }
                                    result.append("],\n");
                                }

                                btmSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            } catch (JSONException e) {
                                Log.e("JSON_PARSE", e.getMessage());
                            }
                            tvJson.setText(result.toString());
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(MainActivity.this, "Item not found in Green Guide DB", Toast.LENGTH_SHORT).show();
                            Log.e("JsonRSP", error.toString());
                            btmSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                        }
                    });
                requestsQueue.add(greenGuideDBRequest);
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
        System.out.println(item.getItemId());
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
                suggest.add(new BaiduSuggestion(info.key, info.pt.latitude, info.pt.longitude));
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
}