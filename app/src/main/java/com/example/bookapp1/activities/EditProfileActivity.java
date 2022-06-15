package com.example.bookapp1.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookapp1.R;
import com.example.bookapp1.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    // view binding
    private ActivityEditProfileBinding binding;

    // create firebase auth to get/update user's data using uid
    private FirebaseAuth firebaseAuth;

    // progress dialog
    private ProgressDialog progressDialog;

    private static final String TAG = "PROFILE_EDIT_TAG";

    private Uri imageUri = null;

    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // setup firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        // handle click => go back
        binding.backIBtn8ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onBackPressed();
                            }
                        }
                );
        // handle click => pick image
        binding.profileSIV1ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showImageAttachView();
                            }
                        }
                );
        // handle click => update data
        binding.updateProfileBtnID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                validatingData();
                            }
                        }
                );
        // handle click => go back
        binding.backIBtn8ID.setOnClickListener
                (
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onBackPressed();
                            }
                        }
                );
    }

    private void validatingData() {
        // get data
        name = binding.nameET1ID.getText().toString().trim();

        // validating data
        if (TextUtils.isEmpty(name))
        {
            // name is not entered
            Toast.makeText(this, "Name is required.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // name is entered
            if (imageUri == null)
            {
                // updating profile without image
                updateProfile("");
            }
            else
            {
                // updating profile with image
                uploadImage();
            }
        }
    }

    private void updateProfile(String imageUrl) {
        Log.d(TAG, "updateProfile: Updating user profile...");
        progressDialog.setMessage("Updating user profile...");
        progressDialog.show();

        // setup data to update in the database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "" + name);
        if (imageUri != null)
        {hashMap.put("profileImage", "" + imageUrl);}

        // update data to the database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Profile uploaded successfully!");
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Profile uploaded successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to update database due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Failed to update database due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImage() {
        Log.d(TAG, "uploadImage: Uploading profile image...");
        progressDialog.setMessage("Updating profile image...");
        progressDialog.show();

        // image path and name, use uid to replace previous
        String filePathAndName = "ProfileImages/" + firebaseAuth.getUid();

        // storage reference
        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imageUri)
                .addOnSuccessListener
                        (
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Log.d(TAG, "onSuccess: Profile image uploaded successfully!");
                                        Log.d(TAG, "onSuccess: Getting url of uploaded image...");
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful());
                                        String uploadedImageUrl = "" + uriTask.getResult();

                                        Log.d(TAG, "onSuccess: Uploaded image URL: " + uploadedImageUrl);

                                        updateProfile(uploadedImageUrl);
                                    }
                                }
                        )
                .addOnFailureListener
                        (
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to upload image due to " + e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProfileActivity.this, "Failed to upload image due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
    }

    private void showImageAttachView() {
        // initializing/ setting up popup menu
        PopupMenu popupMenu = new PopupMenu(this, binding.profileSIV1ID);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Gallery");

        popupMenu.show();

        // handle clicking menu item
        popupMenu.setOnMenuItemClickListener
                (
                        new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                // get id of clicked item
                                int which = menuItem.getItemId();
                                if (which == 0)
                                {
                                    // if camera is clicked
                                    pickImageCamera();
                                }
                                else if (which == 1)
                                {
                                    // if gallery is clicked
                                    pickImageGallery();
                                }
                                return false;
                            }
                        }
                );
    }

    private void pickImageGallery() {
        // create an intent to pick image from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void pickImageCamera() {
        // create an intent to pick image from camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New pick");  // image title
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample image description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher
            = registerForActivityResult
            (
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            // this is used to handle result of camera intents
                            // get image's uri
                            if (result.getResultCode() == Activity.RESULT_OK)
                            {
                                Log.d(TAG, "onActivityResult: " + imageUri);
                                Intent data = result.getData();     // this isn't necessary as imageUri variable has got image already
                                binding.profileSIV1ID.setImageURI(imageUri);
                            }
                            else
                            {
                                Toast.makeText(EditProfileActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher
            = registerForActivityResult
            (
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            // get image's uri
                            if (result.getResultCode() == Activity.RESULT_OK)
                            {
                                Log.d(TAG, "onActivityResult: " + imageUri);
                                Intent data = result.getData();
                                imageUri = data.getData();
                                Log.d(TAG, "onActivityResult: Picked from gallery: " + imageUri);
                                binding.profileSIV1ID.setImageURI(imageUri);
                            }
                            else
                            {
                                Toast.makeText(EditProfileActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading user information of user " + firebaseAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener
                        (
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // get all info of selected user from snapshot
                                        String name = "" + snapshot.child("name").getValue(),
                                                email = "" + snapshot.child("email").getValue(),
                                                profileImage = "" + snapshot.child("profileImage").getValue(),
                                                timestamp = "" + snapshot.child("timestamp").getValue(),
                                                uid = "" + snapshot.child("uid").getValue(),
                                                userType = "" + snapshot.child("userType").getValue();

                                        // set data to UI
                                        binding.nameET1ID.setText(name);

                                        // setting image using glide
                                        Glide.with(EditProfileActivity.this)
                                                .load(profileImage)
                                                .placeholder(R.drawable.ic_person_gray)
                                                .into(binding.profileSIV1ID);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
    }
}