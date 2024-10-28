package com.example.mobilepatient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CasesActivityClosed extends AppCompatActivity {
    // Importing buttons, recyclerView
    Button gobackButton, confirmButton;

    Spinner spinner;
    ArrayList<String> dropList;

    RecyclerView recyclerView; // RecyclerView to store the Firebase 'list' array generated in MyAdapter.java
    DatabaseReference database;
    MyAdapter myAdapter;
    ArrayList<Cases> list, cleanList;


    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cases);

        //initializing buttons//
        gobackButton = findViewById(R.id.gobackButton);
        confirmButton = findViewById(R.id.confirmButton);
        spinner = findViewById(R.id.spinner);

        //make firebase database connected to this instance
        FirebaseDatabase firebase_database = FirebaseDatabase.getInstance();
        DatabaseReference db = firebase_database.getReference("patients");





        //get current users ID
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserID = currentFirebaseUser.getUid();

        // Initializing Firebase RecyclerView requirements
        recyclerView = findViewById(R.id.casesList);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Initialized Spinner (drop down menu) dropList, should be updated each array 'dropList' iteration: dropAdapter.notifyDataSetChanged();
        dropList = new ArrayList<>();
        ArrayAdapter<String> dropAdapter = new ArrayAdapter<String>(CasesActivityClosed.this, android.R.layout.simple_spinner_item, dropList);
        dropAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        list = new ArrayList<>(); // Create the list array



        //read the signed in patients userID that is shared with the doctor
        db.child(currentUserID).child("userID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String patientID = snapshot.getValue(String.class);

                database = FirebaseDatabase.getInstance().getReference("patientInfo/"+patientID+"/cases");

                // Read Firebase to load in RecyclerView
                database.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String caseInfo = dataSnapshot.getValue().toString();
                            Log.d("TAG", "CASE INFO: " + caseInfo);
                            Cases cases = dataSnapshot.getValue(Cases.class); // Reading from Users/ all sub folders to insert into List

                            //check if message variable inside case exists, it holds the doctors note for closing the case, if it exists the case is closed
                            if (caseInfo.contains("nurseClosed=") || caseInfo.contains("message")) {
                                list.add(cases);
                                Log.d("TAG", "CASE ADDED: " + caseInfo);
                                Log.d("TAG", "MESSAGE: " + cases.getMessage());

                                String text = cases.getText();

                                dropList.add(text);
                                dropAdapter.notifyDataSetChanged();
                            }



                        }

                        //made recycler view update live on activity if removed/added event listener by adding a clear list on line 96 and
                        //pushing this adapter to below the first onvalue event change listener
                        myAdapter = new MyAdapter(CasesActivityClosed.this, list, "casesdisplay"); // Populate the array
                        recyclerView.setAdapter(myAdapter);
                        myAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CasesActivityClosed.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        Log.d("Spinner1", "Updating spinner adapter");
        // Spinner listener for drop down menu selection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String value = adapterView.getItemAtPosition(i).toString();
                Log.d("dropDownSelected: ", value);
                Toast.makeText(CasesActivityClosed.this, "Selected Case" + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // Final setAdapter to update the above Spinner
        spinner.setAdapter(dropAdapter);


        gobackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent gobackActivity = new Intent(CasesActivityClosed.this, MainActivity.class);
                startActivity(gobackActivity);

            }
        });

        //open closed case activity with specified case from spinner item
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent confirmActivity = new Intent(CasesActivityClosed.this, myCasesClosed.class);
                confirmActivity.putExtra("spinnerItem", spinner.getSelectedItem().toString()); // Transferring data to another class
                startActivity(confirmActivity);

            }
        });



    }

}
