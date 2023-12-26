package com.databit.seguimientoruta_cesardiaz;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;


public class RouteModel {
    private String nombre;
    private long inicio;
    private List<FirebaseLatLng> ubicaciones;
    private long fin;
    private float distancia; // en metros
    private long duracion; // en

    public RouteModel() {
        // Constructor vacío necesario para Firebase
    }

    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public long getInicio() {
        return inicio;
    }

    public void setInicio(long inicio) {
        this.inicio = inicio;
    }

    public long getFin() {
        return fin;
    }

    public void setFin(long fin) {
        this.fin = fin;
    }

    public float getDistancia() {
        return distancia;
    }

    public void setDistancia(float distancia) {
        this.distancia = distancia;
    }

    public long getDuracion() {
        return duracion;
    }

    public void setDuracion(long duracion) {
        this.duracion = duracion;
    }

    public List<FirebaseLatLng> getUbicaciones() {
        return ubicaciones;
    }

    public void setUbicaciones(List<FirebaseLatLng> ubicaciones) {
        this.ubicaciones = ubicaciones;
    }

}
