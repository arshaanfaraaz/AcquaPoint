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
import android.widget.Toast;

import com.example.acquapoint.Helper.Helper;
import com.example.acquapoint.Models.UserModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;

    ImageView imageViewLogout;
    TextView textViewName,textViewEmail;
    Button buttonDonateFood,buttonCollectFood
            ,buttonProfile
            ,buttonLogout;

    GoogleSignInClient googleSignInClient;
    Helper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper=new Helper(this);
        updateToken();
        initUI();
        initDB();
        getUserInfo();
        initGoogleClient();
    }

    private void updateToken() {
        Dialog dialog=helper.openNetLoaderDialog();
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            String  token = Objects.requireNonNull(task.getResult()).getToken();

                            reference.child("notificationID").setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    dialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull  Exception e) {
                                    dialog.dismiss();
                                }
                            });

                        }else {
                            dialog.dismiss();
                        }
                    }
                });



    }

    private void initGoogleClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient= GoogleSignIn.getClient(this,gso);
    }

    private void getUserInfo() {

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel=snapshot.getValue(UserModel.class);
                textViewEmail.setText(userModel.getEmail());
                textViewName.setText(userModel.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initDB() {
       auth=FirebaseAuth.getInstance();
       user=auth.getCurrentUser();
       reference= FirebaseDatabase.getInstance().getReference()
               .child("users")
               .child(user.getUid());
    }

    private void initUI() {
        textViewName=findViewById(R.id.textViewName);
        textViewEmail=findViewById(R.id.textViewEmail);
        buttonDonateFood=findViewById(R.id.buttonDonateFood);
        buttonCollectFood=findViewById(R.id.buttonCollectFood);
        buttonProfile=findViewById(R.id.buttonProfile);
        buttonLogout=findViewById(R.id.buttonLogout);
        imageViewLogout=findViewById(R.id.imageViewLogout);
        buttonCollectFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CollectWaterActivity.class));
            }
        });

        buttonDonateFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, DonateWaterActivity.class));
            }
        });
        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,ProfileActivity.class));
            }
        });

         buttonLogout.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 logoutUser();
             }
         });
        imageViewLogout.setOnClickListener(new View.OnClickListener() {
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
                                    finish();
                                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    finish();
                                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                                }
                            });
                        }

                    }
                })
                .show();




    }
}