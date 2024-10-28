package com.example.mobilepatient;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class myCases extends AppCompatActivity {

    StorageReference storageReference;
    ImageView imageDisplay, imageDisplay2, imageDisplay3;
    Button goBackButton, caseChatButton;
    TextView caseMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycases);

        //get the select case from spinner
        Bundle extras = getIntent().getExtras();
        String spinnerItem = extras.getString("spinnerItem");

        //initialize images and buttons
        imageDisplay = findViewById(R.id.imageView1);
        imageDisplay2 = findViewById(R.id.imageView2);
        imageDisplay3 = findViewById(R.id.imageView3);
        goBackButton = findViewById(R.id.backBtn);
        caseChatButton = findViewById(R.id.caseChat);
        caseMsg = findViewById(R.id.caseInfo);

        //fill in case message from spinner
        caseMsg.setText(spinnerItem);

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

                            caseChatButton.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v) {
                                    //creating intent activity to open case chat and also pass in caseID to the casechat activity
                                    Intent caseChat = new Intent(myCases.this, CaseChat.class);
                                    caseChat.putExtra("caseID", caseID);
                                    caseChat.putExtra("spinnerItem", spinnerItem);
                                    startActivity(caseChat);

                                }
                            });
                        }
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

                                            Toast.makeText(myCases.this, "Failed to get image", Toast.LENGTH_SHORT).show();

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
                Intent goBackActivity = new Intent(myCases.this, CasesActivity.class);
                startActivity(goBackActivity);

            }
        });

    }

}
