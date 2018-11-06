package com.example.tristangriffin.projectx.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

        Log.d("demo", "USER: " + firebaseCommands.user);
        if (firebaseCommands.user != null) {
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

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
                signIn();
            }
        });
    }

    private void signIn() {
        if (validate()) {
            firebaseCommands.signIn(mEmail.getText().toString(), mPassword.getText().toString(),
                    SignInActivity.this, new OnSignInListener() {
                        @Override
                        public void onSignIn(FirebaseUser user) {
                            progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.fragment_enter_from_right, R.anim.fragment_exit_to_left);
                        }

                        @Override
                        public void failedSignIn() {
                            progressBar.setVisibility(View.GONE);
                            mEmail.setError("Possibly Wrong");
                            mPassword.setError("Possibly Wrong");
                        }
                    });
        }
    }

    private boolean validate() {
        boolean valid = true;

        String email = mEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Required");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Required");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }
}
