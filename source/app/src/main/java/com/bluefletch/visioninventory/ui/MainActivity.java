package com.bluefletch.visioninventory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bluefletch.visioninventory.BaseActivity;
import com.bluefletch.visioninventory.R;
import com.bluefletch.visioninventory.data.ProductDataService;
import com.bluefletch.visioninventory.helpers.ProductFileLoader;

public class MainActivity extends BaseActivity {

    final String TAG = getClass().getSimpleName();
    ProductFileLoader _productFileLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProductDataService.getInstance(this);
        _productFileLoader = ProductFileLoader.getInstance(this);
        _productFileLoader.loadDataFromExternalFile();

    }

    public void onStartScanner(View v) {
        startActivity(new Intent(this, ScannerActivity.class));
    }
}
