package com.example.onlyqr;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.google.zxing.RGBLuminanceSource;
import java.io.IOException;

public class ScannerQR extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int IMAGE_CODE = 100;

    private BarcodeView scanner;
    private TextView scannerTV;
    private String qrData;
    private Button btnChooseGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_qr);

        scanner = findViewById(R.id.camView);
        scannerTV = findViewById(R.id.idTVScannerData);
        btnChooseGallery = findViewById(R.id.idBtnChooseImage);

        if (checkPermissions()) {
            startScanning();
        } else {
            requestPermissions();
        }

        scannerTV.setOnClickListener(v -> {
            Intent intent = new Intent(ScannerQR.this, ScannerExecute.class);
            intent.putExtra("DATA", qrData);
            startActivity(intent);
        });
        scannerTV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (qrData != null && !qrData.isEmpty()) {
                    copyToClipboard(qrData);
                } else {
                    Toast.makeText(ScannerQR.this, "No text to copy!", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        btnChooseGallery.setOnClickListener(v -> openGallery());
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
                    qrData = result.getText();
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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                String qrCodeResult = decodeQRCodeFromBitmap(bitmap);
                if (qrCodeResult != null) {
                    Toast.makeText(this, "QR Code: " + qrCodeResult, Toast.LENGTH_LONG).show();
                    scannerTV.setText(qrCodeResult);
                    qrData = qrCodeResult;  // Ensure qrData is updated
                } else {
                    Toast.makeText(this, "No QR code found in the image", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String decodeQRCodeFromBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new MultiFormatReader().decode(binaryBitmap);
            return result.getText();
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void copyToClipboard(String text) {
        if (text == null || text.isEmpty()) {
            Toast.makeText(this, "No text to copy!", Toast.LENGTH_SHORT).show();
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("QR Code", text);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(ScannerQR.this, "Copied to Clipboard", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ScannerQR.this, "Clipboard not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
