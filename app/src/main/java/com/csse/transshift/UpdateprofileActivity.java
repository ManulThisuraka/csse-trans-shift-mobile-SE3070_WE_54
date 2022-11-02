package com.csse.transshift;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class UpdateprofileActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView imageView;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateprofile);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Upload Profile Picture");

        Button buttonChoose = findViewById(R.id.btn_choose);
        Button buttonUpload = findViewById(R.id.btn_upload);
        progressBar = findViewById(R.id.progressUP);
        imageView = findViewById(R.id.imageview_uploaded);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");

        Uri uri = firebaseUser.getPhotoUrl();

        //set user's current dp in imageview (if uploaded already. we will picasso since imageviewer setimage
        //regular URIs
        Picasso.get().load(uri).into(imageView);

        buttonChoose.setOnClickListener(view -> openFileChooser());

        buttonUpload.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            UploadPic();
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();
            imageView.setImageURI(uriImage);
        }
    }

    private void UploadPic() {
        if (uriImage != null) {
            //save the image with uid of the currently logged user
            StorageReference fileReference = storageReference.child(Objects.requireNonNull(authProfile.getCurrentUser()).getUid() + "."
                    + getFileExtension(uriImage));

            //upload image to storage
            fileReference.putFile(uriImage).addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                firebaseUser = authProfile.getCurrentUser();

                //Finally set the display image of the user after upload
                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(uri).build();
                firebaseUser.updateProfile(userProfileChangeRequest);
            }));

            progressBar.setVisibility(View.GONE);
            Toast.makeText(UpdateprofileActivity.this, "Upload successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UpdateprofileActivity.this, UserprofileActivity.class);
            startActivity(intent);
            finish();
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(UpdateprofileActivity.this, "No file selected!", Toast.LENGTH_SHORT).show();
        }
    }

    //obtain file extension of the image
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
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
            Intent intent = new Intent(UpdateprofileActivity.this, UpdateprofileActivity.class);
            startActivity(intent);
        } /*else if (id == R.id.menu_update_email) {
            Intent intent= new Intent(UpdateprofileActivity.this, UpdateemailActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_settings) {
            Intent intent= new Intent(UpdateprofileActivity.this, ChangepasswordActivity.class);
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
                                    Intent intent = new Intent(UpdateprofileActivity.this, MainActivity.class);

                                    startActivity(intent);
                                } else {
                                    Toast.makeText(UpdateprofileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            }));
        } else if (id == R.id.logout) {
            authProfile.signOut();
            Toast.makeText(UpdateprofileActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UpdateprofileActivity.this, MainActivity.class);

            //clear stack to prevent user coming back to userprofile activity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UpdateprofileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}