package com.databit.seguimientoruta_cesardiaz;
import com.google.android.gms.maps.model.LatLng;


public class FirebaseLatLng {
    public double latitude;
    public double longitude;

    // Constructor sin argumentos requerido para Firebase
    public FirebaseLatLng() {
    }

    // Constructor para tus propios usos
    public FirebaseLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public static FirebaseLatLng fromLatLng(com.google.android.gms.maps.model.LatLng latLng) {
        return new FirebaseLatLng(latLng.latitude, latLng.longitude);
    }

    public com.google.android.gms.maps.model.LatLng toLatLng() {
        return new com.google.android.gms.maps.model.LatLng(latitude, longitude);
    }
}