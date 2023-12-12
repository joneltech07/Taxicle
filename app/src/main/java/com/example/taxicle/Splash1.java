package com.example.taxicle;

import static com.example.taxicle.viewsetting.ViewSetting.makeViewFullScreen;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;

import com.example.taxicle.databinding.ActivitySplash1Binding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Splash1 extends AppCompatActivity {

    private ActivitySplash1Binding binding;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        binding = ActivitySplash1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View view = getWindow().getDecorView();
        makeViewFullScreen(view);

        binding.btnNext.setOnClickListener(v -> {
            startActivity(new Intent(this, Splash2.class));
            finish();
        });
        binding.tvSkip.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}