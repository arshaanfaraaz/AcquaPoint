package com.example.acquapoint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.acquapoint.Helper.Helper;
import com.example.acquapoint.Models.UserModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {
  
    ImageView backArrow;
    TextView textViewName,textViewEmail,textViewAddress,textViewID;
    Helper helper;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    Button buttonLogout;
    GoogleSignInClient googleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initUI();
        initDB();
        getUserProfile();
    }
    private void initGoogleClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient= GoogleSignIn.getClient(this,gso);
    }
    private void getUserProfile() {
        Dialog progressDialog=helper.openNetLoaderDialog();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                UserModel userModel=snapshot.getValue(UserModel.class);
                textViewName.setText(userModel.getName());
                textViewEmail.setText(userModel.getEmail());
                textViewAddress.setText(userModel.getAddress());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                 progressDialog.dismiss();
            }
        });


    }

    private void initDB() {
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        reference= FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(user.getUid());

        textViewID.setText("DI: "+user.getUid());
    }

    private void initUI() {
        helper=new Helper(this);
        backArrow=findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        buttonLogout=findViewById(R.id.buttonLogout);
        textViewName=findViewById(R.id.textViewName);
        textViewID=findViewById(R.id.textViewID);
        textViewEmail=findViewById(R.id.textViewEmail);
        textViewAddress=findViewById(R.id.textViewAddress);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 logoutUser();
            }
        });


    }

    private void logoutUser() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout from Acqua Point?")
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(user!=null){
                            auth.signOut();
                            googleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                   closeOut();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    closeOut();
                                }
                            });
                        }

                    }
                })
                .show();




    }

    private void closeOut() {
        Intent intent=new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}