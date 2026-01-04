package com.infanji.ai_based_meal_recommendation_app.account_creation;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.infanji.ai_based_meal_recommendation_app.MainActivity;
import com.infanji.ai_based_meal_recommendation_app.R;
import com.infanji.ai_based_meal_recommendation_app.utilities.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    ImageView googleSignInButton, mobileSignInButton;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressbar);

        preferenceManager = new PreferenceManager(this);

        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(getString(R.string.default_web_client_id))
                                .setFilterByAuthorizedAccounts(false)
                                .build())
                .build();

        googleSignInButton = findViewById(R.id.btn_google);
        googleSignInButton.setOnClickListener(v -> {
            googleSignInButton.setVisibility(View.GONE);
            mobileSignInButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            signInWithGoogle();
        });

        mobileSignInButton = findViewById(R.id.btn_mobile);
        mobileSignInButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MobileLoginActivity.class);
            startActivity(intent);
        });

        if (preferenceManager.isLoggedIn()) {
            if (preferenceManager.isNotCompletedLogin()) {
                navigateToSignUp();
            } else {
                navigateToHome();
            }
        }

    }

    private void signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    try {
                        startIntentSenderForResult(result.getPendingIntent().getIntentSender(), 100, null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException e) {
                        throw new RuntimeException(e);
                    }
                })
                .addOnFailureListener(this, e -> {
                    googleSignInButton.setVisibility(View.VISIBLE);
                    mobileSignInButton.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Google Sign-in failed", e);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    mAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        saveUserToFirestore(user);
                                    }
                                } else {
                                    googleSignInButton.setVisibility(View.VISIBLE);
                                    mobileSignInButton.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    Log.e(TAG, "Authentication Failed: ", task.getException());
                                }
                            });
                }
            } catch (Exception e) {
                googleSignInButton.setVisibility(View.VISIBLE);
                mobileSignInButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Google Sign-in error", e);
            }
        }
    }

    private void saveUserToFirestore(FirebaseUser user) {
        String uid = user.getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("uid", uid);
                userData.put("name", user.getDisplayName());
                userData.put("email", user.getEmail());
                userData.put("profilePic", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                userData.put("createdAt", FieldValue.serverTimestamp());

                // Save to local preferences in both cases (exist or not)
                preferenceManager.setUid(uid);
                preferenceManager.setUsername(user.getDisplayName());
                preferenceManager.setEmail(user.getEmail());
                preferenceManager.setLoggedIn(true); // Mark as logged in

                if (task.getResult().exists()) {
                    // User exists: check if signup was completed or not
                    boolean isCompleted = task.getResult().getBoolean("isProfileCompleted") != null &&
                            task.getResult().getBoolean("isProfileCompleted");

                    if (!isCompleted) {
                        preferenceManager.setNotCompletedLogin(true);
                        Toast.makeText(this, "Continue profile setup", Toast.LENGTH_SHORT).show();
                        navigateToSignUp();
                    } else {
                        preferenceManager.setNotCompletedLogin(false);
                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                        finish();
                    }
                } else {
                    // New user: set profile as not completed in Firestore
                    userData.put("isProfileCompleted", false);

                    userRef.set(userData)
                            .addOnSuccessListener(aVoid -> {
                                preferenceManager.setNotCompletedLogin(true);
                                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                                navigateToSignUp();
                            })
                            .addOnFailureListener(e -> {
                                googleSignInButton.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                mobileSignInButton.setVisibility(View.VISIBLE);
                                Log.e(TAG, "Error storing user in Firestore", e);
                                Toast.makeText(this, "Failed to save user", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                googleSignInButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                mobileSignInButton.setVisibility(View.VISIBLE);
                Log.e(TAG, "Failed to retrieve user data", task.getException());
            }
        });
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}