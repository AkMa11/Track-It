package com.example.amodh.trackit_driver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText driverID, driverPassword;
    private Button btnLogin;
    private DatabaseReference mDatabase;
    private String key, value, getDriverName;
    private HashMap<String, String> driverInfoAll = new HashMap<>();
    private HashMap<String, String> driverIDPass = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeVaribales();

        clickLogin();

        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchIdAndPasswordFromDatabase(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchIdAndPasswordFromDatabase(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                fetchIdAndPasswordFromDatabase(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void clickLogin() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFieldsNotEmpty();
            }
        });
    }

    private void checkFieldsNotEmpty() {
        if(driverID.getText().toString().matches("") || driverPassword.getText().toString().matches("")){
            Toast.makeText(LoginActivity.this, "Please enter your ID and Password", Toast.LENGTH_SHORT).show();
        } else {
            String password = driverInfoAll.get(driverID.getText().toString().trim());
            verifyIdAndPassword(password);
        }
    }

    private void verifyIdAndPassword(String password) {
        if(driverPassword.getText().toString().matches(password)){
            startMainActivity();
        } else {
            Toast.makeText(LoginActivity.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("DID", driverID.getText().toString().trim());
        getDriverName = driverIDPass.get(driverID.getText().toString().trim());
        intent.putExtra("DNAME", getDriverName);
        startActivity(intent);
        finish();
    }

    private void fetchIdAndPasswordFromDatabase(DataSnapshot dataSnapshot) {
        key = dataSnapshot.getKey();
        value = dataSnapshot.getValue(String.class);
        String [] separateText = value.split(",");
        String driverNameFetch = separateText[0];
        String driverPassFetch = separateText[1];
        String mDriverName = driverNameFetch;
        String mDriverPassword = driverPassFetch;
        driverInfoAll.put(key, mDriverPassword);
        driverIDPass.put(key, mDriverName);
    }

    private void initializeVaribales() {
        driverID = (EditText) findViewById(R.id.driverID);
        driverPassword = (EditText) findViewById(R.id.driverPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Drivers");
    }
}
