package com.example.taxicle.data_access_object;

import com.example.taxicle.constructors.Booking;
import com.example.taxicle.constructors.Passenger;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DAOBooking {
    private DatabaseReference databaseReference;

    public DAOBooking() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(Booking.class.getSimpleName());
    }

    public Task<Void> addBooking(Booking booking){
        return databaseReference.child(booking.getId()).setValue(booking);
    }

    public Task<Void> cancelBooked(String id) {
        return databaseReference.child(id).removeValue();
    }
}
