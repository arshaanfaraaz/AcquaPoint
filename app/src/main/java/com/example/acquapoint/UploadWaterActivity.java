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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.acquapoint.Helper.Helper;
import com.example.acquapoint.Models.Water;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UploadWaterActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    TextView textViewLat,textViewLng;
    EditText editTextFoodName,editTextFoodAddress;
    Button buttonUpload;


    LocationManager locationManager;
    LocationListener locationListener;
    LatLng currentLocation;
    LatLng selectedLocation=null;
    String[] locationPermissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_COARSE_LOCATION};
   Marker locationSelectedMarker=null;
   FirebaseAuth auth;
   FirebaseUser user;
   DatabaseReference reference;
   Helper helper;
    FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_water);

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
        textViewLat=findViewById(R.id.textViewLat);
        textViewLng=findViewById(R.id.textViewLng);
        editTextFoodName=findViewById(R.id.editTextFoodName);
        editTextFoodAddress=findViewById(R.id.editTextFoodAddress);
        buttonUpload=findViewById(R.id.buttonUpload);


        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Water water =new Water();
                water.setFoodName(editTextFoodName.getText().toString());
                water.setFoodAddress(editTextFoodAddress.getText().toString());
                water.setFoodStatus("Available");
                water.setRequestedID("");
                water.setDonatorID(user.getUid());
                if(selectedLocation==null){
                    Toast.makeText(getApplicationContext(), "Please select location", Toast.LENGTH_SHORT).show();
                }else if(water.getFoodName().equals("")){
                    Toast.makeText(getApplicationContext(), "Type of water Required", Toast.LENGTH_SHORT).show();
                }else if(water.getFoodAddress().equals("")){
                    Toast.makeText(getApplicationContext(), "Address required", Toast.LENGTH_SHORT).show();
                }else {
                    water.setLocationLatitude(selectedLocation.latitude);
                    water.setLocationLongitude(selectedLocation.longitude);
                    Dialog dialogProgress=helper.openNetLoaderDialog();

                    reference.push().setValue(water).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void unused) {
                            Toast.makeText(getApplicationContext(), "Successfully uploaded", Toast.LENGTH_SHORT).show();
                            dialogProgress.dismiss();
                            finish();
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
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getUserLocation();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                selectedLocation=latLng;
                if(locationSelectedMarker!=null){
                    locationSelectedMarker.remove();
                }
                drawMarker();
            }
        });


    }

    private void drawMarker() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("Food Location");
        markerOptions.snippet("Your Food Will show on this location");
        markerOptions.position(selectedLocation);

            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        locationSelectedMarker = mMap.addMarker(markerOptions);
    }

    private void getUserLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermissions();
            return;
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // loadingDialog();
            getLatsLocation();
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    // dialogA.dismiss();
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    locationManager.removeUpdates(locationListener);
                    selectedLocation=currentLocation;
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
    private void animateLocation() {
        if (ActivityCompat.checkSelfPermission(UploadWaterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UploadWaterActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                Toast.makeText(getApplicationContext(), "My Location Button Clicked ", Toast.LENGTH_SHORT).show();
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
        //markerCurrentLocation= mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
        textViewLat.setText(currentLocation.latitude+"");
        textViewLng.setText(currentLocation.longitude+"");
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
            ActivityCompat.requestPermissions(UploadWaterActivity.this, permissionsList.toArray(new String[permissionsList.size()]), 202);
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
                Toast.makeText(getApplicationContext(), "Permission deaned", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "Permission granted successfully", Toast.LENGTH_SHORT).show();
                getUserLocation();
            }
        }
    }
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS not enable");
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
                Toast.makeText(getApplicationContext(), "GPS Enable successfully", Toast.LENGTH_SHORT).show();
                getUserLocation();
            } else {
                Toast.makeText(getApplicationContext(), "Please Enable Gps", Toast.LENGTH_SHORT).show();
            }
        }
    }

}