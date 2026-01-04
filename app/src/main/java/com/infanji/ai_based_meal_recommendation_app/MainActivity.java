package com.infanji.ai_based_meal_recommendation_app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.infanji.ai_based_meal_recommendation_app.account_creation.LoginActivity;
import com.infanji.ai_based_meal_recommendation_app.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private String greetings = "";
    private List<String> recipe;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private String gender, meal_name_recipe;

    private ImageView meal_img, btn_view, cooking;
    private TextView meal_des;

    private Handler handler;
    private Runnable fetchRunnable;

    private ProgressBar progressBar;

    String breakfast_des = "Here’s your personalized breakfast recommendation to kickstart your day:";
    String lunch_des = "Here’s your AI-picked lunch to keep you energized through the afternoon:";
    String dinner_des = "Here’s your personalized dinner recommendation to wrap up your day right:";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        greetings = getGreetingMessage();

        preferenceManager = new PreferenceManager(this);
        db = FirebaseFirestore.getInstance();

        TextView good = findViewById(R.id.greeting);
        good.setText(greetings);
        TextView name = findViewById(R.id.username);
        name.setText(preferenceManager.getUsername());

        meal_des = findViewById(R.id.meal_des);
        btn_view = findViewById(R.id.btn_view);
        meal_img = findViewById(R.id.meal_type_img);
        cooking = findViewById(R.id.cooking);
        ImageView dp = findViewById(R.id.dp);

        String uid = preferenceManager.getUid();

        progressBar = findViewById(R.id.cooking_bar);

        // Fetch gender from Firestore
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    gender = snapshot.getString("gender");
                    if (gender != null) {
                        preferenceManager.setGender(gender);
                    } else {
                        Toast.makeText(MainActivity.this, "Gender not found in database", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No user data found for UID: " + uid, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
            }

            // Set profile image based on gender
            if ("Male".equals(preferenceManager.getGender())) {
                dp.setImageResource(R.drawable.male);
            } else {
                dp.setImageResource(R.drawable.female);
            }
        });

        // Start auto-refreshing every 30 seconds
        handler = new Handler();
        fetchRunnable = this::fetchMealSuggestion;
        fetchMealSuggestion(); // Initial fetch
        handler.postDelayed(fetchRunnable, 10000);

        btn_view.setOnClickListener(v -> {
            showRecipePopup(MainActivity.this, meal_name_recipe, recipe);
        });

        dp.setOnClickListener(v -> showLogoutDialog());
    }

    private void fetchMealSuggestion() {
        String uid = preferenceManager.getUid();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String mealType = getMealType(); // "Breakfast", "Lunch", or "Dinner"

        DocumentReference suggestionRef = db.collection("meal_suggestions")
                .document(uid)
                .collection(mealType)
                .document(date);

        suggestionRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String rawJson = documentSnapshot.getString("suggestion");

                if (rawJson != null && !rawJson.isEmpty()) {
                    try {
                        // Clean up Markdown-style JSON formatting if needed
                        if (rawJson.startsWith("```json")) {
                            rawJson = rawJson.replace("```json", "").replace("```", "").trim();
                        }

                        // Try to extract the actual JSON content from wrapped responses
                        int firstBrace = rawJson.indexOf('{');
                        int lastBrace = rawJson.lastIndexOf('}');
                        if (firstBrace != -1 && lastBrace != -1) {
                            rawJson = rawJson.substring(firstBrace, lastBrace + 1);
                        }

                        JSONObject jsonObject = new JSONObject(rawJson);
                        Meal meal = new Meal();

                        // Get the name
                        if (jsonObject.has("name")) {
                            meal.setName(jsonObject.getString("name"));
                        } else if (jsonObject.has("mealName")) {
                            meal.setName(jsonObject.getString("mealName"));
                        } else {
                            meal.setName("Meal");
                        }

                        // Get the calories
                        if (jsonObject.has("calories")) {
                            try {
                                meal.setCalories(jsonObject.getInt("calories"));
                            } catch (Exception e) {
                                meal.setCalories(Integer.parseInt(jsonObject.getString("calories").replaceAll("[^\\d]", "")));
                            }
                        }

                        // Handle recipe
                        List<String> recipeSteps = new ArrayList<>();
                        if (jsonObject.has("recipe")) {
                            Object recipeObj = jsonObject.get("recipe");

                            if (recipeObj instanceof JSONArray) {
                                JSONArray arr = (JSONArray) recipeObj;
                                for (int i = 0; i < arr.length(); i++) {
                                    recipeSteps.add(arr.getString(i));
                                }
                            } else if (recipeObj instanceof JSONObject) {
                                JSONObject recipeJson = (JSONObject) recipeObj;

                                if (recipeJson.has("instructions")) {
                                    Object instructions = recipeJson.get("instructions");
                                    if (instructions instanceof JSONArray) {
                                        JSONArray instructionsArr = (JSONArray) instructions;
                                        for (int i = 0; i < instructionsArr.length(); i++) {
                                            recipeSteps.add(instructionsArr.getString(i));
                                        }
                                    } else if (instructions instanceof String) {
                                        recipeSteps.addAll(splitSteps((String) instructions));
                                    }
                                } else if (recipeJson.has("steps")) {
                                    JSONArray steps = recipeJson.getJSONArray("steps");
                                    for (int i = 0; i < steps.length(); i++) {
                                        recipeSteps.add(steps.getString(i));
                                    }
                                } else if (recipeJson.has("preparation")) {
                                    JSONArray prep = recipeJson.getJSONArray("preparation");
                                    recipeSteps.add("Preparation:");
                                    for (int i = 0; i < prep.length(); i++) {
                                        recipeSteps.add("- " + prep.getString(i));
                                    }
                                } else {
                                    recipeSteps.add("No detailed instructions found.");
                                }
                            } else if (recipeObj instanceof String) {
                                recipeSteps.addAll(splitSteps((String) recipeObj));
                            }
                        } else {
                            recipeSteps.add("No recipe found.");
                        }

                        meal.setRecipe(recipeSteps);
                        displayMealData(meal);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "⚠️ Failed to parse meal suggestion.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this, "⚠️ Suggestion data is empty", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(MainActivity.this, mealType + " suggestion not found", Toast.LENGTH_SHORT).show();
            }

            handler.postDelayed(fetchRunnable, 30000);

        }).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, "⚠️ Error fetching meal data", Toast.LENGTH_SHORT).show();
            handler.postDelayed(fetchRunnable, 30000);
        });
    }

    private List<String> splitSteps(String raw) {
        List<String> steps = new ArrayList<>();
        if (raw != null) {
            String[] split = raw.split("\\r?\\n|\\d+\\.\\s*");
            for (String s : split) {
                if (!s.trim().isEmpty()) {
                    steps.add(s.trim());
                }
            }
        }
        return steps;
    }

    private void displayMealData(Meal meal) {
        cooking.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        btn_view.setVisibility(View.VISIBLE);

        TextView mealTypeView = findViewById(R.id.meal_type);
        TextView mealName = findViewById(R.id.meal_name);
        TextView calories = findViewById(R.id.calories);

        String mealType = getMealType();
        mealTypeView.setText(mealType);
        set_meal_img(mealType);
        recipe = meal.getRecipe();

        meal_name_recipe = meal.getName();

        mealName.setText("\uD83C\uDF72 " + meal.getName());
        calories.setText(String.format("🔥 %d Calories", meal.getCalories()));

        // You can set recipe elsewhere (e.g., in a detail screen)
    }

    private void showRecipePopup(Context context, String mealName, List<String> recipeSteps) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_recipe, null);

        TextView title = view.findViewById(R.id.recipeTitle);
        TextView steps = view.findViewById(R.id.recipeSteps);
        ImageView closeBtn = view.findViewById(R.id.closeDialogBtn);

        title.setText("Recipe 😋 for " + mealName);

        StringBuilder recipeBuilder = new StringBuilder();
        for (int i = 0; i < recipeSteps.size(); i++) {
            recipeBuilder.append(i + 1).append(". ").append(recipeSteps.get(i)).append("\n\n");
        }

        steps.setText(recipeBuilder.toString());

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        closeBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showLogoutDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    preferenceManager.clearPreferences();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new android.content.Intent(MainActivity.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private String getMealType() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour < 12) return "Breakfast";
        else if (hour >= 12 && hour < 17) return "Lunch";
        else return "Dinner";
    }

    private void set_meal_img(String mealType) {
        if (mealType.equals("Breakfast")) {
            meal_img.setImageResource(R.drawable.breakfast);
            meal_des.setText(breakfast_des);
        } else if (mealType.equals("Lunch")) {
            meal_img.setImageResource(R.drawable.lunch);
            meal_des.setText(lunch_des);
        } else if (mealType.equals("Dinner")) {
            meal_img.setImageResource(R.drawable.dinner);
            meal_des.setText(dinner_des);
        }
    }

    private String getGreetingMessage() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour < 12) return "Good Morning!";
        else if (hour >= 12 && hour < 17) return "Good Afternoon!";
        else if (hour >= 17 && hour < 21) return "Good Evening!";
        else return "Good Night!";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && fetchRunnable != null) {
            handler.removeCallbacks(fetchRunnable);
        }
    }
}