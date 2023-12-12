package com.example.taxicle.data_access_object;

import com.example.taxicle.constructors.Passenger;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class DAO {

    private DatabaseReference databaseReference;

    public DAO() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(Passenger.class.getSimpleName());
    }

    public Task<Void> add(Passenger passenger){
        return databaseReference.child(passenger.getId()).setValue(passenger);
    }

    // Booking now
    public Task<Void> shareLocation(String id, HashMap<String, Object> passenger){

        return databaseReference.child(id).child("sharedLocations").setValue(passenger);
    }

    public Task<Void> update(String key, HashMap<String, Object> hashMap){
        return databaseReference.child(key).updateChildren(hashMap);
    }

    public Task<Void> cancelBooked(String id) {
        return databaseReference.child(id).child("sharedLocations").removeValue();
    }

    // Advance booking
    public Task<Void> saveLocation (String id, String key, HashMap<String, Object> passenger) {
        return databaseReference.child(id).child("savedLocations").child(key).setValue(passenger);
    }

    public Task<Void> updateSavedLocation (String id, String key, HashMap<String, Object> passenger) {
        return databaseReference.child(id).child("savedLocations").child(key).updateChildren(passenger);
    }
}
