package com.example.fintrack.TransactionService.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fintrack.MainActivity;
import com.example.fintrack.R;
import com.example.fintrack.TransactionService.util.ReceiptParser;
import com.example.fintrack.TransactionService.data.entity.TransactionEntity;
import com.example.fintrack.TransactionService.usecase.CreateTransactionUseCase;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@androidx.camera.core.ExperimentalGetImage
public class ScanReceiptActivity extends AppCompatActivity {

    private PreviewView previewView;
    private EditText txtMerchant, txtDate, txtTotalAmount;
    private Button btnConfirm;
    private ImageView btnGallery, btnCapture;

    private TextRecognizer recognizer;

    private boolean scanCompleted = false;
    private boolean isUsingGallery = false;

    private ProcessCameraProvider cameraProvider;

    private ActivityResultLauncher<Intent> galleryLauncher;

    // ===========================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_receipt);
// Khởi tạo OCR engine của Google ML Kit
        recognizer = TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS
        );

        previewView = findViewById(R.id.previewView);
        txtMerchant = findViewById(R.id.txtMerchant);
        txtDate = findViewById(R.id.txtDate);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnGallery = findViewById(R.id.btnGallery);
        btnCapture = findViewById(R.id.btnCapture);

        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        Button btnRetake = findViewById(R.id.btnRetake);

        btnRetake.setOnClickListener(v -> {
            scanCompleted = false;
            isUsingGallery = false;

            txtMerchant.setText("");
            txtDate.setText("");
            txtTotalAmount.setText("");

            startCamera();
        });

        requestPermissions();
        setupGallery();
        setupButtons();
    }

    // ===========================
    private void requestPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startCamera();
        }
    }

    // ===========================
    private void startCamera() {
// thành phần quản lý camera trong CameraX
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {

                cameraProvider = cameraProviderFuture.get();
// Hiển thị hình ảnh camera lên PreviewView
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(
                                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(this),
                        imageProxy -> {

                            if (scanCompleted || isUsingGallery) {
                                imageProxy.close();
                                return;
                            }

                            if (imageProxy.getImage() == null) {
                                imageProxy.close();
                                return;
                            }

                            InputImage image =
                                    InputImage.fromMediaImage(
                                            imageProxy.getImage(),
                                            imageProxy.getImageInfo().getRotationDegrees()
                                    );

                            recognizer.process(image)
                                    .addOnSuccessListener(visionText -> {

                                        String text = visionText.getText();

                                        String total =
                                                ReceiptParser.extractTotal(text);

                                        if (total != null &&
                                                !total.equals("-") &&
                                                !total.isEmpty()) {

                                            scanCompleted = true;

                                            String date =
                                                    ReceiptParser.extractDate(text);
                                            String store =
                                                    ReceiptParser.extractStoreName(text);

                                            txtMerchant.setText(store);
                                            txtDate.setText(date);
                                            txtTotalAmount.setText(total);

                                            stopCamera();

                                            Toast.makeText(this,
                                                    "Scan Completed",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Log.e("OCR_FAIL", e.getMessage())
                                    )
                                    .addOnCompleteListener(task ->
                                            imageProxy.close()
                                    );
                        });

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(this));
    }

    // ===========================
    private void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    // ===========================
    private void setupGallery() {

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == RESULT_OK &&
                            result.getData() != null) {

                        isUsingGallery = true;
                        stopCamera();

                        Uri imageUri = result.getData().getData();

                        try {
                            InputImage image =
                                    InputImage.fromFilePath(this, imageUri);

                            recognizer.process(image)
                                    .addOnSuccessListener(visionText -> {

                                        String text = visionText.getText();

                                        String total =
                                                ReceiptParser.extractTotal(text);
                                        String date =
                                                ReceiptParser.extractDate(text);
                                        String store =
                                                ReceiptParser.extractStoreName(text);

                                        txtMerchant.setText(store);
                                        txtDate.setText(date);
                                        txtTotalAmount.setText(total);

                                        scanCompleted = true;
                                    });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        btnGallery.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });
    }

    // ===========================
    private void setupButtons() {

        // Manual stop camera
        btnCapture.setOnClickListener(v -> {
            stopCamera();
            Toast.makeText(this,
                    "Camera Stopped",
                    Toast.LENGTH_SHORT).show();
        });

        btnConfirm.setOnClickListener(v -> {

            String amount = txtTotalAmount.getText().toString().trim();
            String date = txtDate.getText().toString().trim();
            String merchant = txtMerchant.getText().toString().trim();

            if (amount.isEmpty()) {
                Toast.makeText(this, "Amount required", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(
                    ScanReceiptActivity.this,
                    AddTransactionActivity.class
            );

            intent.putExtra("amount", amount);
            intent.putExtra("date", date);
            intent.putExtra("merchant", merchant);

            startActivity(intent);
        });
    }

    // ===========================
    private void returnResult() {

        Intent resultIntent = new Intent();
        resultIntent.putExtra("TOTAL",
                txtTotalAmount.getText().toString());
        resultIntent.putExtra("DATE",
                txtDate.getText().toString());
        resultIntent.putExtra("STORE",
                txtMerchant.getText().toString());

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    // ===========================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCamera();
        recognizer.close();
    }
}