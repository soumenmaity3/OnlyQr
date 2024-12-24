package com.example.onlyqr;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;

public class GenerativeQR extends AppCompatActivity {
private TextView qrCodeTv;
private ImageView qrCodeIv;
private TextInputEditText dataEdt;
private Button generateQRBtn;
private ImageButton btnDownload;


    @SuppressLint({"WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_generative_qr);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        qrCodeTv=findViewById(R.id.idTVGenerateQR);
        qrCodeIv=findViewById(R.id.idIVQRCode);
        dataEdt=findViewById(R.id.idEditData);
        generateQRBtn=findViewById(R.id.idBtnGenerateQr);
        btnDownload=findViewById(R.id.idBtnDownload);



        generateQRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = dataEdt.getText().toString();
                if (data.isEmpty()) {
                    Toast.makeText(GenerativeQR.this, "Please enter some data to generate QR Code", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        // Get screen size
                        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                        Display display = manager.getDefaultDisplay();
                        Point point = new Point();
                        display.getSize(point);
                        int width = point.x;
                        int height = point.y;
                        int dimen = Math.min(width, height);
                        dimen = dimen * 3 / 4;

                        // Generate QR code
                        com.google.zxing.qrcode.QRCodeWriter writer = new com.google.zxing.qrcode.QRCodeWriter();
                        com.google.zxing.common.BitMatrix bitMatrix = writer.encode(data, com.google.zxing.BarcodeFormat.QR_CODE, dimen, dimen);

                        // Convert BitMatrix to Bitmap
                        Bitmap bitmap = Bitmap.createBitmap(dimen, dimen, Bitmap.Config.RGB_565);
                        for (int x = 0; x < dimen; x++) {
                            for (int y = 0; y < dimen; y++) {
                                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
                            }
                        }

                        // Display the QR code in the ImageView
                        qrCodeIv.setImageBitmap(bitmap);
                        qrCodeTv.setText("QR Code Generated Successfully!");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(GenerativeQR.this, "Error generating QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use the QR code bitmap displayed in the ImageView
                qrCodeIv.setDrawingCacheEnabled(true);
                Bitmap bitmap = qrCodeIv.getDrawingCache();
                downloadQR(bitmap);
                qrCodeIv.setDrawingCacheEnabled(false);
            }
        });

    }

    public void downloadQR(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, "Generate QR Code before downloading!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String fileName = "QRCode_" + System.currentTimeMillis() + ".png";
            String savedImagePath;

            // Check if external storage is writable
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/QRCodeGenerator");
                if (!storageDir.exists()) {
                    boolean mkdirs = storageDir.mkdirs();
                    if (!mkdirs) {
                        Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                File imageFile = new File(storageDir, fileName);
                savedImagePath = imageFile.getAbsolutePath();

                // Save the bitmap to the file
                try (FileOutputStream fileOutputStream = new FileOutputStream(imageFile)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    fileOutputStream.flush();
                }

                // Add the image to the gallery
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(imageFile);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

                Toast.makeText(this, "QR Code saved to Gallery: " + savedImagePath, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "External storage not writable", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}