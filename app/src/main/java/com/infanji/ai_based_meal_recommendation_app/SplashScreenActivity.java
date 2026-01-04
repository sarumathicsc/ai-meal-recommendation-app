package com.infanji.ai_based_meal_recommendation_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.infanji.ai_based_meal_recommendation_app.account_creation.LoginActivity;
import com.infanji.ai_based_meal_recommendation_app.account_creation.SignUpActivity;
import com.infanji.ai_based_meal_recommendation_app.utilities.PreferenceManager;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends BaseActivity {

    private static final int SPLASH_SCREEN_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashScreenActivity.this, GetStartedActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, SPLASH_SCREEN_TIME_OUT);

    }
}