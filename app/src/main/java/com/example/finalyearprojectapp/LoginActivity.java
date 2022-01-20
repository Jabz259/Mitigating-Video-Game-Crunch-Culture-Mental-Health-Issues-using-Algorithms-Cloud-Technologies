package com.example.finalyearprojectapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class LoginActivity extends AppCompatActivity {
    //BackButton Variable
    ImageView backButton;
    EditText logEmail, logPassword;
    FirebaseAuth fAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MultiDex.install(this);
        //Welcome Login Button Id
        //assigning variable backButton with XML ID
        backButton = findViewById(R.id.backBtn);
        logEmail = findViewById(R.id.loginEmail);
        logPassword = findViewById(R.id.loginPassword);
        fAuth = FirebaseAuth.getInstance();

    }

    //going back button method
    public void backBtn (View v) {
        finish();
    }

    //Accessing the Registration Activity from both Login and Welcome pages
    public void RegisterButton (View V) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void loginUser (View v) {

        MainScreen mc = new MainScreen();

        String email = logEmail.getText().toString().trim();
        String password = logPassword.getText().toString().trim();

        if (email.isEmpty()) {
            logEmail.setError("Please Input an Email");
        } else if (password.isEmpty()) {
            logPassword.setError("Please Input a valid Password");
        } else {
            fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Signed In.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainScreen.class);
                        startActivity(intent);
                        //startActivity(new Intent(getApplicationContext(), MainScreen.class));

                    } else {
                        Toast.makeText(LoginActivity.this, "Sign In Failed. Try Again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }



    }

}