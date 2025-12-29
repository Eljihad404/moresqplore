package com.example.moresqplore;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.moresqplore.ui.chat.ChatActivity;

/**
 * Legacy ChatbotActivity - redirects to the modern ChatActivity
 * This maintains backward compatibility for any old intents
 */
public class ChatbotActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Redirect to the modern ChatActivity
        Intent intent = new Intent(this, ChatActivity.class);
        
        // Forward any extras from the original intent
        if (getIntent() != null && getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        
        startActivity(intent);
        finish(); // Close this activity immediately
    }
}
