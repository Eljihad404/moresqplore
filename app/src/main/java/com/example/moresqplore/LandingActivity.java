package com.example.moresqplore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

public class LandingActivity extends AppCompatActivity {

    private static final String TAG = "LandingActivity";
    private static final String PREFS_NAME = "MoresQplorePrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHOTO_URL = "userPhotoUrl";

    private GoogleSignInClient mGoogleSignInClient;
    private MaterialButton btnGoogleSignIn, btnSkip;

    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        setContentView(R.layout.activity_landing);

        initializeViews();
        setupGoogleSignIn();
        setupClickListeners();
        setupSignInLauncher();
    }

    private void initializeViews() {
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnSkip = findViewById(R.id.btnSkip);
    }

    private void setupGoogleSignIn() {
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupSignInLauncher() {
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        handleSignInResult(data);
                    } else {
                        Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupClickListeners() {
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        btnSkip.setOnClickListener(v -> continueAsGuest());
    }

    private void signInWithGoogle() {
        btnGoogleSignIn.setEnabled(false);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                // Save user information
                saveUserInfo(
                        account.getDisplayName(),
                        account.getEmail(),
                        account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null
                );

                Toast.makeText(this, "Welcome, " + account.getDisplayName() + "!",
                        Toast.LENGTH_SHORT).show();

                navigateToMainActivity();
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Sign in failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            btnGoogleSignIn.setEnabled(true);
        }
    }

    private void continueAsGuest() {
        saveUserInfo("Guest", null, null);
        navigateToMainActivity();
    }

    private void saveUserInfo(String name, String email, String photoUrl) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PHOTO_URL, photoUrl);
        editor.apply();
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LandingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            saveUserInfo(
                    account.getDisplayName(),
                    account.getEmail(),
                    account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null
            );
            navigateToMainActivity();
        }
    }
}