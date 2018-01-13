package com.bluefletch.visioninventory.helpers;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.bluefletch.visioninventory.data.ProductDataService;
import com.bluefletch.visioninventory.data.ProductModel;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by alanlampa on 11/9/17.
 */

public class ProductFileLoader {

    private final String TAG = getClass().getSimpleName();

    private static String DATAFILE_FOLDER = "InventoryAR";
    private static String DATAFILE_NAME = "products.json";

    private Context _context;
    private static ProductFileLoader _fileLoader;

    private ProductFileLoader(Context context) {
        _context = context;
    }

    public static ProductFileLoader getInstance(Context context) {
        if (_fileLoader == null) {
            _fileLoader = new ProductFileLoader(context);
        }

        return _fileLoader;
    }

    public void loadDataFromExternalFile() {

        Log.i(TAG, "Loading data from external file...");

        // Newer Android versions (6+) requires us to use a default public folder.
        String pathName = "";

       if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            pathName = Environment.getExternalStorageDirectory().getPath();
        }
        else {
            File path = _context.getExternalFilesDir(null);
            pathName = path.getAbsolutePath();
        }

        String externalDataFilePathName = pathName + "/" + DATAFILE_FOLDER + "/" + DATAFILE_NAME;
        Log.d(TAG, "External file pathname: " + externalDataFilePathName);
        File externalDataFile = new File(externalDataFilePathName);
        externalDataFile.getParentFile().mkdirs();

        if (externalDataFile.exists()) {

            Log.d(TAG, "File: " + externalDataFilePathName + " exists, will attempt to load.");

            FileReader reader = null;
            ProductModel[] products;

            Gson gson = new Gson();
            try {
                reader = new FileReader(externalDataFile);
                products = gson.fromJson(reader, ProductModel[].class);
            } catch(FileNotFoundException fnfx) {
                Log.e(TAG, "lFileNotFoundException", fnfx);
                products = null;
            }
            catch(JsonSyntaxException jsex) {
                Log.e(TAG, "JsonSyntaxException Unable to parse json: ", jsex);
                products = null;
            } catch(JsonIOException jsiox) {
                Log.e(TAG, "JsonIOException Error reading json: ", jsiox);
                products = null;
            } catch(Exception ex) {
                Log.e(TAG, "lUnexpected exception thrown while reading json: ", ex);
                products = null;
            }
            finally {
                try {
                    if(reader != null) {
                        reader.close();
                    }
                } catch(IOException ex) {
                    Log.e(TAG, "IOException error closing file reader ", ex);

                }
            }

            if (products != null) {
                ProductDataService dataService = ProductDataService.getInstance(_context);
                dataService.cacheData(products);
            }

        }

    }




}
