package com.bluefletch.visioninventory.helpers;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.bluefletch.visioninventory.BaseActivity;
import com.bluefletch.visioninventory.data.ProductModel;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.text.DateFormat;
import java.util.Date;


public class PrinterManager {

    final String TAG = getClass().getSimpleName();

    Connection _printerConnection;
    BaseActivity _activity;
    String _currentBtAddress;

    private static PrinterManager _printerManager;

    public static PrinterManager getInstance(BaseActivity activity) {
        if (_printerManager == null) {
            _printerManager = new PrinterManager(activity);
        }

        return _printerManager;
    }


    private PrinterManager(BaseActivity activity) {
        _activity = activity;

        Log.d(TAG, "Initialized printer...");
    }

    public void setActivityContext(BaseActivity activity) {
        _activity = activity;
    }

    private void showConnected(String connected) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                _activity);

        if (connected.equals("OK")) {

                alertDialogBuilder.setTitle("Printer Connected");
                alertDialogBuilder.setMessage(_printerConnection.getSimpleConnectionName());
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing.
                    }
                });

        }
        else {
            alertDialogBuilder.setTitle("Connection Error");
            alertDialogBuilder.setMessage("Could not connect - is the printer on?");
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing.
                }
            });
        }

        alertDialogBuilder.create().show();
    }

    public void connect(String btMacAddress) {

        _currentBtAddress = btMacAddress;

        disconnect();

        try {

            new AsyncTask<String, Void, String>() {

                String address;

                @Override
                protected String doInBackground(String... params) {
                    try {

                        address = params[0];

                        // Instantiate connection for given Bluetooth&reg; MAC Address.
                        Log.d(TAG, "Connecting to printer with address: " + address);
                        _printerConnection = new BluetoothConnection(address);

                        // Initialize
                        Looper.prepare();

                        // Open the connection - physical connection is established here.
                        _printerConnection.open();
                        Log.i(TAG, "Connected to printer.");

                        Looper.myLooper().quit();
                    } catch (ConnectionException e) {
                        // Handle communications error here.
                        e.printStackTrace();
                        return e.getMessage();

                    }
                    return "OK";
                }

                @Override
                protected void onPreExecute() {
                    _activity.showProgress("Connecting to printer...");
                }

                @Override
                protected void onPostExecute(String connectState) {
                    //super.onPostExecute(connectState);
                    _activity.hideProgress();
                    showConnected(connectState);

                }
            }.execute(btMacAddress);

        } catch(RuntimeException e) {
            Log.w(TAG, "Runtime exception in pairing, possibly because pairing occurred in an unknown activity.", e);
        }

    }

    public void disconnect() {

        try {

            if (_printerConnection != null && _printerConnection.isConnected()) {
                _printerConnection.close();
            }

        } catch (ConnectionException e) {
            Log.e(TAG, "Connection error on disconnect: ", e);
        }
    }

    public void printLabel(ProductModel d) {

        String currentDateTime = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date());

        String printCommand =
                "^XA~TA000~JSN^LT0^MNM^MTD^PON^PMN^LH0,0^JMA^PR4,4~SD10^JUS^LRN^CI0^XZ\n" +
                "^XA\n" +
                "^MMT\n" +
                "^PW591\n" +
                "^LL0355\n" +
                "^LS0\n" +
                "^FT480,90^A0I,20,19^FH\\^FD" + d.getDpci() + "^FS\n" +
                "^FT558,90^A0I,20,19^FH\\^FD" + d.getPricePerUnit() + "^FS\n" +
                "^FT558,68^A0I,20,19^ FH\\^FDper " + d.getUnitType() + "^FS\n" +
                "^FO7,50^GB567,189,4^FS\n" +
                "^FT558,136^A0I,31,31^FH\\^FD" + d.getUnits() + d.getUnitType() + "^FS\n" +
                "^FT558,170^A0I,31,31^FB300,2,0,L^FH\\^FD" + d.getLabel() + "^FS\n" +
                "^BY2,3,43^FT230,81^BCI,,Y,N\n" +
                "^FD>;" + d.getBarcode() + "^FS\n" +
                "^FT480,68^A0I,20,19^FH\\^FD" + currentDateTime + "^FS\n" +
                "^FT300,145^A0I,85,84^FB271,1,0,R^FH\\^FD$" + d.getPrice() + "^FS\n" +
                "^PQ1,0,1,Y^XZ";


        try {
            if (_printerConnection != null) {
                _printerConnection.write(printCommand.getBytes());
            }
        } catch (ConnectionException e) {
            Log.e(TAG, "Connection error: ", e);
        }

    }

    public boolean isPrinterReady() {
        boolean isOK = false;
        String statusTitle = "Printer Error";
        String statusMessage = "Unknown Printer error.";
        try {

            if ((_printerConnection == null) || (!_printerConnection.isConnected())) {

                statusTitle = "Printer Not Connected";
                statusMessage = "Please pair with the Printer using the NFC tag.";

            }
            else {

                //thePrinterConn.open();
                // Creates a ZebraPrinter object to use Zebra specific functionality like getCurrentStatus()
                ZebraPrinter printer = ZebraPrinterFactory.getInstance(_printerConnection);

                PrinterStatus printerStatus = printer.getCurrentStatus();

                if (printerStatus.isReadyToPrint) {
                    isOK = true;
                } else if (printerStatus.isPaused) {
                    statusMessage = "Cannot Print because the printer is paused.";
                } else if (printerStatus.isHeadOpen) {
                    statusMessage = "Cannot Print because the printer media door is open.";
                } else if (printerStatus.isPaperOut) {
                    statusMessage = "Cannot Print because the paper is out.";
                } else {
                    statusMessage = "Cannot Print.";
                }
            }
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (ZebraPrinterLanguageUnknownException e) {
            e.printStackTrace();
        }

        if (isOK) {
            return isOK;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                _activity);
        alertDialogBuilder.setTitle(statusTitle);
        alertDialogBuilder.setMessage(statusMessage);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing.
            }
        });
        alertDialogBuilder.create().show();

        return isOK;

    }

}
