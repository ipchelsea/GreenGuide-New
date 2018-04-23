package com.guide.green.green_guide;

import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.util.Log;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.guide.green.green_guide.Fragments.AboutFragment;
import com.guide.green.green_guide.Fragments.GuidelinesFragment;
import com.guide.green.green_guide.Fragments.HomeFragment;
import com.guide.green.green_guide.Fragments.LogInOutFragment;
import com.guide.green.green_guide.Fragments.MyReviewsFragment;
import com.guide.green.green_guide.Fragments.SignUpFragment;
import com.guide.green.green_guide.Fragments.UserGuideFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String[] list;
    ArrayAdapter<String> adapter;
    ListView lv;

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private PoiSearch mPoiSearch;
    SuggestionSearch mSuggestionSearch;

    FloatingActionButton fab;
    FloatingActionButton normalMapView;
    FloatingActionButton satelliteMapView;
    boolean fabIsOpen = false;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private AutoCompleteTextView mSearchBar;

    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initToolsAndWidgets();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFabActions(view);
            }
        });



        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.screen_area, new HomeFragment());
        tx.commit();

//        mSearchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
//                        actionId == EditorInfo.IME_ACTION_DONE ||
//                            event.getAction() == event.ACTION_DOWN ||
//                                event.getAction() == event.KEYCODE_ENTER) {
//                    geoLocate();
//                }
//                return false;
//            }
//        });
    }

    private void initToolsAndWidgets() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        normalMapView = (FloatingActionButton) findViewById(R.id.normalfab);
        satelliteMapView = (FloatingActionButton) findViewById(R.id.satellitefab);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        list = new String[]{"Clipcodes", "Android", "Tutorials", "SearchView", "Searchbar", "Sandun", "Somalia", "Singhe", "Sea", "Seees"};
        mSearchBar = (AutoCompleteTextView) findViewById(R.id.input_search);

        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, list);
        mSearchBar.setAdapter(adapter);
        mSearchBar.setThreshold(1);

        mPoiSearch = PoiSearch.newInstance();
        mSuggestionSearch = SuggestionSearch.newInstance();
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
                return false;
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
    @Override
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
                fragment = new HomeFragment();
                findViewById(R.id.search1).setVisibility(View.VISIBLE);
                findViewById(R.id.search1).setActivated(true);
                mSearchBar.setVisibility(View.VISIBLE);
                mSearchBar.setText("");
        }

        if (id != R.id.home) {
            findViewById(R.id.search1).setVisibility(View.GONE);
            mSearchBar.setVisibility(View.GONE);
        }

        if (fragment != null) {

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ft.replace(R.id.screen_area, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
