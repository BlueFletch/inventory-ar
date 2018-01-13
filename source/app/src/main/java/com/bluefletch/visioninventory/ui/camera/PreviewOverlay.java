package com.bluefletch.visioninventory.ui.camera;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bluefletch.visioninventory.R;
import com.bluefletch.visioninventory.tracker.Graphic;
import com.bluefletch.visioninventory.ui.ScannerActivity;
import com.bluefletch.visioninventory.data.ProductDataService;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.text.Text;

import java.util.concurrent.ConcurrentHashMap;

public class PreviewOverlay extends FrameLayout {

    public final static int ACTION_SHOW = 10;
    public final static int ACTION_HIDE = 20;
    public final static int ACTION_TAP = 30;

    private int mVisualType = ProductDataService.INVENTORY_DATA;

    public interface PreviewOverlayAction {
        void onPreviewOverlayAction(int action, Barcode barcode);
        void onLabelReprintAction(Barcode barcode);
        void onPreviewInteraction();
    }

    private PreviewOverlayAction mActionListener;

    final String TAG = getClass().getSimpleName();
    private Context mContext;

    private final Object mLock = new Object();
    private int mPreviewWidth;
    private float mWidthScaleFactor = 1.0f;
    private int mPreviewHeight;
    private float mHeightScaleFactor = 1.0f;

    private ConcurrentHashMap<String, Graphic> mTrackedBarcodes;
    private ConcurrentHashMap<Graphic, Boolean> mActiveGraphics;

