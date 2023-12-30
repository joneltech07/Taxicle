package com.example.taxicle.adapter;

import static com.mapbox.turf.TurfConstants.UNIT_METERS;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taxicle.R;
import com.example.taxicle.constructors.AvailableDriver;
import com.example.taxicle.constructors.Booking;
import com.example.taxicle.constructors.Driver;
import com.example.taxicle.data_access_object.BookTricycle;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.geojson.Point;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AvailableDriverAdapter extends FirebaseRecyclerAdapter<AvailableDriver, AvailableDriverAdapter.myViewHolder> {
    FirebaseAuth auth;
    FirebaseUser user;

    boolean hasBooked = false;

    public AvailableDriverAdapter(@NonNull FirebaseRecyclerOptions<AvailableDriver> options) {
        super(options);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull AvailableDriver model) {


        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbRefDriver, dbRefBooking;


//      Get Driver Name
        dbRefDriver = db.getReference(Driver.class.getSimpleName());
        dbRefDriver.child(model.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Driver driver = snapshot.getValue(Driver.class);
                    assert driver != null;
                    holder.driver.setText(driver.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        Point point = Point.fromLngLat(model.getLongitude(), model.getLatitude());
        String location = getGeoCode(point, holder.driver.getContext()).getAddressLine(0);
        holder.location.setText(location);



        // Get Booking Pick-Up Point
        dbRefBooking = db.getReference(Booking.class.getSimpleName());
        dbRefBooking.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    assert booking != null;

                    Point pickUpPoint = Point.fromLngLat(booking.getPickUpLongitude(), booking.getPickUpLatitude());
                    double distanceBetweenDeviceAndTarget = TurfMeasurement.distance(pickUpPoint, point, UNIT_METERS);

                    if (distanceBetweenDeviceAndTarget >= 1000) {
                        distanceBetweenDeviceAndTarget = TurfMeasurement.distance(pickUpPoint,
                                point, TurfConstants.UNIT_KILOMETERS);
                        holder.distance.setText(
                                String.format(
                                        "%skm",
                                        NumberFormat.getNumberInstance(Locale.US).format(distanceBetweenDeviceAndTarget)
                                )
                        );
                    } else {
                        holder.distance.setText(
                                String.format(
                                        "%sm",
                                        NumberFormat.getNumberInstance(Locale.US).format(distanceBetweenDeviceAndTarget)
                                )
                        );
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        BookTricycle bookTricycle = new BookTricycle();
//      Book Tricycle
        holder.btnSelect.setOnClickListener(v -> {
            bookTricycle.bookTricycle(model.getId(), user.getUid());
            holder.btnSelect.setText("Waiting for driver's respond...");
            holder.btnSelect.setClickable(false);
        });




    }





    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.driver_item, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        TextView driver, location, distance;
        Button btnSelect;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            driver = itemView.findViewById(R.id.tv_driver_name);
            location = itemView.findViewById(R.id.location);
            distance = itemView.findViewById(R.id.distance);

            btnSelect = itemView.findViewById(R.id.btnSelect);


        }
    }

    private Address getGeoCode(Point point, Context context) {
        Geocoder geocoder = new Geocoder(context);
        Address address = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(point.latitude(), point.longitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0);
                String locationName = address.getAddressLine(0); // Full address

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }
}
