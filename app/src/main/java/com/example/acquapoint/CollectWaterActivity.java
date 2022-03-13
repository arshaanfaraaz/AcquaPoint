package com.example.acquapoint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.acquapoint.Helper.Helper;
import com.example.acquapoint.Models.Water;
import com.example.acquapoint.Models.UserModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectWaterActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    LatLng currentLocation;
    String[] locationPermissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_COARSE_LOCATION};
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    Helper helper;
    List<Water> waters;
    List<String> foodsID;
    UserModel userModel=null;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_water);
        helper=new Helper(this);
        initUI();
        initDB();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initDB() {
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference()
                .child("foods");
    }

    private void initUI() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getUserLocation();
        waters =new ArrayList<>();
        foodsID=new ArrayList<>();
        getAllAvailableFoods();
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                openDialogDetails(marker);
                return  true;
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void getLatsLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location == null) {
                    getUserLocation();
                } else {
                    currentLocation=new LatLng(location.getLatitude(),location.getLongitude());
                     animateLocation();
                }
            }
        });
    }

    private void openDialogDetails(Marker marker) {

        Dialog dialog=new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_details);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
            TextView textViewFoodName,textViewAddress
                    ,textViewStatus,textViewClientName,
                    textViewEmail;
            Button buttonClose,buttonRequest;


            textViewFoodName=dialog.findViewById(R.id.textViewFoodName);
            textViewAddress=dialog.findViewById(R.id.textViewAddress);
            textViewStatus=dialog.findViewById(R.id.textViewStatus);
            textViewClientName=dialog.findViewById(R.id.textViewClientName);
            textViewEmail=dialog.findViewById(R.id.textViewEmail);
            buttonClose=dialog.findViewById(R.id.buttonClose);
            buttonRequest=dialog.findViewById(R.id.buttonRequest);
            int index=Integer.parseInt(marker.getTag().toString());


           textViewFoodName.setText(waters.get(index).getFoodName());
           textViewAddress.setText(waters.get(index).getFoodAddress());
           textViewStatus.setText(waters.get(index).getFoodStatus());


           if(!waters.get(index).getFoodStatus().equals("Available")){
               buttonRequest.setEnabled(false);
           }

           buttonClose.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                  dialog.dismiss();
                  userModel=null;
               }
           });


           buttonRequest.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {

                  if(userModel==null){
                      Toast.makeText(getApplicationContext(), "Please wait application loading data", Toast.LENGTH_SHORT).show();
                  }else {
                      Dialog dialog1=helper.openNetLoaderDialog();
                    reference.child(foodsID.get(index))
                            .child("foodStatus")
                            .setValue("Requested")
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void unused) {
                                    reference.child(foodsID.get(index))
                                            .child("requestedID").setValue(user.getUid())
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(@NonNull Void unused) {
                                                    dialog1.dismiss();
                                                    dialog.dismiss();
                                                    sendNotification(userModel.getNotificationID(), waters.get(index));
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                 @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog1.dismiss();
                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                        }
                    });

                  }
               }
           });



           DatabaseReference reference=FirebaseDatabase
                   .getInstance()
                   .getReference()
                   .child("users")
                   .child(waters.get(index).getDonatorID());
           reference.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   userModel=snapshot.getValue(UserModel.class);
                   textViewEmail.setText(userModel.getEmail());
                   textViewClientName.setText(userModel.getName());
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           });

        }

    private void sendNotification(String notificationID, Water water) {
        Dialog dialog=helper.openNetLoaderDialog();
        RequestQueue requestQueue;
        String postUrl = "https://fcm.googleapis.com/fcm/send";
        String fcmServerKey ="AAAAZubg9D4:APA91bEh41bAQJGTi1tLNw8j6dCx2vqL6SzZ6M-Y1gzemVANmYr0XTQ-BbpKfry1V0QdYkIC6W3N-sXHEwFHajYrqm7_2ZxOLRDzJKsOfXmUwmhoLzYEb9_tLjui4McJGvkBzeWj2ENW";
        requestQueue = Volley.newRequestQueue(this);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", notificationID);
            JSONObject notiObject = new JSONObject();
            notiObject.put("title", "Water Requested");
            notiObject.put("body", "User Submit request to get the water");
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

    private void getAllAvailableFoods() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data:snapshot.getChildren()){
                    Water water=data.getValue(Water.class);
                    if(water.getFoodStatus().equals("Available")) {
                        waters.add(water);
                        foodsID.add(data.getKey());
                    }

                }
                for(int i = 0; i< waters.size(); i++){
                        drawMarker(waters.get(i),i);
                }

             //   Toast.makeText(getApplicationContext(), waters.size()+"", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void drawMarker(Water water, int i) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(water.getFoodName());
        markerOptions.snippet(water.getFoodAddress());
        markerOptions.position(new LatLng(water.getLocationLatitude(), water.getLocationLongitude()));

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
       Marker marker= mMap.addMarker(markerOptions);
       marker.setTag(String.valueOf(i));
    }


    private void getUserLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermissions();
            return;
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                getLatsLocation();
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    // dialogA.dismiss();

                    Toast.makeText(getApplicationContext(), "Location", Toast.LENGTH_SHORT).show();
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    locationManager.removeUpdates(locationListener);
                    // currentLocation=new LatLng(48.762181, 11.425408);
                   animateLocation();
                }
                @Override
                public void onProviderDisabled(@NonNull String provider) {
                }
                @Override
                public void onProviderEnabled(@NonNull String provider) {
                }
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,locationListener);
        } else {
            showSettingsAlert();
        }
    }

    private void animateLocation() {
        if (ActivityCompat.checkSelfPermission(CollectWaterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CollectWaterActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Toast.makeText(getApplicationContext(), "Tapped 'Fetch my location' button ", Toast.LENGTH_SHORT).show();
                drawCurrentLocationMarker();
                return  true;
            }
        });
        drawCurrentLocationMarker();
    }

    private void drawCurrentLocationMarker() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("Current Location");
        markerOptions.position(currentLocation);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
       // mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
    }

    private boolean checkLocationPermissions() {
        int permissionResult;
        List<String> permissionsList = new ArrayList<>();
        for (String p : locationPermissions) {
            permissionResult = ContextCompat.checkSelfPermission(this, p);
            if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(p);
            }
        }
        if (!permissionsList.isEmpty()) { //this is okay yes
            ActivityCompat.requestPermissions(CollectWaterActivity.this, permissionsList.toArray(new String[permissionsList.size()]), 202);
            return false;
        }else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 202) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "Permission granted successfully", Toast.LENGTH_SHORT).show();
                getUserLocation();
            }
        }
    }
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS not enabled");
        alertDialog.setMessage("GPS is disabled. Do you want to go to settings?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent,2021);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2021) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(getApplicationContext(), "GPS Enabled successfully", Toast.LENGTH_SHORT).show();
                getUserLocation();
            } else {
                Toast.makeText(getApplicationContext(), "Please Enable Gps", Toast.LENGTH_SHORT).show();
            }
        }
    }

}