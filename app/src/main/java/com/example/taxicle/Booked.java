package com.example.taxicle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taxicle.constructors.Booking;
import com.example.taxicle.data_access_object.DAOBooking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Booked extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;

    Double longitude, latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        RelativeLayout bookingInfo = findViewById(R.id.rl_booking_info);
        TextView tvEmptyBooking = findViewById(R.id.tv_no_booking);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Booking.class.getSimpleName());
        try {
            databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (snapshot.exists()) {
                            tvEmptyBooking.setVisibility(View.GONE);
                            bookingInfo.setVisibility(View.VISIBLE);

                            Booking booking = snapshot.getValue(Booking.class);
                            longitude = booking.getPickUpLongitude();
                            latitude = booking.getPickUpLatitude();
                            String locationName = booking.getPickUplocationName();
                            String notes = booking.getNotes();

                            TextView tvLocationName = findViewById(R.id.location_name);
                            tvLocationName.setText(locationName);

                            TextView tvPoint = findViewById(R.id.point);
                            tvPoint.setText(String.format("long: %s lat: %s", longitude, latitude));

                            TextView tvNotes = findViewById(R.id.pick_up_notes);
                            tvNotes.setText(String.format("notes: %s", notes));
                        } else {
                            bookingInfo.setVisibility(View.GONE);
                            tvEmptyBooking.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Toast.makeText(Booked.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            Toast.makeText(Booked.this, "Error2: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Button btnCancelBooked = findViewById(R.id.btn_cancel);
        btnCancelBooked.setOnClickListener(v -> {
            DAOBooking dao = new DAOBooking();
            dao.cancelBooked(user.getUid());
            Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.ib_back).setOnClickListener(v -> {
            onBackPressed();
        });
    }
}