package com.csse.transshift;

import android.app.DatePickerDialog;
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

import com.csse.transshift.models.ReadWriteUserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextEmail, editTextDob, editTextMobile, editTextPassword,
            editTextRePassword;
    private ProgressBar progressBar;
    private DatePickerDialog picker;
    private static final String TAG = "RegisterActivity";
    private String balance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Register");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Toast.makeText(RegisterActivity.this, "You can register now", Toast.LENGTH_LONG).show();

        editTextFullName = findViewById(R.id.fullname_register_et);
        editTextEmail = findViewById(R.id.email_register_et);
        editTextDob = findViewById(R.id.dob_register_et);
        editTextMobile = findViewById(R.id.mobile_register_et);
        editTextPassword = findViewById(R.id.psd_register_et);
        editTextRePassword = findViewById(R.id.repsd_register_et);
        Button buttonRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress);

        //Setting up DatePicker on EditText
        editTextDob.setOnClickListener(view -> {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            //Date Picker Dialog
            picker = new DatePickerDialog(RegisterActivity.this, (view1, year1, month1, dayOfMonth) -> editTextDob.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1), year, month, day);
            picker.show();
        });

        buttonRegister.setOnClickListener(view -> {

            //Obtain the entered data
            String textFullName = editTextFullName.getText().toString();
            String textEmail = editTextEmail.getText().toString();
            String textDob = editTextDob.getText().toString();
            String textMobile = editTextMobile.getText().toString();
            String textPassword = editTextPassword.getText().toString();
            String textRePassword = editTextRePassword.getText().toString();

            //validate mobile number using matcher and pattern
            String mobileRegex = "[7][0-9]{8}"; //first number should be 7
            Matcher mobileMatcher;
            Pattern mobilePattern = Pattern.compile(mobileRegex);
            mobileMatcher = mobilePattern.matcher(textMobile);

            if (TextUtils.isEmpty(textFullName)) {
                Toast.makeText(RegisterActivity.this, "Please enter your full name", Toast.LENGTH_LONG).show();
                editTextFullName.setError("Full Name is required");
                editTextFullName.requestFocus();
            } else if (TextUtils.isEmpty(textEmail)) {
                Toast.makeText(RegisterActivity.this, "Please enter your email", Toast.LENGTH_LONG).show();
                editTextEmail.setError("Email is required");
                editTextEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                Toast.makeText(RegisterActivity.this, "Please re-enter your email", Toast.LENGTH_LONG).show();
                editTextEmail.setError("Valid email is required");
                editTextEmail.requestFocus();
            } else if (TextUtils.isEmpty(textDob)) {
                Toast.makeText(RegisterActivity.this, "Please enter your date of birth", Toast.LENGTH_LONG).show();
                editTextDob.setError("Date of birth is required");
                editTextDob.requestFocus();
            } else if (TextUtils.isEmpty(textMobile)) {
                Toast.makeText(RegisterActivity.this, "Please enter your mobile number", Toast.LENGTH_LONG).show();
                editTextMobile.setError("Mobile Number is required");
                editTextMobile.requestFocus();
            } else if (textMobile.length() != 9) {
                Toast.makeText(RegisterActivity.this, "Please re-enter your mobile number", Toast.LENGTH_LONG).show();
                editTextMobile.setError("Mobile Number should be 9 digits");
                editTextMobile.requestFocus();
            } else if (!mobileMatcher.find()) {
                Toast.makeText(RegisterActivity.this, "Please re-enter your mobile number", Toast.LENGTH_LONG).show();
                editTextMobile.setError("Mobile Number is not valid");
                editTextMobile.requestFocus();
            } else if (TextUtils.isEmpty(textPassword)) {
                Toast.makeText(RegisterActivity.this, "Please enter your password", Toast.LENGTH_LONG).show();
                editTextPassword.setError("Password is required");
                editTextPassword.requestFocus();
            } else if (textPassword.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Password should be at least 6 digits", Toast.LENGTH_LONG).show();
                editTextPassword.setError("Password is too weak");
                editTextMobile.requestFocus();
            } else if (TextUtils.isEmpty(textRePassword)) {
                Toast.makeText(RegisterActivity.this, "Please enter your confirm password", Toast.LENGTH_LONG).show();
                editTextRePassword.setError("Confirm Password is required");
                editTextRePassword.requestFocus();
            } else if (!textPassword.equals(textRePassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords must be equal", Toast.LENGTH_LONG).show();
                editTextRePassword.setError("Passwords isn't matched");
                editTextRePassword.requestFocus();
                //Clear the entered passwords
                editTextPassword.clearComposingText();
                editTextRePassword.clearComposingText();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                registerUser(textFullName, textEmail, textDob, textMobile, textPassword);
            }
        });
    }

    //Register User using the credentials given
    private void registerUser(String textFullName, String textEmail, String textDob, String textMobile, String textPassword) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        //create user profile
        auth.createUserWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener(RegisterActivity.this,
                task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        //Update display name of user
                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                        Objects.requireNonNull(firebaseUser).updateProfile(profileChangeRequest);

                        //Enter user data into the firebase realtime db
                        ReadWriteUserDetails readWriteUserDetails = new ReadWriteUserDetails(textDob, textMobile, balance);

                        //Extracting user reference from firebase db for "Registered Users"
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");

                        databaseReference.child(Objects.requireNonNull(firebaseUser).getUid()).setValue(readWriteUserDetails).addOnCompleteListener(task1 -> {

                            if (task.isSuccessful()) {
                                //Send Verification Email
                                Objects.requireNonNull(firebaseUser).sendEmailVerification();

                                Toast.makeText(RegisterActivity.this, "User registered successfully. Please verify your email", Toast.LENGTH_LONG).show();

                                //Open User Profile after successful registration
                                Intent intent = new Intent(RegisterActivity.this, UserprofileActivity.class);
                                //To prevent user from returning back to Register activity on pressing back button after registration
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        | Intent.FLAG_ACTIVITY_NEW_TASK);

                                startActivity(intent);
                                //to close Register activity
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "User registered failed. Please try again",
                                        Toast.LENGTH_LONG).show();
                            }
                            //Hide progressBar whether user creation is successful or failed
                            progressBar.setVisibility(View.GONE);
                        });
                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthWeakPasswordException e) {
                            editTextPassword.setError("Your password is too weak. Kindly use mix of alphabets, numbers, and special characters");
                            editTextPassword.requestFocus();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            editTextPassword.setError("Your email is invalid or already in use. Kindly re-enter");
                            editTextPassword.requestFocus();
                        } catch (FirebaseAuthUserCollisionException e) {
                            editTextPassword.setError("User is already registered with this email. Use another email address");
                            editTextPassword.requestFocus();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean checkEmailForValidity(String email) {

        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }
}