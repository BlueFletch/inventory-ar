package com.bluefletch.visioninventory.labeler;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.bluefletch.visioninventory.BaseActivity;
import com.bluefletch.visioninventory.R;
import com.bluefletch.visioninventory.data.ProductDataService;
import com.bluefletch.visioninventory.data.ProductModel;
import com.bluefletch.visioninventory.helpers.PrinterManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LabelerActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();

    private RecyclerView _productListView;
    private LabelerListAdapter _labelerListAdapter;
    private RecyclerView.LayoutManager _layoutManager;

    private ProductDataService _dataService;
    private List<ProductModel> _productList;

    private PrinterManager _printerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);

        _printerManager = PrinterManager.getInstance(this);
        _printerManager.setActivityContext(this);

        _dataService = ProductDataService.getInstance(this);
        _productList =  new ArrayList(_dataService.getProducts());

        _productListView = findViewById(R.id.productListView);
        _layoutManager = new LinearLayoutManager(this);
        _productListView.setLayoutManager(_layoutManager);
        _labelerListAdapter = new LabelerListAdapter(_productList);
        _productListView.setAdapter(_labelerListAdapter);
    }

    public void onPrintSelected(View view) {


        HashSet<String> selectedProducts = _labelerListAdapter.getSelectedProducts();

        if (_printerManager.isPrinterReady()) {

            for (ProductModel product : _productList) {
                if (selectedProducts.contains(product.getBarcode())) {
                    Log.d(TAG, "Printing barcode: " + product.getBarcode() + " with price: " + product.getPriceFormatted());
                    _printerManager.printLabel(product);
                }
            }

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
