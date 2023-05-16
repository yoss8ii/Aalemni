package com.example.aalemni;

import static com.google.android.material.color.utilities.MaterialDynamicColors.error;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//L'activité de base qui contient l'ajout et le traitement de l'image
public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageView imageView;
    Button addbtn, scannbtn;
    TextView resulttext;

    FloatingActionButton speakbtn;

    String imageUrl;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2001;

    String cameraPermission[];
    String storagePermission[];

    Uri image_uri, result_image_uri;

    FirebaseFirestore firestore;
    CollectionReference imagesCollectionRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView= findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);
        navigation();

        //Initialisation de firestore et cloud storage
        FirebaseApp.initializeApp(this);
        firestore = FirebaseFirestore.getInstance();
        imagesCollectionRef = firestore.collection("images");

        //Initialisation des éléments graphiques
        imageView = findViewById(R.id.img);
        addbtn = findViewById(R.id.pickbtn);
        scannbtn = findViewById(R.id.recbtn);
        resulttext = findViewById(R.id.rectext);
        speakbtn = findViewById(R.id.tts);
        MyDialoge myDialoge = new MyDialoge(this);

        //Test si on a choisi une image depuis l'historique
        Intent imageintent = getIntent();
        imageUrl = imageintent.getStringExtra("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(imageView);
        }

        //Demande des permission
        cameraPermission = new String[] {android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //Les actions sur les boutons
        addbtn.setOnClickListener(view -> showImageImportDialog());
        scannbtn.setOnClickListener(v -> {
            if (imageView.getDrawable() == null) {
                Toast.makeText(MainActivity.this,
                        "Vous devez d'abord importé une image !",
                        Toast.LENGTH_SHORT).show();
            } else {
                myDialoge.showFor(2000);
                detectTxt();
            }
        });
        speakbtn.setOnClickListener(v -> {
            String getresulttext = resulttext.getText().toString();
            Intent intent = new Intent(getApplicationContext(), SpeakActivity.class);
            intent.putExtra("result", getresulttext);
            startActivity(intent);
            overridePendingTransition(R.transition.slide_in, R.transition.slide_out);
        });
    }

    //Menu de source de l'image
    private void showImageImportDialog() {
        String[] items = {"Camera", "Gallerie"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Choisir le source de l'image");
        dialog.setItems(items, (dialog1, which) -> {
            if (which == 0) {
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickCamera();
                }
            }
            if (which == 1) {
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickGallery();
                }
            }
        });
        dialog.create().show();
    }

    //Ouverture de la gallerie
    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    //Ouverture du caméra et enregistrement de l'image dans la gellerie
    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPick"); //title of the picture
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image To Text"); //title of the picture
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    //Demande des permissions
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }
    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }
    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    //Résulats des permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //Résultat du choix de l'image et sa redimensionnement
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAllowRotation(true)
                        .start(this);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAllowRotation(true)
                        .start(this);
            }
        }

        //Redimensionnement de l'image choisi
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                result_image_uri = result.getUri();
                imageView.setImageURI(result_image_uri);
                uploadImageToFirebase();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Detection du text depuis l'image
    private void detectTxt() {
        Drawable drawable = imageView.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
        detector.detectInImage(image).
                addOnSuccessListener(firebaseVisionText -> processTxt(firebaseVisionText))
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this,
                            "Erreur de detection de text , pas de text dans l'image ",
                            Toast.LENGTH_SHORT).show();
                });

    }

    //Traitement de text
    private void processTxt(FirebaseVisionText text) {
        List<FirebaseVisionText.Block> blocks = text.getBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(MainActivity.this, "Erreur de detection de text , pas de text dans l'image ", Toast.LENGTH_LONG).show();
            return;
        }
        for (FirebaseVisionText.Block block : text.getBlocks()) {
            String txt = block.getText();
            resulttext.setText(txt);
        }
    }

    //Sauvegarde de l'image dans cloud storage
    private void uploadImageToFirebase() {
        String fileName = "image_" + System.currentTimeMillis() + ".jpg";
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(fileName);
        storageReference.putFile(result_image_uri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveImageDataToFirestore(imageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                });
    }

    //Sauvegarde des données de l'image dans firestore
    private void saveImageDataToFirestore(String imageUrl) {
        String documentId = firestore.collection("images").document().getId();
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("imageUrl", imageUrl);
        imageData.put("uploadTime", new Date());
        imagesCollectionRef.document(documentId)
                .set(imageData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this,
                            "Image enregistré avec succée",
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this,
                            "Erreur d'enregitrement de l'image :"+e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }


    //Configuration du bar de navigation
    public void navigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                return true;
            } else if (itemId == R.id.bottom_history) {
                startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                overridePendingTransition(R.transition.slide_in, R.transition.slide_out);
                finish();
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