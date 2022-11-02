package com.csse.transshift;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class UserprofileActivity extends AppCompatActivity {

    private TextView textViewname, textViewemail, textViewdob, textViewmobile, textViewtitle;
    private ProgressBar progressBar;
    private String name, email, dob, mobile;
    private ImageView imageView;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

        Objects.requireNonNull(getSupportActionBar()).setTitle("User Profile");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        textViewtitle = findViewById(R.id.title_profile_tv);
        textViewname = findViewById(R.id.name_profile_tv);
        textViewemail = findViewById(R.id.email_profile_tv);
        textViewdob = findViewById(R.id.date_balance_tv);
        textViewmobile = findViewById(R.id.mobile_profile_tv);
        progressBar = findViewById(R.id.progressU);

        //Set OnclickListener on imageview to open uploadprofile activity
        imageView = findViewById(R.id.imageview_profile);
        imageView.setOnClickListener(view -> {
            Intent intent = new Intent(UserprofileActivity.this, UpdateprofileActivity.class);
            startActivity(intent);
        });

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(UserprofileActivity.this, "Something went wrong! User's details are not available at the moment", Toast.LENGTH_LONG).show();
        } else {
            checkifEmailVerified(firebaseUser);
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(UserprofileActivity.this);
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

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        //Extracting user reference from db for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    name = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    dob = readUserDetails.dob;
                    mobile = readUserDetails.mobile;

                    textViewtitle.setText("Welcome, " + name + "!");
                    textViewname.setText(name);
                    textViewemail.setText(email);
                    textViewdob.setText(dob);
                    textViewmobile.setText(mobile);

                    //Set user dp 
                    Uri uri = firebaseUser.getPhotoUrl();

                    //Imageviewer setImageURI() should not be used with regular URIs.
                    Picasso.get().load(uri).into(imageView);
                } else {
                    Toast.makeText(UserprofileActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserprofileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(UserprofileActivity.this, UpdateprofileActivity.class);
            startActivity(intent);
        } /*else if (id == R.id.menu_update_email) {
            Intent intent= new Intent(UserprofileActivity.this, UpdateemailActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_settings) {
            Intent intent= new Intent(UserprofileActivity.this, ChangepasswordActivity.class);
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
                                    Intent intent = new Intent(UserprofileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(UserprofileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            }));
        } else if (id == R.id.logout) {
            authProfile.signOut();
            Toast.makeText(UserprofileActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserprofileActivity.this, MainActivity.class);

            //clear stack to prevent user coming back to userprofile activity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UserprofileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}