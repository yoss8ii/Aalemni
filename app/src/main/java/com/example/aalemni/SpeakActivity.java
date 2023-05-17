package com.example.aalemni;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;


//L'activité responsable au texte pour parler
public class SpeakActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {



    TextView result, voice;
    FloatingActionButton playpausebtn, micbtn;
    Button comparebtn;
    TextToSpeech tts;
    String getresulttext;
    int currentPosition;
    boolean isDone, notempty = false;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;


    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speak);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_speaker);
        navigation();

        //Initialisation des éléments graphiques
        result = findViewById(R.id.newresulttext);
        voice = findViewById(R.id.voiceresult);
        playpausebtn = findViewById(R.id.play_pause);
        micbtn = findViewById(R.id.speak);
        comparebtn = findViewById(R.id.compare);
        playpausebtn.setTag("START");
        playpausebtn.setEnabled(false);

        //Avoir le text traité de l'activité principale
        Intent intent = getIntent();
        getresulttext = intent.getStringExtra("result");
        result.setText(getresulttext);

        tts = new TextToSpeech(this, this);
        tts.setSpeechRate(0.3f);
        if(!result.getText().toString().isEmpty()){
            playpausebtn.setEnabled(true);
            playpausebtn.setOnClickListener(v -> {
                if (result != null && TextUtils.isEmpty(result.getText())) {
                    Toast.makeText(SpeakActivity.this, "Il y a pas de texte à lire", Toast.LENGTH_SHORT).show();
                } else {
                    if (playpausebtn.getTag().equals("START")) {
                        playpausebtn.setTag("PAUSE");
                        playpausebtn.setImageResource(android.R.drawable.ic_media_pause);
                        tts.speak(getresulttext.substring(currentPosition), TextToSpeech.QUEUE_FLUSH, null, "UNIQUE_UTTERANCE_ID");
                    } else if (playpausebtn.getTag().equals("PAUSE")) {
                        tts.stop();
                        playpausebtn.setTag("START");
                        playpausebtn.setImageResource(android.R.drawable.ic_media_play);
                    }
                }
            });
        }

        micbtn.setOnClickListener(view -> getVoiceText());
        comparebtn.setOnClickListener(v -> {
            if(notempty){
                if(isAlmostIdentical(result.getText().toString(), voice.getText().toString())){
                    DialogWinn();
                } else {
                    DialogLose();
                }
            }else {
                Toast.makeText(SpeakActivity.this, "Il y a pas de text à lire", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Les méthodes du tts
    @Override
    public void onInit(int status) {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Log.i("TTS", "utterance started");
            }
            @Override
            public void onDone(String utteranceId) {
                Log.i("TTS", "utterance done");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result.setText(getresulttext);
                        Toast.makeText(SpeakActivity.this, "Lecture terminé", Toast.LENGTH_SHORT).show();
                        playpausebtn.setImageResource(android.R.drawable.ic_media_play);
                    }
                });
            }
            @Override
            public void onError(String utteranceId) {
                Log.i("TTS", "utterance error");
            }
            @Override
            public void onRangeStart(String utteranceId,
                                     final int start,
                                     final int end,
                                     int frame) {
                Log.i("TTS", "onRangeStart() ... utteranceId: " + utteranceId + ", start: " + start
                        + ", end: " + end + ", frame: " + frame);
                currentPosition = start;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /*Spannable textWithHighlights = new SpannableString(getresulttext);
                        textWithHighlights.setSpan(new ForegroundColorSpan(Color.RED),currentPosition , end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        result.setText(textWithHighlights);*/
                        isDone = true;
                    }
                });
            }
        });
    }
    @Override
    public void onDestroy() {
        //don't forget to shutdown tts
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }


    //Detection de text par la reconnaisance vocale
    public void getVoiceText(){
        Intent intent
                = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Litre attentivement le text");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), " " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                voice.setText(
                        Objects.requireNonNull(result).get(0));
                notempty = true;
            }
        }
    }

    //Comparaison entre les deux textes
    public static boolean isAlmostIdentical(String s1, String s2) {
        int threshold = (int) (s1.length() * 0.2);
        int[][] distance = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            distance[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;

                distance[i][j] = Math.min(Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1),
                        distance[i - 1][j - 1] + cost);
            }
        }

        return distance[s1.length()][s2.length()] <= threshold;
    }

    public void DialogWinn() {
        View winn = LayoutInflater.from(this).inflate(R.layout.dialog_win,null);
        AlertDialog.Builder alertok = new AlertDialog.Builder(this);
        alertok.setView(winn);
        final AlertDialog dialog = alertok.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        Button okibtn = (Button) winn.findViewById(R.id.oki);
        okibtn.setOnClickListener(v -> dialog.dismiss());
    }
    public void DialogLose() {
        View lose = LayoutInflater.from(this).inflate(R.layout.dialog_lose,null);
        AlertDialog.Builder alertok = new AlertDialog.Builder(this);
        alertok.setView(lose);
        final AlertDialog dialog = alertok.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        Button okibtn = (Button) lose.findViewById(R.id.oki);
        okibtn.setOnClickListener(v -> dialog.dismiss());
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
                startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                overridePendingTransition(R.transition.slide_in, R.transition.slide_out);
                finish();
                return true;
            } else if (itemId == R.id.bottom_speaker) {
                return true;
            }
            return false;
        });
    }
}