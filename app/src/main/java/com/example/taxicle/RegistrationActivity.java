package com.example.taxicle;

import static android.content.ContentValues.TAG;
import static com.example.taxicle.viewsetting.ViewSetting.makeViewFullScreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.taxicle.constructors.Passenger;
import com.example.taxicle.data_access_object.DAO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationActivity extends AppCompatActivity {

    EditText editTextName, editTextEmail, editTextPassword, editTextConfirm;
    Button btnSignup;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        View view = getWindow().getDecorView();
        makeViewFullScreen(view);

        findViewById(R.id.tv_signin).setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        mAuth = FirebaseAuth.getInstance();
        editTextName = findViewById(R.id.et_name);
        editTextEmail = findViewById(R.id.et_email);
        editTextPassword = findViewById(R.id.et_password);
        editTextConfirm = findViewById(R.id.et_confirm_pass);

        progressBar = findViewById(R.id.progress_bar);

        btnSignup = findViewById(R.id.btn_signup);

        btnSignup.setOnClickListener(v -> {
            String name, email, password, comf_pass;
            name = String.valueOf(editTextName.getText());
            email = String.valueOf(editTextEmail.getText());
            password = String.valueOf(editTextPassword.getText());
            comf_pass = String.valueOf(editTextConfirm.getText());

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(comf_pass)) {
                Toast.makeText(this, "Re-enter password", Toast.LENGTH_SHORT).show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    FirebaseUser user = mAuth.getCurrentUser();
                    Passenger passenger = new Passenger(name, user.getUid());
                    DAO dao = new DAO();
                    dao.add(passenger).addOnCompleteListener(task1 -> {
                        progressBar.setVisibility(View.GONE);
                        if (task1.isSuccessful()) {
                            Toast.makeText(RegistrationActivity.this, "Hello!, "+ passenger.getName(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            });
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