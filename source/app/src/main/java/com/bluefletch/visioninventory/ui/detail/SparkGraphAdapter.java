package com.bluefletch.visioninventory.ui.detail;

import com.robinhood.spark.SparkAdapter;

import java.util.ArrayList;
import java.util.List;

public class SparkGraphAdapter extends SparkAdapter {

    private List<Float> _dataset;

    public SparkGraphAdapter() {
        if (_dataset == null) _dataset = new ArrayList<>();
    }

    public void setData(List<Float> data) {
        _dataset.clear();
        _dataset.addAll(data);

        // notify changes
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return _dataset.size();
    }

    @Override
    public Object getItem(int index) {
        return _dataset.get(index);
    }

    @Override
    public float getY(int index) {
        return _dataset.get(index);
    }
}
