package com.example.taxicle;

import static android.app.PendingIntent.getActivity;
import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.addOnMapClickListener;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;
import static com.mapbox.navigation.base.extensions.RouteOptionsExtensions.applyDefaultNavigationOptions;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.Bearing;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.bindgen.Expected;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
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
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources;
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue;
import com.mapbox.search.autocomplete.PlaceAutocomplete;
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion;
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter;
import com.mapbox.search.ui.view.CommonSearchViewConfiguration;
import com.mapbox.search.ui.view.SearchResultsView;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
            getRouteData();
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
        CameraOptions cameraOptions = new CameraOptions.Builder().center(point).zoom(17.0).bearing(bearing)
                .padding(new EdgeInsets(0.0, 0.0, 0.0, 0.0)).build();
        
        getCamera(mapView).easeTo(cameraOptions, animationOptions);
    }

    private final NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    private MapboxNavigation mapboxNavigation;
    private MapboxRouteLineView routeLineView;
    private MapboxRouteLineApi routeLineApi;

    private final LocationObserver locationObserver = new LocationObserver() {
        @Override
        public void onNewRawLocation(@NonNull Location location) {

        }

        @Override
        public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
            Location location = locationMatcherResult.getEnhancedLocation();
            navigationLocationProvider.changePosition(location, locationMatcherResult.getKeyPoints(), null, null);
        }
    };
    private final RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NonNull RoutesUpdatedResult routesUpdatedResult) {
            routeLineApi.setNavigationRoutes(routesUpdatedResult.getNavigationRoutes(), routeLineErrorRouteSetValueExpected -> {
                Style style = mapView.getMapboxMap().getStyle();
                if (style != null) {
                    routeLineView.renderRouteDrawData(style, routeLineErrorRouteSetValueExpected);
                }
            });
        }
    };

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

    private RelativeLayout progressBar;

    private PointAnnotationManager pointAnnotationManager;

    private boolean hasBooked = false;

    private Drawable drawableRedLocation, drawableBlueLocation, drawablePin;
    private Bitmap bitmapRedLocation, bitmapBLueLocation, bitmapPin;
    private AnnotationPlugin annotationPlugin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.rl_progress_bar_container);




        TextView tvView = findViewById(R.id.tv_view);
        TextView tvCancel = findViewById(R.id.tv_cancel);
        tvView.setOnClickListener(v -> {
            findViewById(R.id.bottom_notif).setVisibility(View.GONE);
            mapboxNavigation.onDestroy();
            mapboxNavigation.unregisterRoutesObserver(routesObserver);
            mapboxNavigation.unregisterLocationObserver(locationObserver);
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
                mapboxNavigation.onDestroy();
                mapboxNavigation.unregisterRoutesObserver(routesObserver);
                mapboxNavigation.unregisterLocationObserver(locationObserver);
                startActivity(new Intent(this, Booked.class));
            }
            else if (item.getItemId() == R.id.logout) {
                logout();
            }
            return false;
        });

        ImageButton showDrawer = findViewById(R.id.show_drawer);
        showDrawer.setOnClickListener(v -> drawerLayout.open());

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
        } else {
            try {
                getRouteData();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }








//      initialize drawable sources and bitmaps
        drawableRedLocation = ContextCompat.getDrawable(this, R.drawable.location_red);
        assert drawableRedLocation != null;
        bitmapRedLocation = getBitmapFromVectorDrawable(drawableRedLocation);


        drawableBlueLocation = ContextCompat.getDrawable(this, R.drawable.location_blue);
        assert drawableBlueLocation != null;
        bitmapBLueLocation = getBitmapFromVectorDrawable(drawableBlueLocation);


        drawablePin = ContextCompat.getDrawable(this, R.drawable.baseline_push_pin_24);
        assert drawablePin != null;
        bitmapPin = getBitmapFromVectorDrawable(drawablePin);




        annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
        pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, new AnnotationConfig());








