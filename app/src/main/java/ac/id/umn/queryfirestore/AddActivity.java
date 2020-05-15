package ac.id.umn.queryfirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Calendar;

public class AddActivity extends AppCompatActivity {
    private static final int REQUEST_DOC = 3;
    private final int CAMERA_REQUEST_CODE = 124;
    private final int GALLERY_REQUEST_CODE = 125;

    private EditText title, desc, mDate;
    private Spinner status;
    private Button btnDoc, btnSubmit, btnCancel;
    DatePickerDialog picker;

    private UserModel user;
    private Uri filepath;
    private File file;
    private String id;
    private int stats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        title = findViewById(R.id.title);
        desc = findViewById(R.id.desc);
        btnDoc = findViewById(R.id.btnDoc);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);
        mDate = findViewById(R.id.mDate);
        status = findViewById(R.id.status);

        String[] items = new String[] {"On-Progress", "Finish", "Postponed", "Cancel"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);
        status.setAdapter(adapter);
        status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                //Log.v("item", (String) parent.getItemAtPosition(position));
                stats = position+1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        mDate.setInputType(InputType.TYPE_NULL);
        mDate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(AddActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String text = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                mDate.setText(text);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        user = getIntent().getParcelableExtra("USER");
        if (getIntent().getAction().equals("EDIT")) {
            MasterModel master = getIntent().getParcelableExtra("MASTER");
            title.setText(master.getTitle());
            desc.setText(master.getDesc());
            mDate.setText(master.getmDate());
            //status.

            if(master.getDoc()) {
                btnDoc.setText(R.string.btnDown);
                btnDoc.setVisibility(View.VISIBLE);
            }
            else
                btnDoc.setText(R.string.btnUp);

            id = getIntent().getStringExtra("id");
        }
        else{
            btnDoc.setText(R.string.btnUp);
            btnDoc.setVisibility(View.VISIBLE);
        }


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (title.getText().toString().trim().isEmpty() || desc.getText().toString().trim().isEmpty() || mDate.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Still blank space", Toast.LENGTH_SHORT).show();
                } else {
                    MasterModel master = new MasterModel(
                            user.getfName(),
                            desc.getText().toString(),
                            mDate.getText().toString(),
                            stats,
                            title.getText().toString(),
                            Timestamp.now(),
                            false
                    );

                    Intent hasil = new Intent();
                    if (getIntent().getAction() == "EDIT") hasil.putExtra("id", id);
                    if(filepath != null && getIntent().getAction().equals("ADD")) {
                        hasil.putExtra("file", filepath.toString());
                        master.setDoc(true);
                    }
                    hasil.putExtra("MASTER", master);

                    setResult(RESULT_OK, hasil);
                    finish();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        btnDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadFile(title.getText().toString());
            }
        });
    }

    private void downloadFile(String title) {
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, GALLERY_REQUEST_CODE);
        if((getIntent().getAction().equals("EDIT") && btnDoc.getText().toString().equals("UPLOAD"))|| getIntent().getAction().equals("ADD")){
            Intent intent = new Intent();
            intent.setType("application/pdf");
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            try {
                startActivityForResult(Intent.createChooser(intent, "Select Doc"), REQUEST_DOC);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(AddActivity.this, "Please Install a File Manager", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://uasmobile-2f57b.appspot.com/doc");
            StorageReference islandRef = storageRef.child(id);

            File rootPath = new File(Environment.getExternalStorageDirectory(), "Download");
            if (!rootPath.exists()) {
                rootPath.mkdirs();
            }

            final File localFile = new File(rootPath,title+".pdf");

            islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.e("Upload Status ", "Success! @" + localFile.toString());
                    //  updateDb(timestamp,localFile.toString(),position);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("Upload Status ", "Error! @" + exception.toString());
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_DOC && resultCode == RESULT_OK){
            filepath = data.getData();
            file = new File(filepath.getPath());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(AddActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(AddActivity.this, new String[] { permission }, requestCode);
        } else {
            Toast.makeText(AddActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(AddActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AddActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == GALLERY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(AddActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AddActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
