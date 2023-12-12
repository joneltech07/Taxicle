package com.example.taxicle.constructors;

import java.util.Date;

public class Booking {
    public String id, locationName, notes;
    public Date date;
    public double longitude, latitude;

    public Booking() {}

    public Booking(String id, String locationName, String notes, Date date, double longitude, double latitude) {
        this.id = id;
        this.locationName = locationName;
        this.notes = notes;
        this.date = date;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
