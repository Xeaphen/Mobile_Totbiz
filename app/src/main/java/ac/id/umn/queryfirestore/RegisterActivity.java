package ac.id.umn.queryfirestore;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    private final int CAMERA_REQUEST_CODE = 124;
    private final int GALLERY_REQUEST_CODE = 125;

    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private CollectionReference userRef = rootRef.collection("user");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseStorage storage;
    StorageReference storageReference;

    private Uri filePath;
    File photoFile;

    EditText fName, lName, bod, addr, password, email;
    ImageView imageView;
    DatePickerDialog picker;

    Spinner role;
    Button pic, submit;
    UserModel user;
    int choosenRole;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        imageView = findViewById(R.id.imgView);

        fName = findViewById(R.id.fName);
        lName = findViewById(R.id.lName);
        bod = findViewById(R.id.bod);
        addr = findViewById(R.id.addr);
        email = findViewById(R.id.email);
        password = findViewById(R.id.pass);
        role = findViewById(R.id.role);
        pic = findViewById(R.id.btnPic);
        submit = findViewById(R.id.btnSub);


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        String[] items = new String[] { "Associate" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);
        role.setAdapter(adapter);
        role.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                //Log.v("item", (String) parent.getItemAtPosition(position));
                choosenRole = 5;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        bod.setInputType(InputType.TYPE_NULL);
        bod.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(RegisterActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                bod.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String em = email.getText().toString();
                String pass = password.getText().toString();

                if(em.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Email atau password harus diisi",Toast.LENGTH_SHORT).show();
                }
                else {
                    mAuth.createUserWithEmailAndPassword(em, pass).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String id = mAuth.getCurrentUser().getUid();
                                user = new UserModel(
                                        fName.getText().toString(),
                                        lName.getText().toString(),
                                        addr.getText().toString(),
                                        bod.getText().toString(),
                                        choosenRole
                                );
                                userRef.document(id).set(user);
                                uploadImage();
                                //                    FirebaseUser user = mAuth.getCurrentUser();
                                //                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                //                    startActivity(intent);
                                Toast.makeText(RegisterActivity.this, "Register Success!",Toast.LENGTH_LONG).show();
                                email.setText("");
                                password.setText("");
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            } else
                                Toast.makeText(RegisterActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        pic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                checkPermission(Manifest.permission.CAMERA, CAMERA_REQUEST_CODE);
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, GALLERY_REQUEST_CODE);
                selectAction(RegisterActivity.this);
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
            Toast.makeText(RegisterActivity.this, "Fail", Toast.LENGTH_SHORT).show();
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    private void takePhoto() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri("photo.jpg");

        Uri fileProvider = FileProvider.getUriForFile(RegisterActivity.this,
                "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        try {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(RegisterActivity.this, "Please Install a File Manager", Toast.LENGTH_SHORT).show();
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(RegisterActivity.this, "Please Install a File Manager", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            filePath = Uri.fromFile(photoFile);
            Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            imageView.setImageBitmap(takenImage);
        }
        else if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e){ e.printStackTrace(); }
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
            StorageReference ref = storageReference.child("img/" + uid);

            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(RegisterActivity.this, new String[] { permission }, requestCode);
        } else {
            Toast.makeText(RegisterActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(RegisterActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == GALLERY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(RegisterActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
