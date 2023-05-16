package com.example.aalemni;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;

public class MyDialoge extends Dialog {

    public MyDialoge(@NonNull Context context) {
        super(context);
        setContentView(R.layout.loading); // Set the layout for your dialog
        setCancelable(false);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do additional setup here
    }
    public void showFor(long durationInMillis) {
        show(); // Show the dialog
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss(); // Dismiss the dialog after the specified duration
            }
        }, durationInMillis);
    }
}
