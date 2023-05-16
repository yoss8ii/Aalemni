package com.example.aalemni;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//C'est la classe qui va contenir touts les images
//utilisés avec leur dates d'utilisation
public class HistoryActivity extends AppCompatActivity {

    private RecyclerView photoRecyclerView;
    private ImageAdapter imageAdapter;
    private List<ImageHandler> imageList;

    private FirebaseFirestore firestore;
    private CollectionReference imagesCollectionRef;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        bottomNavigationView= findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_history);
        navigation();

        //Connexion à firestore database
        firestore = FirebaseFirestore.getInstance();
        imagesCollectionRef = firestore.collection("images");

        //Initialisation de recyclerview où les images seront affichés
        photoRecyclerView = findViewById(R.id.recyclerview);
        imageList = new ArrayList<ImageHandler>();
        imageAdapter = new ImageAdapter(imageList, this);
        photoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        photoRecyclerView.setAdapter(imageAdapter);

        //Appel à la méthode qui va téléchargé la liste des images
        retrievePhotoData();
    }


    private void retrievePhotoData() {
        imagesCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                imageList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String imageUrl = document.getString("imageUrl");
                    Date uploadTime = document.getDate("uploadTime");
                    String imgId = document.getId();
                    if (imageUrl != null && uploadTime != null) {
                        ImageHandler img = new ImageHandler(imageUrl, uploadTime, imgId);
                        imageList.add(img);
                    }
                }
                imageAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(HistoryActivity.this,
                        "Erreur d'affichage des images, peut être il y a pas d'image dans la base",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    //Configuration du bar de navigation
    public void navigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.transition.slide_in, R.transition.slide_out);
                finish();
                return true;
            } else if (itemId == R.id.bottom_history) {
                return true;
            } else if (itemId == R.id.bottom_speaker) {
                startActivity(new Intent(getApplicationContext(), SpeakActivity.class));
                overridePendingTransition(R.transition.slide_in, R.transition.slide_out);
                finish();
                return true;
            }
            return false;
        });
    }
}