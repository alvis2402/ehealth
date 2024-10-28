package com.example.mobilepatient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CaseChatClosed extends AppCompatActivity {
    // Importing buttons, recyclerView
    Button gobackButton;

    RecyclerView recyclerView; // RecyclerView to store the Firebase 'list' array generated in MyAdapter.java
    DatabaseReference database;
    MyAdapter myAdapter;
    ArrayList<Cases> list, cleanList;


    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_closed);

        //get the select caseID from extras passed in from case view activity
        Bundle extras = getIntent().getExtras();
        String caseID = extras.getString("caseID");
        String caseText = extras.getString("spinnerItem");
        Log.d("PASSED IN caseID: ", caseID);

        //initializing buttons
        gobackButton = findViewById(R.id.gobackButton);

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

        list = new ArrayList<>(); // Create the list array



        //read the signed in patients userID that is shared with the doctor
        db.child(currentUserID).child("userID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String patientID = snapshot.getValue(String.class);

                database = FirebaseDatabase.getInstance().getReference("patientInfo/"+patientID+"/cases/"+caseID+"/messages");


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
                            if (caseInfo.contains("msg=")) {
                                list.add(cases);
                                Log.d("TAG", "CASE ADDED: " + caseInfo);
                            }
                        }

                        //made recycler view update live on activity if removed/added event listener by adding a clear list on line 96 and
                        //pushing this adapter to below the first onvalue event change listener
                        myAdapter = new MyAdapter(CaseChatClosed.this, list, "chatdisplay"); // Populate the array
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
                Toast.makeText(CaseChatClosed.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



        gobackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent gobackActivity = new Intent(CaseChatClosed.this, myCasesClosed.class);
                gobackActivity.putExtra("spinnerItem", caseText);
                startActivity(gobackActivity);

            }
        });

    }

}
