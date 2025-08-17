package com.example.our_project;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText usernameReg, emailReg, passwordReg, confPasswordReg;
    private Button registerBtn;

    private TextView login;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.d(TAG, "onCreate() called");

        usernameReg = findViewById(R.id.username_reg);
        emailReg = findViewById(R.id.email_reg);
        passwordReg = findViewById(R.id.password_reg);
        confPasswordReg = findViewById(R.id.conf_password_reg);
        registerBtn = findViewById(R.id.register);

        login=findViewById(R.id.reg_to_login);



        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");

        login.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("userRegistered", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isRegistered", true);
            editor.apply();
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        registerBtn.setOnClickListener(view -> {
            String username = usernameReg.getText().toString().trim();
            String email = emailReg.getText().toString().trim();
            String password = passwordReg.getText().toString().trim();
            String confPassword = confPasswordReg.getText().toString().trim();

            Log.d(TAG, "Register button clicked");

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(password) || TextUtils.isEmpty(confPassword)) {
                Log.d(TAG, "Validation failed: Empty fields");
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confPassword)) {
                Log.d(TAG, "Password mismatch");
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Log.d(TAG, "Password too short");
                Toast.makeText(RegisterActivity.this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            registerBtn.setEnabled(false);
            progressDialog.show();

            registerUser(username, email, password);
        });
    }

    private void registerUser(String username, String email, String password) {
        Log.d(TAG, "Creating user with email: " + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        Log.d(TAG, "User registered successfully");

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // Store in Firestore
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("username", username);
                            userMap.put("email", email);

                            firestore.collection("Users").document(userId)
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "User data saved to Firestore");
                                        Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();

                                        // Save to SharedPreferences
                                        SharedPreferences sharedPreferences = getSharedPreferences("userDetails", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("username", username);
                                        editor.putString("email", email);
                                        editor.putString("password", password); // optional, not recommended to store raw password
                                        editor.apply();

                                        // Also store registration status
                                        SharedPreferences sp = getSharedPreferences("userRegistered", MODE_PRIVATE);
                                        SharedPreferences.Editor spEditor = sp.edit();
                                        spEditor.putBoolean("isRegistered", true);
                                        spEditor.apply();

                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "Failed to save user data: " + e.getMessage());
                                        Toast.makeText(RegisterActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                        registerBtn.setEnabled(true);
                                    });
                        }
                    } else {
                        Log.d(TAG, "Registration failed: " + task.getException().getMessage());
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        registerBtn.setEnabled(true);
                    }
                });
    }
}
