package com.example.amodh.trackit_bustracker;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Vars
    private static final int PERMISSIONS_REQUEST = 1;

    private FloatingActionButton goToMyLocation;
    private DatabaseReference mDatabase;
    private ArrayList<String> busesArray = new ArrayList<>();
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private String key, value, latitudePosition, longitudePosition, driverID, mBusNumber, busNumberTitle;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Double mLatitudePosition, mLongitudePosition, userLat, userLng;
    private LatLng selectedLatLng;
    private AutoCompleteTextView busNumberText;
    private ImageView dropDownImage;
    private ArrayList<String> busNumberArray = new ArrayList<String>();
    private HashMap<String, LatLng> busNumberLocationMap;
    private boolean isLocationLoaded = false;
    private Marker marker, markerBus;
    //End vars


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeVariables();

        checkGPSIsEnabled();

        checkGoogleServicesAreAvailable();

        clickResetLocationButton();

        dropDownBusNumbers();


    }

    private void dropDownBusNumbers() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, busNumberArray);
        busNumberText.setAdapter(adapter);

        dropDownImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                busNumberText.showDropDown();
            }
        });
        busNumberText.setThreshold(1);
    }

    private void initializeVariables() {
        goToMyLocation = (FloatingActionButton) findViewById(R.id.goToMyLocation);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Buses");
        dropDownImage = findViewById(R.id.dropDownImage);
        busNumberText = findViewById(R.id.busNumber);
        busNumberArray.add("Show all buses");
    }

    private void clickResetLocationButton() {
        goToMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLocationLoaded = false;
                requestLocationUpdates();
            }
        });
    }

    private void checkGoogleServicesAreAvailable() {
        if(googleServicesAvailable()){
            initMap();
        } else {
            Toast.makeText(this, "Error with google maps", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkGPSIsEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(100);
        request.setFastestInterval(50);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        userLat = location.getLatitude(); //set user Latitude to userLat
                        userLng = location.getLongitude(); //set user Longitude to userLng
                        LatLng latLng = new LatLng(userLat, userLng);
                        moveCamera(latLng);
                        setMarker(latLng);
                    } else {
                        Toast.makeText(MainActivity.this, "Couldn't find your location", Toast.LENGTH_LONG).show();
                    }
                }
            }, null);
        }
    }

    private void moveCamera(LatLng latLng) {
        while(isLocationLoaded == false){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16 ));
            isLocationLoaded = true;
        }
    }

    private void removeExistingUserLocationMarker() {
        if(marker != null){
            marker.remove();
        }
    }

    private void setMarker(LatLng latLng) {
        removeExistingUserLocationMarker();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker = mMap.addMarker(markerOptions);
    }


    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    public boolean googleServicesAvailable(){
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS){
            return true;
        } else {
            if (api.isUserResolvableError(isAvailable)){
                Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
                dialog.show();;
            } else {
                Toast.makeText(this, "Can't connect to google play services", Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationInternetPermissionsFromManifest();
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setPadding(0, 250, 0, 0);
        fetchBusNumberList();
        selectBusesToShowFromTheDropdown();
    }

    private void selectBusesToShowFromTheDropdown() {
        busNumberText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                showBuses();
            }
        });
    }

    private void showBuses() {
        if(busNumberText.getText().toString().trim().matches("Show all buses")) {
            mMarkers.clear();
            subscribeToUpdates();
        } else {
            clearBusMarkers();
            subscribeToUpdatesAgain();
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10 ));
        }
    }

    private void clearBusMarkers() {
        if(markerBus != null) {
            mMarkers.clear();
            mMap.clear();
            requestLocationUpdates();
        }
    }

    private void checkLocationInternetPermissionsFromManifest() {
        int permission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }
    }

    private void subscribeToUpdates() {
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                setMarkerOfAllBuses(dataSnapshot);
            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, String s) {
                setMarkerOfAllBuses(dataSnapshot);
            }

            @Override
            public void onChildRemoved(final DataSnapshot dataSnapshot) {
                setMarkerOfAllBuses(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void subscribeToUpdatesAgain() {
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                setMarkerOfSelectedBuses(dataSnapshot);
            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, String s) {
                setMarkerOfSelectedBuses(dataSnapshot);
            }

            @Override
            public void onChildRemoved(final DataSnapshot dataSnapshot) {
                setMarkerOfSelectedBuses(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchBusNumberList(){
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchDataFromDatabase(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                fetchDataFromDatabase(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchDataFromDatabase(DataSnapshot dataSnapshot) {
        key = dataSnapshot.getKey();
        value = dataSnapshot.getValue(String.class);
        String [] separateText = value.split(",");
        String busNumber = separateText[0];
        latitudePosition = separateText[1];
        longitudePosition = separateText[2];
        driverID = separateText[3];
        mBusNumber = busNumber;
        if(!busNumberArray.contains(mBusNumber)){
            busNumberArray.add(mBusNumber);
        }
        if(mBusNumber.matches("0")) {
            busNumberArray.remove("0");
        }
        mLatitudePosition = Double.parseDouble(latitudePosition);
        mLongitudePosition = Double.parseDouble(longitudePosition);
    }

    private void setMarkerOfAllBuses(DataSnapshot dataSnapshot) {
        key = dataSnapshot.getKey();
        value = dataSnapshot.getValue(String.class);
        String [] separateText = value.split(",");
        String busNumber = separateText[0];
        latitudePosition = separateText[1];
        longitudePosition = separateText[2];
        driverID = separateText[3];
        mBusNumber = busNumber;
        mLatitudePosition = Double.parseDouble(latitudePosition);
        mLongitudePosition = Double.parseDouble(longitudePosition);

        final LatLng location = new LatLng(mLatitudePosition, mLongitudePosition);

        markerBus = mMap.addMarker(new MarkerOptions()
                .title(mBusNumber)
                .position(location)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus3)));


        if (!mMarkers.containsKey(key)) {
            mMarkers.put(key, markerBus);
        } else {
            if(markerBus != null) {
                markerBus.remove();
                mMarkers.get(key).setPosition(location);
            }
        }

        for (Marker marker : mMarkers.values()) {
            if (marker.getTitle().equals("0")) {
                marker.setVisible(false);
            } else {
                marker.setVisible(true);
            }
        }



    }

    private void setMarkerOfSelectedBuses(DataSnapshot dataSnapshot) {
        key = dataSnapshot.getKey();
        value = dataSnapshot.getValue(String.class);
        String [] separateText = value.split(",");
        String busNumber = separateText[0];
        latitudePosition = separateText[1];
        longitudePosition = separateText[2];
        driverID = separateText[3];
        mBusNumber = busNumber;
        mLatitudePosition = Double.parseDouble(latitudePosition);
        mLongitudePosition = Double.parseDouble(longitudePosition);

        final LatLng location = new LatLng(mLatitudePosition, mLongitudePosition);

        markerBus = mMap.addMarker(new MarkerOptions()
                .title(mBusNumber)
                .position(location)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus3)));


        if (!mMarkers.containsKey(key)) {
            mMarkers.put(key, markerBus);
        } else {
            if(markerBus != null) {
                markerBus.remove();
                mMarkers.get(key).setPosition(location);
            }
        }

        for (Marker marker : mMarkers.values()) {
            if (marker.getTitle().equalsIgnoreCase(busNumberText.getText().toString().trim())) {
                marker.setVisible(true);
            } else {
                marker.setVisible(false);
            }
        }
    }
}