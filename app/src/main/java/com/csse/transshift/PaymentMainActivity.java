package com.csse.transshift;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class PaymentMainActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private EditText amount, name, number, cvv, date;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_main);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Recharge Account");

        Toast.makeText(PaymentMainActivity.this, "You can recharge account now", Toast.LENGTH_LONG).show();

        context = this;
        Button payid = findViewById(R.id.paybt);
        name = findViewById(R.id.cardname);
        amount = findViewById(R.id.amount_et);
        number = findViewById(R.id.getcardnum);
        cvv = findViewById(R.id.cvv_et);
        date = findViewById(R.id.exdate_et);

        payid.setOnClickListener(v -> {

            String etName = name.getText().toString();
            String etAmount = amount.getText().toString();
            String etNumber = number.getText().toString();
            String etCvv = cvv.getText().toString();
            String etDate = date.getText().toString();

            if (TextUtils.isEmpty(etAmount)) {
                Toast.makeText(PaymentMainActivity.this, "Please enter recharge amount", Toast.LENGTH_LONG).show();
                amount.setError("Amount is required");
                amount.requestFocus();
            } else if (TextUtils.isEmpty(etName)) {
                Toast.makeText(PaymentMainActivity.this, "Please enter your name", Toast.LENGTH_LONG).show();
                name.setError("Name is required");
                name.requestFocus();
            } else if (TextUtils.isEmpty(etNumber)) {
                Toast.makeText(PaymentMainActivity.this, "Please enter card number", Toast.LENGTH_LONG).show();
                number.setError("Number is required");
                number.requestFocus();
            } else if (TextUtils.isEmpty(etCvv)) {
                Toast.makeText(PaymentMainActivity.this, "Please enter cvv", Toast.LENGTH_LONG).show();
                cvv.setError("CVV is required");
                cvv.requestFocus();
            } else if (TextUtils.isEmpty(etDate)) {
                Toast.makeText(PaymentMainActivity.this, "Please enter date", Toast.LENGTH_LONG).show();
                date.setError("Date is required");
                date.requestFocus();
            } else if (etNumber.length() != 16) {
                Toast.makeText(PaymentMainActivity.this, "Please re-enter your card number", Toast.LENGTH_LONG).show();
                number.setError("Card Number should be 16 digits");
                number.requestFocus();
            } else if (etCvv.length() != 3) {
                Toast.makeText(PaymentMainActivity.this, "Please re-enter your cvv number", Toast.LENGTH_LONG).show();
                cvv.setError("CVV Number should be 3 digits");
                cvv.requestFocus();
            } else {
                Toast.makeText(getApplicationContext(), "Account Recharge Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(context, HomeActivity.class));
            }
        });

        authProfile = FirebaseAuth.getInstance();
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
            Intent intent = new Intent(PaymentMainActivity.this, UpdateprofileActivity.class);
            startActivity(intent);
        } /*else if (id == R.id.menu_update_email) {
            Intent intent= new Intent(HomeActivity.this, UpdateemailActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_settings) {
            Intent intent= new Intent(HomeActivity.this, ChangepasswordActivity.class);
            startActivity(intent);
        }*/else if (id == R.id.delete_profile) {
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
                                    Intent intent = new Intent(PaymentMainActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(PaymentMainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            }));
        } else if (id == R.id.logout) {
            authProfile.signOut();
            Toast.makeText(PaymentMainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PaymentMainActivity.this, MainActivity.class);

            //clear stack to prevent user coming back to userprofile activity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(PaymentMainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}