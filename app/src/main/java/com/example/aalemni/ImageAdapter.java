package com.example.aalemni;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.DateFormat;
import java.util.List;


//La classe qui insére les données de l'image dans le recyclerview
//pour l'activité historique
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private List<ImageHandler> photoList;
    private Context context;

    public ImageAdapter(List<ImageHandler> photoList, Context context) {
        this.photoList = photoList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageHandler photo = photoList.get(position);
        Glide.with(context).load(photo.getImageUrl()).into(holder.photoImageView);
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String formattedTime = dateFormat.format(photo.getUploadTime());
        holder.uploadTimeTextView.setText(formattedTime);
        holder.itemView.setOnClickListener(v -> {
            int clickedPosition = holder.getAdapterPosition();
            if (clickedPosition != RecyclerView.NO_POSITION) {
                ImageHandler clickedPhoto = photoList.get(clickedPosition);
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("imageUrl", clickedPhoto.getImageUrl());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView photoImageView;
        public TextView uploadTimeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            uploadTimeTextView = itemView.findViewById(R.id.uploadTimeTextView);
        }
    }
}