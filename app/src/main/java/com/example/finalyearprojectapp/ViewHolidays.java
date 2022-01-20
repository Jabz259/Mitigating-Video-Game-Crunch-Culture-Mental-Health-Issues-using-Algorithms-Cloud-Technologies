package com.example.finalyearprojectapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ViewHolidays extends AppCompatActivity {

    ListView holidayListView;
    EditText enterHolidayNameField;
    Button deleteHolidayBtn;
    Button editSelectedHolidayBtn;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_holidays);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        holidayListView = findViewById(R.id.holidayCollection);
        enterHolidayNameField =  findViewById(R.id.holidayNameDeleteField);
        deleteHolidayBtn = findViewById(R.id.holidayDeleteBtn);
        editSelectedHolidayBtn = findViewById(R.id.editHolidayBtn);


        final String userId = fAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final ArrayList<String> listData = new ArrayList<>();
        final ListAdapter listadapter = new ArrayAdapter<>(this, R.layout.row);
        holidayListView.setAdapter(listadapter);

        editSelectedHolidayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fieldName = enterHolidayNameField.getText().toString().trim();
                boolean flagger = true;

                //For loop
                for (int i = 0; i < listData.size(); i++) {

                    if (fieldName.equals(listData.get(i))) {
                        flagger = false;
                    }

                }

                //Validation
                if (fieldName.isEmpty()) {
                    enterHolidayNameField.setError("Please input a name from the list. If it is incorrect then it will not set a edit/delete procedure.");
                } else if (!flagger) {
                    Intent intent = new Intent(ViewHolidays.this, HolidayScreen.class);
                    startActivity(intent);
                    CalendarInfo.setSetEditName(fieldName);
                    CalendarInfo.setEditButtonStatus(false);

                } else {
                    enterHolidayNameField.setError("Name Does not exist");
                }

            }
        });


        deleteHolidayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String holidayName = enterHolidayNameField.getText().toString().trim();
                FirebaseFirestore dbInstance = FirebaseFirestore.getInstance();

                String docID = userId + holidayName;
                DocumentReference deleteNoteRef = dbInstance.collection("holidays").document(docID);

                String fieldName = enterHolidayNameField.getText().toString().trim();
                boolean flagger = true;

                //For loop
                for (int i = 0; i < listData.size(); i++) {

                    if (fieldName.equals(listData.get(i))) {
                        flagger = false;
                    }

                }

                //Validation
                if (fieldName.isEmpty()) {
                    enterHolidayNameField.setError("Please input a name from the list. If it is incorrect then it will not set a edit/delete procedure.");
                } else if (!flagger) {

                    deleteNoteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ViewHolidays.this, "Successfully Deleted",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ViewHolidays.this, "Could not delete",Toast.LENGTH_SHORT).show();

                        }
                    });

                } else {
                    enterHolidayNameField.setError("Name Does not exist");
                }





            }
        });

        DocumentReference documentReference = fStore.collection("users").document(userId);

        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String username = (documentSnapshot.getString("regName"));
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                final Query query = db.collection("holidays").whereEqualTo("username", username);
                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Map<String, Object> data = new HashMap<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Note note = document.toObject(Note.class);
                            listData.add(document.getString("holidayTitle"));
                            System.out.println(document.getString("holidayTitle"));
                        }
                        ((ArrayAdapter) listadapter).addAll(listData);
                    }
                });
            }
        });






    }

}