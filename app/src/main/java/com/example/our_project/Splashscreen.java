package com.example.our_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
public class Splashscreen extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        // Adding animation to TextView
        TextView flashingText = findViewById(R.id.app_name);
        Animation blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.flash_animation);
        flashingText.startAnimation(blinkAnimation);

        //splash screen setting
        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences=getSharedPreferences("userRegistered",MODE_PRIVATE);
            boolean isRegistered=sharedPreferences.getBoolean("isRegistered",false);

            SharedPreferences sharedPreferences2=getSharedPreferences("userLogged",MODE_PRIVATE);
            boolean isLogged=sharedPreferences2.getBoolean("isLogged",false);

            if(!isRegistered)
            {
                startActivity(new Intent(Splashscreen.this, RegisterActivity.class));
            }
            else if(isRegistered && !isLogged)
            {
                startActivity(new Intent(Splashscreen.this, LoginActivity.class));
            }
            else
            {
                startActivity(new Intent(Splashscreen.this,MainActivity.class ));
            }
            finish();
        },3000);

    }
}
