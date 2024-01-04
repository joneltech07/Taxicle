package com.example.taxicle;

import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.addOnMapClickListener;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;
import static com.mapbox.navigation.base.extensions.RouteOptionsExtensions.applyDefaultNavigationOptions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taxicle.constructors.Booking;
import com.example.taxicle.data_access_object.DAOBooking;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
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
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
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
import com.mapbox.search.ui.view.SearchResultsView;

import java.util.Arrays;
import java.util.List;

public class Booked extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;

    private MapView mapView;

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

    private RelativeLayout progressBar;

    private PointAnnotationManager pointAnnotationManager;

    private Drawable drawableRedLocation, drawableBlueLocation;
    private Bitmap bitmapRedLocation, bitmapBLueLocation;
    private AnnotationPlugin annotationPlugin;


    private Button btnCancelBooked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked);

        try {

            progressBar = findViewById(R.id.rl_progress_bar_container);
            btnCancelBooked = findViewById(R.id.btn_cancel);


//      initialize drawable sources and bitmaps
            drawableRedLocation = ContextCompat.getDrawable(this, R.drawable.location_red);
            assert drawableRedLocation != null;
            bitmapRedLocation = getBitmapFromVectorDrawable(drawableRedLocation);


            drawableBlueLocation = ContextCompat.getDrawable(this, R.drawable.location_blue);
            assert drawableBlueLocation != null;
            bitmapBLueLocation = getBitmapFromVectorDrawable(drawableBlueLocation);


            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();


            LinearLayout bookingInfo = findViewById(R.id.ll_content);
            TextView tvEmptyBooking = findViewById(R.id.tv_no_booking);


            mapView = findViewById(R.id.mapView);
            mapView.getMapboxMap().loadStyleUri("mapbox://styles/jltolentino/clpxx8g5j00jr01re4o8x833g", style -> {

            });


            annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
            pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, new AnnotationConfig());


            MapboxRouteLineOptions options = new MapboxRouteLineOptions.Builder(this).withRouteLineResources(new RouteLineResources.Builder().build())
                    .withRouteLineBelowLayerId("road-label-navigation").build();
            routeLineView = new MapboxRouteLineView(options);
            routeLineApi = new MapboxRouteLineApi(options);

            NavigationOptions navigationOptions = new NavigationOptions.Builder(this).accessToken(getString(R.string.mapbox_access_token)).build();

            MapboxNavigationApp.setup(navigationOptions);
            mapboxNavigation = new MapboxNavigation(navigationOptions);

            mapboxNavigation.registerRoutesObserver(routesObserver);
            mapboxNavigation.registerLocationObserver(locationObserver);


            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Booking.class.getSimpleName());
            try {
                databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            if (snapshot.exists()) {
                                tvEmptyBooking.setVisibility(View.GONE);
                                bookingInfo.setVisibility(View.VISIBLE);

                                Booking booking = snapshot.getValue(Booking.class);

                                assert booking != null;
                                String pickUpLocationName = booking.getPickUplocationName();
                                String dropOffLocationName = booking.getDropOffLocationName();
                                String notes = booking.getNotes();
                                boolean isAccepted = booking.isAccepted();

                                TextView tvPickUpLocationName = findViewById(R.id.pickup_location_name);
                                tvPickUpLocationName.setText(pickUpLocationName);

                                TextView tvDropOffLocationName = findViewById(R.id.drop_location_name);
                                tvDropOffLocationName.setText(dropOffLocationName);

                                TextView tvNotes = findViewById(R.id.pick_up_notes);
                                tvNotes.setText(String.format("notes: %s", notes));

                                TextView status = findViewById(R.id.tv_status);

                                if (isAccepted) {
                                    status.setText("Accepted");
                                    Toast.makeText(Booked.this, "Accepted", Toast.LENGTH_SHORT).show();
                                    btnCancelBooked.setVisibility(View.GONE);
                                }


                                assert booking != null;
                                Point pickupPoint = Point.fromLngLat(booking.getPickUpLongitude(), booking.getPickUpLatitude());
                                Point dropOffPoint = Point.fromLngLat(booking.getDropOffLongitude(), booking.getDropOffLatitude());


//                              Set camera focus on pickup location
                                updateCamera(pickupPoint, 0.0);


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
                                bookingInfo.setVisibility(View.GONE);
                                tvEmptyBooking.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            Toast.makeText(Booked.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } catch (Exception e) {
                Toast.makeText(Booked.this, "Error2: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }



            btnCancelBooked.setOnClickListener(v -> {
                DAOBooking dao = new DAOBooking();
                dao.cancelBooked(user.getUid());
                Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show();
            });


//          Back Button
            findViewById(R.id.ib_back).setOnClickListener(v -> {
                onBackPressed();
            });


            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
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






    //    Show Line direction
    @SuppressLint("MissingPermission")
    private void fetchRoute(Point pickUpPoint, Point dropOffPoint) {
        //      set progress bar visibility to Visible
        progressBar.setVisibility(View.VISIBLE);
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(Booked.this);
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
                        Toast.makeText(Booked.this, "Route request failed", Toast.LENGTH_SHORT).show();
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





    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mapboxNavigation.onDestroy();
        mapboxNavigation.unregisterRoutesObserver(routesObserver);
        mapboxNavigation.unregisterLocationObserver(locationObserver);
    }




    @SuppressLint("Lifecycle")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapboxNavigation.onDestroy();
        mapboxNavigation.unregisterRoutesObserver(routesObserver);
        mapboxNavigation.unregisterLocationObserver(locationObserver);
    }
}