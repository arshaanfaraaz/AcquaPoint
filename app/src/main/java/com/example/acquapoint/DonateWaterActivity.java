package com.example.acquapoint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.acquapoint.Adapter.WaterAdapter;
import com.example.acquapoint.Helper.Helper;
import com.example.acquapoint.Models.Water;
import com.example.acquapoint.Models.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonateWaterActivity extends AppCompatActivity {

    ImageView backArrow;
    RecyclerView recyclerView;
    LinearLayout layoutEmpty;
    Button buttonDonate;
    List<Water> waters;
    List<String> foodIds;
     WaterAdapter waterAdapter;

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    Helper helper;
    UserModel userModel=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate_water);
        helper=new Helper(this);
        initUI();
        initDB();
        initRecyclerView();
        getAllFoods();
    }

    private void initDB() {
      auth=FirebaseAuth.getInstance();
      user=auth.getCurrentUser();
      reference= FirebaseDatabase.getInstance()
              .getReference()
              .child("foods");
    }

    private void getAllFoods() {
        Query query=reference.orderByChild("donatorID").equalTo(user.getUid());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    waters.clear();
                    foodIds.clear();
                    for(DataSnapshot data:snapshot.getChildren()){
                       Water water=data.getValue(Water.class);
                       if(!water.getFoodStatus().equals("Collected")){
                           waters.add(data.getValue(Water.class));
                           foodIds.add(data.getKey());
                       }

                    }
                    if(waters.size()<=0){
                        layoutEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }else {
                        layoutEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
               waterAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void initRecyclerView() {
        waters =new ArrayList<>();
        foodIds=new ArrayList<>();

        waterAdapter = new WaterAdapter(waters, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(waterAdapter);
        waterAdapter.setOnItemClickListener(new WaterAdapter.onItemClickListener() {
            @Override
            public void respond(int position) {
                openDialog(position);
            }
        });
        
        

    }

    private void openDialog(int position) {
        Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.respond_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        ImageView imageViewCancel;
        TextView textViewName,textViewEmail;
        Button buttonDecline,buttonAccept;

        imageViewCancel=dialog.findViewById(R.id.imageViewCancel);
        textViewName=dialog.findViewById(R.id.textViewName);
        textViewEmail=dialog.findViewById(R.id.textViewEmail);
        buttonDecline=dialog.findViewById(R.id.buttonDecline);
        buttonAccept=dialog.findViewById(R.id.buttonAccept);

        DatabaseReference
                reference2=FirebaseDatabase
                .getInstance().getReference()
                .child("users")
                .child(waters.get(position).getRequestedID());
        reference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModel=snapshot.getValue(UserModel.class);
                textViewName.setText(userModel.getName());
                textViewEmail.setText(userModel.getEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });








        imageViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userModel=null;
                dialog.dismiss();
            }
        });

        DatabaseReference reference=FirebaseDatabase
                .getInstance()
                .getReference()
                .child("foods")
                .child(foodIds.get(position));

        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userModel==null){
                    Toast.makeText(getApplicationContext(), "Please wait.. loading data ", Toast.LENGTH_SHORT).show();
                  return;
                }

                Dialog dialog1=helper.openNetLoaderDialog();
                reference.child("foodStatus").setValue("Collected")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void unused) {
                                sendNotification("Request accepted","Your request to collect water has been accepted",userModel.getNotificationID());
                                dialog1.dismiss();
                                dialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog1.dismiss();
                    }
                });
            }
        });

        buttonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userModel==null){
                    Toast.makeText(getApplicationContext(), "Pleaase wait.. loading data ", Toast.LENGTH_SHORT).show();
                    return;
                }
                Dialog dialog1=helper.openNetLoaderDialog();
                reference.child("foodStatus").setValue("Available")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void unused) {
                                sendNotification("Request rejected","Your request to collect water has been rejected",userModel.getNotificationID());
                                dialog1.dismiss();
                                dialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog1.dismiss();
                    }
                });
            }
        });
    }
  private void sendNotification(String title,String body,String ID){
      Dialog dialog=helper.openNetLoaderDialog();
      RequestQueue requestQueue;
      String postUrl = "https://fcm.googleapis.com/fcm/send";
      String fcmServerKey ="AAAAZubg9D4:APA91bEh41bAQJGTi1tLNw8j6dCx2vqL6SzZ6M-Y1gzemVANmYr0XTQ-BbpKfry1V0QdYkIC6W3N-sXHEwFHajYrqm7_2ZxOLRDzJKsOfXmUwmhoLzYEb9_tLjui4McJGvkBzeWj2ENW";
      requestQueue = Volley.newRequestQueue(this);
      JSONObject mainObj = new JSONObject();
      try {
          mainObj.put("to", ID);
          JSONObject notiObject = new JSONObject();
          notiObject.put("title", title);
          notiObject.put("body", body);
          notiObject.put("icon", "ic_logo"); // enter icon that exists in drawable only
          mainObj.put("notification", notiObject);
          JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
              @Override
              public void onResponse(JSONObject response) {
                  dialog.dismiss();
                  Toast.makeText(getApplicationContext(), "Notification sent ", Toast.LENGTH_SHORT).show();
                  // code run is got response
                  userModel=null;
              }
          }, new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                  // code run is got error
                  dialog.dismiss();
                  Toast.makeText(getApplicationContext(), "Can't send notification"+error.getMessage(), Toast.LENGTH_SHORT).show();
                  userModel=null;
              }
          }) {
              @Override
              public Map<String, String> getHeaders() throws AuthFailureError {
                  Map<String, String> header = new HashMap<>();
                  header.put("content-type", "application/json");
                  header.put("authorization", "key=" + fcmServerKey);
                  return header;
              }
          };
          requestQueue.add(request);
      } catch (JSONException e) {
          e.printStackTrace();
      }
  }
    private void initUI() {
        backArrow=findViewById(R.id.backArrow);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView=findViewById(R.id.recyclerView);
        layoutEmpty=findViewById(R.id.layoutEmpty);
        buttonDonate=findViewById(R.id.buttonDonate);
        buttonDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              startActivity(new Intent(DonateWaterActivity.this, UploadWaterActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        getAllFoods();
        super.onResume();

    }
}