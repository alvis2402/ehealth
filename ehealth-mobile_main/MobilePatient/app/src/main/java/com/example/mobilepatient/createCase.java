package com.example.mobilepatient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class createCase extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1234;
    private static final int CAPTURE_CODE = 1001;
    ImageView imageView, imageView2, imageView3;
    Uri imageUri, imageUri2, imageUri3;
    boolean valid = true, image1Selected = false, image2Selected = false, image3Selected = false, image1Upload = false, image2Upload = false, image3Upload = false;
    ProgressDialog progressDialog;

    StorageReference storageReference;

    Button uploadImageBtn, backButton;

    //setting up notifications
    NotificationManagerCompat notificationManagerCompat;
    Notification notification;

    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createcase);

        EditText problemDescription = findViewById(R.id.problemDescInput);
        backButton = findViewById(R.id.gobackButton);


        fAuth = FirebaseAuth.getInstance(); //initializing fAuth, fStore
        fStore = FirebaseFirestore.getInstance();

        FirebaseDatabase firebase_database = FirebaseDatabase.getInstance();
        DatabaseReference db = firebase_database.getReference("patients");

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserID = currentFirebaseUser.getUid();

        //read the signed in patients userID that is shared with the doctor
        db.child(currentUserID).child("userID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String patientID = snapshot.getValue(String.class);
                String key = db.push().getKey();
                Log.d("TAG", "key to push to: " + key);


                uploadImageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //check that the input field for problem description is not empty

                        checkField(problemDescription);

                        if (valid) {
                            notificationCreate("New Case Created", "Case can be found on the My Cases page.");
                            uploadImage(patientID, key, problemDescription.getText().toString(), image1Upload, image2Upload, image3Upload);
                            //problem description is the user input for the case they're opening
                        }
                        }
                });

                DatabaseReference db2 = firebase_database.getReference("patientInfo");
                db2.child(patientID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //read the signed in patients name and mobile number
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String mobileNum = dataSnapshot.child("mobileNum").getValue(String.class);
                        Log.d("TAG", "signed in user name: " + name + " and num: " + mobileNum);



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(createCase.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(createCase.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent goBackActivity = new Intent(createCase.this, MainActivity.class);
                startActivity(goBackActivity);

            }
        });

        uploadImageBtn = findViewById(R.id.uploadimagebtn);
        imageView = findViewById(R.id.firebaseimage);
        imageView2 = findViewById(R.id.firebaseimage2);
        imageView3 = findViewById(R.id.firebaseimage3);

        //if image clicked then let user choose to take photo or select existing from library
        registerForContextMenu(imageView);
        registerForContextMenu(imageView2);
        registerForContextMenu(imageView3);

    }

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


    private void uploadImage(String currentUserID, String key, String problemDescription, boolean image1, boolean image2, boolean image3) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading file");
        progressDialog.show();


        //upload images 1-3 only if they are provided as true meaning they exist
        if (image1) {
            Log.d("IMAGE1: ", String.valueOf(image1));
            uploadImageFirebase("image1", currentUserID, key, problemDescription, imageUri);
        }
        if (image2) {
            Log.d("IMAGE2: ", String.valueOf(image2));
            uploadImageFirebase("image2", currentUserID, key, problemDescription, imageUri2);
        }
        if (image3) {
            Log.d("IMAGE3: ", String.valueOf(image3));
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    uploadImageFirebase("image3", currentUserID, key, problemDescription, imageUri3);
                }
            }, 5000);
        }

        //return user to previous main menu as the case problem has been created and uploaded
        Intent mainActivity = new Intent(createCase.this, MainActivity.class);
        startActivity(mainActivity);

    }

    //method called for 3 images depending on a bool value if they exist.
    //will upload the image to firebase under the name that was passed in using String imageID
    private void uploadImageFirebase(String imageID, String currentUserID, String key, String problemDescription, Uri image_path) {
        //initialize firebase to save the created key as case ID and push a patient message+imageURL in database under it.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mRef = database.getReference().child("patientInfo/" + currentUserID + "/cases/" + key);

        //create database initialize under patients ID and save push value of database
        storageReference = FirebaseStorage.getInstance().getReference("images/"+currentUserID+ "/" + key + "/" + imageID);

        storageReference.putFile(image_path)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //when image has successfully uploaded to firebaseStorage, get the image reference URL
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map<String, Object> caseInfo = new HashMap<>();//create hashmap to store case description
                                caseInfo.put("text", problemDescription);
                                caseInfo.put(imageID, uri.toString()); //store image url
                                //mRef.setValue(caseInfo);
                                mRef.updateChildren(caseInfo);
                            }
                        });

                        //its been uploaded, reset the image URI
                        imageView.setImageURI(null);
                        Toast.makeText(createCase.this, "Successfully Uploaded", Toast.LENGTH_SHORT).show();
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(createCase.this, "Failed to Upload", Toast.LENGTH_SHORT).show();
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();

                    }
                });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.photo_menu, menu);

        //check did image1 image2 or image3 were selected to create context menu
        //and enable bool value that they were selected to true
        Log.d("View V: ", String.valueOf(v.getId()));

        switch (String.valueOf(v.getId())) {
            case "2131230924":
                image1Selected = true;
                image1Upload = true;
                Log.d("Selected image 1: ", String.valueOf(v.getId()));
                return;
            case "2131230925":
                image2Selected = true;
                image2Upload = true;
                Log.d("Selected image 2: ", String.valueOf(v.getId()));
                return;
            case "2131230926":
                image3Selected = true;
                image3Upload = true;
                Log.d("Selected image 3: ", String.valueOf(v.getId()));
                return;
            default:
                Log.d("Error, imageView ID not matched: ", String.valueOf(v.getId()));

        }

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_1:
                Toast.makeText(this, "option 1", Toast.LENGTH_SHORT).show();

                //check sdk version matches or is newer than the build version to use camera feature
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);

                    } else {
                        openCamera();
                    }
                }
                else {
                    openCamera();
                }
                return true;
            case R.id.option_2:
                Toast.makeText(this, "option 2", Toast.LENGTH_SHORT).show();

                selectImage();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void selectImage() {
        //disable all buttons while new activity is showing recent photo album
        //there was a bug where the buttons would be pressed in background

        uploadImageBtn.setEnabled(false);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);

        //after activity of recent photos done then enable the buttons again
        uploadImageBtn.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if code for selected images activity result
        if (requestCode == 100 && data != null && data.getData() != null) {

            //check which image was selected and put image into selected imageview location
            if (image1Selected) {
                imageUri = data.getData();
                imageView.setImageURI(imageUri);
                image1Selected = false;
            }
            else if (image2Selected) {
                imageUri2 = data.getData();
                imageView2.setImageURI(imageUri2);
                image2Selected = false;
            }
            else if (image3Selected) {
                imageUri3 = data.getData();
                imageView3.setImageURI(imageUri3);
                image3Selected = false;
            }
        }

        //if code for photo capture image activity result
        if (resultCode == RESULT_OK) {
            //check which image was selected and put image into selected imageview location
            if (image1Selected) {
                imageView.setImageURI(imageUri);
                image1Selected = false;
            }
            else if (image2Selected) {
                imageView2.setImageURI(imageUri);
                image2Selected = false;
            }
            else if (image3Selected) {
                imageView3.setImageURI(imageUri);
                image3Selected = false;
            }

        }
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

    private void openCamera() {
        //disable all buttons while new activity is showing recent photo album
        //there was a bug where the buttons would be pressed in background

        uploadImageBtn.setEnabled(false);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "new image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "from the camera");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent camintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camintent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(camintent, CAPTURE_CODE);

        //after activity of recent photos done then enable the buttons again
        uploadImageBtn.setEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show();
                }
        }
    }
}