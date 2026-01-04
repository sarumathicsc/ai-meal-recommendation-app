package com.infanji.ai_based_meal_recommendation_app.account_creation;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.infanji.ai_based_meal_recommendation_app.BaseActivity;
import com.infanji.ai_based_meal_recommendation_app.MainActivity;
import com.infanji.ai_based_meal_recommendation_app.R;
import com.infanji.ai_based_meal_recommendation_app.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUpActivity extends BaseActivity {

    private PreferenceManager preferenceManager;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Spinner genderSpinner, dietarySpinner, fitnessGoalSpinner;

    private EditText edit_age;
    private String age, gender, dietary, fitnessGoal, allergies, cuisine;

    private FlexboxLayout allergiesLayout, cuisineLayout;
    private ProgressBar progressBar;
    private ImageView btn_continue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);

        allergiesLayout = findViewById(R.id.allergy_group);
        cuisineLayout = findViewById(R.id.cuisine_group);
        genderSpinner = findViewById(R.id.input_gender);
        dietarySpinner = findViewById(R.id.ditery_type);
        fitnessGoalSpinner = findViewById(R.id.health);

        edit_age = findViewById(R.id.input_age);

        progressBar = findViewById(R.id.done_bar);


        String[] genderOptions = getResources().getStringArray(R.array.gender_options);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                genderOptions
        ) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // Disable "Select Gender"
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                if (position == 0) {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.hint_text));
                } else {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                }

                return view;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;

                if (position == 0) {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.hint_text));
                } else {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.text));
                }

                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        genderSpinner.setSelection(0);

        dietarySpinner = findViewById(R.id.ditery_type);
        String[] dietaryOptions = getResources().getStringArray(R.array.dietary_options);

        ArrayAdapter<String> dietaryAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                dietaryOptions
        ) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.hint_text));
                } else {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                }
                return view;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.hint_text));
                } else {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.text));
                }
                return view;
            }
        };

        dietaryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietarySpinner.setAdapter(dietaryAdapter);
        dietarySpinner.setSelection(0);

        fitnessGoalSpinner = findViewById(R.id.health);
        String[] fitnessGoals = getResources().getStringArray(R.array.fitness_goal_options);

        ArrayAdapter<String> goalAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                fitnessGoals
        ) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.hint_text));
                } else {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                }
                return view;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.hint_text));
                } else {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.text));
                }
                return view;
            }
        };

        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fitnessGoalSpinner.setAdapter(goalAdapter);
        fitnessGoalSpinner.setSelection(0);

        allergies = getSelectedFlexItems(allergiesLayout);
        cuisine = getSelectedFlexItems(cuisineLayout);
        age = edit_age.getText().toString();
        gender = genderSpinner.getSelectedItem().toString();
        dietary = dietarySpinner.getSelectedItem().toString();
        fitnessGoal = fitnessGoalSpinner.getSelectedItem().toString();

        btn_continue = findViewById(R.id.btn_done);
        btn_continue.setOnClickListener(v -> completeProfileSetup());

    }

    private String getSelectedFlexItems(FlexboxLayout layout) {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof CheckBox && ((CheckBox) child).isChecked()) {
                selected.add(((CheckBox) child).getText().toString());
            }
        }
        return TextUtils.join(", ", selected);
    }


    private void completeProfileSetup() {

        btn_continue.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        // Move these inside this method to get the latest user input
        age = edit_age.getText().toString().trim();
        gender = genderSpinner.getSelectedItem().toString();
        dietary = dietarySpinner.getSelectedItem().toString();
        fitnessGoal = fitnessGoalSpinner.getSelectedItem().toString();
        allergies = getSelectedFlexItems(allergiesLayout);
        cuisine = getSelectedFlexItems(cuisineLayout);

        String uid = preferenceManager.getUid();

        if (TextUtils.isEmpty(age) || genderSpinner.getSelectedItemPosition() == 0 ||
                dietarySpinner.getSelectedItemPosition() == 0 || fitnessGoalSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            btn_continue.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            return;
        }

        String preferences = "I'm " + dietary + ", I want to " + fitnessGoal +
                (TextUtils.isEmpty(allergies) ? "" : ", have " + allergies) +
                (TextUtils.isEmpty(cuisine) ? "" : ", and like " + cuisine);

        preferenceManager.setPreferences(preferences);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("age", age);
        userMap.put("gender", gender);
        userMap.put("preferences", preferences);
        userMap.put("isProfileCompleted", true);

        db.collection("users")
                .document(uid)
                .update(userMap)
                .addOnSuccessListener(unused -> {
                    preferenceManager.setNotCompletedLogin(false);
                    preferenceManager.setAge(age);
                    preferenceManager.setGender(gender);
                    preferenceManager.setPreferences(preferences);
                    Toast.makeText(this, "Profile setup complete!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    btn_continue.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to update profile status", Toast.LENGTH_SHORT).show();
                    Log.e("SignUpActivity", "Error updating Firestore", e);
                });
    }

}
