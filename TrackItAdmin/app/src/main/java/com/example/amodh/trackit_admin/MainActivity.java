package com.example.amodh.trackit_admin;

import android.content.Intent;
import android.renderscript.Sampler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.Key;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText driverName, driverPassword;
    private AutoCompleteTextView driverID;
    private Button btnAddDriver, btnRemoveDriver;
    private FloatingActionButton fabToggle;
    private DatabaseReference mDatabase;
    private String key, value;
    private ImageView dropDownImage;
    private ArrayList<String> driverIdArray = new ArrayList<String>();
    private ArrayList<String> driverNamePass = new ArrayList<String>();
    private HashMap<String, Map.Entry<String, String>> driverInfoAll = new HashMap<String, Map.Entry<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeVariables();

        toggleToBusPanel();

        clickAddDriver();

        clickRemoveDriver();

        fetchDrivers();

        driverIdDropdown();

        fetchNameAndPasswordFromSelectedID();

    }

    private void fetchNameAndPasswordFromSelectedID() {
        driverID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()!= 0){
                    Map.Entry<String, String> namePass = driverInfoAll.get(driverID.getText().toString().trim());
                    checkIfIdExists(namePass);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void checkIfIdExists(Map.Entry<String, String> namePass) {
        if(namePass != null){
            fetchNameAndPassword(namePass);
        } else {
            clearFieldsNameAndPassword();
        }
    }

    private void clearFieldsNameAndPassword() {
        driverName.setText("");
        driverPassword.setText("");
    }

    private void fetchNameAndPassword(Map.Entry<String, String> namePass) {
        String name = namePass.getKey();
        String password = namePass.getValue();
        driverName.setText(name);
        driverPassword.setText(password);
    }

    private void fetchDrivers() {
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchDriversFromDatabase(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchDriversFromDatabase(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                fetchDriversFromDatabase(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchDriversFromDatabase(final DataSnapshot dataSnapshot) {
        key = dataSnapshot.getKey();
        driverIdArray.add(key);
        value = dataSnapshot.getValue(String.class);
        String [] separateText = value.split(",");
        String driverNameFetch = separateText[0];
        String driverPassFetch = separateText[1];
        String mDriverName = driverNameFetch;
        String mDriverPassword = driverPassFetch;
        driverNamePass.add(value);
        Map.Entry<String, String> pair = new AbstractMap.SimpleEntry<>(mDriverName, mDriverPassword);
        driverInfoAll.put(key, pair);

    }

    private void driverIdDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, driverIdArray);
        driverID.setAdapter(adapter);

        dropDownImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverID.showDropDown();
            }
        });
        driverID.setThreshold(1);
    }

    private void clickRemoveDriver() {
        btnRemoveDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAllFieldsAreFilledToRemoveDriver();
            }
        });
    }

    private void checkAllFieldsAreFilledToRemoveDriver() {
        if(driverID.getText().toString().matches("")
                || driverName.getText().toString().matches("")
                || driverPassword.getText().toString().matches("")){
            Toast.makeText(MainActivity.this, "Enter Driver ID, Name and Password", Toast.LENGTH_SHORT).show();
        } else{
            removeDriver();
        }
    }

    private void removeDriver() {
        FirebaseDatabase.getInstance().getReference("Drivers")
                .child(driverID.getText().toString().trim()).removeValue();
        Toast.makeText(MainActivity.this, "Driver Removed", Toast.LENGTH_SHORT).show();
    }

    private void clickAddDriver() {
        btnAddDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAllFieldsAreFilledToAddDriver();
            }
        });
    }

    private void checkAllFieldsAreFilledToAddDriver() {
        if(driverID.getText().toString().matches("")
                || driverName.getText().toString().matches("")
                || driverPassword.getText().toString().matches("")){
            Toast.makeText(MainActivity.this, "Enter Driver ID, Name and Password", Toast.LENGTH_SHORT).show();
        } else{
            addDriver();
        }
    }

    private void addDriver() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference
                ("Drivers" + "/" + driverID.getText().toString().trim());
        ref.setValue(driverName.getText().toString().trim() + ","
                + driverPassword.getText().toString().trim());
        Toast.makeText(MainActivity.this, "Driver Added", Toast.LENGTH_SHORT).show();
    }

    private void toggleToBusPanel() {
        fabToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BusPanelActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initializeVariables() {
        driverID = (AutoCompleteTextView) findViewById(R.id.driverID);
        driverName = (EditText) findViewById(R.id.driverName);
        driverPassword = (EditText) findViewById(R.id.driverPassword);
        btnAddDriver = (Button) findViewById(R.id.btnAddDriver);
        btnRemoveDriver = (Button) findViewById(R.id.btnDeleteDriver);
        fabToggle = (FloatingActionButton) findViewById(R.id.fabToggle);
        dropDownImage = (ImageView) findViewById(R.id.dropDownImage);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Drivers");
    }
}
