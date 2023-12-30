package com.example.taxicle.data_access_object;

import com.example.taxicle.constructors.AvailableDriver;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BookTricycle {
    private DatabaseReference databaseReference;
    public BookTricycle() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(AvailableDriver.class.getSimpleName());
    }

    public Task<Void> bookTricycle(String driver, String passenger) {
        return databaseReference.child(driver).child("passengers").child(passenger).child("id").setValue(passenger);
    }
}
