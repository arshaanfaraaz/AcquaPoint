package com.example.acquapoint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        FirebaseApp.initializeApp(this);
        setAuth();
        stayForWhile();

    }

    private void stayForWhile() {
        Thread thread=new Thread(){
            @Override
            public void run() {
                try {
                    sleep(4000);
                  if(isUserLogin()){
                    startActivity(new Intent(SplashScreen.this,MainActivity.class));
                  }else {
                      startActivity(new Intent(SplashScreen.this,LoginActivity.class));
                  }
                 finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

    }

    private boolean isUserLogin() {
        if(user==null){
            return false;
        }else{
            return true;
        }


    }

    private void setAuth() {
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
    }
}