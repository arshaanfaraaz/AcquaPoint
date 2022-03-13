package com.example.acquapoint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.acquapoint.Helper.Helper;
import com.example.acquapoint.Models.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;

    EditText editTextFullName,editTextEmail,editTextAddress
            ,editTextPassword,editTextConfirmPassword;
    Button buttonRegister;
    TextView textViewLogin;

    Helper helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        helper=new Helper(this);
        initUI();
        initDB();

    }

    private void initUI() {
        editTextFullName=findViewById(R.id.editTextFullName);
        editTextEmail=findViewById(R.id.editTextEmail);
        editTextAddress=findViewById(R.id.editTextAddress);
        editTextPassword=findViewById(R.id.editTextPassword);
        editTextConfirmPassword=findViewById(R.id.editTextConfirmPassword);
        buttonRegister=findViewById(R.id. buttonRegister);
        textViewLogin=findViewById(R.id.textViewLogin);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doRegistration();
            }
        });
    }

    private void doRegistration() {
        UserModel userModel=new UserModel();
        userModel.setName(editTextFullName.getText().toString());
        userModel.setEmail(editTextEmail.getText().toString());
        userModel.setAddress(editTextAddress.getText().toString());

        String password=editTextPassword.getText().toString();
        String confirmPassword=editTextConfirmPassword.getText().toString();

        if(userModel.getEmail().equals("")){
            Toast.makeText(getApplicationContext(), "Email Required", Toast.LENGTH_SHORT).show();
        }else if(userModel.getName().equals("")){
            Toast.makeText(getApplicationContext(), "Name Required", Toast.LENGTH_SHORT).show();
        }else if(userModel.getAddress().equals("")){
            Toast.makeText(getApplicationContext(), "Address required", Toast.LENGTH_SHORT).show();
        }else if(password.equals("")){
            Toast.makeText(getApplicationContext(), "Password required", Toast.LENGTH_SHORT).show();
        }else if(!password.equals(confirmPassword)){
            Toast.makeText(getApplicationContext(), "Passwords not matched. Please check again", Toast.LENGTH_SHORT).show();
        }else if(!userModel.getEmail().contains("@")){
            Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
        }else {
            Dialog dialogProgress=helper.openNetLoaderDialog();

            auth.createUserWithEmailAndPassword(userModel.getEmail(),password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(getApplicationContext(), "Account created ", Toast.LENGTH_SHORT).show();
                             user=auth.getCurrentUser();
                             reference.child(user.getUid()).setValue(userModel)
                                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                                         @Override
                                         public void onSuccess(Void unused) {
                                             Toast.makeText(getApplicationContext(), "Data uploaded successfully", Toast.LENGTH_SHORT).show();
                                             dialogProgress.dismiss();
                                             Intent intent=new Intent(RegistrationActivity.this, MainActivity.class);
                                             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                             startActivity(intent);
                                         }
                                     }).addOnFailureListener(new OnFailureListener() {
                                 @Override
                                 public void onFailure(@NonNull Exception e) {
                                     dialogProgress.dismiss();
                                     Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                                 }
                             });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                       dialogProgress.dismiss();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                 }
            });



        }


    }

    private void initDB() {
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference()
                .child("users");
    }
}