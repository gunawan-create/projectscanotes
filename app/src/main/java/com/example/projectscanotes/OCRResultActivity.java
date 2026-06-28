package com.example.projectscanotes;

import android.content.Intent; // 👈 Kunci perbaikannya ada di baris ini!
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class OCRResultActivity extends AppCompatActivity {

    private EditText etScannedResult;
    private AppCompatButton btnScanAgain, btnExplain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);

        // Inisialisasi komponen UI sesuai layout XML
        etScannedResult = findViewById(R.id.etScannedResult);
        btnScanAgain = findViewById(R.id.btnScanAgain);
        btnExplain = findViewById(R.id.btnExplain);

        // Menangkap data hasil scan dari MainActivity (jika ada)
        String scannedText = getIntent().getStringExtra("scanned_text");
        if (scannedText != null) {
            etScannedResult.setText(scannedText);
        }

        // Aksi tombol Scan Ulang: kembali ke layar kamera
        btnScanAgain.setOnClickListener(v -> finish());

        // Aksi tombol Jelaskan dengan AI: mengirim data ke ExplainAIActivity
        btnExplain.setOnClickListener(v -> {
            String finalUpdatedText = etScannedResult.getText().toString().trim();

            if (finalUpdatedText.isEmpty()) {
                Toast.makeText(OCRResultActivity.this, "Teks kosong! Tidak ada yang bisa dijelaskan.", Toast.LENGTH_SHORT).show();
            } else {
                // Berpindah ke halaman ExplainAI membawa teks modifikasi user
                Intent intent = new Intent(OCRResultActivity.this, ExplainAIActivity.class);
                intent.putExtra("text_to_explain", finalUpdatedText);
                startActivity(intent);
            }
        });
    }
}