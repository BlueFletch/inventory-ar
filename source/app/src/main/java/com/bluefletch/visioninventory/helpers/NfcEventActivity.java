package com.bluefletch.visioninventory.helpers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.bluefletch.visioninventory.BaseActivity;

public class NfcEventActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Started NfcEventActivity...");

        Intent intent = getIntent();

        if (intent != null) {

            String data = intent.getDataString();
            Log.d(TAG, "NfcEventActivity called, action: " + intent.getAction());
            Log.d(TAG, "NfcEventActivity called, data: " + data);

            if (data != null) {

                Uri uri = Uri.parse(data);
                final String btMacAddress = uri.getQueryParameter("mB").replaceAll("(.{2})", "$1" + ":").substring(0, 17).toUpperCase();

                Log.d(TAG, "Found BT mac address: " + btMacAddress);

                PrinterManager printerManager = PrinterManager.getInstance(this);
                printerManager.connect(btMacAddress);
            }

        }

        finish();

    }
}
