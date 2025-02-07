package com.example.user.mytracker;

import java.util.ArrayList;

public class Page {
    private String filename;
    private int layoutResId;
    private int rvId;
    private ArrayList<Entry> list; // Either expense or shopping list

    public Page(String filename, int layoutResId, int rvId) {
        this.filename = filename;
        this.layoutResId = layoutResId;
        this.rvId = rvId;
        this.list = new ArrayList<>();
    }

    public void appendToList(Entry entry) {
        list.add(entry);
    }

    public String getFilename() {
        return filename;
    }

    public int getLayoutResId() {
        return layoutResId;
    }

    public int getRvId() {
        return rvId;
    }

    public ArrayList<Entry> getList() {
        return list;
    }
}
