package com.csse.transshift;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

public class Forgotpassword extends AppCompatActivity {

    private EditText editTextEmail;
    private ProgressBar progressBar;
    private final static String TAG = "Forgotpassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Forgot Password");

        editTextEmail = findViewById(R.id.email_forgot_et);
        Button buttonReset = findViewById(R.id.btn_resetpsd);
        progressBar = findViewById(R.id.progressR);

        buttonReset.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Forgotpassword.this, "Please enter your registered email", Toast.LENGTH_LONG).show();
                editTextEmail.setError("Email is required");
                editTextEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Forgotpassword.this, "Please re-enter your email", Toast.LENGTH_LONG).show();
                editTextEmail.setError("Valid email is required");
                editTextEmail.requestFocus();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                resetPassword(email);
            }
        });
    }

    private void resetPassword(String email) {
        FirebaseAuth authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(Forgotpassword.this, "Please check your inbox for password reset link", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Forgotpassword.this, MainActivity.class);

                //clear stack to prevent user coming back to forgotpassword activity
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthInvalidUserException e) {
                    editTextEmail.setError("User does not exists or is no longer valid. Please register again.");
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(Forgotpassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            progressBar.setVisibility(View.GONE);
        });
    }
}