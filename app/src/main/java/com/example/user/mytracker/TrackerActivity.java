package com.example.user.mytracker;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.viewpagerindicator.CirclePageIndicator;
import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;

public class TrackerActivity extends AppCompatActivity {
    private List<Page> pages = new ArrayList<>();

    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        setOnClickListeners();
        retrieveData();
        setViewPager();
    }

    private void setOnClickListeners() {
        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Settings.canDrawOverlays(getApplicationContext())) {
                    startService(new Intent(TrackerActivity.this, FloatingViewService.class));
                    finish();
                } else {
                    askPermission();
                    Toast.makeText(getApplicationContext(), "Please give permission to enable floating widget", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    private void retrieveData() {
        // instantiate 2 page objects
        pages.add(new Page("Expenses List", R.layout.page_expenses, R.id.rv_expenses));
        pages.add(new Page("Shopping List", R.layout.page_shopping, R.id.rv_shopping));

        // retrieve data
        for (Page p: pages) {
            FileInputStream fis = null;
            try {
                fis = openFileInput(p.getFilename());
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                String entry;
                while ((entry = br.readLine()) != null) {
                    p.appendToList(new Entry(entry));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setViewPager() {
        PagerAdapter pagerAdapter = new PagerAdapter(TrackerActivity.this, pages);
        AutoScrollViewPager pagerView = findViewById(R.id.pager_view);
        pagerView.setAdapter(pagerAdapter);
        setPageIndicator(pagerView);
    }

    private void setPageIndicator(AutoScrollViewPager pagerView) {
        CirclePageIndicator pageIndicator = findViewById(R.id.page_indicator);
        pageIndicator.setViewPager(pagerView);
        pageIndicator.setFillColor(Color.parseColor("#FFFF3E3E")); // fill colour of the selected circle
        pageIndicator.setStrokeColor(Color.parseColor("#000000")); // stroke or the circle's border colour
        pageIndicator.setPageColor(Color.parseColor("#C0C0C0")); // default fill colour of the circle
        pageIndicator.setRadius(14);
        pageIndicator.setCurrentItem(0);
    }
}
