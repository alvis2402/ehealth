package com.example.mobilepatient;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{

    Context context;

    // Array to hold the meetings
    ArrayList<Cases> list;

    // Decide which adapter to use, instead of having an adapter for each recyclerview - it's dynamic
    String type;

    public MyAdapter(Context context, ArrayList<Cases> list, String type) {
        this.type = type;
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // On Create of ViewHolder - inflate the layout

        // Dynamic adapter choosing layout type
        View v = null;
        if (type == "casesdisplay") {
            v = LayoutInflater.from(context).inflate(R.layout.cases, parent, false);
        }
        if (type == "chatdisplay") {
            v = LayoutInflater.from(context).inflate(R.layout.chat, parent, false);
        }
//        if (type == "bookedroom") {
//            v = LayoutInflater.from(context).inflate(R.layout.itemexpanded, parent, false);
//        }
//        if (type == "vacatedroom") {
//            v = LayoutInflater.from(context).inflate(R.layout.itemvacated, parent, false);
//        }
//        if (type == "officetransfer") {
//            v = LayoutInflater.from(context).inflate(R.layout.itemtransfers, parent, false);
//        }


        return new MyViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        // calling new dynamic bindView with position
        holder.bindView(position);

        // Create a class Meeting with Array 'list' values, fields as 'firstRoom' and 'firstVacancy'
//        Meeting meeting = list.get(position);
//        holder.room.setText(meeting.getRoom());
//        holder.vacancy.setText(meeting.getVacancy());

        // On click listener for rooms 1/2
//        holder.v.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String med = meeting.getVacancy();
//                if (med == "taken") {
//
//                    Intent sharingIntent = new Intent(Intent.ACTION_VIEW);
//                    sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    sharingIntent.setData(Uri.parse("http://google.com"));
//
//                    Intent chooserIntent = Intent.createChooser(sharingIntent, "Open With");
//                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                    context.startActivity(chooserIntent);
//                }
//
//            }
//        });


    }

    @Override
    public int getItemCount() {
        return list.size(); // get Size of Array 'list'
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        // Initialize TextView for rooms and vacancy
        TextView text, user, room, vacancy, date, message, office, name, image2;


        // On click listener for rooms 2/2
        //View v;

        // Superclass for the MyViewHolder
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //v = itemView;
            // Initialize the TextView fields by ID
            text = itemView.findViewById(R.id.tvcase);
            user = itemView.findViewById(R.id.tvuser);
//            vacancy = itemView.findViewById(R.id.tvvacancy);
//
//            date = itemView.findViewById(R.id.tvdate);
//            message = itemView.findViewById(R.id.tvmessage);
//            office = itemView.findViewById(R.id.tvoffice);
//            name = itemView.findViewById(R.id.tvname);
//            image = itemView.findViewById(R.id.tvimage);
        }


        public void bindView(final int position)
        {
            if (type == "casesdisplay") {
                Cases cases = list.get(position);
                text.setText(cases.getText());
            }

            if (type == "chatdisplay") {
                Cases cases = list.get(position);

                //check if user 1 or 0, 1 for doctor, 0 for patient
                if (cases.getUser() == 1) {
                    user.setText("Doctor:");
                    text.setText(cases.getMsg());
                }
                else if (cases.getUser() == 0) {
                    user.setText("You:");
                    text.setText(cases.getMsg());
                }
                else {
                    user.setText("Nurse:");
                    text.setText(cases.getMsg());
                }
//                user.setText(cases.getImage2());
            }

//            if (type == "bookedroom") {
//                Meeting meeting = list.get(position);
//                room.setText(meeting.getRoom());
//                date.setText(meeting.getDate());
//                message.setText(meeting.getMessage());
//            }
//            if (type == "vacatedroom") {
//                Meeting meeting = list.get(position);
//                room.setText(meeting.getRoom());
//                date.setText(meeting.getDate());
//            }
//            if (type == "officetransfer") {
//                Meeting meeting = list.get(position);
//                name.setText(meeting.getFullName());
//                office.setText(meeting.getOffice());
//
//            }
        }
    }
}
