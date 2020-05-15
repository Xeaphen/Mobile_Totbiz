package ac.id.umn.queryfirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText email1, password1;
    private String email,password;

    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            updateUI(firebaseUser);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(authListener);

        email1 = findViewById(R.id.username);
        password1 = findViewById(R.id.password);
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        updateUI(user);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authListener);
    }

    public void updateUI(FirebaseUser account){
        if(account != null){
            Toast.makeText(this,"U Signed In successfully",Toast.LENGTH_LONG).show();
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }else {
            Toast.makeText(this,"U Didnt signed in",Toast.LENGTH_LONG).show();
        }
    }

    public void masukLogin(View view) {
        email = email1.getText().toString();
        password = password1.getText().toString();

        if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Email atau password salah",Toast.LENGTH_SHORT).show();
        }
        else{
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    }
                }
            });
        }
    }

    public void daftar(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        finish();
    }
}
