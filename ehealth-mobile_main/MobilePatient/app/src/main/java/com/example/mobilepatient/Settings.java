package com.example.mobilepatient;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;


public class Settings extends AppCompatActivity {

    FirebaseAuth fAuth; //to register new users in firebase
    FirebaseFirestore fStore;
    boolean valid = true;

    NotificationManagerCompat notificationManagerCompat;
    Notification notification;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Button logoutBtn = findViewById(R.id.logoutButton);
        Button updateBtn = findViewById(R.id.update);
        Button goBackbtn = findViewById(R.id.gobackButton);
        Button updatePassBtn = findViewById(R.id.updatePassword);
        EditText oldEmail = findViewById(R.id.loginEmail);
        EditText newEmail = findViewById(R.id.newEmail);
        EditText oldPass = findViewById(R.id.loginPassword);
        EditText newPass = findViewById(R.id.newPassword);

        //make firebase database connected to this instance
        FirebaseDatabase firebase_database = FirebaseDatabase.getInstance();
        DatabaseReference db = firebase_database.getReference("patients");

        fAuth = FirebaseAuth.getInstance(); //initializing fAuth, fStore
        fStore = FirebaseFirestore.getInstance();

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserID = currentFirebaseUser.getUid();

        //update email button
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check that input fields are not empty
                checkField(oldEmail);
                checkField(newEmail);

                if (valid) {
                    //make notification informing user that email has been updated
                    notificationCreate("Email has been updated", "You can now login using your new email");

                    currentFirebaseUser.updateEmail(newEmail.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            Log.d("THIS", "email has been updated");
                            Toast.makeText(Settings.this, "Updated Email", Toast.LENGTH_SHORT).show();
                            Intent loginActivity = new Intent(Settings.this, MainActivity.class);
                            startActivity(loginActivity);
                        }
                    });
                }
            }
        });

        //update password button
        updatePassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check that input fields are not empty
                checkField(oldPass);
                checkField(newPass);

                if (valid) {
                    //make notification informing user that password has been updated
                    notificationCreate("Password has been updated", "You can now login using your new password");

                    currentFirebaseUser.updatePassword(newPass.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("THIS", "email has been updated");
                            Toast.makeText(Settings.this, "Updated Password", Toast.LENGTH_SHORT).show();
                            Intent loginActivity = new Intent(Settings.this, MainActivity.class);
                            startActivity(loginActivity);
                        }
                    });
                }
            }
        });


        //sign user out
        goBackbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginActivity = new Intent(Settings.this, MainActivity.class);
                startActivity(loginActivity);

            }
        });
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