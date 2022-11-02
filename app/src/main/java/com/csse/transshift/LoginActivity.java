package com.csse.transshift;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPsd;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //set the title
        Objects.requireNonNull(getSupportActionBar()).setTitle("Login");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        editTextEmail = findViewById(R.id.email_login_et);
        editTextPsd = findViewById(R.id.psd_login_et);
        progressBar = findViewById(R.id.progressL);

        authProfile = FirebaseAuth.getInstance();

        //Rest Password
        Button buttonforgotPsd = findViewById(R.id.btn_forgot);
        buttonforgotPsd.setOnClickListener(view -> {
            Toast.makeText(LoginActivity.this, "You can reset your password now!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(LoginActivity.this, Forgotpassword.class));

        });

        //show hide password using eye icon
        ImageView imageView_psd = findViewById(R.id.psd_shower);
        imageView_psd.setImageResource(R.drawable.ic_psd_hide);
        imageView_psd.setOnClickListener(view -> {
            if (editTextPsd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                //if password is visible then hide it
                editTextPsd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                //change icon
                imageView_psd.setImageResource(R.drawable.ic_psd_hide);
            } else {
                editTextPsd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                imageView_psd.setImageResource(R.drawable.ic_psd_show);
            }
        });

        //Login user
        Button buttonLogin = findViewById(R.id.btn_login);
        buttonLogin.setOnClickListener(view -> {
            String textEmail = editTextEmail.getText().toString();
            String textPsd = editTextPsd.getText().toString();

            if (TextUtils.isEmpty(textEmail)) {
                Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_LONG).show();
                editTextEmail.setError("Email is required");
                editTextEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                Toast.makeText(LoginActivity.this, "Please re-enter your email", Toast.LENGTH_LONG).show();
                editTextEmail.setError("Valid email is required");
                editTextEmail.requestFocus();
            } else if (TextUtils.isEmpty(textPsd)) {
                Toast.makeText(LoginActivity.this, "Please enter your password", Toast.LENGTH_LONG).show();
                editTextPsd.setError("Password is required");
                editTextPsd.requestFocus();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                loginUser(textEmail, textPsd);
            }
        });
    }

    private void loginUser(String email, String psd) {
        authProfile.signInWithEmailAndPassword(email, psd).addOnCompleteListener(LoginActivity.this, task -> {
            if (task.isSuccessful()) {

                //get instance of the current user
                FirebaseUser firebaseUser = authProfile.getCurrentUser();

                //check if email is verified before user can access their profile
                if (Objects.requireNonNull(firebaseUser).isEmailVerified()) {
                    Toast.makeText(LoginActivity.this, "You are logged in now", Toast.LENGTH_SHORT).show();

                    //open user profile
                    //start new activity
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();

                } else {
                    firebaseUser.sendEmailVerification();
                    authProfile.signOut();
                    showAlertDialog();
                }
            } else {
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthInvalidUserException e) {
                    editTextEmail.setError("User does not exists or no longer valid. Please register again.");
                    editTextEmail.requestFocus();
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    editTextEmail.setError("Invalid credentials. Kindly check and re-enter.");
                    editTextEmail.requestFocus();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
            progressBar.setVisibility(View.GONE);
        });
    }

    private void showAlertDialog() {
        //setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email is not verified!");
        builder.setMessage("Please verify your email now. You can not login without email verification.");

        //open email apps if user clicks/taps continue button
        builder.setPositiveButton("Continue", (dialogInterface, i) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        //create the alertdialog
        AlertDialog alertDialog = builder.create();

        //show the alertdialog
        alertDialog.show();
    }

    //Check if user is already logged in
    @Override
    protected void onStart() {
        super.onStart();
        if (authProfile.getCurrentUser() != null) {
            Toast.makeText(LoginActivity.this, "Already logged in!", Toast.LENGTH_SHORT).show();
            //start new activity
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "You login now!", Toast.LENGTH_SHORT).show();
        }
    }
}