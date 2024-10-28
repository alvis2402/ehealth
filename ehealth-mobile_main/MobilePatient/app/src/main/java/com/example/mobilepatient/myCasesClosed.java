package com.example.mobilepatient;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class myCasesClosed extends AppCompatActivity {

    StorageReference storageReference;
    ImageView imageDisplay, imageDisplay2, imageDisplay3;
    TextView caseCloseMsg;
    Button goBackButton, caseChatButton, reOpenButton;


    NotificationManagerCompat notificationManagerCompat;
    Notification notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycases_closed);

        //get the select case from spinner
        Bundle extras = getIntent().getExtras();
        String spinnerItem = extras.getString("spinnerItem");

        //initialize images and buttons
        imageDisplay = findViewById(R.id.imageView1);
        imageDisplay2 = findViewById(R.id.imageView2);
        imageDisplay3 = findViewById(R.id.imageView3);
        goBackButton = findViewById(R.id.backBtn);
        caseChatButton = findViewById(R.id.caseChat);
        reOpenButton = findViewById(R.id.reOpen);
        caseCloseMsg = findViewById(R.id.caseMsgText);

        //make firebase database connected to this instance
        FirebaseDatabase firebase_database = FirebaseDatabase.getInstance();
        DatabaseReference db = firebase_database.getReference("patients");

        //get current users ID
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserID = currentFirebaseUser.getUid();

        //read the signed in patients userID that is shared with the doctor
        db.child(currentUserID).child("userID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String patientID = snapshot.getValue(String.class);

                //creating root reference in database using patientID to match case with spinner text
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("patientInfo");
                Query query = rootRef.child(patientID + "/cases").orderByChild("text").equalTo(spinnerItem);

                //creating event listener to get caseID and 3 images attached to the caseID
                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            //save case ID in string
                            String caseID = ds.getKey();
                            Log.d("case ID: ", caseID);

                            //building storage reference url to selected case, using patientID and case ID selected from spinner
                            storageReference = FirebaseStorage.getInstance().getReference("images/" + patientID + "/" + caseID + "/image1");

                            loadImage(imageDisplay, "image1", caseID);
                            loadImage(imageDisplay2, "image2", caseID);
                            loadImage(imageDisplay3, "image3", caseID);
                            loadCaseMsg(patientID, caseID);

                            caseChatButton.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v) {
                                    //creating intent activity to open case chat and also pass in caseID to the casechat activity
                                    Intent caseChat = new Intent(myCasesClosed.this, CaseChatClosed.class);
                                    caseChat.putExtra("caseID", caseID);
                                    caseChat.putExtra("spinnerItem", spinnerItem);
                                    startActivity(caseChat);

                                }
                            });

                        }
                    }

                    //method used to load the doctor note message that was made when the case was closed, will contain reason for clouse
                    //display the reason to user for closing by overwriting a textfield on case page
                    private void loadCaseMsg(String patientID, String caseID) {
                        DatabaseReference dbCaseMsg = firebase_database.getReference("patientInfo/" + patientID + "/cases/" + caseID + "/status");

                        dbCaseMsg.child("message").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //saving the case reason for closing message
                                String caseMessage = snapshot.getValue(String.class);
                                Log.d("TAG", "Case MSG: " + patientID + " " + caseID + " " + caseMessage);

                                //display message to user
                                caseCloseMsg.setText("Close Reason: " + caseMessage);

                                //reopen the case by writing to firebase that case is no longer closed
                                reOpenButton.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v) {
                                        //create notification informing user case is closed
                                        notificationCreate("Case reopened: " + caseID, "You can find the case in My Cases page.");

                                        //check did doctor or nurse close case
                                        dbCaseMsg.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String nurse = dataSnapshot.child("nurseCase").getValue(String.class);
                                                Log.d("READING NURSE", "READING NURSE: " + nurse);

//                                                if (nurse.equals("yes")) {
//                                                    Log.d("Nurse case detected", "nurse");
//
//                                                    //setting path to nurse case to reopen case with them
//                                                    DatabaseReference dbNurseCase = firebase_database.getReference("NurseCases/" + caseID);
//                                                    dbNurseCase.addValueEventListener(new ValueEventListener() {
//                                                        @Override
//                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                                            String doctorMsg = dataSnapshot.child("doctorNote").getValue(String.class);
//                                                            String patientName = dataSnapshot.child("name").getValue(String.class);
//
//                                                            Log.d("Doctor msg:", ": " + doctorMsg);
//
//                                                            //creating map to reopen nurse case with
//                                                            Map<String, Object> caseInfo = new HashMap<>();
//                                                            caseInfo.put("caseID", caseID);
//                                                            caseInfo.put("doctorNote", doctorMsg);
//                                                            caseInfo.put("name", patientName);
//                                                            caseInfo.put("patientID", patientID);
//                                                            //dbNurseCase.updateChildren(caseInfo);
//                                                            dbNurseCase.setValue(caseInfo);
//                                                        }
//
//                                                        @Override
//                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                                        }
//                                                    });
//
//
//
//                                                } else {
//                                                    //remove doctor note from closed case
//                                                    dbCaseMsg.child("message").removeValue();
//                                                }
                                                //remove doctor note from closed case
                                               dbCaseMsg.child("message").removeValue();

                                                //go back to main page
                                                Intent loginActivity = new Intent(myCasesClosed.this, MainActivity.class);
                                                startActivity(loginActivity);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });





                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(myCasesClosed.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    private void loadImage(ImageView imageDisplay, String imageID, String caseID) {
                        //get image inside the bitmap
                        try {
                            File localfile = File.createTempFile("tempfile", ".jpg");

                            Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());

                            //set bitmap to image1
                            imageDisplay.setImageBitmap(bitmap);

                            //try to load third image if exists
                            storageReference = FirebaseStorage.getInstance().getReference("images/" + patientID + "/" + caseID + "/" + imageID);
                            storageReference.getFile(localfile)
                                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                            //get image inside the bitmap
                                            Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());

                                            //set bitmap to image1
                                            imageDisplay.setImageBitmap(bitmap);

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(myCasesClosed.this, "Failed to get image", Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //failed to find caseID in database
                        Log.d("Failed to get caseID", databaseError.getMessage());
                    }
                };

                //start the listener to find caseID and 3 images using listener we created
                query.addListenerForSingleValueEvent(valueEventListener);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //failed to get patientID from database
                Log.d("Failed to get patientID: ", error.getMessage());
            }
        });


        goBackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent goBackActivity = new Intent(myCasesClosed.this, CasesActivityClosed.class);
                startActivity(goBackActivity);

            }
        });

    }

    //method that creates a notification with passed in title and notification message
    private void notificationCreate(String notiTitle, String msg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("myCh", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "myCh")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle(notiTitle)
                .setContentText(msg);

        notification = builder.build();
        notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(1, notification);
    }

}
