package com.azizorkun.instablog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextView toptext, conditions;
    private EditText email, password;
    private FirebaseAuth mAuth;
    private Button login;
    private ProgressDialog pdialog;
    private final static int RC_SIGN_IN = 2;
    CallbackManager mCallbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        toptext = findViewById(R.id.toptext);
        mAuth = FirebaseAuth.getInstance();

        mCallbackManager = CallbackManager.Factory.create();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signinwithmail();
            }
        });



    }

    private void signinwithmail(){
        String useremail = email.getText().toString();
        String userpassword = password.getText().toString();

        if (TextUtils.isEmpty(useremail)){
            email.setError("Lütfen mailinizi giriniz.");
        }else if (!useremail.contains("@") || !useremail.contains(".")){
            email.setError("Lütfen geçerli bir mail giriniz.");
        }else if (TextUtils.isEmpty(userpassword)){
            password.setError("Lütfen şifrenizi giriniz.");
        } else {
            pdialog = new ProgressDialog(LoginActivity.this, R.style.MyAlertDialogStyle);
            pdialog.setMessage("Lütfen bekleyin...");
            pdialog.setIndeterminate(true);
            pdialog.setCancelable(false);
            pdialog.setCanceledOnTouchOutside(false);
            pdialog.show();

            mAuth.signInWithEmailAndPassword(useremail, userpassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                startactivity();
                                pdialog.dismiss();
                            } else {
                                String message = task.getException().toString();
                                if (message.contains("password is invalid")){
                                    pdialog.dismiss();
                                    Toast.makeText(LoginActivity.this, "Email veya şifre hatalı.", Toast.LENGTH_LONG).show();
                                }else if (message.contains("There is no user")){
                                    pdialog.dismiss();
                                    Toast.makeText(LoginActivity.this, "Böyle bir hesap yok.", Toast.LENGTH_LONG).show();
                                }else {
                                    pdialog.dismiss();
                                    Toast.makeText(LoginActivity.this, "Giriş yapılamadı!", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
        }
    }





    private void startactivity(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}
