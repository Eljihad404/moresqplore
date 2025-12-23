package com.example.moresqplore;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class WelcomingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // MAKE SURE your XML file is actually named "welcome_to_morocco.xml"
        setContentView(R.layout.welcome_to_morocco);

        // 1. Initialize Views
        LinearLayout welcomeContainer = findViewById(R.id.welcomeContainer);
        TextView tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        MaterialButton btnStartTrip = findViewById(R.id.btnStartTrip);

        // 2. Set User Name from Intent (Passed from LoginActivity)
        String userName = getIntent().getStringExtra("USER_NAME");
        if (userName != null) {
            tvWelcomeTitle.setText("Welcome " + userName + " to Morocco");
        }

        // 3. Run Animation (Make sure res/anim/slow_fade_up.xml exists)
        try {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slow_fade_up);
            welcomeContainer.startAnimation(slideUp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 4. Handle Button Click -> Go to City Selection
        btnStartTrip.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomingActivity.this, SelectCityActivity.class);
            startActivity(intent);
        });
    }
}