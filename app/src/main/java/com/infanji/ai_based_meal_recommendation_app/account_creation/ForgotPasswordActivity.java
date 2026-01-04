package com.infanji.ai_based_meal_recommendation_app.account_creation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;

import com.infanji.ai_based_meal_recommendation_app.BaseActivity;
import com.infanji.ai_based_meal_recommendation_app.R;

public class ForgotPasswordActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());


    }
}