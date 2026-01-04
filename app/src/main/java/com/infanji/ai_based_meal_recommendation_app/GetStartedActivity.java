package com.infanji.ai_based_meal_recommendation_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.infanji.ai_based_meal_recommendation_app.account_creation.LoginActivity;
import com.infanji.ai_based_meal_recommendation_app.utilities.SaveState;

public class GetStartedActivity extends BaseActivity {

    private SaveState savestate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView get_started_btn = findViewById(R.id.btn_get_started);

        savestate = new SaveState(this, "ob");

        if (savestate.getState() == 1) {
            Intent i = new Intent(GetStartedActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        get_started_btn.setOnClickListener(v -> GetStartedActivity.this.get_started());

    }

    private void get_started() {
        savestate.setState(1);
        Intent i = new Intent(GetStartedActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }


}