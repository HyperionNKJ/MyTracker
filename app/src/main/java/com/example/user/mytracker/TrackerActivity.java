package com.example.user.mytracker;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
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
    private static final long INITIAL_BUDGET = 6949000; // KRW
    private static final float EXCHANGE_RATE = 878.0f; // 1 SGD to ? KRW

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
        findViewById(R.id.calculate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long totalExpenses = calculateTotalExpenses();
                buildDialog(totalExpenses);
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

    private long calculateTotalExpenses() {
        List<Entry> expenseEntries = pages.get(0).getList();
        long total = 0;
        for (Entry e : expenseEntries) {
            total += Long.parseLong(e.getAmount());
        }
        return total;
    }

    private void buildDialog(final long totalExpenses) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.calculate_dialog);

        final TextView mBudget = dialog.findViewById(R.id.cal_budget);
        final TextView mExpenses = dialog.findViewById(R.id.cal_expenses);
        final TextView mRemaining = dialog.findViewById(R.id.cal_remaining);

        // we use button's tag attribute to track currency. Default tag = default currency = KRW
        mBudget.setText(formatAmount(INITIAL_BUDGET));
        mExpenses.setText(formatAmount(totalExpenses));
        final long remainingBalance = INITIAL_BUDGET - totalExpenses;
        mRemaining.setText(formatAmount(remainingBalance));

        dialog.findViewById(R.id.cal_currency).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag().toString().equals("KRW")) {
                    mBudget.setText(formatToSGD(INITIAL_BUDGET));
                    mExpenses.setText(formatToSGD(totalExpenses));
                    mRemaining.setText(formatToSGD(remainingBalance));
                    view.setTag("SGD");
                } else {
                    mBudget.setText(formatAmount(INITIAL_BUDGET));
                    mExpenses.setText(formatAmount(totalExpenses));
                    mRemaining.setText(formatAmount(remainingBalance));
                    view.setTag("KRW");
                }
            }
        });

        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public static String formatAmount(long amountToFormat) {
        return String.format("%,d", amountToFormat) + " KRW";
    }

    private static String formatToSGD(long amountInKRW) {
        return String.format("%,.2f", amountInKRW / EXCHANGE_RATE) + " SGD";
    }
}
