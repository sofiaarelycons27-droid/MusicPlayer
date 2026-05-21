package com.musicplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.R;
import com.musicplayer.activities.PlayerActivity;
import com.musicplayer.database.DatabaseManager;
import com.musicplayer.models.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private List<Song> songList;
    private Context context;
    private DatabaseManager dbManager;
    private int userId;
    private OnSongChangedListener listener;

    public interface OnSongChangedListener {
        void onSongChanged();
    }

    public SongAdapter(Context context, List<Song> songList, int userId, OnSongChangedListener listener) {
        this.context = context;
        this.songList = songList;
        this.userId = userId;
        this.listener = listener;
        this.dbManager = new DatabaseManager(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songList.get(position);

        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        holder.tvDuration.setText(song.getDuration());

        if (song.isFavorite()) {
            holder.btnFavorite.setImageResource(R.drawable.ic_heart_filled);
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_heart_outline);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("song_id", song.getId());
            intent.putExtra("user_id", userId);
            context.startActivity(intent);
        });

        holder.btnFavorite.setOnClickListener(v -> {
            dbManager.open();
            song.setFavorite(!song.isFavorite());
            dbManager.updateSong(song);
            dbManager.close();

            if (listener != null) {
                listener.onSongChanged();
            }

            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist, tvDuration;
        ImageButton btnFavorite;
        ImageView ivArtwork;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            ivArtwork = itemView.findViewById(R.id.ivArtwork);
        }
    }

    public void updateList(List<Song> newList) {
        this.songList = newList;
        notifyDataSetChanged();
    }
}