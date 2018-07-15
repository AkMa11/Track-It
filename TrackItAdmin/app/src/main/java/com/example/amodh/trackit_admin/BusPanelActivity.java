package com.example.amodh.trackit_admin;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class BusPanelActivity extends AppCompatActivity {

    private AutoCompleteTextView busLicenseNumber;
    private EditText busNumberText, driverIdText;
    private Button btnAddBus, btnDeleteBus, btnAssign, btnUnassign;
    private FloatingActionButton fabToggle;
    private TextView statusText;
    private ImageView dropDownImage;
    private String key, value;
    private DatabaseReference mDatabase, mDatabaseDrivers;
    private Double mLatitudePosition, mLongitudePosition;
    private ArrayList<String> busLicArray = new ArrayList<String>();
    private HashMap<String, String> driverIdMap = new HashMap<>();
    private HashMap<String, Double> locationMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_panel);

        initializeVariables();

        toggleToDriverPanel();

        fetchBuses();

        busLicenseNumbersDropdown();

        fetchBusStatus();

        clickAddBus();

        clickDeleteBus();

        clickAssign();

        clickUnassign();

    }

    private void clickUnassign() {
        btnUnassign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (busLicenseNumber.getText().toString().matches("")) {
                    Toast.makeText(BusPanelActivity.this, "Enter Bus License Plate Number", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference
                            ("Buses" + "/" + busLicenseNumber.getText().toString().trim());
                    ref.setValue("0,0.0,0.0,0");
                    Toast.makeText(BusPanelActivity.this, "Bus Reset Successfully", Toast.LENGTH_SHORT).show();
                    busNumberText.setText("");
                    driverIdText.setText("");
                }
            }
        });
    }

    private void clickAssign() {
        btnAssign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (busNumberText.getText().toString().matches("")
                        || driverIdText.getText().toString().matches("")
                        || busLicenseNumber.getText().toString().matches("")) {
                    Toast.makeText(BusPanelActivity.this, "Enter Bus number and Driver ID", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference
                            ("Buses" + "/" + busLicenseNumber.getText().toString().trim());
                    ref.setValue(busNumberText.getText().toString().trim() + "," + "0.0" + "," + "0.0" + "," + driverIdText.getText().toString().trim());
                    Toast.makeText(BusPanelActivity.this, "Bus Assigned to Driver Successfully", Toast.LENGTH_SHORT).show();
                    busNumberText.setText("");
                    driverIdText.setText("");
                }
            }
        });
    }

    private void fetchBusStatus() {
        busLicenseNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 10) {
                    if (driverIdMap.containsKey(busLicenseNumber.getText().toString().trim())) {
                        String getDriverID = driverIdMap.get(busLicenseNumber.getText().toString().trim());
                        Double getStatus = locationMap.get(busLicenseNumber.getText().toString().trim());
                        if (getStatus.toString().matches("0.0")) {
                            statusText.setText("Status: Off Duty");
                        }  else {
                            statusText.setText("Status: On Duty" + " | " + "Driver ID: " + getDriverID);
                        }
                    } else if(busLicenseNumber.getText().toString().matches("")){
                        statusText.setText("");
                    } else {
                        statusText.setText("New bus license plate number detected");
                    }
                }
            }
        });
    }

    private void fetchBuses() {
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchBusesFromDatabase(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                fetchBusesFromDatabase(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchBusesFromDatabase(DataSnapshot dataSnapshot) {
        key = dataSnapshot.getKey();
        busLicArray.add(key);
        value = dataSnapshot.getValue(String.class);
        String [] separateText = value.split(",");
        String busNumber = separateText[0];
        String latitudePosition = separateText[1];
        String longitudePosition = separateText[2];
        String driverID = separateText[3];
        String mBusNumber = busNumber;
        mLatitudePosition = Double.parseDouble(latitudePosition);
        mLongitudePosition = Double.parseDouble(longitudePosition);
        String mDriverID = driverID;
        driverIdMap.put(key, mDriverID);
        locationMap.put(key, mLatitudePosition);
    }

    private void busLicenseNumbersDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, busLicArray);
        busLicenseNumber.setAdapter(adapter);

        dropDownImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                busLicenseNumber.showDropDown();
            }
        });
        busLicenseNumber.setThreshold(1);
    }

    private void clickDeleteBus() {
        btnDeleteBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAllFieldsAreFilledToRemoveBus();
            }
        });
    }

    private void checkAllFieldsAreFilledToRemoveBus() {
        if(busLicenseNumber.getText().toString().matches("")){
            Toast.makeText(BusPanelActivity.this, "Enter a bus license number", Toast.LENGTH_SHORT).show();
        } else{
            removeBus();
        }
    }

    private void removeBus() {
        FirebaseDatabase.getInstance().getReference("Buses")
                .child(busLicenseNumber.getText().toString().trim()).removeValue();
        busLicenseNumber.setText("");
        Toast.makeText(BusPanelActivity.this, "Bus Removed", Toast.LENGTH_SHORT).show();
    }

    private void clickAddBus() {
        btnAddBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAllFieldsAreFilledToAddBus();
            }
        });
    }

    private void checkAllFieldsAreFilledToAddBus() {
        if(busLicenseNumber.getText().toString().matches("")){
            Toast.makeText(BusPanelActivity.this, "Enter a bus license number", Toast.LENGTH_SHORT).show();
        } else{
            addBus();
        }
    }

    private void addBus() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference
                ("Buses" + "/" + busLicenseNumber.getText().toString().trim());
        ref.setValue("0,0.0,0.0,0");
        Toast.makeText(BusPanelActivity.this, "Bus Added", Toast.LENGTH_SHORT).show();
    }

    private void toggleToDriverPanel() {
        fabToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusPanelActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initializeVariables() {
        busLicenseNumber = (AutoCompleteTextView) findViewById(R.id.busLicenseNumber);
        busNumberText = findViewById(R.id.busNumberText);
        driverIdText = findViewById(R.id.driverIdText);
        btnAssign = findViewById(R.id.btnAssign);
        btnUnassign = findViewById(R.id.btnUnassign);
        btnAddBus = (Button) findViewById(R.id.btnAddBus);
        btnDeleteBus = (Button) findViewById(R.id.btnDeleteBus);
        fabToggle = (FloatingActionButton) findViewById(R.id.fabToggle);
        dropDownImage = (ImageView) findViewById(R.id.dropDownImage);
        statusText = (TextView) findViewById(R.id.statusText);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Buses");
        mDatabaseDrivers = FirebaseDatabase.getInstance().getReference().child("Drivers");
    }
}
