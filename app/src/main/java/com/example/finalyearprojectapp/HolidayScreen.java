package com.example.finalyearprojectapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HolidayScreen extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    //initializing objects
    public static final String TAG = "TAG";
    boolean testingBool;
    boolean dateStatus = false;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    EditText holidayTitle;
    TextView displayText;
    TextView startDateTxt;
    TextView endDateTxt;
    CheckBox annualHolidayBox;
    CheckBox bankHolidayBox;
    Button bookButton;
    Button confirmationButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holiday_screen);
        //locating the following objects
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        holidayTitle = findViewById(R.id.holidayName);
        displayText = findViewById(R.id.generalText);
        startDateTxt = findViewById(R.id.startDateText);
        endDateTxt = findViewById(R.id.endDateText);
        annualHolidayBox = findViewById(R.id.annualCheckbox);

        //Set the holiday name based on the status of other activties
        holidayTitle.setText(CalendarInfo.getSetEditName());
        holidayTitle.setEnabled(CalendarInfo.isEditButtonStatus());

        final DocumentReference documentReference = fStore.collection("holidays").document();

       //Setting listener for start date button for picking date
        Button startButton = (Button) findViewById(R.id.startDateButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateStatus = true;
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "start date picker");

            }
        });

        //Setting listener for start date button for picking date
        Button endButton = (Button) findViewById(R.id.endDateButton);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateStatus = false;
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "end date picker");
            }
        });

        //Setting listener for storing both dates into the cloud
        bookButton = (Button) findViewById(R.id.bookButton);
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String holidayName = holidayTitle.getText().toString().trim();

                CalendarInfo ci = new CalendarInfo();
                CalendarValidation cv = new CalendarValidation();
                //store start dates fields individually
                final String startYearData = ci.getStartDateYear();
                final String startMonthData = ci.getStartDateMonth();
                final String startDayData = ci.getStartDateDay();
                final String startWeekData = ci.getStartDateWeek();
                final String totalDateCode = startYearData + startMonthData + startDayData + startWeekData;

                //store end dates fields individually
                final String endYearData = ci.getEndDateYear();
                final String endMonthData = ci.getEndDateMonth();
                final String endDayData = ci.getEndDateDay();
                final String endWeekData = ci.getEndDateWeek();
                final String endTotalDateCode = endYearData + endMonthData + endDayData + endWeekData;
                final String fullDate = totalDateCode + endTotalDateCode;

                //storeDates (startYearData, startMonthData, startDayData, startWeekData, totalDateCode, endYearData, endMonthData, endDayData, endWeekData, endTotalDateCode);
                //duplicateHolidays();

                String verifyStartInput = startDateTxt.getText().toString().trim();
                String verifyEndInput = endDateTxt.getText().toString().trim();
                String verifyTitle = holidayTitle.getText().toString().trim();

                //basic validation for the user interface
                if (annualHolidayBox.getText().toString().trim().equals("Available Annual Holidays: 0")) {
                    displayText.setText("You have reached an allowance of 0 please contact: RedemptionStudios@studio.com");

                } else if (verifyTitle.isEmpty() || verifyTitle.length() < 5){
                    displayText.setText("Please input a valid holiday title that is more than 4 characters.");

                } else if (verifyStartInput.equals("Set a Start Date.")) {
                    displayText.setText("Please input a valid start date.");

                } else if (verifyEndInput.equals("Set a End Date.")) {
                    displayText.setText("Please input a valid end date.");

                } else if ((verifyStartInput.equals("Set a Start Date.")) && (verifyEndInput.equals("Set a End Date."))) {
                    displayText.setText("No dates have been set.");

                } else if ((startWeekData.equals("Saturday")) || (startWeekData.equals("Sunday"))) {
                    displayText.setText("Cannot Start on Weekends.");

                } else if (endWeekData.equals("Saturday") || startWeekData.equals("Saturday")) {
                    displayText.setText("Cannot end on Weekends.");

                } else if ((!annualHolidayBox.isChecked())) {
                    displayText.setText("Please confirm the the annual holiday allowance by checking the box.");

                } else {
                        if (!currentDateVerification()) {
                        } else {
                            verifyValidations();
                        }
                }
            }
        });

        //calculatte the current allowance upon entering the activity
        currentAllowance();

    }


    public void updateAnnualDaysCalc(final Long Data) {
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        //get current user information
        final String userD = fAuth.getCurrentUser().getUid();


        final DocumentReference docRef = fStore.collection("users").document(userD);

        //this will store data into holidays document

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    if (doc.exists()) {
                        //read oldCount here from document.getData()
                        Long result = (Long) doc.get("annualHoliday");

                        if (result > 0) {

                            long newResult = result - Data;
                            Map<String, Object> docData = new HashMap<>();
                            docData.put("annualHoliday", newResult);

                           docRef.update(docData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                        } else {
                    displayText.setText("Error with calculating your days.");
                }

            }
        });
    }


    public long countDays () {

        CalendarInfo ci = new CalendarInfo();

        //calling start day input
        final String startYearData = ci.getStartDateYear();
        final String startMonthData = ci.getStartDateMonth();
        final String startDayData = ci.getStartDateDay();
        //converting start day input into integer
        int setStartDayDate = Integer.parseInt(startDayData);
        int setStartMonthDate = Integer.parseInt(startMonthData);
        int setYearDate = Integer.parseInt(startYearData);

        //setting the start
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, setStartDayDate);
        calendar.set(Calendar.MONTH, setStartMonthDate);
        calendar.set(Calendar.YEAR, setYearDate);
        Date startDates = calendar.getTime();

        //calling end day input
        final String endMonthData = ci.getEndDateMonth();
        final String endDayData = ci.getEndDateDay();
        //converting end day input into integer
        int setEndDayDate = Integer.parseInt(endDayData);
        int setEndMonthDate = Integer.parseInt(endMonthData);

        //setting the ending
        calendar.set(Calendar.DAY_OF_MONTH, setEndDayDate);
        calendar.set(Calendar.MONTH,setEndMonthDate);
        Date endDates = calendar.getTime();

        //
        calendar.setTime(startDates);
        Date dateIterate = null;

        //counters
        int dayCounter = 0;

        //store current times before the end date
        //if the current dateIterate reaches the same time then stop loop
        while ((dateIterate = calendar.getTime()).before(endDates) || dateIterate.equals(endDates)) {
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if (day != Calendar.SATURDAY && day != Calendar.SUNDAY) {
                dayCounter++;
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        //return value
        return dayCounter;

    }


    public boolean currentDateVerification () {
        CalendarInfo ci = new CalendarInfo();
        CalendarValidation cv = new CalendarValidation();
        //store start dates fields individually
        final String startYearData = ci.getStartDateYear();
        final String startMonthData = ci.getStartDateMonth();
        final String startDayData = ci.getStartDateDay();
        final String endMonthData = ci.getEndDateMonth();
        final String endDayData = ci.getEndDateDay();


        //get current day, month and year to compare
        int startD = Integer.parseInt(startDayData);
        int startM = Integer.parseInt(startMonthData);
        int startY = Integer.parseInt(startYearData);
        int endD = Integer.parseInt(endDayData);
        int endM = Integer.parseInt(endMonthData);

        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get((Calendar.DAY_OF_MONTH));
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        //is the chose day before the current day of the month?
       if (startM != endM) {
           displayText.setText("Cross Month Booking is not implmeneted");
           return false;

       } else if (startD > endD && endD < startD) {
            displayText.setText("Invalid Date.");
            return  false;

        } else if ((startM < currentMonth) || (endM < currentMonth)) {
            displayText.setText("Invalid Date.");
            return false;

        }  else if (((startD < currentDay) || (startD == currentDay)) && (startM == currentMonth)){
            displayText.setText("Invalid Date.");
            return false;

        }else if (((endD < currentDay) || (endD == currentDay)) && (endM == currentMonth)){
            displayText.setText("Invalid End.");
            return false;

        }else if (startY < currentYear) {
            displayText.setText("Invalid Year.");
            return false;

        } else {
            return true;
        }
    }
    

    public void verifyValidations() {

        //boolean sameDate = CalendarValidation.isSameDateFlag();
        CalendarInfo ci = new CalendarInfo();
        //store start dates fields individually
        final String startYearData = ci.getStartDateYear();
        final String startMonthData = ci.getStartDateMonth();
        final String startDayData = ci.getStartDateDay();
        final String startWeekData = ci.getStartDateWeek();
        final String totalDateCode = startYearData + startMonthData + startDayData + startWeekData;

        //store end dates fields individually
        final String endYearData = ci.getEndDateYear();
        final String endMonthData = ci.getEndDateMonth();
        final String endDayData = ci.getEndDateDay();
        final String endWeekData = ci.getEndDateWeek();
        final String endTotalDateCode = endYearData + endMonthData + endDayData + endWeekData;
        String fullDate = totalDateCode + endTotalDateCode;

        final long days = countDays();

        //sameDatesFunction(totalDateCode,endTotalDateCode);
        int startDayLimiter = Integer.parseInt(startDayData);
        int endDayLimiter = Integer.parseInt(endDayData);
        int counter = 0;
        for (int i = startDayLimiter; i < endDayLimiter; i++ ){ counter++; }

        if ( counter > 13 || counter < 0) {
            System.out.println(counter);
            displayText.setText("Please ensure your chosen dates are more than or equal to 1 days and less than 10 (Excluding weekends).");

        } else {

            //SameDate Function
            sameDatesFunction(new Callback() {
                @Override
                public void sameDateCallback(boolean sameDateWeek) {


                    if (!sameDateWeek){
                        displayText.setText("Maximum Users have booked this start day. Try next day or later week.");
                        return;

                    } else {

                        //past week function
                        similarPastWeek(new Callback() {
                            @Override
                            public void sameDateCallback(boolean sameDateWeek) {

                                if (!sameDateWeek){
                                    displayText.setText("Maximum Employees reached from previous weeks. Try a later date.");
                                    return;

                                } else {

                                    //future week function
                                    similarFutureWeek(new Callback() {
                                        @Override
                                        public void sameDateCallback(boolean sameDateWeek) {

                                            if (!sameDateWeek){
                                                displayText.setText("Maximum Employees reached for future weeks. Try a date earlier or later.");
                                                return;
                                            } else {
//
                                                //consider both future and past function
                                                considerPastFutureWeek(new Callback() {
                                                    @Override
                                                    public void sameDateCallback(boolean sameDateWeek) {

                                                        if (!sameDateWeek) {
                                                            displayText.setText("Maximum number of users in current chosen time frame.");
                                                            return;

                                                        } else {

//                                                            currentAllowance();
//                                                            displayText.setText("Booked dates.");
//
//                                                            System.out.println(startMonthData + "Seperate" + monthNameConverter());
//                                                            storeDates (startYearData, startMonthData, startDayData, startWeekData,
//                                                                    totalDateCode, endYearData, endMonthData, endDayData, endWeekData, endTotalDateCode, monthNameConverter());

                                                            //Skill check function
                                                     skillCheck(new Callback() {
                                                         @Override
                                                         public void sameDateCallback(boolean sameDateWeek) {

                                                             if (!sameDateWeek) {
                                                                 displayText.setText("Maximum Senior Developers in this month");
                                                             } else {

                                                                 //Recalculate current holiday entitlement
                                                                 currentAllowance();
                                                                 //set a booking success message
                                                                 displayText.setText("Booked dates.");
                                                                 //call function store dates which will store the current dates
                                                                 storeDates (startYearData, startMonthData, startDayData, startWeekData,
                                                                         totalDateCode, endYearData, endMonthData, endDayData, endWeekData, endTotalDateCode, monthNameConverter());



                                                             }
                                                         }
                                                     });




//                                                            duplicateHolidays(new Callback() {
//                                                    @Override
//                                                    public void sameDateCallback(boolean sameDateWeek) {
//
//                                                        if (!sameDateWeek) {
//                                                            displayText.setText("You have already booked on this month.");
//
//                                                        } else{
//
//                                                        }
//                                                    }
//                                                });
//                                                            currentAllowance();
//                                                            displayText.setText("Booked dates.");
//
//                                                            System.out.println(startMonthData + "Seperate" + monthNameConverter());
//                                                                storeDates (startYearData, startMonthData, startDayData, startWeekData,
//                                                                        totalDateCode, endYearData, endMonthData, endDayData, endWeekData, endTotalDateCode, monthNameConverter());
                                                        }
                                                    }
                                                });

                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }
    }


    public void skillCheck (final Callback callback) {
        fAuth = FirebaseAuth.getInstance();
        String userId = fAuth.getCurrentUser().getUid();

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                final Query query = db.collection("holidays").whereEqualTo("retrieveDepartment", "130");
                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        //creating a map to obtain information
                        Map<String, Object> allowanceData = new HashMap<>();
                        //retrieve data as a hashmap document
                        int skillCounter = 0;
                        int monthFlag = 0;

                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                            Note data = documentSnapshot.toObject(Note.class);
                            allowanceData.put("retrieveDepartment", data.getRetrieveDepartment());
                            allowanceData.put("startMonthName", data.getStartMonthName());
                            Collection<Object> values = allowanceData.values();

                            //retrieve results as single values
                            for (Object string : values) {

                                //increment skill counter for each match
                                if (string.equals("130")) {
                                    skillCounter++;
                                }

                                //increment for each match
                                if (string.equals(monthNameConverter())){
                                    monthFlag++;
                                }
                            }
                        }

                        if (skillCounter >= 2 && monthFlag >= 2) {
                            callback.sameDateCallback(false);
                        } else {
                            callback.sameDateCallback(true);
                        }

                    }
                });
            }



    public void currentAllowance () {

        fAuth = FirebaseAuth.getInstance();
        String userId = fAuth.getCurrentUser().getUid();
        DocumentReference documentReference = fStore.collection("users").document(userId);

        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String username = (documentSnapshot.getString("regName"));
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                final Query query = db.collection("holidays").whereEqualTo("username",username);
                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        //creating a map to obtain information
                        Map<String, Object> allowanceData = new HashMap<>();
                        long convertValues = 0;
                        final ArrayList<Long> totalDaysTaken = new ArrayList<>();

                        //retrieve data as a hashmap document
                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                            Note data = documentSnapshot.toObject(Note.class);
                            allowanceData.put("annualAllowance", data.getAnnualAllowance());
                            //test.put("startYearData", data.getStartYearData());
                            Collection<Object> values = allowanceData.values();

                            //retrieve results as single values
                            for (Object string : values) {
                                convertValues = (long) string;
                                totalDaysTaken.add(convertValues);
                            }
                        }

                        //Storing taken allowance into totalDays
                        long totalDays = 0;
                        for (int i = 0; i < totalDaysTaken.size(); i++) {
                            if(totalDaysTaken.get(i) != 0){
                                totalDays += totalDaysTaken.get(i);
                                System.out.println("How many total days am i" + totalDays);
                            }
                        }

                        long maxAllowance = 60;
                        long allowanceTotal = maxAllowance - totalDays;

                        if (totalDays > 0) {
                            if (allowanceTotal <= 0) {
                                annualHolidayBox.setText("Available Annual Holidays: 0");
                            } else{
                                annualHolidayBox.setText("Available Annual Holidays: " + allowanceTotal);
                            }
                        } else {
                            annualHolidayBox.setText("Available Annual Holidays: " +  allowanceTotal);
                        }

                    }
                });
            }
        });
    }

    public void duplicateHolidays (final Callback callback) {
        Calendar calendar = Calendar.getInstance();
        CalendarInfo ci = new CalendarInfo();
        final String startMonthData = ci.getStartDateMonth();
        final int intYear = calendar.get(Calendar.YEAR);
        final String currentYear = String.valueOf(intYear);

        fAuth = FirebaseAuth.getInstance();
        String userId = fAuth.getCurrentUser().getUid();
        DocumentReference documentReference = fStore.collection("users").document(userId);

        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String username = (documentSnapshot.getString("regName"));
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                final Query query = db.collection("holidays").whereEqualTo("username",username);

                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        //creating a map to obtain information
                        Map<String, Object> test = new HashMap<>();
                        int duplicateCounter = 0;

                        //retrieve data as a hashmap document
                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                            Note data = documentSnapshot.toObject(Note.class);
                            test.put("startMonthData", data.getStartMonthData());
                            //test.put("startYearData", data.getStartYearData());

                            Collection<Object> values = test.values();
                            //retrieve results as single values
                            for (Object string : values) {
                                if (startMonthData.equals(string))
                                    duplicateCounter++;
                            }
                        }

                        if (duplicateCounter > 0 && currentYear.equals("2021") ) {
                            callback.sameDateCallback(false);
                        } else {
                            callback.sameDateCallback(true);
                        }
                    }
                });
            }
        });

    }

    public void sameDatesFunction(final Callback callback) {

        CalendarInfo ci = new CalendarInfo();
        final String startDateReference = ci.getStartDateYear() + ci.getStartDateMonth() + ci.getStartDateDay() + ci.getStartDateWeek();

        final String startDay = ci.getStartDateDay();
        final int checker = Integer.parseInt(ci.getStartDateDay());
        final int peakValue;

        if (checker >= 1 && checker <= 14 ) {
            peakValue = 1;
        } else {
            peakValue = 2;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Query query = db.collection("holidays").whereEqualTo("startDayData",startDay);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                //creating a map to obtain information
                Map<String, Object> test = new HashMap<>();
                //counter
                int similarStartCounter = 0;
                int monthFlag = 0;

                //retrieve data as a hashmap document
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    Note data = documentSnapshot.toObject(Note.class);
                    test.put("startDayData", data.getStartDayData());
                    test.put("startMonthName", data.getStartMonthName());
                    Collection<Object> values = test.values();

                    //retrieve results as single values
                    for (Object string : values) {

                        if ((string.equals(startDay))) {
                            similarStartCounter++;
                        }

                        if (string.equals(monthNameConverter())){
                            monthFlag++;
                        }

                    }
                }

                if (similarStartCounter >= peakValue && monthFlag >= peakValue) {
                    callback.sameDateCallback(false);

                } else {
                    callback.sameDateCallback(true);

                }

            }
        });

    }

    public void considerPastFutureWeek (final Callback callback) {

        String startDayData = CalendarInfo.getStartDateDay();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        int startChecker = Integer.parseInt(startDayData);
        int futureChecker = Integer.parseInt(startDayData);

        final int peakValue;

        //if there is 3 end dates inside of the 14th then that means there are
        if ((startChecker >= 1 && startChecker <= 14)) {
            peakValue = 3;
        } else {
            peakValue = 5;
        }

        final ArrayList <String> startList = new ArrayList<>();
        final ArrayList <String> futureList = new ArrayList<>();
        final Query query = db.collection("holidays").whereEqualTo("startMonthName", monthNameConverter());

        for (int i= 1; i < 8; i++) {
            startChecker--;
            futureChecker++;

            if (startChecker == 0){
                startChecker = 31;
            }

            if (futureChecker == 0) {
                futureChecker = 31;
            }

//            if (startChecker == 32){
//                startChecker = 1;
//            }
//            if (futureChecker == 32) {
//                futureChecker = 1;
//            }

            final String startConversion = String.valueOf(startChecker);
            startList.add(startConversion);

            final String futureConversion = String.valueOf(futureChecker);
            futureList.add(futureConversion);

            query.whereEqualTo("startDayData", startConversion);
            query.whereEqualTo("startDayData", futureConversion);
        }
        startList.add(startDayData);


        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                Map<String, Object> test = new HashMap<>();
                int startCounter = 0;
                int futureCounter = 0;
                int monthFlag = 0;
                //retrieve data as a hashmap document
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    Note data = documentSnapshot.toObject(Note.class);
                    test.put("startDayData", data.getStartDayData());
                    test.put("startMonthName", data.getStartMonthName());

                    Collection<Object> values = test.values();
                    //retrieve results as single values
                    for (Object string : values) {

                        //check for matches of past start days
                        for (int i = 0; i < startList.size(); i++) {
                            //System.out.println(counter);
                            if (string.equals(startList.get(i))) {
                                startCounter++;
                                System.out.println("past"+ startList);
                                System.out.println("I am working");
                            }
                        }

                        //check for matches of future start days
                        for (int i = 0; i < futureList.size(); i++) {
                            //System.out.println(counter);
                            if (string.equals(futureList.get(i))) {
                                System.out.println("Future" + futureList);
                                futureCounter ++;
                            }

                        }

                        //check for matches of current month
                        if (string.equals(monthNameConverter())) {
                            monthFlag++;
                        }

                    }
                }

                //add counter results
                int totalCounts = futureCounter + startCounter;

                System.out.println("Show me the goods" + "Future Counter = " + futureCounter + "past counter = " + startCounter + "total counts = " + totalCounts);

                if (monthFlag >= peakValue && totalCounts >= peakValue) {
                    callback.sameDateCallback(false);

                } else {
                    callback.sameDateCallback(true);

                }
            }
        });

    }


    public void similarFutureWeek (final Callback callback) {

        String startDayData = CalendarInfo.getStartDateDay();


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        int checker = Integer.parseInt(startDayData);
        final int peakValue;

        //if there is 3 end dates inside of the 14th then that means there are
        if (checker >= 1 && checker <= 14 ) {
            peakValue = 3;
        } else {
            peakValue = 5;
        }

        final ArrayList <String> futureWeekList = new ArrayList<>();
        //so since only technically 8 people are allowed to take holiday as the max in a month

        final Query query = db.collection("holidays").whereEqualTo("startMonthName", monthNameConverter());

        for (int i= 1; i <8; i++) {

            checker++;

            if (checker == 0) {
                checker = 31;
            }

            final String conversion = String.valueOf(checker);
            futureWeekList.add(conversion);
            //System.out.println(conversion);
            query.whereEqualTo("startDayData", conversion);
        }


        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                Map<String, Object> test = new HashMap<>();
                int similarFutureCounter = 0;
                int monthFlag = 0;

                //retrieve data as a hashmap document
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    Note data = documentSnapshot.toObject(Note.class);
                    test.put("startDayData", data.getStartDayData());
                    test.put("startMonthName", data.getStartMonthName());

                    Collection<Object> values = test.values();
                    //retrieve results as single values
                    for (Object string : values) {

                        //check for matches in the future
                        for (int i = 0; i < futureWeekList.size(); i++) {
                            if (string.equals(futureWeekList.get(i))) {
                                similarFutureCounter ++;
                                System.out.println("Why is future not working" + string);
                            }
                        }

                        //check for matches in the same month
                        if (string.equals(monthNameConverter())){
                            monthFlag++;
                        }

                    }
                }

                if ((similarFutureCounter >= peakValue && monthFlag >= peakValue)) {
                    callback.sameDateCallback(false);
                } else{
                    callback.sameDateCallback(true);
                }
            }
        });

    }


    public void similarPastWeek (final Callback callback) {

        String startDayData = CalendarInfo.getStartDateDay();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        int checker = Integer.parseInt(startDayData);
        final int peakValue;

        //set a peak value
        if (checker >= 1 && checker <= 14 ) {
            peakValue = 3;
        } else {
            peakValue = 5;
        }

        final ArrayList <String> pastWeekList = new ArrayList<>();
        final Query query = db.collection("holidays").whereEqualTo("startMonthName", monthNameConverter());

        //search for days in the future
        for (int i= 1; i < 8; i++) {

            checker--;
            if (checker == 0) {
                checker = 31;
            }

            final String conversion = String.valueOf(checker);
            pastWeekList.add(conversion);
            query.whereEqualTo("startDayData", conversion);
        }


        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                Map<String, Object> test = new HashMap<>();
                int similarPastCounter = 0;
                int monthFlag = 0;

                //retrieve data as a hashmap document
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    Note data = documentSnapshot.toObject(Note.class);
                    test.put("startDayData", data.getStartDayData());
                    test.put("startMonthName", data.getStartMonthName());

                    Collection<Object> values = test.values();
                    //retrieve results as single values
                    for (Object string : values) {
                        for (int i = 0; i < pastWeekList.size(); i++) {
                            if (string.equals(pastWeekList.get(i))) {
                                similarPastCounter ++;
                            }
                        }

                        if (string.equals(monthNameConverter())){
                            monthFlag ++;
                        }

                    }
                }


                if ((similarPastCounter >= peakValue && monthFlag >= peakValue)) {
                    callback.sameDateCallback(false);
                } else{
                    callback.sameDateCallback(true);
                }
            }
            });
    }

    public String monthNameConverter () {
        final String startMonthData = CalendarInfo.getStartDateMonth();
        String monthName = null;

        if(startMonthData.equals("0")) {
            monthName = "January";

        } else if (startMonthData.equals("1")) {
             monthName = "February";

        } else if (startMonthData.equals("2")) {
             monthName = "March";

        } else if (startMonthData.equals("3")) {
             monthName = "April";

        } else if (startMonthData.equals("4")) {
            monthName = "May";

        }else if (startMonthData.equals("5")) {
            monthName = "June";

        } else if (startMonthData.equals("6")) {
            monthName= "July";

        }else if (startMonthData.equals("7")) {
            monthName = "August";

        } else if (startMonthData.equals("8")) {
            monthName = "September";

        }else if (startMonthData.equals("9")) {
            monthName = "October";

        }else if (startMonthData.equals("10")) {
            monthName = "November";

        } else if (startMonthData.equals("11")){
             monthName = "December";
        }

        return monthName;
    }

    public void storeDates (final String startYear, final String startMonth, final String startDay, final String startWeek, final String startReference,
                            final String endYear, final String endMonth, final String endDay, final String endWeek, final String endReference, final String monthName) {

        //creating a userID string to store username accompanied with date information
        final String userID;
        userID = fAuth.getCurrentUser().getUid();
        final String holidayName = holidayTitle.getText().toString().trim();

        //Initiliase flag and senior users into an array
        boolean skillFlag = false;
        String departmentCode;
        ArrayList <String> userSkill = new ArrayList<>();
        userSkill.add("Arnold Dennis");
        userSkill.add("Ava Gordon");
        userSkill.add("Serena Coleman");
        userSkill.add("Lara Rios");

        //check if the current user is in fact a senior developer
        for (int i = 0; i < userSkill.size(); i++){
            if (userSkill.get(i).equals(CalendarInfo.getCurrentUser())){
                skillFlag = true;
            }
        }

        //check what the value is based on the user
        if (skillFlag) {
            departmentCode = "130";
        } else {
            departmentCode = "100";
        }

        //this will retrieve information from users document from cloud server
        final DocumentReference userName = fStore.collection("users").document(userID);

        //If anything goes wrong delete docID
        String docId = userID + holidayName;
        //this will store data into holidays document
        final DocumentReference documentReference = fStore.collection("holidays").document(docId);


        //so add the following details according to the user
        final String finalDepartmentCode = departmentCode;
        userName.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                //retrieving field username for users
                String name = documentSnapshot.getString("regName");

                //hashmap for storing details
                Map<String, Object> user = new HashMap<>();
                user.put("username", name);
                user.put("holidayTitle", holidayName);

                //storing start dates
                user.put("startYearData", startYear);
                user.put("startMonthData", startMonth);
                user.put("startMonthName", monthName);
                user.put("startDayData", startDay);
                user.put("startWeekData", startWeek);
                user.put("startDateReference", startReference);

                //storing end dates
                user.put("endYearData", endYear);
                user.put("endMonthData", endMonth);
                user.put("endDayData", endDay);
                user.put("endWeekData", endWeek);
                user.put("endDateReference", endReference);
                //adding the dates booked
                user.put("annualAllowance", countDays());
                user.put("retrieveDepartment", finalDepartmentCode);

                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Dates stored for" + userID);
                    }
                });


            }
        });

    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        if (dateStatus) {
            //Setting the date
            CalendarInfo ci = new CalendarInfo();
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            //getting the date
            int y = c.get(Calendar.YEAR);
            int m = c.get(Calendar.MONTH);
            int d = c.get(Calendar.DAY_OF_MONTH);

                //convert dates into strings
                String startYear = Integer.toString(y);
                String startMonth = Integer.toString(m);
                String startDay = Integer.toString(d);

                //create class object and pass data through getters and setters CalenderInfo Class
                ci.setStartDateYear(startYear);
                ci.setStartDateMonth(startMonth);
                ci.setStartDateDay(startDay);

                //converting day of week to actual string names
                String weekDay;
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.UK);
                weekDay = dayFormat.format(c.getTime());
                ci.setStartDateWeek(weekDay);
                System.out.println("start = " + weekDay);


                String currentDateString = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());
                TextView startTextView = (TextView) findViewById(R.id.startDateText);
                startTextView.setText("Start Date: " + currentDateString);



        } else if (!dateStatus){
            //Setting the date
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            //getting the date
            int y = c.get(Calendar.YEAR);
            int m = c.get(Calendar.MONTH);
            int d = c.get(Calendar.DAY_OF_MONTH);

            //convert dates into strings
            String endYear = Integer.toString(y);
            String endMonth = Integer.toString(m);
            String endDay = Integer.toString(d);

            //create class object and pass data through getters and setters CalenderInfo Class
            CalendarInfo ci = new CalendarInfo();
            ci.setEndDateYear(endYear);
            ci.setEndDateMonth(endMonth);
            ci.setEndDateDay(endDay);


                //converting day of week to actual string names
                String weekDay;
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.UK);
                weekDay = dayFormat.format(c.getTime());
                ci.setEndDateWeek(weekDay);


            String currentDateString = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());
            TextView endTextView = (TextView) findViewById(R.id.endDateText);
            endTextView.setText("End Date: " + currentDateString);

        }

    }

    }


    //                //just getting instance
