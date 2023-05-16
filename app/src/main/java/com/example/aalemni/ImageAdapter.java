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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.List;


//La classe qui insére les données de l'image dans le recyclerview
//pour l'activité historique
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private FirebaseFirestore firestore;
    private static List<ImageHandler> imageList;
    private static Context context;

    public ImageAdapter(List<ImageHandler> imageList, Context context) {
        this.imageList = imageList;
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageHandler photo = imageList.get(position);
        Glide.with(context).load(photo.getImageUrl()).into(holder.photoImageView);
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String formattedTime = dateFormat.format(photo.getUploadTime());
        holder.uploadTimeTextView.setText(formattedTime);
        holder.itemView.setOnClickListener(v -> {
            int clickedPosition = holder.getAdapterPosition();
            if (clickedPosition != RecyclerView.NO_POSITION) {
                ImageHandler clickedPhoto = imageList.get(clickedPosition);
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("imageUrl", clickedPhoto.getImageUrl());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView photoImageView;
        public TextView uploadTimeTextView;
        public FloatingActionButton deletebtn;

        public ViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            uploadTimeTextView = itemView.findViewById(R.id.uploadTimeTextView);
            deletebtn = itemView.findViewById(R.id.delete);

            deletebtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ImageHandler image = imageList.get(position);
                    deletePhotoFromDatabase(image);
                    imageList.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }
        private void deletePhotoFromDatabase(ImageHandler img) {
            firestore.collection("images")
                    .document(img.getImageID())
                    .delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference imageRef = storage.getReferenceFromUrl(img.getImageUrl());
                            imageRef.delete().addOnSuccessListener(aVoid ->
                                    Toast.makeText(context, "Image supprimée avec succès",
                                    Toast.LENGTH_SHORT).show()).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Erreur de suppression de l'image",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(context, "Erreur de suppression de l'image",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}