package com.databit.seguimientoruta_cesardiaz;

public class Ruta {
    private long timestamp;
    private double latitud;
    private double longitud;

    public Ruta() {
        // Constructor vacío necesario para Firebase
    }

    public Ruta(long timestamp, double latitud, double longitud) {
        this.timestamp = timestamp;
        this.latitud = latitud;
        this.longitud = longitud;
    }


    // Getters y setters

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
}
