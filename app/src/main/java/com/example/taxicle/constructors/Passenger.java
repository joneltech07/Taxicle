package com.example.taxicle.constructors;

public class Passenger {

    public String name, id;
    public double longitude, latitude;

    public Passenger() {

    }
    public Passenger(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public Passenger(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
