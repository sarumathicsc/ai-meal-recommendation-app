package com.infanji.ai_based_meal_recommendation_app.account_creation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import com.hbb20.CountryCodePicker;
import com.infanji.ai_based_meal_recommendation_app.BaseActivity;
import com.infanji.ai_based_meal_recommendation_app.MainActivity;
import com.infanji.ai_based_meal_recommendation_app.R;
import com.infanji.ai_based_meal_recommendation_app.utilities.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

public class MobileLoginActivity extends BaseActivity {

    private EditText phone, name, password, confirm_password;
    private CountryCodePicker code;
    private ImageView btn_create;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_login);

        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        btn_create = findViewById(R.id.btn_create);
        progressBar = findViewById(R.id.create_bar);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            finish();
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView sign_up = findViewById(R.id.sign_up);
        sign_up.setOnClickListener(v -> {
            Intent intent = new Intent(MobileLoginActivity.this, MobileSignUpActivity.class);
            startActivity(intent);
        });

        phone = findViewById(R.id.input_no);
        name = findViewById(R.id.input_name);
        password = findViewById(R.id.input_password);
        confirm_password = findViewById(R.id.input_confirm_password);

        code = findViewById(R.id.ccp);
        code.registerCarrierNumberEditText(phone);

        btn_create.setOnClickListener(v -> {
            if (phone.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
            } else if (name.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            } else if (password.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            } else if (confirm_password.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter confirm password", Toast.LENGTH_SHORT).show();
            } else if (password.getText().toString().length() < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            } else if (!password.getText().toString().equals(confirm_password.getText().toString())) {
                Toast.makeText(this, "Password and confirm password must be same", Toast.LENGTH_SHORT).show();
            } else {
                create();
            }
        });

        if (preferenceManager.isLoggedIn()) {
            if (preferenceManager.isNotCompletedLogin()) {
                navigateToSignUp();
            } else {
                navigateToHome();
            }
        }

    }

    private void create() {
        String phoneNumber = code.getFullNumberWithPlus();
        String userName = name.getText().toString().trim();
        String rawPassword = password.getText().toString().trim();
        String userPassword = hashPassword(rawPassword);

        btn_create.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        if (!userName.isEmpty() && !userPassword.isEmpty() && code.isValidFullNumber()) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", userName);
            userData.put("phone", phoneNumber);
            userData.put("password", userPassword);
            userData.put("createdAt", FieldValue.serverTimestamp());
            userData.put("uid", phoneNumber);
            userData.put("isProfileCompleted", false);

            db.collection("users").document(phoneNumber)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        preferenceManager.setUsername(userName);
                        preferenceManager.setLoggedIn(true);
                        preferenceManager.setUid(phoneNumber);
                        preferenceManager.setNotCompletedLogin(true);

                        Toast.makeText(this, "User created successfully!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MobileLoginActivity.this, SignUpActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btn_create.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    });

        } else {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
            btn_create.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}