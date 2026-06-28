package com.example.projectscanotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

// Mengimpor R proyek sendiri secara aman
import com.example.projectscanotes.R;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class ExplainAIActivity extends AppCompatActivity {

    // Kunci API resmi milik kamu dan URL Endpoint Gemini 2.5 Flash
    private static final String GEMINI_API_KEY = "AQ.Ab8RN6KWAPG-jkBtB2_hRQvBnbf-A8DsSi2YzQgJ8CZ8QZt8Og";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + GEMINI_API_KEY;

    private TextView tvAiTitle, tvAiExplanation, tvAiExamples, tvAiSummary;
    private AppCompatButton btnSaveHistory;
    private RequestQueue requestQueue;
    private DatabaseHelper databaseHelper;

    // Variabel penampung hasil analisis teks untuk disimpan ke database SQLite
    private String currentTitle = "";
    private String currentExplanation = "";
    private String currentExamples = "";
    private String currentSummary = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explain_ai);

        // Inisialisasi komponen UI sesuai XML
        ImageView btnBack = findViewById(R.id.btnBack);
        tvAiTitle = findViewById(R.id.tvAiTitle);
        tvAiExplanation = findViewById(R.id.tvAiExplanation);
        tvAiExamples = findViewById(R.id.tvAiExamples);
        tvAiSummary = findViewById(R.id.tvAiSummary);
        btnSaveHistory = findViewById(R.id.btnSaveHistory);

        // Inisialisasi Volley Queue dan SQLite Database Helper
        requestQueue = Volley.newRequestQueue(this);
        databaseHelper = new DatabaseHelper(this);

        // Tombol kembali
        btnBack.setOnClickListener(v -> finish());

        // Memeriksa Intent: Apakah halaman dibuka dari List Catatan Tersimpan atau dari Scan Baru
        boolean isFromHistory = getIntent().getBooleanExtra("is_from_history", false);

        if (isFromHistory) {
            // MODE BACA: Tampilkan data lama langsung dari database tanpa panggil API Gemini
            currentTitle = getIntent().getStringExtra("history_title");
            currentExplanation = getIntent().getStringExtra("history_explanation");
            currentExamples = getIntent().getStringExtra("history_examples");
            currentSummary = getIntent().getStringExtra("history_summary");

            tvAiTitle.setText(currentTitle);
            tvAiExplanation.setText(currentExplanation);
            tvAiExamples.setText(currentExamples);
            tvAiSummary.setText(currentSummary);

            // Ubah fungsi tombol bawah menjadi tombol kembali biasa karena data sudah tersimpan
            btnSaveHistory.setText("Kembali ke Catatan");
            btnSaveHistory.setOnClickListener(v -> finish());
        } else {
            // MODE SCAN BARU: Jalankan panggil Gemini AI dan fungsikan tombol Simpan ke database
            btnSaveHistory.setOnClickListener(v -> {
                if (currentTitle.isEmpty() || currentTitle.equals("Memuat...")) {
                    Toast.makeText(this, "Tunggu hingga analisis AI selesai!", Toast.LENGTH_SHORT).show();
                } else {
                    // Masukkan data hasil analisis ke SQLite
                    long result = databaseHelper.insertHistory(currentTitle, currentExplanation, currentExamples, currentSummary);
                    if (result != -1) {
                        Toast.makeText(this, "Berhasil disimpan ke catatan!", Toast.LENGTH_SHORT).show();

                        // Setelah berhasil disimpan, lompat langsung ke halaman SavedNotesActivity
                        Intent intent = new Intent(ExplainAIActivity.this, SavedNotesActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Gagal menyimpan catatan.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Menangkap kiriman teks scan dari halaman konfirmasi
            String textToExplain = getIntent().getStringExtra("text_to_explain");
            if (textToExplain != null && !textToExplain.trim().isEmpty()) {
                panggilGeminiAI(textToExplain);
            } else {
                tvAiExplanation.setText("Gagal memuat teks karena kosong.");
            }
        }
    }

    private void panggilGeminiAI(String teksInput) {
        try {
            // Menyusun prompt perintah terstruktur tanpa menggunakan karakter fisik backtick dalam kode
            String perintahPrompt = "Analisislah teks berikut secara mendalam. Berikan output HANYA dalam bentuk format JSON mentah tanpa dibungkus format markdown atau teks penjelasan tambahan apa pun. Struktur JSON harus memiliki 4 key string berikut:\n"
                    + "\"judul\": judul singkat dari topik teks ini,\n"
                    + "\"penjelasan\": penjelasan lengkap, padat, dan informatif mengenai topik tersebut,\n"
                    + "\"contoh\": berikan contoh-contoh relevan yang konkret,\n"
                    + "\"kesimpulan\": kesimpulan akhir yang merangkum semuanya.\n\n"
                    + "Teks yang harus dianalisis:\n" + teksInput;

            // Menyusun JSON payload sesuai standar input Google Gemini API
            JSONObject textObj = new JSONObject().put("text", perintahPrompt);
            JSONArray partsArray = new JSONArray().put(textObj);
            JSONObject partsObj = new JSONObject().put("parts", partsArray);
            JSONArray contentsArray = new JSONArray().put(partsObj);
            JSONObject jsonBody = new JSONObject().put("contents", contentsArray);

            // Membuat Request POST via Volley
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, GEMINI_URL, jsonBody,
                    response -> {
                        try {
                            // Mengambil string teks mentah dari respons JSON server Gemini
                            String jsonMentahDariAi = response.getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");

                            // Trik membersihkan format pembungkus markdown (karakter ```) menggunakan kode ascii char 96
                            String bTick = String.valueOf((char) 96);
                            String tripleBTick = bTick + bTick + bTick;

                            jsonMentahDariAi = jsonMentahDariAi.replace(tripleBTick + "json", "")
                                    .replace(tripleBTick, "")
                                    .trim();

                            // Mengurai string bersih menjadi objek JSON utama
                            JSONObject hasilJson = new JSONObject(jsonMentahDariAi);

                            // Simpan ke variabel kelas
                            currentTitle = hasilJson.optString("judul", "Tidak ada judul");
                            currentExplanation = hasilJson.optString("penjelasan", "-");
                            currentExamples = hasilJson.optString("contoh", "-");
                            currentSummary = hasilJson.optString("kesimpulan", "-");

                            // Masukkan ke masing-masing TextView UI secara dinamis
                            tvAiTitle.setText(currentTitle);
                            tvAiExplanation.setText(currentExplanation);
                            tvAiExamples.setText(currentExamples);
                            tvAiSummary.setText(currentSummary);

                        } catch (JSONException e) {
                            tvAiExplanation.setText("Gagal mengurai jawaban AI. Coba sekali lagi.");
                        }
                    },
                    error -> {
                        String errorMsg = "Terjadi kesalahan jaringan atau waktu tunggu habis.";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String data = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                JSONObject errObj = new JSONObject(data);
                                errorMsg = errObj.getJSONObject("error").getString("message");
                            } catch (Exception ignored) {}
                        } else if (error.getMessage() != null) {
                            errorMsg = error.getMessage();
                        }
                        tvAiExplanation.setText("Error: " + errorMsg);
                    });

            // Memperpanjang durasi waktu tunggu Volley menjadi 20 detik (20000 milidetik)
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            // Jalankan antrean request ke server Google
            requestQueue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}