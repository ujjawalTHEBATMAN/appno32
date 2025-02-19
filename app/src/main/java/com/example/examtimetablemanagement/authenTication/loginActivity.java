package com.example.examtimetablemanagement.authenTication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.examtimetablemanagement.DashboardActivity.DashboardActivity;
import com.example.examtimetablemanagement.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class loginActivity extends AppCompatActivity {
    private EditText editTextUserName;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private FirebaseAuth firebaseAuthInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuthInstance = FirebaseAuth.getInstance();
        editTextUserName = findViewById(R.id.etEmail);
        editTextPassword = findViewById(R.id.etPassword);
        buttonLogin = findViewById(R.id.btnLogin);
        textViewRegister = findViewById(R.id.tvRegister);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userNameInput = editTextUserName.getText().toString().trim();
                String userPasswordInput = editTextPassword.getText().toString().trim();
                if (TextUtils.isEmpty(userNameInput) || TextUtils.isEmpty(userPasswordInput)) {
                    Toast.makeText(loginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                DatabaseReference userInformationReference = FirebaseDatabase.getInstance().getReference("userInformation");
                Query usernameQuery = userInformationReference.orderByChild("username").equalTo(userNameInput);
                usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String retrievedEmail = null;
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                retrievedEmail = userSnapshot.child("email").getValue(String.class);
                                break;
                            }
                            if (retrievedEmail != null) {
                                firebaseAuthInstance.signInWithEmailAndPassword(retrievedEmail, userPasswordInput)
                                        .addOnCompleteListener(loginActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> signInTask) {
                                                if (signInTask.isSuccessful()) {
                                                    startActivity(new Intent(loginActivity.this, DashboardActivity.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(loginActivity.this, "Username or password is incorrect", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(loginActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(loginActivity.this, "User information not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(loginActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(loginActivity.this, RegistrationActivity.class));
            }
        });
    }
}