//    FirebaseFirestore db = FirebaseFirestore.getInstance();
//noteRef is here to basically find the name of the holiday again, if you get confused again
//                  the holiday name is stored in the method not this
//                DocumentReference noteRef = db.collection("holidays").document(holidayName);

//                noteRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if (documentSnapshot.exists()){
//                            String inputTitle = holidayName;
//                            String name = documentSnapshot.getString("holidayTitle");
//                            if (name.equals(inputTitle)){
//                                System.out.println("There is already a holiday with that name");
//                                return;
//                            }
//                        } else if(!documentSnapshot.exists()){
//
//                        }else{
//                            Toast.makeText(HolidayScreen.this,"Document does not exist", Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                        Toast.makeText(HolidayScreen.this,"ERROR", Toast.LENGTH_SHORT).show();
//                        Log.d(TAG,e.toString());
//                    }
//                });

//
//    public void skillCheck (final Callback callback) {
//        fAuth = FirebaseAuth.getInstance();
//        String userId = fAuth.getCurrentUser().getUid();
//        final String startMonthData = CalendarInfo.getStartDateMonth();
//        DocumentReference documentReference = fStore.collection("users").document(userId);
//
//        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                String username = (documentSnapshot.getString("regName"));
//                FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//                final Query query = db.collection("holidays").whereEqualTo("username", username);
//                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//
//                        //creating a map to obtain information
//                        Map<String, Object> allowanceData = new HashMap<>();
//                        //retrieve data as a hashmap document
//                        int skillCounter = 0;
//                        int monthFlag = 0;
//
//                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
//                            Note data = documentSnapshot.toObject(Note.class);
//                            allowanceData.put("retrieveDepartment", data.getRetrieveDepartment());
//                            allowanceData.put("startMonthName", data.getStartMonthName());
//                            Collection<Object> values = allowanceData.values();
//
//                            //retrieve results as single values
//                            for (Object string : values) {
//
//                                if (string.equals("130")) {
//                                    skillCounter++;
//                                }
//
//                                if (string.equals(monthNameConverter())){
//                                    monthFlag++;
//                                }
//                            }
//                        }
//
//                        if (skillCounter >= 2 && monthFlag >= 2) {
//                            callback.sameDateCallback(false);
//                        } else {
//                            callback.sameDateCallback(true);
//                        }
//
//                    }
//                });
//            }
//        });
//    }


