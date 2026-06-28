package com.example.projectscanotes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SavedNotesAdapter extends RecyclerView.Adapter<SavedNotesAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<SavedNoteModel> listNotes;

    public SavedNotesAdapter(Context context, ArrayList<SavedNoteModel> listNotes) {
        this.context = context;
        this.listNotes = listNotes;
    }

    // 👈 METODE YANG HILANG: Fungsi ini wajib ada untuk memperbarui daftar hasil pencarian secara dinamis!
    public void updateList(ArrayList<SavedNoteModel> newList) {
        this.listNotes = newList;
        notifyDataSetChanged(); // Memberitahu RecyclerView untuk menggambar ulang daftar dengan data baru
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saved_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int viewType) {
        SavedNoteModel item = listNotes.get(viewType);
        holder.tvCardTitle.setText(item.getTitle());
        holder.tvCardTimestamp.setText(item.getTimestamp());

        // Aksi ketika kartu catatan diklik: membuka kembali detail analisis AI
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExplainAIActivity.class);
            intent.putExtra("is_from_history", true);
            intent.putExtra("history_title", item.getTitle());
            intent.putExtra("history_explanation", item.getExplanation());
            intent.putExtra("history_examples", item.getExamples());
            intent.putExtra("history_summary", item.getSummary());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listNotes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardTitle, tvCardTimestamp;
        ImageView ivArrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardTitle = itemView.findViewById(R.id.tvCardTitle);
            tvCardTimestamp = itemView.findViewById(R.id.tvCardTimestamp);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}