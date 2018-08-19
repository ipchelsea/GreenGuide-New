package com.guide.green.green_guide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static android.app.PendingIntent.getActivity;
import static android.content.Intent.getIntent;

public class ResultDetail extends Activity {
    private TextView reviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_detail);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        reviews =  findViewById(R.id. Reviews);
        if (!extras.isEmpty()) {
            String value = extras.getString("id");
            reviews.setText("Reviews");
        }
    }
}
