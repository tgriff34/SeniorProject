package com.example.tristangriffin.projectx;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class SignUpActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mEmail = findViewById(R.id.edit_createemail);
        mPassword = findViewById(R.id.edit_createpassword);

        findViewById(R.id.button_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp(mEmail.getText().toString(), mPassword.getText().toString());
            }
        });
    }

    private void signUp(String email, String password) {
        Log.d("demo", email);
        if (!validate()) {
            return;
        }

        firebaseCommands.createUser(email, password, this);
        setResult(RESULT_OK, null);
        finish();

    }

    public boolean validate() {
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
