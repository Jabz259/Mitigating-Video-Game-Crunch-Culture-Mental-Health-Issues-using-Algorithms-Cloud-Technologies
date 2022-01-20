package com.example.finalyearprojectapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EmergencyActivity extends AppCompatActivity {

    TextView emergencyDesc;
    EditText emergencyTitle;
    EditText emergencyReason;
    Button storeDetailsBtn;
    TextView emergencyDate;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        //locate all objects
        emergencyTitle = findViewById(R.id.emergencyNameField);
        emergencyReason = findViewById(R.id.emergencyReasonField);
        storeDetailsBtn = findViewById(R.id.emergencyBtn);
        emergencyDate = findViewById(R.id.emergencyDate);
        emergencyDesc = findViewById(R.id.emergencyDescription);

        //setting current date
        final Calendar calendar = Calendar.getInstance();
        final int currentDayofMonth = calendar.get(Calendar.DAY_OF_MONTH) + 1;
        final int getCurrentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        final int getCurrentMonth = calendar.get(Calendar.MONTH);
        final int getCurrentYear = calendar.get(Calendar.YEAR);
        emergencyDate.setText( "Book for: "+ currentDayofMonth + "/" + getCurrentMonth + "/" + getCurrentYear);

        storeDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //retrieve all input
                String emTitle = emergencyTitle.getText().toString().trim();
                String emReason = emergencyReason.getText().toString().trim();
                String emDate = emergencyDate.getText().toString().trim();

                ///basic validation
                if (emTitle.equals("") || emTitle.length() < 5) {
                    emergencyTitle.setError("Please enter a valid title.");
                } else if (emReason.equals("") || emReason.length() < 50) {
                    emergencyReason.setError("Please enter a reason longer than 50 characters, we need more information.");
                } else if (emDate.isEmpty()) {
                    emergencyDate.setText("Problem with date selection, try again later.");
                } else {
                    //weekend validation
                           if (getCurrentDayOfWeek == Calendar.SATURDAY) {
                               emergencyDesc.setError("Cannot book emergency on a friday. Contact Management for further details.");
                           } else if ( getCurrentDayOfWeek == Calendar.FRIDAY) {
                               emergencyDesc.setError("Cannot book emergency on a friday. Contact Management for further details.");
                           } else {
                               //reset the fields and store input
                               String emergencyFullDate = currentDayofMonth + "/" + getCurrentMonth + "/" + getCurrentYear;
                               storeEmergencyDates(emTitle,emReason,emergencyFullDate);
                               emergencyTitle.setText("");
                               emergencyReason.setText("");
                           }
                }

            }
        });

    }

    public void storeEmergencyDates (final String emTitle, final String emReason, final String emDate) {

        //creating a userID string to store username accompanied with date information
        final String userID;
        userID = fAuth.getCurrentUser().getUid();
        //this will retrieve information from users document from cloud server
        final DocumentReference emUser = fStore.collection("users").document(userID);
        //this will store data into holidays document
        final DocumentReference documentReference = fStore.collection("emergencyRequests").document();

        //so add the following details according to the user
        emUser.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                //retrieving field username for users
                String name = documentSnapshot.getString("regName");

                //hashmap for storing details
                Map<String, Object> emRequest = new HashMap<>();
                emRequest.put("username", name);
                emRequest.put("emergencyTitle", emTitle);
                emRequest.put("emergencyReason", emReason);
                emRequest.put("emergencyDate", emDate);
                emRequest.put("approvalStatus", "awaiting");

                documentReference.set(emRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EmergencyActivity.this, "Booked successfully", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

}