//    public void verifyValidations() {
//
//        //boolean sameDate = CalendarValidation.isSameDateFlag();
//
//        CalendarInfo ci = new CalendarInfo();
//        //store start dates fields individually
//        final String startYearData = ci.getStartDateYear();
//        final String startMonthData = ci.getStartDateMonth();
//        final String startDayData = ci.getStartDateDay();
//        final String startWeekData = ci.getStartDateWeek();
//        final String totalDateCode = startYearData + startMonthData + startDayData + startWeekData;
//
//        //store end dates fields individually
//        final String endYearData = ci.getEndDateYear();
//        final String endMonthData = ci.getEndDateMonth();
//        final String endDayData = ci.getEndDateDay();
//        final String endWeekData = ci.getEndDateWeek();
//        final String endTotalDateCode = endYearData + endMonthData + endDayData + endWeekData;
//        String fullDate = totalDateCode + endTotalDateCode;
//
//        final long days = countDays();
//
//        //sameDatesFunction(totalDateCode,endTotalDateCode);
//        int startDayLimiter = Integer.parseInt(startDayData);
//        int endDayLimiter = Integer.parseInt(endDayData);
//        int counter = 0;
//        for (int i = startDayLimiter; i < endDayLimiter; i++ ){ counter++; }
//
//        if ( counter > 13 || counter < 0) {
//            System.out.println(counter);
//            displayText.setText("Please ensure your chosen dates are more than or equal to 1 days and less than 10 (Excluding weekends).");
//
//        } else {
//
//            sameDatesFunction(new Callback() {
//                @Override
//                public void sameDateCallback(boolean sameDateWeek) {
//
//                    CalendarValidation.setSameDateFlag(sameDateWeek);
//                    if (!sameDateWeek){
//                        displayText.setText("Maximum Users have booked this start day. Try next day or later week.");
//                        return;
//
//                    } else {
//
//                        similarPastWeek(new Callback() {
//                            @Override
//                            public void sameDateCallback(boolean sameDateWeek) {
//
//                                CalendarValidation.setSameDateFlag(sameDateWeek);
//                                if (!sameDateWeek){
//                                    displayText.setText("Maximum Employees reached from previous weeks. Try a later date.");
//                                    return;
//
//                                } else {
//
//                                    similarFutureWeek(new Callback() {
//                                        @Override
//                                        public void sameDateCallback(boolean sameDateWeek) {
//
//                                            CalendarValidation.setSameDateFlag(sameDateWeek);
//                                            if (!sameDateWeek){
//                                                displayText.setText("Maximum Employees reached for future weeks. Try a date earlier or later.");
//                                                return;
//                                            } else {
////
//                                                considerPastFutureWeek(new Callback() {
//                                                    @Override
//                                                    public void sameDateCallback(boolean sameDateWeek) {
//
//                                                        if (!sameDateWeek) {
//                                                            displayText.setText("Maximum number of users in current chosen time frame.");
//                                                            return;
//
//                                                        } else {
//
////                                                            currentAllowance();
////                                                            displayText.setText("Booked dates.");
////
////                                                            System.out.println(startMonthData + "Seperate" + monthNameConverter());
////                                                            storeDates (startYearData, startMonthData, startDayData, startWeekData,
////                                                                    totalDateCode, endYearData, endMonthData, endDayData, endWeekData, endTotalDateCode, monthNameConverter());
//
//                                                            skillCheck(new Callback() {
//                                                                @Override
//                                                                public void sameDateCallback(boolean sameDateWeek) {
//
//                                                                    if (!sameDateWeek) {
//                                                                        displayText.setText("Maximum Senior Developers in this month");
//                                                                    } else {
//
//                                                                        currentAllowance();
//                                                                        displayText.setText("Booked dates.");
//
//                                                                        System.out.println(startMonthData + "Seperate" + monthNameConverter());
//                                                                        storeDates (startYearData, startMonthData, startDayData, startWeekData,
//                                                                                totalDateCode, endYearData, endMonthData, endDayData, endWeekData, endTotalDateCode, monthNameConverter());
//
//
//
//                                                                    }
//                                                                }
//                                                            });
//
//
//
//
////                                                            duplicateHolidays(new Callback() {
////                                                    @Override
////                                                    public void sameDateCallback(boolean sameDateWeek) {
////
////                                                        if (!sameDateWeek) {
////                                                            displayText.setText("You have already booked on this month.");
////
////                                                        } else{
////
////                                                        }
////                                                    }
////                                                });
////                                                            currentAllowance();
////                                                            displayText.setText("Booked dates.");
////
////                                                            System.out.println(startMonthData + "Seperate" + monthNameConverter());
////                                                                storeDates (startYearData, startMonthData, startDayData, startWeekData,
////                                                                        totalDateCode, endYearData, endMonthData, endDayData, endWeekData, endTotalDateCode, monthNameConverter());
//                                                        }
//                                                    }
//                                                });
//
//                                            }
//                                        }
//                                    });
//                                }
//                            }
//                        });
//                    }
//                }
//            });
//        }
//    }