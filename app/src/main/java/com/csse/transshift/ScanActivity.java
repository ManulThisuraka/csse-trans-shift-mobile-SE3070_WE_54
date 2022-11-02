package com.csse.transshift;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.csse.transshift.models.QRCardModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Objects;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private FirebaseAuth authProfile;
    private ZXingScannerView scannerView;
    private TextView txtResults;
    Button selectBtn, refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Scan QR Code");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //checking the connection with firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        myRef.setValue("Hello, World!");

        selectBtn = (Button) findViewById(R.id.userselectbtn);
        selectBtn.setVisibility(View.GONE);
        refresh = findViewById(R.id.refresh);
        scannerView = findViewById(R.id.zxscan);
        txtResults = findViewById(R.id.txt_result);
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        scannerView.setResultHandler(ScanActivity.this);
                        scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(ScanActivity.this, "You must grant permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        authProfile = FirebaseAuth.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        selectBtn.setOnClickListener(view -> {
            Intent i = new Intent(ScanActivity.this, Ticketing.class);
            String idNo = (String) txtResults.getText();
            i.putExtra("id", idNo);
            startActivity(i);
        });
        refresh.setOnClickListener(view -> {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        scannerView.stopCamera();
        super.onDestroy();
    }

    @Override
    public void handleResult(Result rawResult) {
        //txtResults.setText(rawResult.getText());
        processRawResult(rawResult.getText());

    }

    private void processRawResult(String text) {
        QRCardModel qrvCardModel = new QRCardModel();
        txtResults.setText(text);
        selectBtn.setVisibility(View.VISIBLE);
        scannerView.startCamera();
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
            Intent intent = new Intent(ScanActivity.this, UpdateprofileActivity.class);
            startActivity(intent);
        } /*else if (id == R.id.menu_update_email) {
            Intent intent= new Intent(ScanActivity.this, UpdateemailActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_settings) {
            Intent intent= new Intent(ScanActivity.this, ChangepasswordActivity.class);
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
                                    Intent intent = new Intent(ScanActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(ScanActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            }));
        } else if (id == R.id.logout) {
            authProfile.signOut();
            Toast.makeText(ScanActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ScanActivity.this, MainActivity.class);

            //clear stack to prevent user coming back to userprofile activity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(ScanActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}