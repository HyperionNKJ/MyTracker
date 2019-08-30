package com.example.user.mytracker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class PagerAdapter extends android.support.v4.view.PagerAdapter{
    private Context context;
    private List<Page> pages;

    PagerAdapter(Context context, List<Page> pages) {
        this.context = context;
        this.pages = pages;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Page currentPage = pages.get(position);
        View view = LayoutInflater.from(context).inflate(currentPage.getLayoutResId(), container, false);
        RecyclerView mRecyclerView = view.findViewById(currentPage.getRvId());
        final RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(context, currentPage.getList(), currentPage.getFilename());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(recyclerViewAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        view.setTag(position);
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
