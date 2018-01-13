package com.bluefletch.visioninventory.data;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import com.bluefletch.visioninventory.R;
import com.bluefletch.visioninventory.ui.detail.ProductData;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ProductDataService {

    final String TAG = getClass().getSimpleName();

    public static final int INVENTORY_DATA = 0;
    public static final int SALES_DATA = 1;
    public static final int PRICE_DATA = 2;

    private final Context _context;
    private static ProductDataService _dataService;
    private ConcurrentHashMap<String, ProductModel> _cachedProducts;

    public static ProductDataService getInstance(Context context) {
        if (_dataService == null) {
            _dataService = new ProductDataService(context);
        }
        return _dataService;

    }

    private ProductDataService(Context context) {
        _context = context;
        loadAndCacheData();
    }

    private void loadAndCacheData() {

        try {

            InputStream is = _context.getAssets().open("products.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String bufferString = new String(buffer);
            Gson gson = new Gson();
            ProductModel[] products = gson.fromJson(bufferString, ProductModel[].class);

            cacheData(products);

        } catch (Exception e) {
            Log.e(TAG, "Error loading product data: ", e);
        }
    }

    public void cacheData(ProductModel[] products) {
        /**
         * cache the data..
         */
        if (_cachedProducts == null) {
            _cachedProducts = new ConcurrentHashMap<>();
        }
        else {
            _cachedProducts.clear();
        }

        for (ProductModel product : products) {
            _cachedProducts.put(product.getBarcode(), product);
        }

        Log.i(TAG, "Cached: " + _cachedProducts.size() + " products!");
    }

    public ProductData getProductOverlayData(String barcode, int type, String compareString) {

        ProductModel product = _cachedProducts.get(barcode);
        if (product == null) {
            Log.e(TAG, "Cached barcode not found: " + barcode);
        }
        ProductData data = null;

        if (product != null) {

            switch (type) {
                case SALES_DATA:

                    // fix to make sure 0s have % too.
                    if (product.getSalesTrend().equals("0")) {
                        product.setSalesTrend("0%");
                    }

                    data = new ProductData(product.getSalesTrend(),
                            Float.valueOf(product.getSalesTrend().replace("%", "")) < 0f,
                            ResourcesCompat.getColor(_context.getResources(), R.color.highlight, null));
                    break;
                case INVENTORY_DATA:
                    data = new ProductData(product.getOnHand(),
                            Float.valueOf(product.getOnHand())
                                    < Float.valueOf(product.getReorderThreshold()),
                            ResourcesCompat.getColor(_context.getResources(), R.color.highlight, null));
                    break;
                case PRICE_DATA:

                    if (compareString != null) {
                        Log.v(TAG, "product price: [" + product.getPrice() + "], compare: [" + compareString + "]");
                        data = new ProductData(product.getPrice(), !product.getPrice().equals(compareString),
                                ResourcesCompat.getColor(_context.getResources(), R.color.highlight, null));
                    }
                    else {
                        data = new ProductData(product.getPrice(),
                                false,
                                ResourcesCompat.getColor(_context.getResources(), R.color.highlight, null));
                    }
                    break;
            }
        }
        else {
            data = new ProductData("?",
                    true,
                    ResourcesCompat.getColor(_context.getResources(), R.color.highlight, null));
        }

        return data;

    }

    public ProductModel getProduct(String barcode) {
        return _cachedProducts.get(barcode);
    }
    public List<ProductModel> getProducts() { return new ArrayList<>(_cachedProducts.values()); }


}
