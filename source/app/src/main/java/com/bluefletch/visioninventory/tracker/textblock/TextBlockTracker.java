package com.bluefletch.visioninventory.tracker.textblock;

import android.util.Log;

import com.bluefletch.visioninventory.ui.camera.PreviewOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;


public class TextBlockTracker extends Tracker<TextBlock> {

    final String TAG = getClass().getSimpleName();

    PreviewOverlay _overlay;
    Text _text;

    public TextBlockTracker(PreviewOverlay overlay) {
        _overlay = overlay;
    }

    @Override
    public void onNewItem(int id, TextBlock textBlock) {

    }

    @Override
    public void onUpdate(Detector.Detections<TextBlock> detections, TextBlock textBlock) {

        Log.v(TAG, "Tracker detected textBlock: " + textBlock.getValue());

        for (Text line : textBlock.getComponents()) {
            Log.v(TAG, "Lines: " + line.getValue());
            for (Text element : line.getComponents()) {
                Log.v(TAG, "Element: " + element.getValue());
                if (element.getValue().startsWith("$") || element.getValue().startsWith("S")) {
                    _text = element;
                    _overlay.renderTextBlock(_text);
                    return;
                }
            }
        }
    }

    @Override
    public void onMissing(Detector.Detections<TextBlock> detections) {
    }

    @Override
    public void onDone() {
        _text = null;
    }
}
