package com.example.taxicle;

import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.addOnMapClickListener;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taxicle.constructors.Booking;
import com.example.taxicle.constructors.Passenger;
import com.example.taxicle.data_access_object.DAO;
import com.example.taxicle.data_access_object.DAOBooking;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import com.mapbox.search.autocomplete.PlaceAutocomplete;
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion;
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter;
import com.mapbox.search.ui.view.CommonSearchViewConfiguration;
import com.mapbox.search.ui.view.SearchResultsView;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button logOut;
    FirebaseUser user;
    private MapView mapView;
    FloatingActionButton floatingActionButton;
    Point point;
    Passenger location;
    
    private ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (result) {
            Toast.makeText(MainActivity.this, "Permission granted!", Toast.LENGTH_SHORT).show();
        }
    });
    private final OnIndicatorBearingChangedListener onIndicatorBearingChangedListener = new OnIndicatorBearingChangedListener() {
        @Override
        public void onIndicatorBearingChanged(double v) {
            try {
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(v).build());
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    };
    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = new OnIndicatorPositionChangedListener() {
        @Override
        public void onIndicatorPositionChanged(@NonNull Point point) {
            try {
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(17.0).build());
                getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));
                MainActivity.this.point = point;

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };
    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            try {
                getLocationComponent(mapView).removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                getGestures(mapView).removeOnMoveListener(onMoveListener);
                floatingActionButton.show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {

        }
    };

    private void updateCamera(Point point, Double bearing) {
        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(1500L).build();
        CameraOptions cameraOptions = new CameraOptions.Builder().center(point).zoom(18.0).bearing(bearing).pitch(45.0)
                .padding(new EdgeInsets(1000.0, 0.0, 0.0, 0.0)).build();
        
        getCamera(mapView).easeTo(cameraOptions, animationOptions);
    }

    private PlaceAutocomplete placeAutocomplete;
    private SearchResultsView searchResultsView;
    private PlaceAutocompleteUiAdapter placeAutocompleteUiAdapter;
    private TextInputEditText searchET;
    private boolean ignoreNextQueryUpdate = false;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isProceed = false;
    private String pickUpLocationName, dropOffLocationName, notesForDriver;
    private Point pickUpPoint, dropOffPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvView = findViewById(R.id.tv_view);
        TextView tvCancel = findViewById(R.id.tv_cancel);
        tvView.setOnClickListener(v -> {
            findViewById(R.id.bottom_notif).setVisibility(View.GONE);
            startActivity(new Intent(this, Booked.class));
        });
        tvCancel.setOnClickListener(v -> {
            DAO dao = new DAO();
            dao.cancelBooked(user.getUid());
            findViewById(R.id.bottom_notif).setVisibility(View.GONE);
            Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show();
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.acc_setting) {
                Toast.makeText(MainActivity.this, "Setting", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.booked) {
                startActivity(new Intent(this, Booked.class));
            }
            else if (item.getItemId() == R.id.logout) {
                logout();
            }
            return false;
        });

        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.user_name);
        TextView tvEmail = headerView.findViewById(R.id.email);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Passenger.class.getSimpleName());
        databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Passenger passenger = snapshot.getValue(Passenger.class);
                try {
                    tvUserName.setText(passenger.getName());
                    tvEmail.setText(user.getEmail());
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        FirebaseApp.initializeApp(this);

        placeAutocomplete = PlaceAutocomplete.create(getString(R.string.mapbox_access_token));
        searchET = findViewById(R.id.searchET);

        searchResultsView = findViewById(R.id.search_results_view);
        searchResultsView.initialize(new SearchResultsView.Configuration(new CommonSearchViewConfiguration()));
        placeAutocompleteUiAdapter = new PlaceAutocompleteUiAdapter(searchResultsView, placeAutocomplete, LocationEngineProvider.getBestLocationEngine(this));

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (ignoreNextQueryUpdate) {
                    ignoreNextQueryUpdate = false;
                } else {
                    placeAutocompleteUiAdapter.search(charSequence.toString(), new Continuation<Unit>() {
                        @NonNull
                        @Override
                        public CoroutineContext getContext() {
                            return EmptyCoroutineContext.INSTANCE;
                        }

                        @Override
                        public void resumeWith(@NonNull Object o) {
                            runOnUiThread(() -> {
                                searchResultsView.setVisibility(View.VISIBLE);
                            });
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mapView = findViewById(R.id.mapview);
        floatingActionButton = findViewById(R.id.focusLocation);

        location = new Passenger();

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        mapView.getMapboxMap().loadStyleUri("mapbox://styles/jltolentino/clpxwr3q200iy01r7gzqf2gdt", style -> {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location);
            AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
            PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, new AnnotationConfig());

            addOnMapClickListener(mapView.getMapboxMap(), point -> {
                pointAnnotationManager.deleteAll();
                PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions().withTextAnchor(TextAnchor.CENTER).withIconImage(bitmap)
                        .withPoint(point);
                pointAnnotationManager.create(pointAnnotationOptions);

                searchResultsView.setVisibility(View.GONE);

//              Display pickup or drop-off dialog info
                if (isProceed) dropLocationInfo(point);
                else pickLocationInfo(point);

                return true;
            });

            floatingActionButton.setOnClickListener(view1 -> {
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(20.0).build());
                LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
                locationComponentPlugin.setEnabled(true);
                LocationPuck2D locationPuck2D = new LocationPuck2D();
                locationPuck2D.setBearingImage(AppCompatResources.getDrawable(MainActivity.this,R.drawable.baseline_my_location_24));
                locationComponentPlugin.setLocationPuck(locationPuck2D);

                locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                getGestures(mapView).addOnMoveListener(onMoveListener);
                floatingActionButton.hide();


            });


            placeAutocompleteUiAdapter.addSearchListener(new PlaceAutocompleteUiAdapter.SearchListener() {
                @Override
                public void onSuggestionsShown(@NonNull List<PlaceAutocompleteSuggestion> list) {

                }

                @Override
                public void onSuggestionSelected(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {
                    ignoreNextQueryUpdate = true;
                    searchET.setText(placeAutocompleteSuggestion.getName());
                    searchResultsView.setVisibility(View.GONE);

                    pointAnnotationManager.deleteAll();
                    PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions().withTextAnchor(TextAnchor.CENTER).withIconImage(bitmap)
                            .withPoint(placeAutocompleteSuggestion.getCoordinate());
                    pointAnnotationManager.create(pointAnnotationOptions);
                    updateCamera(placeAutocompleteSuggestion.getCoordinate(), 0.0);

                    pickLocationInfo(placeAutocompleteSuggestion.getCoordinate());
                    searchET.clearFocus();
                    hideKeyboard(MainActivity.this, searchET);
                }

                @Override
                public void onPopulateQueryClick(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {

                }

                @Override
                public void onError(@NonNull Exception e) {

                }
            });
        });
    }

    private Address getGeoCode(Point point) {
        Geocoder geocoder = new Geocoder(MainActivity.this);
        Address address = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(point.latitude(), point.longitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0);
                String locationName = address.getAddressLine(0); // Full address

            }
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return address;
    }

//    Show pick up info
    private void pickLocationInfo(Point point) {
        // Inflate the bottom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.pickup_info, null);

        // Create a dialog without a title
        Dialog bottomDialog = new Dialog(this);
        bottomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomDialog.setContentView(dialogView);

        // Set the dialog to appear at the bottom of the screen
        Window window = bottomDialog.getWindow();
        if (window != null) {
            window.setGravity(android.view.Gravity.BOTTOM);
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvLocationName = dialogView.findViewById(R.id.location_name);
        TextView tvPoint = dialogView.findViewById(R.id.point);
        EditText notes = dialogView.findViewById(R.id.pick_up_notes);

        Address address = getGeoCode(point);
        String locationName = address.getAddressLine(0);
        tvLocationName.setText(locationName);
        tvPoint.setText(String.format("long: %s, lat: %s", point.longitude(), point.latitude()));

        // Set click listener for the "OK" button
        Button dialogButtonOK = dialogView.findViewById(R.id.btn_choose_point);
        dialogButtonOK.setOnClickListener(v -> {
            // Handle button click (dismiss the dialog or perform other actions)
            pickUpLocationName = locationName;
            pickUpPoint = point;
            notesForDriver = notes.getText().toString();
            showBookInfoDialog();
            bottomDialog.dismiss();
        });

        // Show the bottom dialog
        bottomDialog.show();

    }

//    Show drop location info
    private void dropLocationInfo(Point point) {
        // Inflate the bottom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dropoff_info, null);

        // Create a dialog without a title
        Dialog bottomDialog = new Dialog(this);
        bottomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomDialog.setContentView(dialogView);

        // Set the dialog to appear at the bottom of the screen
        Window window = bottomDialog.getWindow();
        if (window != null) {
            window.setGravity(android.view.Gravity.BOTTOM);
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvLocationName = dialogView.findViewById(R.id.location_name);
        TextView tvPoint = dialogView.findViewById(R.id.point);

        Address address = getGeoCode(point);
        String locationName = address.getAddressLine(0);
        tvLocationName.setText(locationName);
        tvPoint.setText(String.format("long: %s, lat: %s", point.longitude(), point.latitude()));

        // Set click listener for the "OK" button
        Button dialogButtonOK = dialogView.findViewById(R.id.btn_choose_point);
        ImageButton dialogButtonReset = dialogView.findViewById(R.id.btn_reset);

        dialogButtonOK.setOnClickListener(v -> {
            // Handle button click (dismiss the dialog or perform other actions)
            dropOffLocationName = locationName;
            dropOffPoint = point;
            confirmBooking();
            bottomDialog.dismiss();
        });

        dialogButtonReset.setOnClickListener(v -> {
            isProceed = false;
            bottomDialog.dismiss();
        });

        // Show the bottom dialog
        bottomDialog.show();

    }

    private void showBookInfoDialog() {
        // Inflate the bottom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.book_option, null);

        // Create a dialog without a title
        Dialog bottomDialog = new Dialog(this);
        bottomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomDialog.setContentView(dialogView);

        // Set the dialog to appear at the bottom of the screen
        Window window = bottomDialog.getWindow();
        if (window != null) {
            window.setGravity(android.view.Gravity.BOTTOM);
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }


        TextView tvLocName, tvPoint, tvNote;
        tvLocName = dialogView.findViewById(R.id.location_name);
        tvPoint = dialogView.findViewById(R.id.point);
        tvNote = dialogView.findViewById(R.id.pick_up_notes);

        tvLocName.setText(pickUpLocationName);
        tvPoint.setText(String.format("long: %s lat: %s", pickUpPoint.longitude(), pickUpPoint.latitude()));
        tvNote.setText(notesForDriver);

        if (notesForDriver.isEmpty()) tvNote.setVisibility(View.GONE);

        // Set click listener for the "OK" button
        Button dialogBtnProceed = dialogView.findViewById(R.id.btn_proceed);
        Button dialogBtnCancel = dialogView.findViewById(R.id.btn_cancel);

        dialogBtnProceed.setOnClickListener(v -> {
            // Handle button click (dismiss the dialog or perform other actions)
            isProceed = true;
            bottomDialog.dismiss();
        });

        dialogBtnCancel.setOnClickListener(v -> {
            bottomDialog.dismiss();
        });

        // Show the bottom dialog
        bottomDialog.show();
    }

    private void confirmBooking() {
        // Inflate the bottom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.confirm_booking, null);

        // Create a dialog without a title
        Dialog bottomDialog = new Dialog(this);
        bottomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomDialog.setContentView(dialogView);

        // Set the dialog to appear at the bottom of the screen
        Window window = bottomDialog.getWindow();
        if (window != null) {
            window.setGravity(android.view.Gravity.BOTTOM);
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvPickUpLocationName = dialogView.findViewById(R.id.pickup_location_name);
        TextView tvPickUpPoint = dialogView.findViewById(R.id.pickup_point);
        TextView tvDropLocationName = dialogView.findViewById(R.id.drop_location_name);
        TextView tvDropPoint = dialogView.findViewById(R.id.drop_point);

        tvPickUpLocationName.setText(pickUpLocationName);
        tvPickUpPoint.setText(String.format("long: %s, lat: %s", pickUpPoint.longitude(), pickUpPoint.latitude()));

        tvDropLocationName.setText(dropOffLocationName);
        tvDropPoint.setText(String.format("long: %s, lat: %s", dropOffPoint.longitude(), dropOffPoint.latitude()));


        Button dialogButtonBookNow = dialogView.findViewById(R.id.btn_book_now);
        Button dialogButtonCancel = dialogView.findViewById(R.id.btn_cancel);

        dialogButtonBookNow.setOnClickListener(v -> {
            // Handle button click (dismiss the dialog or perform other actions)
            isProceed = false;
            bookNow();
            bottomDialog.dismiss();
        });

        dialogButtonCancel.setOnClickListener(v -> {
            isProceed = false;
            bottomDialog.dismiss();
        });

        // Show the bottom dialog
        bottomDialog.show();
    }

    private void bookNow() {
        Date date = new Date();

        Booking booking = new Booking();
        booking.setId(user.getUid());
        booking.setPickUplocationName(pickUpLocationName);
        booking.setPickUpLongitude(pickUpPoint.longitude());
        booking.setPickUpLatitude(pickUpPoint.latitude());
        booking.setNotes(notesForDriver);
        booking.setDate(date);
        booking.setDropOffLocationName(dropOffLocationName);
        booking.setDropOffLongitude(dropOffPoint.longitude());
        booking.setDropOffLatitude(dropOffPoint.latitude());

        DAOBooking daoBooking = new DAOBooking();
        daoBooking.addBooking(booking);

        findViewById(R.id.bottom_notif).setVisibility(View.VISIBLE);
        Toast.makeText(MainActivity.this, "Booked", Toast.LENGTH_SHORT).show();
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputMethodManager != null) {
            IBinder windowToken = view.getWindowToken();
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        searchResultsView.setVisibility(View.GONE);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}