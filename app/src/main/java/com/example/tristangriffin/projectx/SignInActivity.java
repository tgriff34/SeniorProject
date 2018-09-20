package com.example.tristangriffin.projectx;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.signin.SignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private ProgressDialog progressBar;

    private static final int REQUEST_SIGNUP = 0;
    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mEmail = (EditText) findViewById(R.id.edit_email);
        mPassword = (EditText) findViewById(R.id.edit_password);
        progressBar = new ProgressDialog(SignInActivity.this);

        findViewById(R.id.view_signUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this , SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
        findViewById(R.id.button_signin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseCommands.signIn(mEmail.getText().toString(), mPassword.getText().toString(),
                        SignInActivity.this, new OnSignInListener() {
                    @Override
                    public void onSignIn(FirebaseUser user) {
                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }
}
