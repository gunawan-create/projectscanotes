package com.example.projectscanotes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "CEK_NAVIGASI";

    private CardView cardScan;
    private TextView tvSeeAll;
    private CardView profileFrame;
    private ImageView imgDashboardAvatar;

    private TextView tvGreeting;
    private LinearLayout containerLatestHistory;
    private TextView tvEmptyHistory;

    private LinearLayout tabHome;
    private LinearLayout tabAccount;
    private LinearLayout tabHistory;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        databaseHelper = new DatabaseHelper(this);

        // Inisialisasi Komponen Utama
        cardScan = findViewById(R.id.cardScan);
        tvSeeAll = findViewById(R.id.tvSeeAll);
        profileFrame = findViewById(R.id.profileFrame);
        tvGreeting = findViewById(R.id.tvGreeting);
        containerLatestHistory = findViewById(R.id.containerLatestHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);

        // Inisialisasi Menu Tab Bawah
        tabHome = findViewById(R.id.tabHome);
        tabAccount = findViewById(R.id.tabAccount);
        tabHistory = findViewById(R.id.tabHistory);

        // ================= KUNCI SOLUSI AGAR TIDAK ERROR COMPILE =================
        // Kita langsung tembak ID 'imgProfileAvatar' yang sudah pasti dikenali oleh sistem Android Studio Anda
        imgDashboardAvatar = findViewById(R.id.imgProfileAvatar);
        // =========================================================================

        // Navigasi Tombol Scan
        if (cardScan != null) {
            cardScan.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, ScanActivity.class);
                startActivity(intent);
            });
        }

        // Navigasi Lihat Semua Catatan
        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, SavedNotesActivity.class);
                startActivity(intent);
            });
        }

        // Navigasi Foto Profil Atas
        if (profileFrame != null) {
            profileFrame.setOnClickListener(v -> bukaHalamanProfil());
        }

        // Navigasi Menu Tab Bawah
        if (tabHome != null) {
            tabHome.setOnClickListener(v -> Log.d(TAG, "Sudah di posisi Beranda"));
        }

        if (tabAccount != null) {
            tabAccount.setOnClickListener(v -> bukaHalamanProfil());
        }

        if (tabHistory != null) {
            tabHistory.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(DashboardActivity.this, SavedNotesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Gagal pindah ke Riwayat: " + e.getMessage());
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncDashboardProfileName();
        updateDashboardHistorySummary();
    }

    // SINKRONISASI DATA PROFIL DI BERANDA
    private void syncDashboardProfileName() {
        SharedPreferences prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        // 1. Sinkronisasi Teks Sapaan Nama
        if (tvGreeting != null) {
            String savedName = prefs.getString("nama", "").trim();
            if (!savedName.isEmpty()) {
                tvGreeting.setText("Halo, " + savedName + "!");
            } else {
                tvGreeting.setText("Halo, Pengguna!");
            }
        }

        // 2. Sinkronisasi Gambar Profil
        if (imgDashboardAvatar != null) {
            int avatarIndex = prefs.getInt("avatar_index", 0);
            if (avatarIndex == 1) {
                imgDashboardAvatar.setImageResource(R.drawable.ic_history);
            } else {
                imgDashboardAvatar.setImageResource(R.drawable.ic_user);
            }
        }
        // Jika ImageView tidak ditemukan di XML dashboard, kita manipulasi warna CardView-nya sebagai alternatif aman
        else if (profileFrame != null) {
            int avatarIndex = prefs.getInt("avatar_index", 0);
            if (avatarIndex == 1) {
                profileFrame.setCardBackgroundColor(android.graphics.Color.parseColor("#3F2CB6"));
            } else {
                profileFrame.setCardBackgroundColor(android.graphics.Color.parseColor("#F1F4FA"));
            }
        }
    }

    // TAMPILKAN LIST RIWAYAT SECARA DINAMIS
    private void updateDashboardHistorySummary() {
        if (databaseHelper == null || containerLatestHistory == null) return;

        containerLatestHistory.removeAllViews();
        ArrayList<SavedNoteModel> latestNotes = databaseHelper.getLatestActiveHistory(3);

        if (latestNotes != null && !latestNotes.isEmpty()) {
            if (tvEmptyHistory != null) tvEmptyHistory.setVisibility(View.GONE);

            for (SavedNoteModel note : latestNotes) {
                CardView cardView = new CardView(this);
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, 8, 0, 8);
                cardView.setLayoutParams(cardParams);
                cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
                cardView.setRadius(42);
                cardView.setCardElevation(4);

                LinearLayout innerLayout = new LinearLayout(this);
                innerLayout.setOrientation(LinearLayout.VERTICAL);
                innerLayout.setPadding(48, 48, 48, 48);

                TextView tvTitle = new TextView(this);
                tvTitle.setText(note.getTitle());
                tvTitle.setTextColor(android.graphics.Color.parseColor("#111111"));
                tvTitle.setTextSize(15);
                tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

                TextView tvTime = new TextView(this);
                LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                timeParams.setMargins(0, 12, 0, 0);
                tvTime.setLayoutParams(timeParams);
                tvTime.setText("Terakhir dibuka: " + note.getTimestamp());
                tvTime.setTextColor(android.graphics.Color.parseColor("#8A8A8A"));
                tvTime.setTextSize(12);

                innerLayout.addView(tvTitle);
                innerLayout.addView(tvTime);
                cardView.addView(innerLayout);

                cardView.setOnClickListener(v -> {
                    databaseHelper.updateNoteTimestamp(note.getId());
                    Intent intent = new Intent(DashboardActivity.this, OCRResultActivity.class);
                    intent.putExtra("NOTE_ID", note.getId());
                    startActivity(intent);
                });

                containerLatestHistory.addView(cardView);
            }
        } else {
            if (tvEmptyHistory != null) {
                tvEmptyHistory.setVisibility(View.VISIBLE);
                containerLatestHistory.addView(tvEmptyHistory);
            }
        }
    }

    private void bukaHalamanProfil() {
        try {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Gagal pindah ke Profil: " + e.getMessage());
        }
    }
}