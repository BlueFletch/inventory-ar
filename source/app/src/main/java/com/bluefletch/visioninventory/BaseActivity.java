package com.bluefletch.visioninventory;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;


public class BaseActivity extends AppCompatActivity {

    ProgressDialog _progressDialog;

    private void initProgress() {
        if (_progressDialog == null) {
            _progressDialog = new ProgressDialog(this);
            _progressDialog.setIndeterminate(true);
            _progressDialog.setCancelable(false);
        }
    }

    public void showProgress(String message) {

        initProgress();

        _progressDialog.setMessage(message);
        _progressDialog.show();
    }

    public void hideProgress() {
        if (_progressDialog != null) {
            _progressDialog.dismiss();
        }
    }

}
