package com.example.taxicle;

import static com.example.taxicle.viewsetting.ViewSetting.makeViewFullScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.taxicle.databinding.ActivitySplash1Binding;
import com.example.taxicle.databinding.ActivitySplash3Binding;

public class Splash3 extends AppCompatActivity {
    private ActivitySplash3Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplash3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View view = getWindow().getDecorView();
        makeViewFullScreen(view);

        binding.getStarted.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
        });
    }
}