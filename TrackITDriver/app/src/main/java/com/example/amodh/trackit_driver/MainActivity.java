package com.example.amodh.trackit_driver;

import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    //Vars
    private static final int PERMISSIONS_REQUEST = 1;
    private Button buttonGetLocation, btnEndDuty, btnGetDuty;
    private String driverID, driverName, key, value, mBusNumber, latitudePosition, longitudePosition, driverIDFetch, mDriverIDFetch;
    private TextView welcomeText, textBusNumber, textBusLicensePlateNumber, infoText, infoText2;
    private DatabaseReference mDatabase;
    private HashMap<String, Map.Entry<String, String>> driverInfoAll = new HashMap<String, Map.Entry<String, String>>();
    //End Vars


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeVariables();

        setWelcomeMessage();

        checkGPSIsEnabled();

        fetchInfoFromDatabase();

        clickGetDuty();

        clickButtonStartDuty();

        clickButtonEndDuty();

        beforeClickingGetDuty();
    }

    private void beforeClickingGetDuty() {
        infoText.setText("Please click on Get Duty to see if you have any bus assigned.");
        textBusNumber.setText("");
        textBusNumber.setVisibility(View.INVISIBLE);
        textBusLicensePlateNumber.setText("");
        textBusLicensePlateNumber.setVisibility(View.INVISIBLE);
        infoText2.setText("");
    }

    private void clickGetDuty() {
        btnGetDuty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map.Entry<String, String> namePass = driverInfoAll.get(driverID);
                if(namePass != null){
                    String busLicNo = namePass.getKey();
                    String busNo = namePass.getValue();
                    infoText.setText("You are assigned to Bus Number:");
                    infoText2.setText("Bus License Plate Number: ");
                    infoText2.setVisibility(View.VISIBLE);
                    textBusLicensePlateNumber.setText(busLicNo);
                    textBusLicensePlateNumber.setVisibility(View.VISIBLE);
                    textBusNumber.setText(busNo);
                    textBusNumber.setVisibility(View.VISIBLE);
                } else {
                    infoText.setText("You are not assigned to any bus at this moment");
                    textBusNumber.setText("");
                    textBusNumber.setVisibility(View.INVISIBLE);
                    textBusLicensePlateNumber.setText("");
                    textBusLicensePlateNumber.setVisibility(View.INVISIBLE);
                    infoText2.setText("");
                }
            }
        });
    }

    private void fetchInfoFromDatabase() {
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                checkDriverDuty(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                checkDriverDuty(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                checkDriverDuty(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkDriverDuty(DataSnapshot dataSnapshot) {
        key = dataSnapshot.getKey();
        value = dataSnapshot.getValue(String.class);
        String [] separateText = value.split(",");
        String busNumber = separateText[0];
        latitudePosition = separateText[1];
        longitudePosition = separateText[2];
        driverIDFetch = separateText[3];
        mBusNumber = busNumber;
        mDriverIDFetch = driverIDFetch;
        Map.Entry<String, String> pair = new AbstractMap.SimpleEntry<>(key, mBusNumber);
        driverInfoAll.put(mDriverIDFetch, pair);
    }

    private void clickButtonEndDuty() {
        btnEndDuty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(infoText.getText() == "You are not assigned to any bus at this moment"){
                    Toast.makeText(MainActivity.this, "You are not assigned to any bus at this moment", Toast.LENGTH_LONG).show();
                } else {
                    endDutyConfirmation();
                }
            }
        });
    }

    private void endDutyConfirmation() {
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Application")
                .setMessage("Are you sure you want to End Duty?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Buses" + "/" + textBusLicensePlateNumber.getText().toString().trim());
                        ref.setValue("0,0.0,0.0,0");
                        finish();
                        System.exit(0);
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void setWelcomeMessage() {
        welcomeText.setText("Welcome " + driverName);
    }

    private void initializeVariables() {
        buttonGetLocation = (Button) findViewById(R.id.buttonGetLocation);
        textBusNumber = (TextView) findViewById(R.id.busNumberText);
        textBusLicensePlateNumber = (TextView) findViewById(R.id.busLicensePlateNumberText);
        infoText = (TextView) findViewById(R.id.infoText);
        infoText2 = (TextView) findViewById(R.id.infoText2);
        welcomeText = (TextView) findViewById(R.id.welcomeText);
        btnEndDuty = (Button) findViewById(R.id.btnEndDuty);
        btnGetDuty = (Button) findViewById(R.id.btnGetDuty);
        driverName = getIntent().getExtras().getString("DNAME", "Default");
        driverID = getIntent().getExtras().getString("DID", "Default");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Buses");
    }

    private void clickButtonStartDuty() {
        buttonGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check location permission is granted - if it is, start the service, otherwise request the permission
                int permission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // Check bus number and bus license plate number is not empty
                    if (infoText.getText() == "You are not assigned to any bus at this moment"){
                        Toast.makeText(MainActivity.this, "You are not assigned to any bus at this moment", Toast.LENGTH_LONG).show();
                    } else {
                        startTrackerService();
                    }
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST);
                }
            }
        });
    }

    private void checkGPSIsEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startTrackerService() {
        startService(new Intent(this, TrackerService.class)
                .putExtra("BUSLICNO", textBusLicensePlateNumber.getText().toString().trim())
                .putExtra("BUSNO", textBusNumber.getText().toString().trim())
                .putExtra("DID", driverID));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Start the service when the permission is granted
            startTrackerService();
        } else {
            finish();
        }
    }
}
