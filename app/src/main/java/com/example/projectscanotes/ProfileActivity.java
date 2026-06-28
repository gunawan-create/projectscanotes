package com.example.projectscanotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ProfileActivity extends AppCompatActivity {

    private EditText etNama, etGender, etTanggal;
    private Button btnSimpan, btnHapus;
    private CardView btnEditPhoto;
    private ImageView imgProfileAvatar; // TAMBAHAN: Hubungkan komponen gambar dalam bingkai

    private LinearLayout tabHome;
    private LinearLayout tabAccount;
    private LinearLayout tabHistory;

    // Variabel penanda indeks avatar aktif (0 = default user, 1 = avatar alternatif 1, dst)
    private int currentAvatarIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. Inisialisasi input profil
        etNama = findViewById(R.id.etNama);
        etGender = findViewById(R.id.etGender);
        etTanggal = findViewById(R.id.etTanggal);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnHapus = findViewById(R.id.btnHapus);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar); // HUBUNGKAN ID GAMBAR

        // Inisialisasi tab menu navigasi bawah
        tabHome = findViewById(R.id.tabHome);
        tabAccount = findViewById(R.id.tabAccount);
        tabHistory = findViewById(R.id.tabHistory);

        // 2. Membaca data SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        if (etNama != null) etNama.setText(prefs.getString("nama", ""));
        if (etGender != null) etGender.setText(prefs.getString("gender", ""));
        if (etTanggal != null) etTanggal.setText(prefs.getString("tanggal", ""));

        // Memuat status indeks gambar yang pernah disimpan sebelumnya
        currentAvatarIndex = prefs.getInt("avatar_index", 0);
        updateAvatarDisplay(currentAvatarIndex);

        // Aksi Klik Tombol Pensil Edit Foto Profil (Siklus ganti gambar sederhana)
        if (btnEditPhoto != null) {
            btnEditPhoto.setOnClickListener(v -> {
                // Berganti indeks gambar (misal berputar dari 0 ke 1, lalu kembali ke 0)
                currentAvatarIndex = (currentAvatarIndex == 0) ? 1 : 0;
                updateAvatarDisplay(currentAvatarIndex);
                Toast.makeText(ProfileActivity.this, "Gambar profil diganti! Klik Simpan untuk menerapkan.", Toast.LENGTH_SHORT).show();
            });
        }

        // Aksi tombol Simpan data profil
        if (btnSimpan != null) {
            btnSimpan.setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("nama", etNama != null ? etNama.getText().toString() : "");
                editor.putString("gender", etGender != null ? etGender.getText().toString() : "");
                editor.putString("tanggal", etTanggal != null ? etTanggal.getText().toString() : "");
                editor.putInt("avatar_index", currentAvatarIndex); // SIMPAN INDEKS GAMBAR BARU
                editor.apply();

                Toast.makeText(ProfileActivity.this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
            });
        }

        // Aksi tombol Hapus Data Profil
        if (btnHapus != null) {
            btnHapus.setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                if (etNama != null) etNama.setText("");
                if (etGender != null) etGender.setText("");
                if (etTanggal != null) etTanggal.setText("");

                currentAvatarIndex = 0; // Kembalikan ke default
                updateAvatarDisplay(currentAvatarIndex);

                Toast.makeText(ProfileActivity.this, "Data profil berhasil dihapus", Toast.LENGTH_SHORT).show();
            });
        }

        // ================= BOTTOM NAVIGATION LOGIC =================

        if (tabHome != null) {
            tabHome.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
        }

        if (tabAccount != null) {
            tabAccount.setOnClickListener(v -> {
                // Sudah di halaman aktif
            });
        }

        if (tabHistory != null) {
            tabHistory.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, SavedNotesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
        }
    }

    // Fungsi pembantu memperbarui visual komponen ImageView lokal
    private void updateAvatarDisplay(int index) {
        if (imgProfileAvatar != null) {
            if (index == 1) {
                // Ubah ke gambar alternatif kedua milik Anda (Misal menggunakan ic_home atau ic_history sebagai contoh)
                imgProfileAvatar.setImageResource(R.drawable.ic_history);
            } else {
                // Default avatar user biasa
                imgProfileAvatar.setImageResource(R.drawable.ic_user);
            }
        }
    }
}