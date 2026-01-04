package com.infanji.ai_based_meal_recommendation_app.account_creation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;
import com.infanji.ai_based_meal_recommendation_app.BaseActivity;
import com.infanji.ai_based_meal_recommendation_app.MainActivity;
import com.infanji.ai_based_meal_recommendation_app.R;
import com.infanji.ai_based_meal_recommendation_app.utilities.PreferenceManager;

public class MobileSignUpActivity extends BaseActivity {

    private EditText inputNo, inputPassword;
    private CountryCodePicker ccp;
    private ImageView btnContinue;
    private ProgressBar continueBar;
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;

    private static final String COLLECTION_USERS = "users";
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_PROFILE_COMPLETED = "isProfileCompleted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_sign_up);

        preferenceManager = new PreferenceManager(this);
        db = FirebaseFirestore.getInstance();

        ccp = findViewById(R.id.ccp);
        inputNo = findViewById(R.id.input_no);
        inputPassword = findViewById(R.id.input_password);
        btnContinue = findViewById(R.id.btn_continue);
        continueBar = findViewById(R.id.continue_bar);

        ccp.registerCarrierNumberEditText(inputNo);

        findViewById(R.id.back).setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            String phoneNumber = ccp.getFullNumberWithPlus();
            String rawPassword = inputPassword.getText().toString().trim();

            if (!ccp.isValidFullNumber()) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            } else if (rawPassword.isEmpty()) {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            } else {
                login(phoneNumber, rawPassword);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (preferenceManager.isLoggedIn()) {
            if (preferenceManager.isNotCompletedLogin()) {
                navigateToSignUp();
            } else {
                navigateToHome();
            }
        }
    }

    private void login(String phoneNumber, String rawPassword) {
        btnContinue.setVisibility(View.INVISIBLE);
        continueBar.setVisibility(View.VISIBLE);

        String password = hashPassword(rawPassword);

        db.collection(COLLECTION_USERS).document(phoneNumber).get()
                .addOnCompleteListener(task -> {
                    showContinueButton(); // Re-enable button regardless of result

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document != null && document.exists()) {
                            String storedPassword = document.getString(FIELD_PASSWORD);

                            if (storedPassword != null && storedPassword.equals(password)) {
                                // ✅ Get name (username) from Firestore
                                String username = document.getString("name");
                                String gender = document.getString("gender");
                                preferenceManager.setGender(gender);
                                if (username != null && !username.isEmpty()) {
                                    preferenceManager.setUsername(username);
                                }

                                // ✅ Get isProfileCompleted status
                                Boolean isProfileCompleted = document.getBoolean(FIELD_PROFILE_COMPLETED);
                                boolean isCompleted = isProfileCompleted != null && isProfileCompleted;

                                // ✅ Store login state
                                preferenceManager.setLoggedIn(true);
                                preferenceManager.setUid(phoneNumber);
                                preferenceManager.setNotCompletedLogin(!isCompleted);

                                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

                                // ✅ Navigate based on profile status
                                if (isCompleted) {
                                    navigateToHome();
                                    finish();
                                } else {
                                    navigateToSignUp();
                                    finish();
                                }

                            } else {
                                inputPassword.setError("Incorrect password");
                            }
                        } else {
                            inputNo.setError("User not found");
                        }
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showContinueButton() {
        btnContinue.setVisibility(View.VISIBLE);
        continueBar.setVisibility(View.GONE);
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

    private void navigateToHome() {
        Intent intent = new Intent(MobileSignUpActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity(); // 👈 Completely finishes this and any other previous activities
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(MobileSignUpActivity.this, SignUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity(); // 👈 Same here
    }


}