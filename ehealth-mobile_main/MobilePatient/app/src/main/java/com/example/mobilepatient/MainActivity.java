package com.example.mobilepatient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    private TextView displayName;

    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button logoutBtn = findViewById(R.id.logoutButton);
        Button createCase = findViewById(R.id.createCase);
        Button myCases = findViewById(R.id.myCases);
        Button myCasesClosed = findViewById(R.id.myCasesClosed);
        Button settingsBtn = findViewById(R.id.settings);
        displayName = findViewById(R.id.userid);

        //make firebase database connected to this instance
        FirebaseDatabase firebase_database = FirebaseDatabase.getInstance();
        DatabaseReference db = firebase_database.getReference("patients");

        fAuth = FirebaseAuth.getInstance(); //initializing fAuth, fStore
        fStore = FirebaseFirestore.getInstance();

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserID = currentFirebaseUser.getUid();

        displayName.setText(currentUserID);

        //read the signed in patients userID that is shared with the doctor
        db.child(currentUserID).child("userID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String patientID = snapshot.getValue(String.class);
                displayName.setText("Welcome " + patientID + " id: " + currentUserID);

                DatabaseReference db2 = firebase_database.getReference("patientInfo");
                db2.child(patientID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String mobileNum = dataSnapshot.child("mobileNum").getValue(String.class);
                        displayName.setText("Welcome " + name + "\nThis is your homepage.\n\nMobile Number: " + mobileNum);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        //sign user out
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent loginActivity = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginActivity);

            }
        });

        createCase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createCase = new Intent(MainActivity.this, createCase.class);
                startActivity(createCase);
            }
        });

        myCases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myCases = new Intent(MainActivity.this, CasesActivity.class);
                startActivity(myCases);
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsActivity = new Intent(MainActivity.this, Settings.class);
                startActivity(settingsActivity);
            }
        });

        myCasesClosed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myCasesClosed = new Intent(MainActivity.this, CasesActivityClosed.class);
                startActivity(myCasesClosed);
            }
        });
    }
}