package com.example.projectscanotes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;
    private PreviewView cameraPreview;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        cameraPreview = findViewById(R.id.cameraPreview);
        ImageView btnCapture = findViewById(R.id.btnCapture);
        ImageView btnBack = findViewById(R.id.btnBack);

        cameraExecutor = Executors.newSingleThreadExecutor();

        btnBack.setOnClickListener(v -> finish());

        checkPermissionAndStartCamera();

        // Mengaktifkan tombol capture untuk memanggil fungsi potret & OCR
        btnCapture.setOnClickListener(v -> captureAndRecognizeText());
    }

    private void checkPermissionAndStartCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                // Mempersiapkan fitur jepret foto
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Gagal membuka kamera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // === INI FUNGSI YANG TADI HILANG / TIDAK KETEMU ===
    private void captureAndRecognizeText() {
        if (imageCapture == null) return;

        Toast.makeText(this, "Memproses gambar...", Toast.LENGTH_SHORT).show();

        imageCapture.takePicture(cameraExecutor, new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                @SuppressWarnings("ExperimentalGetImage")
                android.media.Image mediaImage = imageProxy.getImage();

                if (mediaImage != null) {
                    InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                    recognizer.process(image)
                            .addOnSuccessListener(visionText -> {
                                imageProxy.close(); // Tutup memori gambar setelah selesai

                                String extractedText = visionText.getText();

                                // Berpindah halaman ke OCRResultActivity sambil membawa hasil teks
                                Intent intent = new Intent(ScanActivity.this, OCRResultActivity.class);
                                intent.putExtra("scanned_text", extractedText);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                imageProxy.close();
                                runOnUiThread(() -> Toast.makeText(ScanActivity.this, "Gagal membaca teks: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            });
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> Toast.makeText(ScanActivity.this, "Gagal mengambil foto: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Aplikasi membutuhkan izin kamera!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}