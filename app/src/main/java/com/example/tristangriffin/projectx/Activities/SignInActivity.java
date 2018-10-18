package com.example.tristangriffin.projectx.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.Listeners.OnSignInListener;
import com.example.tristangriffin.projectx.R;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private ProgressBar progressBar;

    private static final int REQUEST_SIGNUP = 0;
    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mEmail = (EditText) findViewById(R.id.edit_email);
        mPassword = (EditText) findViewById(R.id.edit_password);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        findViewById(R.id.view_signUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
        findViewById(R.id.button_signin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                firebaseCommands.signIn(mEmail.getText().toString(), mPassword.getText().toString(),
                        SignInActivity.this, new OnSignInListener() {
                            @Override
                            public void onSignIn(FirebaseUser user) {
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void failedSignIn() {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(SignInActivity.this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
