package com.example.projectscanotes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectscanotes.R;
import java.util.ArrayList;

public class SavedNotesActivity extends AppCompatActivity {

    private RecyclerView rvNotesList;
    private TextView tvNotesHeader, tvEmptyState;
    private EditText etSearchInput;
    private ImageView btnSearch;

    private DatabaseHelper databaseHelper;
    private SavedNotesAdapter adapter;
    private ArrayList<SavedNoteModel> listNotes;
    private ArrayList<SavedNoteModel> filteredList;

    private boolean isSearchActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_notes);

        // Menghubungkan komponen UI dari layout XML
        rvNotesList = findViewById(R.id.rvNotesList);
        tvNotesHeader = findViewById(R.id.tvNotesHeader);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        etSearchInput = findViewById(R.id.etSearchInput);
        btnSearch = findViewById(R.id.btnSearch);

        // Inisialisasi tab menu navigasi bawah
        LinearLayout tabHome = findViewById(R.id.tabHome);
        LinearLayout tabAccount = findViewById(R.id.tabAccount);
        LinearLayout tabHistory = findViewById(R.id.tabHistory);

        // Basis Data & Inisialisasi List array
        databaseHelper = new DatabaseHelper(this);
        listNotes = new ArrayList<>();
        filteredList = new ArrayList<>();

        if (rvNotesList != null) {
            rvNotesList.setLayoutManager(new LinearLayoutManager(this));
        }

        // ================= INSTAN BOTTOM NAVIGATION LOGIC =================

        // 1. NAVIGASI: TAB BERANDA (Pindah ke DashboardActivity secara instan)
        if (tabHome != null) {
            tabHome.setOnClickListener(v -> {
                Intent intent = new Intent(SavedNotesActivity.this, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0); // Efek transisi instan ala Fragment
                finish(); // Menghilangkan tumpukan Activity agar tidak crash saat di-back
            });
        }

        // 2. NAVIGASI: TAB AKUN (Pindah ke ProfileActivity secara instan)
        if (tabAccount != null) {
            tabAccount.setOnClickListener(v -> {
                Intent intent = new Intent(SavedNotesActivity.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
        }

        // 3. NAVIGASI: TAB RIWAYAT (Halaman aktif saat ini)
        if (tabHistory != null) {
            tabHistory.setOnClickListener(v -> {
                // Biarkan kosong karena user sudah berada di halaman Riwayat Anda
            });
        }

        // ================= LOGIK FITUR PENCARIAN CATATAN =================

        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> toggleSearchMode());
        }

        if (etSearchInput != null) {
            etSearchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterNotes(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Memuat data awal saat halaman pertama kali dibuka
        loadNotesData();
    }

    // Fungsi pengalih (toggle) antara Header Judul dan Kotak Input Pencarian
    private void toggleSearchMode() {
        if (etSearchInput == null || tvNotesHeader == null || btnSearch == null) return;

        if (!isSearchActive) {
            isSearchActive = true;
            tvNotesHeader.setVisibility(View.GONE);
            etSearchInput.setVisibility(View.VISIBLE);
            etSearchInput.setText("");
            etSearchInput.requestFocus();
            btnSearch.setImageResource(R.drawable.ic_close); // Ganti ikon ke tombol Silang (X)

            // Membuka Keyboard Virtual secara otomatis
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearchInput, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            isSearchActive = false;
            tvNotesHeader.setVisibility(View.VISIBLE);
            etSearchInput.setVisibility(View.GONE);
            etSearchInput.setText("");
            btnSearch.setImageResource(R.drawable.ic_search); // Kembalikan ikon kaca pembesar

            // Menyembunyikan Keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearchInput.getWindowToken(), 0);
            }

            // Kembalikan isi daftar ke versi lengkap
            loadNotesData();
        }
    }

    // Mengambil data catatan dari SQLite Database Lokal
    private void loadNotesData() {
        if (databaseHelper == null) return;

        listNotes.clear();
        listNotes.addAll(databaseHelper.getAllHistory());

        filteredList.clear();
        filteredList.addAll(listNotes);

        updateUserInterface();
    }

    // Fungsi pencocokan teks pencarian ke daftar judul/isi catatan
    private void filterNotes(String query) {
        filteredList.clear();
        if (query.trim().isEmpty()) {
            filteredList.addAll(listNotes);
        } else {
            for (SavedNoteModel note : listNotes) {
                if (note.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        note.getExplanation().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(note);
                }
            }
        }
        updateUserInterface();
    }

    // Mengubah visibilitas RecyclerView dan Empty State secara dinamis
    private void updateUserInterface() {
        if (rvNotesList == null || tvEmptyState == null) return;

        if (filteredList.isEmpty()) {
            rvNotesList.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);

            if (isSearchActive) {
                tvEmptyState.setText("Catatan tidak ditemukan");
            } else {
                tvEmptyState.setText("Belum ada catatan");
            }
        } else {
            rvNotesList.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);

            if (adapter == null) {
                adapter = new SavedNotesAdapter(this, filteredList);
                rvNotesList.setAdapter(adapter);
            } else {
                adapter.updateList(filteredList);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Selalu segarkan data ketika kembali ke activity ini dari screen lain
        if (!isSearchActive) {
            loadNotesData();
        }
    }
}