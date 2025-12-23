package com.example.moresqplore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
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
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "MoresQplorePrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHOTO_URL = "userPhotoUrl";

    // UI Components
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogleSignIn;
    private TextView tvCreateAccount, tvForgotPassword;

    // Google Sign In
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            String savedName = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .getString(KEY_USER_NAME, "Traveler");
            navigateToWelcomeActivity(savedName);
            return;
        }

        setContentView(R.layout.activity_landing);

        initializeViews();
        setupGoogleSignIn();
        setupClickListeners();
        setupSignInLauncher();
    }

    private void initializeViews() {
        // Input Fields
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        // Buttons & Links
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupGoogleSignIn() {
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
                        Toast.makeText(this, "Google Sign in cancelled", Toast.LENGTH_SHORT).show();
                        btnGoogleSignIn.setEnabled(true);
                    }
                }
        );
    }

    private void setupClickListeners() {
        // 1. Email/Password Login
        btnLogin.setOnClickListener(v -> attemptEmailLogin());

        // 2. Google Login
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        // 3. Sign Up Link
        tvCreateAccount.setOnClickListener(v -> {
            // TODO: Navigate to RegisterActivity
            Toast.makeText(this, "Redirecting to Sign Up...", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            // startActivity(intent);
        });

        // 4. Forgot Password Link
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Reset password feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void attemptEmailLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Basic Validation
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // TODO: IMPLEMENT ACTUAL AUTHENTICATION HERE (e.g., Firebase Auth)
        // For now, we simulate a successful login for any non-empty input
        if (email.contains("@")) {
            String simulatedName = email.split("@")[0]; // Use part of email as name
            saveUserInfo(simulatedName, email, null);
            navigateToWelcomeActivity(simulatedName);
        } else {
            etEmail.setError("Please enter a valid email");
        }
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
                String name = account.getDisplayName() != null ? account.getDisplayName() : "Traveler";

                saveUserInfo(
                        name,
                        account.getEmail(),
                        account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null
                );

                navigateToWelcomeActivity(name);
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            btnGoogleSignIn.setEnabled(true);
        }
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

    /**
     * Navigates to the Welcome Animation screen instead of directly to Main.
     * Passes the user's name for the personalization text.
     */
    private void navigateToWelcomeActivity(String userName) {
        Intent intent = new Intent(LoginActivity.this, WelcomingActivity.class);
        intent.putExtra("USER_NAME", userName);
        startActivity(intent);
        finish(); // Close LoginActivity so user can't go back to it
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Optional: Check strictly for Google account existence if you prefer strict Google logic
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // If Google says we are signed in, update local prefs and go
            String name = account.getDisplayName() != null ? account.getDisplayName() : "Traveler";
            saveUserInfo(name, account.getEmail(),
                    account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null);
            navigateToWelcomeActivity(name);
        }
    }
}