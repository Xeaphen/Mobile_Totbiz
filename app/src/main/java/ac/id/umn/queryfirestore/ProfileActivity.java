package ac.id.umn.queryfirestore;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private final int CAMERA_REQUEST_CODE = 124;
    private final int GALLERY_REQUEST_CODE = 125;

    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private CollectionReference userRef = rootRef.collection("user");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    FirebaseStorage storage;
    StorageReference storageReference;

    private Uri filePath;
    File photoFile;

    TextView fName, lName, bod, addr, password, email, role;
    ImageView btnImage;
    DatePickerDialog picker;
    Button submit;
    UserModel user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user = getIntent().getParcelableExtra("USER");

        btnImage = findViewById(R.id.imgBtn);

        fName = findViewById(R.id.fName);
        lName = findViewById(R.id.lName);
        bod = findViewById(R.id.bod);
        addr = findViewById(R.id.addr);
        email = findViewById(R.id.email);
        password = findViewById(R.id.pass);
        role = findViewById(R.id.role);

        fName.setText(user.getfName());
        lName.setText(user.getlName());
        bod.setText(user.getBod());
        addr.setText(user.getAddress());
        email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        submit = findViewById(R.id.btnSub);

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(Manifest.permission.CAMERA, CAMERA_REQUEST_CODE);
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, GALLERY_REQUEST_CODE);
                selectAction(ProfileActivity.this);
            }
        });

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storage = FirebaseStorage.getInstance().getReference("img/" +uid);
        StorageReference islandRef = storage.child(uid+".png");

        // Load the image using Glide
        Glide.with(this)
                .load(storage)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true) //pathnya
                .into(btnImage); //ditaronya

        bod.setInputType(InputType.TYPE_NULL);
        bod.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(ProfileActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                bod.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });
    }

    private void selectAction(Context context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    takePhoto();

                } else if (options[item].equals("Choose from Gallery")) {
                    chooseImage();

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Photo");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Toast.makeText(ProfileActivity.this, "Error!", Toast.LENGTH_SHORT).show();
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    private void takePhoto() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri("photo.jpg");

        Uri fileProvider = FileProvider.getUriForFile(ProfileActivity.this,
                "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        try {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ProfileActivity.this, "Please Install a File Manager", Toast.LENGTH_SHORT).show();
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ProfileActivity.this, "Please Install a File Manager", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            filePath = Uri.fromFile(photoFile);
            uploadImage();

            try {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                StorageReference storage = FirebaseStorage.getInstance().getReference("img/" +uid);
                StorageReference islandRef = storage.child(uid+".png");

                // Load the image using Glide
                Glide.with(this)
                        .load(storage)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true) //pathnya
                        .into(btnImage); //ditaronya
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        else if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            filePath = data.getData();
            uploadImage();

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storage = FirebaseStorage.getInstance().getReference("img/" +uid);
            StorageReference islandRef = storage.child(uid+".png");

            // Load the image using Glide
            Glide.with(this)
                    .load(storage)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true) //pathnya
                    .into(btnImage); //ditaronya

        }
        else { // Result was a failure
            Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://uasmobile-2f57b.appspot.com/img");
            StorageReference islandRef = storageRef.child(uid);

            islandRef.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(ProfileActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[] { permission }, requestCode);
        } else {
            Toast.makeText(ProfileActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(ProfileActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == GALLERY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(ProfileActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
