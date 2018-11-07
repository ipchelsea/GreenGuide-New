package com.guide.green.green_guide.Utilities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.baidu.mapapi.SDKInitializer;
import com.guide.green.green_guide.Dialogs.CityPickerDialog;
import com.guide.green.green_guide.Fragments.AboutFragment;
import com.guide.green.green_guide.Fragments.GuidelinesFragment;
import com.guide.green.green_guide.Fragments.LogInOutFragment;
import com.guide.green.green_guide.Fragments.MyReviewsFragment;
import com.guide.green.green_guide.Fragments.SignUpFragment;
import com.guide.green.green_guide.Fragments.UserGuideFragment;
import com.guide.green.green_guide.Fragments.WriteReviewFragment;
import com.guide.green.green_guide.MainActivity;
import com.guide.green.green_guide.R;
import java.util.Stack;

public class FragContainer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Integer lastFragmentId = null;
    private Stack<Integer> backStack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_pages_container);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(FragContainer.this);

        Bundle extras = getIntent().getExtras();
        if (!extras.isEmpty()) {
            Object layoutId = extras.get("itemId");
            if (layoutId != null && layoutId instanceof Integer) {
                openFrament((Integer) layoutId, false);
            }
        }
        if (lastFragmentId == null) {
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!backStack.isEmpty()) {
            openFrament(backStack.pop(), false);
        } else {
            finish();
        }
    }

    private Fragment getFragmentFromId(int layoutId) {
        switch (layoutId) {
            case R.id.my_reviews: return new MyReviewsFragment();
            case R.id.guidelines: return new GuidelinesFragment();
            case R.id.about: return new AboutFragment();
            case R.id.user_guide: return new UserGuideFragment();
            case R.id.sign_up: return new SignUpFragment();
            case R.id.log_in_out: return new LogInOutFragment();
            case R.id.write_review: return new WriteReviewFragment();
        }
        return null;
    }

    private boolean openFrament(int layoutId, boolean addPrevToBackStack) {
        if (lastFragmentId != null && lastFragmentId == layoutId) {
            return false;
        }

        Fragment fragment = getFragmentFromId(layoutId);
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (lastFragmentId == null) {
                transaction.add(R.id.fragment_container, fragment);
            } else {
                transaction.replace(R.id.fragment_container, fragment);
                if (addPrevToBackStack) {
                    backStack.push(lastFragmentId);
                }
            }
            lastFragmentId = layoutId;
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        openFrament(item.getItemId(), true);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static void startActivity(Activity act, int layoutId) {
        Intent intent = new Intent(act, FragContainer.class);
        intent.putExtra("itemId", layoutId);
        act.startActivity(intent);
    }
}