    public PreviewOverlay(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTrackedBarcodes = new ConcurrentHashMap<>();
        mActiveGraphics = new ConcurrentHashMap<>();

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mActionListener != null) {
                    mActionListener.onPreviewInteraction();
                }
            }
        });

    }

    public void initTabListeners() {
        // register listener for tab changes.
        TabLayout tabs = findViewById(R.id.tabType);

        for (int i = 0; i < tabs.getTabCount(); i++) {
            //noinspection ConstantConditions
            TextView tv=(TextView) LayoutInflater.from(mContext).inflate(R.layout.tab_text,null);
            tv.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/montserrat.ttf"));
            tv.setText(tabs.getTabAt(i).getText());
            tabs.getTabAt(i).setCustomView(tv);

        }

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (mActionListener != null) {
                    mActionListener.onPreviewInteraction();
                }

                int previousType = mVisualType;
                mVisualType = tab.getPosition();

                for (Graphic graphic : mTrackedBarcodes.values()) {
                    graphic.updateText();
                }

                if (mVisualType == ProductDataService.PRICE_DATA) {
                    new ScannerActivity.CameraSettingChangeEvent().setUseTextProcessor(true).setFrameRate(2.0f).post();
                }
                else {
                    if (previousType == ProductDataService.PRICE_DATA) {
                        new ScannerActivity.CameraSettingChangeEvent().setUseTextProcessor(false).setFrameRate(15.0f).post();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }


    public int getVisualType() {
        return mVisualType;
    }

    /**
     * Called by tracker to render a barcode graphic.
     */
    public void renderBarcode(final Barcode barcode) {

        synchronized (mLock) {

            if (mTrackedBarcodes.containsKey(barcode.displayValue)) {
                mTrackedBarcodes.get(barcode.displayValue).updateBarcode(barcode);
                mActiveGraphics.put(mTrackedBarcodes.get(barcode.displayValue), true);
            } else {

                Graphic graphic = new Graphic(this, barcode);
                graphic.setOnTapListener(new Graphic.OnGraphicFeedbackListener() {
                    @Override
                    public void onTap(Barcode barcode) {
                        if (mActionListener != null) {
                            mActionListener.onPreviewOverlayAction(ACTION_TAP, barcode);
                        }
                    }

                    @Override
                    public void onAnomaly(Barcode barcode) {

                    }
                });
                Log.d(TAG, "Creating new barcode: " + barcode.displayValue);
                mTrackedBarcodes.put(barcode.displayValue, graphic);
                mActiveGraphics.put(graphic, true);
            }
        }

        /** Only show the product detail for Inventory and Sales **/
        if (mActiveGraphics.size() == 1 && mVisualType != ProductDataService.PRICE_DATA) {

            Graphic graphic = mTrackedBarcodes.get(barcode.displayValue);
            if (graphic.isDominant()) {
                mActionListener.onPreviewOverlayAction(ACTION_SHOW, barcode);
            }
        }
        else {
            mActionListener.onPreviewOverlayAction(ACTION_HIDE, null);
        }

    }

    /**
     * Called by tracker to unrender a barcode graphic.
     * @param barcode
     */
    public void unrenderBarcode(Barcode barcode) {

        synchronized (mLock) {

            if (mTrackedBarcodes.containsKey(barcode.displayValue)) {
                Graphic graphic = mTrackedBarcodes.get(barcode.displayValue);
                graphic.queueForRemoval();
                mActiveGraphics.remove(graphic);
            }

            if (mActiveGraphics.size() == 0) {
                mActionListener.onPreviewOverlayAction(ACTION_HIDE, null);
            }
        }
    }

    /**
     * Called by graphic eventually when the graphic is gone for good.
     * @param barcode
     */

    public void release(Barcode barcode) {

        synchronized (mLock) {

            if (mTrackedBarcodes.containsKey(barcode.displayValue)) {
                Log.d(TAG, "Releasing barcode: " + barcode.displayValue);
                mTrackedBarcodes.remove(barcode.displayValue);
            }
        }

    }

    public void renderTextBlock(Text text) {

        // since the barcode is left of the price, increase the size
        // of this text rectangle to intersect with a barcode.
        RectF rectF = new RectF(text.getBoundingBox());

        // increase the left-X and bottom-Y coordinates to intersect with potential
        // barcodes
        rectF.left = rectF.left - 400.0f;
        rectF.bottom = rectF.bottom + 100.0f;

        String value = text.getValue();

        Log.v("renderTextBlock", "TextBlock raw text: " + value);

        // cleanup the text.
        String price = value.replaceAll("[A-Z,a-z$ ]", "");

        Log.v("renderTextBlock", "Cleaned up text value: " + price);
        if (price.matches("^\\d+\\.\\d{2}")) {
            Log.v("renderTextBlock", "Price validation ok: " + price);
        }
        else {
            Log.v("renderTextBlock", "ignoring due to failed validation.");
            return;
        }

        Log.v("compareCheck", "Text coordinates: " + String.format("(%f,%f)(%f,%f)", rectF.left, rectF.top, rectF.right, rectF.bottom));

        // loop through currently visible barcodes and look for an intersection
        // between the text and the barcode.  If you find one then they're probably
        // from the same tag, so that barcode value relates to that price.
        for (Graphic graphic : mTrackedBarcodes.values()) {

            rectF = new RectF(graphic.getBarcodeCoordinates());

            Log.v("compareCheck", "Barc coordinates: " + String.format("(%f,%f)(%f,%f)", rectF.left, rectF.top, rectF.right, rectF.bottom));


            if (rectF.intersect(graphic.getBarcodeCoordinates())) {
                Log.d("renderTextBlock", "Found matching barcode: " + graphic.getBarcodeValue());

                boolean shouldUpdate = graphic.updateTextWithCompare(price);
                if (shouldUpdate && mActionListener != null) {
                    Log.d(TAG, "Call label reprint");
                    mActionListener.onLabelReprintAction(graphic.getBarcode());
                }
            }
        }

    }

    public void setCameraInfo(int previewWidth, int previewHeight) {
        synchronized (mLock) {
            mPreviewWidth = previewWidth;
            mPreviewHeight = previewHeight;

            Log.d(TAG, "Canvas size: " + getHeight() + "," + getWidth());

            if ((mPreviewWidth != 0) && (mPreviewHeight != 0)) {
                mWidthScaleFactor = (float) getWidth() / (float) mPreviewWidth;
                mHeightScaleFactor = (float) getHeight() / (float) mPreviewHeight;
                //mHeightScaleFactor =  1280.0f / (float) mPreviewHeight;
            }

            Log.d(TAG, "Preview size: " + mPreviewHeight + "x" + mPreviewWidth);
        }
    }

    public void setOnPreviewActionListener(PreviewOverlayAction actionListener) {
        mActionListener = actionListener;
    }


    public float translateX(float x) {
        return x * mWidthScaleFactor;
    }

    public float translateY(float y) {
        return y * mHeightScaleFactor;
    }




}
