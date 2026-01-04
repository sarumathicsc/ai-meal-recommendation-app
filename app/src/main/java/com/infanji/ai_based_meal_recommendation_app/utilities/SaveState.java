package com.infanji.ai_based_meal_recommendation_app.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class SaveState {

    private SharedPreferences sharedPreferences;
    private Context context;
    private String saveName;

    public SaveState(Context context, String saveName) {
        this.context = context;
        this.saveName = saveName;
        sharedPreferences = context.getSharedPreferences(saveName, Context.MODE_PRIVATE);
    }

    public void setState(int key){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("key", key);
        editor.apply();
    }

    public int getState(){
        return sharedPreferences.getInt(  "key", 0);
    }

}