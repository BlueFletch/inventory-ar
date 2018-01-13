package com.bluefletch.visioninventory.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bluefletch.visioninventory.BaseActivity;
import com.bluefletch.visioninventory.R;
import com.bluefletch.visioninventory.tracker.barcode.BarcodeTrackerFactory;
import com.bluefletch.visioninventory.tracker.textblock.TextBlockTrackerFactory;
import com.bluefletch.visioninventory.ui.camera.CameraSource;
import com.bluefletch.visioninventory.ui.detail.ProductDetailView;
import com.bluefletch.visioninventory.ui.camera.CameraSourcePreview;
import com.bluefletch.visioninventory.ui.camera.PreviewOverlay;
import com.bluefletch.visioninventory.ui.reprint.ReprintLabelView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.TextRecognizer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ScannerActivity extends BaseActivity {

    final String TAG = getClass().getSimpleName();

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    public static class CameraSettingChangeEvent {

        public float zoomLevel = 0.0f;
        public float frameRate = 0.0f;
        public Boolean useTextProcessor = null;

        public CameraSettingChangeEvent setZoomLevel(float zoomLevel) {
            this.zoomLevel = zoomLevel;
            return this;
        }

        public CameraSettingChangeEvent setUseTextProcessor(boolean useTextProcessor) {
            this.useTextProcessor = useTextProcessor;
            return this;
        }

        public CameraSettingChangeEvent setFrameRate(float frameRate) {
            this.frameRate = frameRate;
            return this;
        }

        public void post() {
            EventBus.getDefault().post(this);
        }
    }

    private float mZoomLevel = 2;
    private boolean mUseTextProcessor = false;
    private float mFrameRate = 15.0f;

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private PreviewOverlay mPreviewOverlay;
    private ProductDetailView mProductDetailView;
    private ReprintLabelView mReprintLabelView;

    private DisplayMetrics mDisplayMetrics;
    private View mDecorView;

    TextRecognizer mTextRecognizer = null;
    BarcodeDetector mBarcodeDetector = null;
    MultiDetector mMultiDetector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vision_main);

        mDecorView = getWindow().getDecorView();

        mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        mPreview = findViewById(R.id.preview);
        mPreviewOverlay = findViewById(R.id.previewOverlay);
        mProductDetailView = new ProductDetailView(this, (FrameLayout) findViewById(R.id.topLayout));
        mReprintLabelView = new ReprintLabelView(this, (FrameLayout) findViewById(R.id.topLayout));

        mPreviewOverlay.setOnPreviewActionListener(new PreviewOverlay.PreviewOverlayAction() {
            @Override
            public void onPreviewOverlayAction(int action, Barcode barcode) {

                switch (action) {
                    case PreviewOverlay.ACTION_SHOW:
                        //mProductDetailView.refreshData(barcode.displayValue);
                        mProductDetailView.peek(barcode.displayValue);
                        break;
                    case PreviewOverlay.ACTION_HIDE:
                        mProductDetailView.hide();
                        break;
                    case PreviewOverlay.ACTION_TAP:
                        //mProductDetailView.refreshData(barcode.displayValue);
                        mProductDetailView.setToFullscreen(barcode.displayValue);
                        break;

                }
            }

            @Override
            public void onLabelReprintAction(Barcode barcode) {

                final Barcode b = barcode;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mReprintLabelView.show(b.displayValue);
                    }
                });

            }

            @Override
            public void onPreviewInteraction() {
                resetInactivityTimer();
            }
        });

        mPreviewOverlay.initTabListeners();
        mProductDetailView.hide();

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{android.Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mPreviewOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {


        Context context = getApplicationContext();

        // A text recognizer is created to read and track text.  An associated multi-processor instance
        // is set to receive the text blocks read, track the text, and maintain callbacks for text updates
        // The factory is used by the multi-processor to create a separate
        // tracker instance for each text block.
        mTextRecognizer = new TextRecognizer.Builder(context).build();
        TextBlockTrackerFactory textTrackerFactory = new TextBlockTrackerFactory(mPreviewOverlay);
        mTextRecognizer.setProcessor(
                new MultiProcessor.Builder<>(textTrackerFactory).build());

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        mBarcodeDetector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.CODE_128)
                .build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mPreviewOverlay);

        mBarcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        setupDetector();

    }

    private void setupDetector() {

        // A multi-detector groups the two detectors together as one detector.  All images received
        // by this detector from the camera will be sent to each of the underlying detectors, which
        // will each do text and barcode detection, respectively.  The detection results from each
        // are then sent to associated tracker instances which maintain per-item graphics on the
        // screen.

        MultiDetector.Builder detectorBuilder = new MultiDetector.Builder();
        detectorBuilder.add(mBarcodeDetector);

        if (mUseTextProcessor) {
            detectorBuilder.add(mTextRecognizer);
        }
        mMultiDetector = detectorBuilder.build();

        if (!mMultiDetector.isOperational()) {
            // Note: The first time that an app using the barcode or text API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");
            Toast.makeText(this, "Detector not yet available, still waiting on dependency download.", Toast.LENGTH_SHORT).show();

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), mMultiDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(Math.round(mDisplayMetrics.heightPixels * mZoomLevel), Math.round(mDisplayMetrics.widthPixels* mZoomLevel))
                .setRequestedFps(mFrameRate)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        mCameraSource = builder.build();

    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        resetInactivityTimer();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }


    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Inventory AR")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }
    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mPreviewOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public DisplayMetrics getDisplayMetrics() {
        return mDisplayMetrics;
    }

    // listen for events from the preview.
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCameraSettingChangeRequested(CameraSettingChangeEvent event) {

        Log.d(TAG, "Received request to change settings...");

        if (event.useTextProcessor != null) {
            mUseTextProcessor = event.useTextProcessor;
        }

        if (event.zoomLevel > 0.0f) {
            mZoomLevel = event.zoomLevel;
        }

        if (event.frameRate > 0.0f) {
            mFrameRate = event.frameRate;
        }

        setupDetector();
        Log.d(TAG, "Stopping preview...");
        mPreview.stop();
        Log.d(TAG, "Restarting preview...");
        startCameraSource();
    }

    @Override
    public void onUserInteraction() {
        resetInactivityTimer();
    }

    private Timer _inactivityTimer = null;

    private void cancelInactivityTimer() {
        if (_inactivityTimer != null) {
            _inactivityTimer.cancel();
            _inactivityTimer = null;
        }
    }

    private void resetInactivityTimer() {

        Log.v("InactivityTimer", "Inactivity timer reset.");

        cancelInactivityTimer();

        _inactivityTimer = new Timer();
        _inactivityTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.v("InactivityTimer", "Idle for more than two minutes, closing scanning activity.");
                finish();
            }
        }, 1000*60*2);  // set for 2 minutes.

    }
}
