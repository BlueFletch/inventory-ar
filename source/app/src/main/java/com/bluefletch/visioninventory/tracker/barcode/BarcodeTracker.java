package com.bluefletch.visioninventory.tracker.barcode;

import android.util.Log;

import com.bluefletch.visioninventory.ui.camera.PreviewOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

public class BarcodeTracker extends Tracker<Barcode> {

    final String TAG = getClass().getSimpleName();

    PreviewOverlay _overlay;
    Barcode _barcode;

    public BarcodeTracker(PreviewOverlay overlay) {
        _overlay = overlay;
    }

    @Override
    public void onNewItem(int id, Barcode barcode) {
    }

    @Override
    public void onUpdate(Detector.Detections<Barcode> detections, Barcode barcode) {
        Log.v(TAG, "Tracker detected barcode: " + barcode.displayValue);
        _barcode = barcode;
        _overlay.renderBarcode(barcode);

    }

    @Override
    public void onMissing(Detector.Detections<Barcode> detections) {
    }

    @Override
    public void onDone() {
        _overlay.unrenderBarcode(_barcode);
    }
}
