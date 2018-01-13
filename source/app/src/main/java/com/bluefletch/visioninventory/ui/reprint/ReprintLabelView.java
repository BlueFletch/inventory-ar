package com.bluefletch.visioninventory.ui.reprint;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bluefletch.visioninventory.R;
import com.bluefletch.visioninventory.helpers.PrinterManager;
import com.bluefletch.visioninventory.ui.ScannerActivity;
import com.bluefletch.visioninventory.data.ProductDataService;

public class ReprintLabelView {

    private final ScannerActivity _activity;
    private final FrameLayout _parentLayout;
    private ProductDataService _dataService;
    private PrinterManager _printerManager;

    private boolean isActive = false;

    public ReprintLabelView(ScannerActivity activity, FrameLayout parentLayout) {
        _activity = activity;
        _parentLayout = parentLayout;
        _dataService = ProductDataService.getInstance(_activity);
        _printerManager = PrinterManager.getInstance(_activity);
    }

    public void show(String barcode) {

        if (!isActive) isActive = true;
        else return;

        final String currentBarcode = barcode;

        LayoutInflater vi = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final FrameLayout reprintView = (FrameLayout) vi.inflate(R.layout.reprint_label, null);

        ((TextView) reprintView.findViewById(R.id.reprintText)).setText("Reprint label for: " + barcode + "?");
        reprintView.setX(0);
        reprintView.setY(_parentLayout.getHeight());

        _parentLayout.addView(reprintView);
        reprintView.animate().y(_parentLayout.getHeight() - 230);

        reprintView.findViewById(R.id.btnReprintCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _parentLayout.removeView(reprintView);
                isActive = false;
            }
        });

        reprintView.findViewById(R.id.btnReprint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (_printerManager.isPrinterReady()) {
                    _printerManager.printLabel(_dataService.getProduct(currentBarcode));
                    //_dataService.printLabel(currentBarcode);
                    _parentLayout.removeView(reprintView);
                    isActive = false;
                }


            }
        });

    }



}
