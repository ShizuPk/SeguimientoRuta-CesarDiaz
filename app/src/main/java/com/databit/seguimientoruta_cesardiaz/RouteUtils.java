package com.databit.seguimientoruta_cesardiaz;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

public class RouteUtils {

    private GoogleMap googleMap;
    private Polyline routePolyline;
    private boolean recordingRoute = false;
    private Context context;
    private GeoApiContext geoApiContext;

    public RouteUtils(Context context) {
        this.context = context;
        this.geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyATbFDoExiSFGtqucCAeqAkqMoWiMtoORE") // Reemplaza con tu clave API
                .build();
    }

    public void drawRouteToSelectedLocation(LatLng currentLocation, LatLng selectedLocation) {
        try {
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .origin(new com.google.maps.model.LatLng(currentLocation.latitude, currentLocation.longitude))
                    .destination(new com.google.maps.model.LatLng(selectedLocation.latitude, selectedLocation.longitude))
                    .await();

            if (result.routes != null && result.routes.length > 0) {
                DirectionsRoute route = result.routes[0];
                PolylineOptions polylineOptions = new PolylineOptions();

                for (com.google.maps.model.LatLng path : route.overviewPolyline.decodePath()) {
                    polylineOptions.add(new LatLng(path.lat, path.lng));
                }

                polylineOptions.color(Color.BLUE).width(5);
                googleMap.addPolyline(polylineOptions);
            }
        } catch (Exception e) {
            showToast("Error al calcular la ruta: " + e.getMessage());
        }
    }


    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public boolean isRecordingRoute() {
        return recordingRoute;
    }

    public void startRecordingRoute() {
        recordingRoute = true;
        clearRoute();
    }

    public void stopRecordingRoute() {
        recordingRoute = false;
        clearRoute();
    }

    public void drawRouteOnMap(double latitude, double longitude) {
        if (recordingRoute) {
            LatLng latLng = new LatLng(latitude, longitude);
            drawRouteOnMap(latLng);
        }
    }

    public void drawRouteOnMap(LatLng latLng) {
        if (recordingRoute) {
            if (routePolyline == null) {
                PolylineOptions polylineOptions = new PolylineOptions()
                        .add(latLng)
                        .color(Color.BLUE)
                        .width(5);
                routePolyline = googleMap.addPolyline(polylineOptions);
            } else {
                routePolyline.getPoints().add(latLng);
                routePolyline.setPoints(routePolyline.getPoints());
            }
        }
    }



    private void clearRoute() {
        if (routePolyline != null) {
            routePolyline.remove();
            routePolyline = null;
        }
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
