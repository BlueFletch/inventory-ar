package com.bluefletch.visioninventory.ui.detail;

import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bluefletch.visioninventory.R;
import com.bluefletch.visioninventory.ui.ScannerActivity;
import com.bluefletch.visioninventory.data.ProductDataService;
import com.bluefletch.visioninventory.data.ProductModel;
import com.bluefletch.visioninventory.helpers.OnSwipeListener;
import com.robinhood.spark.SparkView;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailView  {

    final String TAG = getClass().getSimpleName();

    private final int STATE_FULLSCREEN = 0;
    private final int STATE_PEEKED = 1;
    private final int STATE_HIDDEN = 2;

    private final float ALPHA_FULLSCREEN = 1.0f;
    private final float ALPHA_PEEKED = 0.8f;
    private final float ALPHA_HIDDEN = 0.5f;

    private ScannerActivity _activity;
    private FrameLayout _parentLayout;
    private RelativeLayout _containerView;
    private String _currentBarcode;
    private int _currentState;

    private float peekPosition = 700f;

    private GestureDetectorCompat _gestureDetector;

    // Data source
    private ProductDataService _dataService;

    // Data adapters
    private SparkGraphAdapter _adapter01;
    private SparkGraphAdapter _adapter02;
    private SparkGraphAdapter _adapter03;

    // View pointers
    private Button _closeButton;
    private List<TextView> _metricsList;
    private List<SparkView> _sparkList;
    private TextView _productName;
    private TextView _productBarcode;
    private TextView _productLabel;
    private TextView _productPrice;
    private TextView _productUnits;
    private TextView _productUnitType;

    // TODO: add additional fields.

    public ProductDetailView(ScannerActivity scanActivity, FrameLayout parentLayout) {
        _activity = scanActivity;
        _parentLayout = parentLayout;

        // setup container view.
        _containerView = _parentLayout.findViewById(R.id.overlayDetailContainer);
        _containerView.setY(_activity.getDisplayMetrics().heightPixels);
        _containerView.setAlpha(ALPHA_HIDDEN);
        _currentState = STATE_HIDDEN;

        // get data service instance
        _dataService = ProductDataService.getInstance(_activity);

        setupViewPointers();
        setContainerListener();

    }

    private void setupViewPointers() {

        _productName = _activity.findViewById(R.id.productName);

        _metricsList = new ArrayList<>();
        _metricsList.add(0, (TextView) _activity.findViewById(R.id.metric01));
        _metricsList.add(1, (TextView) _activity.findViewById(R.id.metric02));
        _metricsList.add(2, (TextView) _activity.findViewById(R.id.metric03));

        _sparkList = new ArrayList<>();
        _sparkList.add(0, (SparkView) _activity.findViewById(R.id.spark01));
        _sparkList.add(1, (SparkView) _activity.findViewById(R.id.spark02));
        _sparkList.add(2, (SparkView) _activity.findViewById(R.id.spark03));

        _sparkList.get(0).setAdapter(new SparkGraphAdapter());
        _sparkList.get(1).setAdapter(new SparkGraphAdapter());
        _sparkList.get(2).setAdapter(new SparkGraphAdapter());

        //TODO: add additional fields.
        _productBarcode = _activity.findViewById(R.id.productBarcode);
        _productLabel = _activity.findViewById(R.id.labelProductLabel);
        _productPrice = _activity.findViewById(R.id.productPrice);
        _productUnits = _activity.findViewById(R.id.productUnits);
        _productUnitType = _activity.findViewById(R.id.productUnitType);

        _closeButton = _activity.findViewById(R.id.closeDetailButton);


    }

    public void setToFullscreen(String barcode) {

        if (barcode != null) {
            refreshData(barcode);
        }

        _currentState = STATE_FULLSCREEN;
        _containerView.animate().alpha(ALPHA_FULLSCREEN).y(0);
        _activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _closeButton.setBackgroundResource(R.drawable.ic_close_black_24dp);
            }
        });
    }

    private void setToHidden() {
        _activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _containerView.animate().y(_activity.getDisplayMetrics().heightPixels).alpha(ALPHA_HIDDEN);
                _currentState = STATE_HIDDEN;
            }
        });

    }

    private void setContainerListener() {
        _containerView.findViewById(R.id.productName).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "Detected title tap");

                if (_currentState == STATE_PEEKED) {
                    setToFullscreen(null);
                }
                else if (_currentState == STATE_FULLSCREEN) {
                    setToHidden();
                }
            }
        });

        _containerView.findViewById(R.id.closeDetailButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Detected close tap");

                if (_currentState == STATE_PEEKED) {
                    setToFullscreen(null);
                }
                else if (_currentState == STATE_FULLSCREEN) {
                    setToHidden();
                }
            }
        });

        _gestureDetector = new GestureDetectorCompat(_activity, new OnSwipeListener() {
            @Override
            public boolean onSwipe(Direction direction) {

                if (direction == Direction.up) {
                    setToFullscreen(null);
                }
                else if (direction == Direction.down) {
                    setToHidden();
                }

                return true;
            }
        });

        _containerView.findViewById(R.id.scrollChild).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                _gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

    }


    public void refreshData(String barcode) {

        final ProductModel product = _dataService.getProduct(barcode);

        _activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (product == null) {
                    _productName.setText("Unknown Product");
                    return;
                }

                _productName.setText(product.getDescription());

                ((SparkGraphAdapter) _sparkList.get(0).getAdapter()).setData(product.getUnitTrendData());
                ((SparkGraphAdapter) _sparkList.get(1).getAdapter()).setData(product.getSalesTrendData());
                ((SparkGraphAdapter) _sparkList.get(2).getAdapter()).setData(product.getOnHandTrendData());

                _metricsList.get(0).setText(product.getUnitsSoldLastWeek());
                _metricsList.get(1).setText(product.getSalesTrend());
                _metricsList.get(2).setText(product.getOnHand());

                _productBarcode.setText(product.getBarcode());
                _productLabel.setText(product.getLabel());
                _productPrice.setText(product.getPriceFormatted());
                _productUnits.setText(product.getUnits());
                _productUnitType.setText(product.getUnitType());

            }
        });



    }

    public void peek(String barcode) {

        if (_dataService.getProduct(barcode) == null) return;

        if (_currentState == STATE_PEEKED) return;

        Log.v(TAG, "Attempting to peek...");

        if (_currentState == STATE_HIDDEN) {
            peek();
            refreshData(barcode);
        }
    }

    public void peek() {

        if (_currentState == STATE_HIDDEN) {
            _currentState = STATE_PEEKED;

            _activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _containerView.animate().alpha(ALPHA_PEEKED).y(peekPosition);
                    _closeButton.setBackgroundResource(R.drawable.ic_arrow_upward_black_24dp);
                }
            });

        }

    }

    public void hide() {

        if (_currentState == STATE_PEEKED) {
            setToHidden();
        }
    }

}
