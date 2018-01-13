package com.bluefletch.visioninventory.tracker.textblock;

import com.bluefletch.visioninventory.ui.camera.PreviewOverlay;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.text.TextBlock;

public class TextBlockTrackerFactory implements MultiProcessor.Factory<TextBlock> {

    private PreviewOverlay mPreviewOverlay;

    public TextBlockTrackerFactory(PreviewOverlay previewOverlay) {
        mPreviewOverlay = previewOverlay;
    }

    @Override
    public Tracker<TextBlock> create(TextBlock textBlock) {
        return new TextBlockTracker(mPreviewOverlay);
    }
}