//        Set Default camera view
        Point defaultPoint = Point.fromLngLat(121.01469445607671, 14.490188015421111);
        updateCamera(defaultPoint, 0.0);




        searchResultsView.setVisibility(View.GONE);
        mapView.getMapboxMap().loadStyleUri("mapbox://styles/jltolentino/clpxx8g5j00jr01re4o8x833g", style -> {

            addOnMapClickListener(mapView.getMapboxMap(), point -> {
                // set progress bar visibility to visible
                progressBar.setVisibility(View.VISIBLE);
                try {
                    searchResultsView.setVisibility(View.GONE);
                    searchET.clearFocus();


                    if (hasBooked) {
                        Address address = getGeoCode(point);
                        Toast.makeText(this, address.getAddressLine(0), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        PointAnnotationOptions pickUpPointAnnotationOptions = new PointAnnotationOptions()
                                .withTextAnchor(TextAnchor.CENTER)
                                .withIconImage(bitmapPin)
                                .withPoint(point);
                        pointAnnotationManager.create(pickUpPointAnnotationOptions);


                        //              Display pickup or drop-off dialog info
                        if (isProceed) {
                            dropLocationInfo(point);
                        } else {
                            pickLocationInfo(point);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Please check your internet connection and turn-on location", Toast.LENGTH_LONG).show();
                }
                return true;
            });

            floatingActionButton.setOnClickListener(view1 -> {
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(20.0).build());
                LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
                locationComponentPlugin.setEnabled(true);
                LocationPuck2D locationPuck2D = new LocationPuck2D();
                locationPuck2D.setBearingImage(AppCompatResources.getDrawable(MainActivity.this,R.drawable.baseline_person_pin_24));
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
                    PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions().withTextAnchor(TextAnchor.CENTER).withIconImage(bitmapRedLocation)
                            .withPoint(placeAutocompleteSuggestion.getCoordinate());
                    pointAnnotationManager.create(pointAnnotationOptions);
                    updateCamera(placeAutocompleteSuggestion.getCoordinate(), 0.0);

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


        MapboxRouteLineOptions options = new MapboxRouteLineOptions.Builder(this).withRouteLineResources(new RouteLineResources.Builder().build())
                .withRouteLineBelowLayerId("road-label-navigation").build();
        routeLineView = new MapboxRouteLineView(options);
        routeLineApi = new MapboxRouteLineApi(options);

        NavigationOptions navigationOptions = new NavigationOptions.Builder(this).accessToken(getString(R.string.mapbox_access_token)).build();

        MapboxNavigationApp.setup(navigationOptions);
        mapboxNavigation = new MapboxNavigation(navigationOptions);

        mapboxNavigation.registerRoutesObserver(routesObserver);
        mapboxNavigation.registerLocationObserver(locationObserver);











//      set progress bar visibility to gone
        progressBar.setVisibility(View.GONE);
    }


    private void getRouteData() {
        DatabaseReference bookingDatabaseReference = FirebaseDatabase
                .getInstance().getReference(Booking.class.getSimpleName()).child(user.getUid());

        // Attach a ValueEventListener to check if the node is not empty
        bookingDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Node is not empty
                    Booking booking = dataSnapshot.getValue(Booking.class);

                    assert booking != null;
                    Point pickupPoint = Point.fromLngLat(booking.getPickUpLongitude(), booking.getPickUpLatitude());
                    Point dropOffPoint = Point.fromLngLat(booking.getDropOffLongitude(), booking.getDropOffLatitude());

                    hasBooked = true;

                    pointAnnotationManager.deleteAll();
                    PointAnnotationOptions pickUpPointAnnotationOptions = new PointAnnotationOptions()
                            .withTextAnchor(TextAnchor.CENTER)
                            .withIconImage(bitmapBLueLocation)
                            .withPoint(pickupPoint);
                    pointAnnotationManager.create(pickUpPointAnnotationOptions);

                    PointAnnotationOptions dropOffPointAnnotationOptions = new PointAnnotationOptions()
                            .withTextAnchor(TextAnchor.CENTER)
                            .withIconImage(bitmapRedLocation)
                            .withPoint(dropOffPoint);
                    pointAnnotationManager.create(dropOffPointAnnotationOptions);

                    fetchRoute(pickupPoint, dropOffPoint);
                } else {
                    // Node is empty
                    hasBooked = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors, if any
            }
        });
    }







    private Bitmap getBitmapFromVectorDrawable(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
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







//    Show Line direction
    @SuppressLint("MissingPermission")
    private void fetchRoute(Point pickUpPoint, Point dropOffPoint) {
        //      set progress bar visibility to Visible
        progressBar.setVisibility(View.VISIBLE);
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(MainActivity.this);
        locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                Location location = result.getLastLocation();
                RouteOptions.Builder builder = RouteOptions.builder();

                builder.coordinatesList(Arrays.asList(pickUpPoint, dropOffPoint));
                builder.alternatives(false);
                builder.profile(DirectionsCriteria.PROFILE_DRIVING);
                builder.bearingsList(Arrays.asList(Bearing.builder().angle(location.getBearing()).degrees(45.0).build(), null));
                applyDefaultNavigationOptions(builder);

                mapboxNavigation.requestRoutes(builder.build(), new NavigationRouterCallback() {
                    @Override
                    public void onRoutesReady(@NonNull List<NavigationRoute> list, @NonNull RouterOrigin routerOrigin) {
                        mapboxNavigation.setNavigationRoutes(list);
                        //      set progress bar visibility to gone
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(@NonNull List<RouterFailure> list, @NonNull RouteOptions routeOptions) {
                        Toast.makeText(MainActivity.this, "Route request failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCanceled(@NonNull RouteOptions routeOptions, @NonNull RouterOrigin routerOrigin) {

                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                //      set progress bar visibility to gone
                progressBar.setVisibility(View.GONE);
            }
        });
    }








//    Show pick up info
    private void pickLocationInfo(Point point) {
        //      set progress bar visibility to gone
        progressBar.setVisibility(View.GONE);

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
        EditText notes = dialogView.findViewById(R.id.pick_up_notes);

        Address address = getGeoCode(point);
        String locationName = address.getAddressLine(0);
        tvLocationName.setText(locationName);

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
        //      set progress bar visibility to gone
        progressBar.setVisibility(View.GONE);

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

        Address address = getGeoCode(point);
        String locationName = address.getAddressLine(0);
        tvLocationName.setText(locationName);

        // Set click listener for the "OK" button
        Button dialogButtonOK = dialogView.findViewById(R.id.btn_choose_point);
        // set click listener for the cancel button
        Button dialogButtonCancel = dialogView.findViewById(R.id.btn_cancel);

        dialogButtonOK.setOnClickListener(v -> {
            // Handle button click (dismiss the dialog or perform other actions)
            dropOffLocationName = locationName;
            dropOffPoint = point;
            confirmBooking();
            bottomDialog.dismiss();
        });

        dialogButtonCancel.setOnClickListener(v -> {

            // Use the Builder class for convenient dialog construction.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Discard booking?")
                    .setPositiveButton("Yes", (dialog, id) -> {
                        isProceed = false;
                        pointAnnotationManager.deleteAll();
                    })
                    .setNegativeButton("No", (dialog, id) -> dialog.dismiss());
            builder.setCancelable(false);
            // Create the AlertDialog object and return it.
            builder.create();
            builder.show();
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
        bottomDialog.setCancelable(false);
        bottomDialog.setContentView(dialogView);

        // Set the dialog to appear at the bottom of the screen
        Window window = bottomDialog.getWindow();
        if (window != null) {
            window.setGravity(android.view.Gravity.BOTTOM);
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

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
        TextView tvDropLocationName = dialogView.findViewById(R.id.drop_location_name);

        tvPickUpLocationName.setText(pickUpLocationName);
        tvDropLocationName.setText(dropOffLocationName);

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

        bottomDialog.setOnDismissListener(d -> {
            isProceed = false;
        });

        // Show the bottom dialog
        bottomDialog.show();
    }




    private void bookNow() {
        //      set progress bar visibility to visible
        progressBar.setVisibility(View.VISIBLE);

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
        //      set progress bar visibility to gone
        findViewById(R.id.rl_progress_bar_container).setVisibility(View.GONE);
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







    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapboxNavigation.onDestroy();
        mapboxNavigation.unregisterRoutesObserver(routesObserver);
        mapboxNavigation.unregisterLocationObserver(locationObserver);
    }
}