package com.example.aalemni;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifOptions;
import com.bumptech.glide.request.RequestOptions;

public class SplashScreen extends AppCompatActivity {

    private TextView txt1, txt2;
    private ImageView wlcmimg;

    private Animation animation = new AlphaAnimation(0.0f, 1.0f);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        txt1 = findViewById(R.id.wlcm1);
        txt2 = findViewById(R.id.wlcm2);
        wlcmimg = findViewById(R.id.gifImageView);
        animation.setDuration(1000);
        txt1.startAnimation(animation);
        txt2.startAnimation(animation);
        Glide.with(this).asGif().load(R.drawable.splash)
                .apply(new RequestOptions()
                        .frame(0) // Set the first frame of the animation
                        .disallowHardwareConfig()
                        .set(GifOptions.DISABLE_ANIMATION, false))
                .into(wlcmimg);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000); // 3 seconds delay
    }
}