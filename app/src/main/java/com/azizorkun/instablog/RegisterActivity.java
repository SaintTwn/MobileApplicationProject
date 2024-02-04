package com.azizorkun.instablog;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    private TextView toptext, conditions;
    private Button signup;
    private ImageView avatar;
    private EditText fullname, email, password, secondpassword;
    private Uri mainImageUri;
    private FirebaseAuth mAuth;
    private ProgressDialog pdialog;
    private StorageReference storageReference;
    public static final int PICK_IMAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        avatar = findViewById(R.id.avatar);
        fullname = findViewById(R.id.fullname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        secondpassword = findViewById(R.id.cnfpassword);
        signup = findViewById(R.id.signup);
        toptext = findViewById(R.id.toptext);



        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        pickimage();
                    }
                } else {
                    pickimage();
                }
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = fullname.getText().toString();
                final String mail = email.getText().toString();
                final String pwd = password.getText().toString();
                final String cnfpwd = secondpassword.getText().toString();
                if (checkfields(name, mail, pwd, cnfpwd)) {
                    pdialog = new ProgressDialog(RegisterActivity.this, R.style.MyAlertDialogStyle);
                    pdialog.setMessage("Kayıt olunuyor...");
                    pdialog.setIndeterminate(true);
                    pdialog.setCanceledOnTouchOutside(false);
                    pdialog.setCancelable(false);
                    pdialog.show();
                    createuser(mail, pwd, name);
                }
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            if (data != null) {
                mainImageUri = data.getData();
                avatar.setImageURI(mainImageUri);
            } else {
                Toast.makeText(this, "Fotoğraf yüklenemedi...", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void createuser(final String email, final String password, final String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            signin(email, password, name);
                        } else {
                            if (task.getException().toString().contains("already in use")) {
                                pdialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "Zaten hesabın var. Lütfen giriş yap.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Bir hata oldu. :(", Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                });

    }

    private boolean checkfields(String name, String mail, String pwd, String cnfpwd) {

        if (TextUtils.isEmpty(name)) {
            fullname.setError("İsmini girmelisin!");
        }
        if (TextUtils.isEmpty(mail)) {
            email.setError("Email girmelisin!");
        }
        if (TextUtils.isEmpty(pwd)) {
            password.setError("Şifre girmelisin!");
        }
        if (TextUtils.isEmpty(cnfpwd)) {
            secondpassword.setError("Lütfen şifreni tekrar gir!");
        } else if (name.length() < 3) {
            fullname.setError("İsim 3 karakterden fazla olmalı");
        } else if (!mail.contains("@") || !mail.contains(".")) {
            email.setError("Geçerli mir mail girmelisin!");
        } else if (pwd.length() < 6) {
            password.setError("Şifren en az 5 karakterden oluşmalı!");
        } else if (!cnfpwd.matches(pwd)) {
            password.setError("Şifrelerin eşleşmiyor! :(");
            secondpassword.setError("Şifrelerin eşleşmiyor! :(");
        }

        else if (mainImageUri == null) {
            Toast.makeText(RegisterActivity.this, "Profil resmini seçmeyi unuttun!", Toast.LENGTH_LONG).show();
        }

        else {
            return true;
        }

        return false;
    }

    private void signin(String email, String password, final String name) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    uploadimage(name);
                } else {
                    pdialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Bir hata meydana geldi. :(", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void uploadimage(final String name) {
        final String userid = mAuth.getCurrentUser().getUid();
        StorageReference riversRef = storageReference.child("profile_images/" + userid + ".png");
        riversRef.putFile(mainImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String url = String.valueOf(taskSnapshot.getDownloadUrl());
                        updateprofile(name, url);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        pdialog.dismiss();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                });


    }

    private void updateprofile(final String name, final String url) {
        UserProfileChangeRequest profileupdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(Uri.parse(url))
                .build();

        mAuth.getCurrentUser().updateProfile(profileupdates).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            saveUserDetails(name, url);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Bir hata meydana geldi. :(", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

    }

    private void saveUserDetails(String name, String url) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("url", url);


        FirebaseFirestore.getInstance().collection("Users").document(mAuth.getCurrentUser().getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Bir hata meydana geldi. :(", Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void pickimage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }
}
