package com.example.finalyearprojectapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText regName, regEmail, regPassword, regRetypePassword;
    TextView errorHint;
    Button registerButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //assigning register button with XML ID
        registerButton = findViewById(R.id.registerBtn);
        regName = findViewById(R.id.registerName);
        regEmail = findViewById(R.id.registerEmail);
        regPassword = findViewById(R.id.registerPassword);
        regRetypePassword = findViewById(R.id.retypePassword);
        errorHint = findViewById((R.id.errorHint));

        //creating instance for firebase to store information
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        //If the current user is already stored into the cloud then just bring them backk
        //to the main login screen
//        if (fAuth.getCurrentUser() != null){
//            startActivity (new Intent(getApplicationContext(), LoginActivity.class));
//        }

    }
    
    public void registerUser (View v) {
        final String name = regName.getText().toString().trim();
        final String email = regEmail.getText().toString().trim();
        String password = regPassword.getText().toString().trim();
        String validPassword = regRetypePassword.getText().toString().trim();

        //If email field is left empty or too short, then show 2 errors
        if (name.isEmpty() || name.length() < 2) {
            regName.setError("");
            errorHint.setText("Input a Valid Name");
            return;
        }

        if (email.isEmpty() || email.length() < 5) {
            //Exclamation mark symbol on required field
            regEmail.setError("");
            //errorHint will display error above fields
            errorHint.setText("Valid Email is Required.");
            return;
        }

        if (password.isEmpty()) {
            //Exclamation mark symbol on password required field
            regPassword.setError("");
            //errorHint will display error above fields
            errorHint.setText("Password is Required.");
            return;

        } else if (validPassword.isEmpty()) {
            //Exclamation mark symbol on retype password required field
            regRetypePassword.setError("");
            //errorHint will display error above fields
            errorHint.setText("Please Re-Type Password.");
            return;
        }

        if (password.length() < 6) {
            //Exclamation mark symbol on required password field
            regPassword.setError("");
            //errorHint will display error above fields
            errorHint.setText("Password Must be 6 Characters or more.");
            return;
        }

        if (password.equals(validPassword)) {
            fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        Toast.makeText(RegisterActivity.this, "Registration Complete", Toast.LENGTH_SHORT).show();

                        userID = fAuth.getCurrentUser().getUid();
                        DocumentReference documentReference = fStore.collection("users").document(userID);
                        Map<String, Object> user = new HashMap<>();

                        long department = 130;

                        user.put("regName",name);
                        user.put("regEmail",email);
                        //user.put("Department", 130);

                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "OnSuccess: User Profile is created for " + userID);

                            }
                        });


                        //This line of code needs to be changed when we make our next activity
                        //Once we login, it will take us back to the login page.
                        startActivity (new Intent(getApplicationContext(), LoginActivity.class));

                    } else if (fAuth.getCurrentUser() != null) {
                        //Need to figure out what is meant but
                        //Authentication Validation with email, verifying whether email has been
                        //used or not previously

                        //Toast class used to display validation
                        Toast.makeText(RegisterActivity.this, "Email has already been taken.", Toast.LENGTH_SHORT).show();
                        //Extra hint for users on what they can do next
                        errorHint.setText("Email Taken, Try Another.");
                        return;

                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed, please try again.", Toast.LENGTH_SHORT).show();
                        //Log.d(TAG, "onComplete: ");
                        return;
                    }
                }
            });

        } else {
            //if both passwords do not match then error
            regPassword.setError("");
            regRetypePassword.setError("");
            errorHint.setText("Passwords did not Match. Try Again.");

        }
    }


    //Go back to previous page
    public void backBtn (View v) {
        //close off current activity
        finish();
    }



        }
