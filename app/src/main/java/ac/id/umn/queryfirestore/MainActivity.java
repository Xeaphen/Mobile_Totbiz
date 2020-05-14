package ac.id.umn.queryfirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class MainActivity extends AppCompatActivity {
    private static final String AUTHOR_KEY = "author";
    private static final String DESC_KEY = "desc";
    private static final String DOC_KEY = "doc";
    private static final String MDATE_KEY = "mDate";
    private static final String STATUS_KEY = "status";
    private static final String TITLE_KEY = "title";
    private static final String UDATE_KEY = "uDate";

    private static final String ACTION_ADD = "ADD";
    private static final String ACTION_EDIT = "EDIT";

    private static final int REQUEST_ADD = 1;
    private static final int REQUEST_EDIT = 2;

    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private CollectionReference productsRef = rootRef.collection("master");
    FirebaseStorage storage;
    StorageReference storageReference;
    private MasterAdapter adapter;
    private UserModel user;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                updateUI(firebaseUser);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth.addAuthStateListener(authListener);
        setRecyclerView();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("UID", uid);
        DocumentReference userDoc = FirebaseFirestore.getInstance().collection("user").document(uid);
        userDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(UserModel.class);

                Toast.makeText(MainActivity.this, "GET USER " + user.getfName() , Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setRecyclerView(){
        Query query = productsRef;

        FirestoreRecyclerOptions<MasterModel> options = new FirestoreRecyclerOptions.Builder<MasterModel>()
                .setQuery(query, MasterModel.class)
                .build();

        adapter = new MasterAdapter(options);

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                final CharSequence[] options = {"Yes", "No"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Are you sure ?");

                builder.setItems(options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Yes")) {
                            adapter.deleteItem(viewHolder.getAdapterPosition());
                        } else if (options[item].equals("No")) {
                            Toast.makeText(MainActivity.this,"Delete canceled", Toast.LENGTH_SHORT).show();
                            adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        }
                    }
                });
                builder.show();
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new MasterAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                MasterModel product = documentSnapshot.toObject(MasterModel.class);
                String id = documentSnapshot.getId();

                assert product != null;
                updateIntent(id, product);

                Toast.makeText(getApplicationContext(),"Position : " + position + " ID : " + id + " " + product.getStatus(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        updateUI(user);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        mAuth.removeAuthStateListener(authListener);
    }

    public void updateUI(FirebaseUser account){
        if(account == null){
            user = null;
            Toast.makeText(this,"U Didnt signed in",Toast.LENGTH_LONG).show();
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }else {
            Toast.makeText(this,"Signed in success",Toast.LENGTH_LONG).show();
        }
    }

    public void pindahUpload(View view) {
        Intent i = new Intent(MainActivity.this, UploadTest.class);
        startActivity(i);
    }

    public void addIntent(View view){
        Intent add = new Intent(MainActivity.this,AddActivity.class);
        add.setAction(ACTION_ADD);
        add.putExtra("USER", this.user);
        startActivityForResult(add, REQUEST_ADD);
    }

    public void updateIntent(String id, MasterModel master){
        Intent up = new Intent(MainActivity.this, AddActivity.class);

        up.setAction(ACTION_EDIT);
        up.putExtra("id", id);
        up.putExtra("MASTER", master);
        up.putExtra("USER", user);

        startActivityForResult(up, REQUEST_EDIT);
    }

    @SuppressLint("ShowToast")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ADD){ //add
            if(resultCode == RESULT_OK){
                assert data != null;
                MasterModel master = data.getParcelableExtra("MASTER");
                Uri file = null;
                if(data.getStringExtra("file") != null)
                    file = Uri.parse(data.getStringExtra("file"));
                addNewMaster(master,file);
                Toast.makeText(getApplicationContext(),"Item Registered {  }", Toast.LENGTH_SHORT);
            }
        }
        if(requestCode == REQUEST_EDIT) { //update
            if(resultCode == RESULT_OK){
                assert data != null;

                String id = data.getStringExtra("id");
                MasterModel master = (MasterModel) data.getParcelableExtra("MASTER");

                updateMaster(id, master);
                Toast.makeText(getApplicationContext(),"Item Registered",Toast.LENGTH_SHORT);
            }
        }
    }

    private void uploadFile(Uri filePath, String id){
        if(filePath != null)
        {
            Toast.makeText(getApplicationContext(), filePath.toString(), Toast.LENGTH_SHORT).show();
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference ref = storageReference.child("doc/"+id);

            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Failed " +e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                            .getTotalByteCount());
                    progressDialog.setMessage("Uploaded "+ (int)progress +"%");
                }
            });
        }
    }

    private void updateMaster(String id, MasterModel product){
        productsRef.document(id).update(
                AUTHOR_KEY, product.getAuthor(),
                DESC_KEY, product.getDesc(),
                DOC_KEY, product.getDoc(),
                MDATE_KEY, product.getmDate(),
                STATUS_KEY, product.getStatus(),
                TITLE_KEY, product.getTitle(),
                UDATE_KEY, product.getuDate()
        );
    }

    private void addNewMaster(MasterModel product, final Uri filePath) {
        productsRef.add(product).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                if(filePath != null) {
                    String docName = documentReference.getId();
                    uploadFile(filePath, docName);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.myProfile:
                Intent profile = new Intent(MainActivity.this, ProfileActivity.class);
                profile.putExtra("USER", user);
                startActivity(profile);
                return true;

            case R.id.logout:
                mAuth.signOut();
                updateUI(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
