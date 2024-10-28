package com.example.mobilepatient;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText forename,surname,email,password,phone,patientID;
    Button registerBtn,goToLogin;
    boolean valid = true;
    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fAuth = FirebaseAuth.getInstance(); //initializing fAuth, fStore
        fStore = FirebaseFirestore.getInstance();

        //get user input information
        patientID = findViewById(R.id.textID);
        forename = findViewById(R.id.textForename);
        surname = findViewById(R.id.textSurname);
        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        phone = findViewById(R.id.registerPhone);
        registerBtn = findViewById(R.id.registerBtn);
        goToLogin = findViewById(R.id.gotoLogin);


        registerBtn.setOnClickListener((view) -> { //Register button listener

            checkField(patientID);//checkField function returns boolean if field is empty or not
            checkField(forename);
            checkField(surname);
            checkField(email);
            checkField(password);
            checkField(phone);


            //start the user registration process
            if(valid) {
                fAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() { //firebase create user & success listener
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user = fAuth.getCurrentUser(); //create user reference for firebase
                        Toast.makeText(RegisterActivity.this, "Account Created", Toast.LENGTH_SHORT).show(); //on success creation display 'created' text in this class Register

                        //create database instance and set to point to users node
                        FirebaseDatabase database_default = FirebaseDatabase.getInstance();
                        DatabaseReference db = database_default.getReference("patientInfo");

                        //save patient information to the database by putting it all into userInfo hashmap and pushing it
                        Map<String, Object> userInfo = new HashMap<>();

                        userInfo.put("activated", true);
                        userInfo.put("forename", forename.getText().toString());
                        userInfo.put("surname", surname.getText().toString());
                        userInfo.put("email", email.getText().toString());
                        userInfo.put("mobileNum", phone.getText().toString());
                        userInfo.put("patientID", patientID.getText().toString());

                        //concatenate patients full name for easier addressing on doctor website interface
                        String full_name = forename.getText().toString() + " " + surname.getText().toString();
                        userInfo.put("name", full_name);

                        //save current date+time the account was created
                        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                        userInfo.put("time", currentDate);

                        //read patients registered doctor ID
                        db.child(patientID.getText().toString()).child("doctorID").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                userInfo.put("doctorID", snapshot.getValue(String.class));

                                //save hashmap to database patientID.getText().toString()
                                db.child(patientID.getText().toString()).setValue(userInfo);

                                Log.d("TAG", "doctor ID: " + snapshot.getValue(String.class));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(RegisterActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });




                        //set account status to activated and save its ID inside the doctors generated ID node
                        Map<String, Object> updatedAccount = new HashMap<>();
                        updatedAccount.put("time", currentDate);
                        updatedAccount.put("userID", patientID.getText().toString());

                        DatabaseReference db2 = database_default.getReference("patients");
                        db2.child(user.getUid()).setValue(updatedAccount);


                        //df.set(userInfo); //save using DocumentReference in fStore database instance "Users"

                        startActivity(new Intent(getApplicationContext(), MainActivity.class)); //start new activity Main on success creation of user
                        finish(); //finish doesn't let user go back with button to this Register page

                    }
                }).addOnFailureListener(new OnFailureListener() { //if fAuth create user failed check
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Failed to Create Account", Toast.LENGTH_SHORT).show(); //display user Toast they failed
                    }
                });


            }


        });

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });

    }


    public boolean checkField(EditText textField){
        if(textField.getText().toString().isEmpty()){
            textField.setError("Error");
            valid = false;
        }else {
            valid = true;
        }

        return valid;
    }
}