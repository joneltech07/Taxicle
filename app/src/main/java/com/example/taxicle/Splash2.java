package com.example.taxicle;

import static com.example.taxicle.viewsetting.ViewSetting.makeViewFullScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.taxicle.databinding.ActivitySplash1Binding;
import com.example.taxicle.databinding.ActivitySplash2Binding;

public class Splash2 extends AppCompatActivity {
    private ActivitySplash2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplash2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View view = getWindow().getDecorView();
        makeViewFullScreen(view);

        binding.btnNext.setOnClickListener(v -> {
            startActivity(new Intent(this, Splash3.class));
            finish();
        });
        binding.tvSkip.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
        });
    }
}