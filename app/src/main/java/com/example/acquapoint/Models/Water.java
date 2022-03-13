package com.example.acquapoint.Models;

public class Water {
    String foodName;
    String foodAddress;
    String donatorID;
    Double locationLatitude;
    Double locationLongitude;
    String foodStatus;
    String requestedID;

    public Water() {
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodAddress() {
        return foodAddress;
    }

    public void setFoodAddress(String foodAddress) {
        this.foodAddress = foodAddress;
    }



    public String getDonatorID() {
        return donatorID;
    }

    public void setDonatorID(String donatorID) {
        this.donatorID = donatorID;
    }

    public Double getLocationLatitude() {
        return locationLatitude;
    }

    public void setLocationLatitude(Double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    public Double getLocationLongitude() {
        return locationLongitude;
    }

    public void setLocationLongitude(Double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }

    public String getFoodStatus() {
        return foodStatus;
    }

    public void setFoodStatus(String foodStatus) {
        this.foodStatus = foodStatus;
    }

    public String getRequestedID() {
        return requestedID;
    }

    public void setRequestedID(String requestedID) {
        this.requestedID = requestedID;
    }
}
