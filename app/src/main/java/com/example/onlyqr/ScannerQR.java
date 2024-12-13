package com.example.onlyqr;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

public class ScannerQR extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private BarcodeView scanner;
    private TextView scannerTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_qr);

        scanner = findViewById(R.id.camView);
        scannerTV = findViewById(R.id.idTVScannerData);

        if (checkPermissions()) {
            startScanning();
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
    }

    private void startScanning() {
        scanner.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null) {
                    String qrData = result.getText();
                    scannerTV.setText(qrData);
                    Toast.makeText(ScannerQR.this, "Scanned: " + qrData, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) {
                // Optional: Handle result points if needed
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scanner != null) {
            scanner.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scanner != null) {
            scanner.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
