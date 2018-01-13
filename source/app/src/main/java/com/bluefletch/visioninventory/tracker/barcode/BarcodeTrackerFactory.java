package com.bluefletch.visioninventory.tracker.barcode;

import com.bluefletch.visioninventory.ui.camera.PreviewOverlay;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

public class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {

    private PreviewOverlay mPreviewOverlay;

    public BarcodeTrackerFactory(PreviewOverlay previewOverlay) {
        mPreviewOverlay = previewOverlay;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        return new BarcodeTracker(mPreviewOverlay);
    }
}
