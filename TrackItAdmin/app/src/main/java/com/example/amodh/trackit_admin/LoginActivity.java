package com.example.amodh.trackit_admin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText username, password;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeVariables();

        clickLogin();


    }

    private void clickLogin() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUsernamePasswordAreEmpty();
            }
        });
    }

    private void checkUsernamePasswordAreEmpty() {
        if(username.getText().toString().matches("")
                || password.getText().toString().matches("")){
            Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
        } else {
            checkUsernamePasswordAreCorrect();
        }
    }

    private void checkUsernamePasswordAreCorrect() {
        if(username.getText().toString().trim().matches("Admin")
                && password.getText().toString().trim().matches("pass123")){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Username or Password is wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeVariables() {
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
    }
}
