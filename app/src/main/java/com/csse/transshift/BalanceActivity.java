package com.csse.transshift;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.csse.transshift.models.ReadWriteUserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class BalanceActivity extends AppCompatActivity {

    private TextView textViewamount, textViewemail, textViewdate;
    private ProgressBar progressBar;
    private String amount, email, date;
    private FirebaseAuth authProfile;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Account Balance");

        textViewamount = findViewById(R.id.amount_balance_tv);
        textViewemail = findViewById(R.id.email_balance_tv);
        textViewdate = findViewById(R.id.date_balance_tv);
        progressBar = findViewById(R.id.progressAB);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        calendar = Calendar.getInstance();

        if (firebaseUser == null) {
            Toast.makeText(BalanceActivity.this, "Something went wrong! User's details are not available at the moment", Toast.LENGTH_LONG).show();
        } else {
            checkifEmailVerified(firebaseUser);
            progressBar.setVisibility(View.VISIBLE);
            showAccountBalance(firebaseUser);
        }
    }

    //users coming to userprofile activity after successful registration
    private void checkifEmailVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()) {
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        //setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(BalanceActivity.this);
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

    private void showAccountBalance(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        //Extracting user reference from db for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    email = firebaseUser.getEmail();
                    amount = readUserDetails.balance;

                    dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    date = dateFormat.format(calendar.getTime());

                    textViewemail.setText(email);
                    textViewamount.setText(amount);
                    textViewdate.setText(date);

                } else {
                    Toast.makeText(BalanceActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BalanceActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //create actionbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //when any item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            //refresh activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        } else if (id == R.id.menu_update) {
            Intent intent = new Intent(BalanceActivity.this, UpdateprofileActivity.class);
            startActivity(intent);
        } /*else if (id == R.id.menu_update_email) {
            Intent intent= new Intent(BalanceActivity.this, UpdateemailActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_settings) {
            Intent intent= new Intent(BalanceActivity.this, ChangepasswordActivity.class);
            startActivity(intent);
        }*/ else if (id == R.id.delete_profile) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Please wait...");
            progressDialog.setMessage("Deleting your account");
            progressDialog.setCancelable(false);
            progressDialog.show();
            FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("Registered Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(null).addOnSuccessListener(unused -> FirebaseAuth.getInstance().getCurrentUser().delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    //progressDialog.dismiss();
                                    Intent intent = new Intent(BalanceActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(BalanceActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            }));
        } else if (id == R.id.logout) {
            authProfile.signOut();
            Toast.makeText(BalanceActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BalanceActivity.this, MainActivity.class);

            //clear stack to prevent user coming back to userprofile activity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(BalanceActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}