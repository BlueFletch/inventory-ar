package com.bluefletch.visioninventory.tracker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bluefletch.visioninventory.R;
import com.bluefletch.visioninventory.data.ProductDataService;
import com.bluefletch.visioninventory.ui.camera.PreviewOverlay;
import com.bluefletch.visioninventory.ui.detail.ProductData;
import com.google.android.gms.vision.barcode.Barcode;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class Graphic {

    final String TAG = getClass().getSimpleName();

    Context _context;
    PreviewOverlay _overlay;
    Barcode _barcode;
    RelativeLayout _overlayView;
    TextView _overlayText;
    Timer _timer = null;

    public interface OnGraphicFeedbackListener {
        void onTap(Barcode barcode);
        void onAnomaly(Barcode barcode);
    }

    private OnGraphicFeedbackListener _onGraphicFeedbackListener;

    public Graphic(PreviewOverlay overlay, Barcode barcode) {
        _context = overlay.getContext();
        _overlay = overlay;
        _barcode = barcode;

        // inflate the barcode graphic overlay
        LayoutInflater vi = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _overlayView = (RelativeLayout) vi.inflate(R.layout.preview_overlay, null);
        _overlayText = (TextView) _overlayView.findViewById(R.id.overlayText);

        _overlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_onGraphicFeedbackListener != null) {
                    _onGraphicFeedbackListener.onTap(_barcode);
                }
            }
        });

        // find the default position for the overlay and animate to it.
        final RectF rect = getRelativePosition(barcode);
        _overlayView.setX(rect.left);
        _overlayView.setY(rect.top);
        _overlayView.setAlpha(0.7f);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "Adding new overlay to view from coordinates: " + rect.left + "," + rect.top);
                _overlay.addView(_overlayView);
                //_overlayView.animate().alpha(0.7f);
                updateText();
            }
        });

    }

    public void updateText() {

        final ProductData data = ProductDataService.getInstance(_context).getProductOverlayData(_barcode.displayValue,
                _overlay.getVisualType(), null);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                _overlayText.setText(data.getValue());
            }
        });

    }

    public boolean updateTextWithCompare(String compareText) {
        final ProductData data = ProductDataService.getInstance(_context).getProductOverlayData(_barcode.displayValue,
                _overlay.getVisualType(), compareText);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                String textValue = data.getValue();

                if (data.getValue().contains(".")) {
                    DecimalFormat df = new DecimalFormat("0.00##");
                    textValue = df.format(Float.valueOf(data.getValue()));
                }

                _overlayText.setText(textValue);

                if (data.shouldHighlight()) {
                    _overlayView.findViewById(R.id.tag).setBackgroundColor(data.getIconResourceId());
                    if (_onGraphicFeedbackListener != null) {
                        _onGraphicFeedbackListener.onAnomaly(_barcode);
                    }
                }
                else {
                    _overlayView.findViewById(R.id.tag).setBackgroundColor(Color.WHITE);
                }

            }
        });

        return data.shouldHighlight();
    }

    public void setOnTapListener(OnGraphicFeedbackListener listener) {
        _onGraphicFeedbackListener = listener;
    }

    public void updateBarcode(Barcode barcode) {

        Log.v("renderTextBlock", "Updating barcode: " + barcode.displayValue);

        cancelPendingTimer();

        _barcode = barcode;
        Log.v("Barcode Coordinates", barcode.displayValue + ": " + barcode.getBoundingBox().toString());

        final RectF rect = getRelativePosition(barcode);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                _overlayView.animate().x(rect.left).y(rect.top);
                _overlayView.setAlpha(0.7f);
            }
        });

    }

    public void queueForRemoval() {

        cancelPendingTimer();

        // start animating out the graphic.  will make it a slow fade out
        // in case it immediately comes back as found from google vision.
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "Removing widget from view...");
                _overlayView.animate().alpha(0.0f);
            }
        });

        // start a timer to permanently remove the barcode from cache.
        _timer = new Timer();
        _timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // permanently remove from the view.
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        _overlay.release(_barcode);
                        _overlay.removeView(_overlayView);
                    }
                });
            }
        }, 1000);

    }

    private RectF getRelativePosition(Barcode barcode) {

        RectF correctedRectF = new RectF();
        RectF rect = new RectF(barcode.getBoundingBox());

        correctedRectF.left = _overlay.translateX(rect.centerX()) - 30.f;
        correctedRectF.top = _overlay.translateY(rect.centerY()) - 80.0f;

        return correctedRectF;
    }



    private void cancelPendingTimer() {
        if (_timer != null) {
            Log.d(TAG, "Timer for " + _barcode.displayValue + " cancelled.");
            _timer.cancel();
            _timer = null;
        }
    }

    public boolean isDominant() {

        float ratio = (float) _barcode.getBoundingBox().width() / (float) _overlay.getWidth();

        if (ratio > 0.4 ) {
            Log.v(TAG, "Current ratio: " + ratio);
            return true;
        }
        return false;

    }

    public Barcode getBarcode() {
        return _barcode;
    }

    public RectF getBarcodeCoordinates() {
        return new RectF(_barcode.getBoundingBox());
    }

    public String getBarcodeValue() {
        return _barcode.displayValue;
    }
}
