package com.example.finalyearprojectapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


public class MainScreen extends AppCompatActivity {

    TextView fullName;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        //getting the textfield from interface
        fullName = findViewById(R.id.UserFullNameText);

        //instantiating firebase objects
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        //get current user information
        userId = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                fullName.setText(documentSnapshot.getString("regName"));
                CalendarInfo.setCurrentUser(documentSnapshot.getString("regName"));

            }
        });

    }


    public void HolidayPageBtn(View view) {
        Intent intent = new Intent(MainScreen.this, HolidayScreen.class);
        startActivity(intent);
        CalendarInfo.setSetEditName("");
        CalendarInfo.setEditButtonStatus(true);
    }

    public void EmergencyPageBtn(View view) {
        Intent intent = new Intent(MainScreen.this, EmergencyActivity.class);
        startActivity(intent);
    }

    public void ViewHolidaysBtn(View view) {
        Intent intent = new Intent(MainScreen.this, ViewHolidays.class);
        startActivity(intent);
    }

    public void HelpMenuBtn(View view) {
        Intent intent = new Intent(MainScreen.this, Rules.class);
        startActivity(intent);
    }

    public void logOut(View view) {
        Intent intent = new Intent(MainScreen.this, MainActivity.class);
        startActivity(intent);
        Toast.makeText(MainScreen.this, "Successfully Logged Out.", Toast.LENGTH_SHORT).show();
    }

}