package com.databit.seguimientoruta_cesardiaz;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartRouteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final float DISTANCE_THRESHOLD = 9; // Distancia límite en metros

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private List<Location> routeLocations;
    private GoogleMap googleMap;
    private RouteUtils routeUtils;
    private DatabaseReference locationDatabase;
    private Marker currentLocationMarker;
    private LatLng selectedLocation;
    private String currentRouteId;
    private String routeName; // Nombre de la ruta actual
    private long startTime; // Hora de inicio de la ruta actual
    private boolean isRecording = false;
    private RouteModel currentRoute;
    Spinner mapTypeSpinner;
    private LatLng destination;
    private DatabaseReference databaseReference; // Referencia a Firebase para guardar las rutas
    private List<RouteModel> routeList; //
    private static final String PREFS_NAME = "RoutePrefs";
    private static final String PREF_ROUTE_NAME_KEY = "RouteName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_route);
        mapTypeSpinner = findViewById(R.id.mapTypeSpinner);
        databaseReference = FirebaseDatabase.getInstance().getReference("rutas");
        routeList = new ArrayList<>();

        // Inicializaciones
        routeLocations = new ArrayList<>();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationDatabase = FirebaseDatabase.getInstance().getReference("locations");
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        SupportMapFragment mapFragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.mapContainer, mapFragment).commit();
        mapFragment.getMapAsync(this);

        routeUtils = new RouteUtils(this);
        checkLocationPermission();
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                this, R.layout.spinner_item, getResources().getStringArray(R.array.map_types));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapTypeSpinner.setAdapter(adapter);

        mapTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (googleMap != null) {
                    switch (position) {
                        case 0: // Normal
                            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            break;
                        case 1: // Satélite
                            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            break;
                        case 2: // Terreno
                            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                            break;
                        case 3: // Híbrido
                            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Opcional: manejar la no selección
            }
        });


    }
    private void saveRouteName(String routeName) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_ROUTE_NAME_KEY, routeName);
        editor.apply();
    }

    private String getSavedRouteName() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getString(PREF_ROUTE_NAME_KEY, "Nombre de la Ruta Default");
    }

    private void displayRouteNameInUI() {
        String routeName = getSavedRouteName();
        TextView routeNameTextView = findViewById(R.id.routeName);
        routeNameTextView.setText(routeName);
    }





    private void saveRouteAndAddToList(RouteModel route) {
        String routeId = databaseReference.push().getKey();
        if (routeId != null) {
            databaseReference.child(routeId).setValue(route)
                    .addOnSuccessListener(aVoid -> {
                        showToast("Ruta guardada con éxito en Firebase");
                        routeList.add(route); // Añade la ruta a la lista
                    })
                    .addOnFailureListener(e -> showToast("Error al guardar la ruta en Firebase: " + e.getLocalizedMessage()));
        }
    }


    private void clearSavedRouteName() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().remove(PREF_ROUTE_NAME_KEY).apply();
    }

// Resto de los métodos de la clase...


    private List<LatLng> convertLocationsToLatLng(List<Location> locations) {
        List<LatLng> latLngs = new ArrayList<>();
        for (Location loc : locations) {
            latLngs.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }
        return latLngs;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) {
                for (Location location : locationResult.getLocations()) {
                    // Añadir ubicación a routeLocations para cálculo de distancia y dibujo en mapa
                    routeLocations.add(location);
                    updateCurrentLocationMarker(location);

                    // Convertir la ubicación a FirebaseLatLng y añadir a currentRoute
                    FirebaseLatLng firebaseLatLng = new FirebaseLatLng(location.getLatitude(), location.getLongitude());
                    if (currentRoute != null && currentRoute.getUbicaciones() != null) {
                        currentRoute.getUbicaciones().add(firebaseLatLng);
                    }

                    // Dibujo en el mapa y otras operaciones
                    if (isRecording) {
                        routeUtils.drawRouteOnMap(location.getLatitude(), location.getLongitude());
                        saveLocationToFirebase(location);
                    }
                }
            }
        }
    };




    public void onStartRouteClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nombre de la Ruta");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            routeName = input.getText().toString();
            startRecording(routeName);  // Comienza la grabación con el nombre de la ruta
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void onStartStopButtonClick(View view) {
        if (!isRecording) {
            // No está grabando, por lo que el botón debería iniciar la grabación
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Nombre de la Ruta");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String routeName = input.getText().toString();
                    startRoute(routeName);
                    saveRouteName(routeName); // Guardar en SharedPreferences
                    startRecording(routeName);
                    displayRouteNameInUI();
                    Button btn = (Button) view;
                    updateTask.run();
                    btn.setText("Detener Grabación");
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        } else {
            // Ya está grabando, así que el botón debería detener la grabación
            onStopRecordingRoute();
            Button btn = (Button) view;
            btn.setText("Comenzar Grabación");
        }
    }

    private void onStopRecordingRoute() {
        if (isRecording) {
            // Detén la grabación de la ruta
            routeUtils.stopRecordingRoute(); // Asume que este método ya existe en tu clase RouteUtils y detiene la grabación

            // Tal vez quieras guardar la ruta final en Firebase
            Map<String, Object> routeData = new HashMap<>();
            routeData.put("endTime", System.currentTimeMillis()); // Tiempo de fin de la ruta
            // Si tienes un ID de ruta o alguna otra referencia, úsala aquí para actualizar la ruta
            String routeId = "1";
            locationDatabase.child(routeId).updateChildren(routeData);

            // Cambiar el estado de grabación a false
            isRecording = false;

            // Actualizar la UI si es necesario, por ejemplo, cambiar el texto de un botón
            Button startButton = findViewById(R.id.btnStartRecording);
            startButton.setText("Comenzar Grabación");

            // Mostrar mensaje al usuario
            showToast("Grabación de ruta detenida.");
        } else {
            showToast("La grabación ya está detenida.");
        }
    }

    public void onStartRecordingClick(View view) {
        Button btn = (Button) view;
        if (!isRecording) {
            // Iniciar la grabación de la ruta
            // Asegúrate de que currentRoute se inicializa aquí
            startRecording(routeName);
            btn.setText("Detener Grabación");
        } else {
            // Detener la grabación de la ruta
            stopRecording();
            btn.setText("Comenzar Grabación");
        }
    }


    private void startRecording(String routeName) {
        if (!isRecording) {
            isRecording = true;
            currentRoute = new RouteModel();
            currentRoute.setNombre(routeName);
            currentRoute.setInicio(System.currentTimeMillis());
            currentRoute.setUbicaciones(new ArrayList<>());

            showToast("Grabación iniciada: " + routeName);

            this.routeName = routeName;
            this.startTime = System.currentTimeMillis();
            handler.postDelayed(updateTask, 5000);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }

            currentRouteId = locationDatabase.push().getKey();
        } else {
            showToast("La grabación ya está en curso.");
        }
    }

    private Handler handler = new Handler();
    private Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                // Calcula la distancia y la duración
                float distancia = calcularDistancia(routeLocations);
                long duracion = calcularDuracion(startTime, System.currentTimeMillis());

                // Calcular la distancia al destino, si el destino está definido
                if (destination != null && !routeLocations.isEmpty()) {
                    Location lastLocation = routeLocations.get(routeLocations.size() - 1);
                    float[] results = new float[1];
                    Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
                            destination.latitude, destination.longitude, results);
                    float distanceToDestination = results[0]; // Distancia en metros

                    showToast("Distancia recorrida: " + distancia + " metros, Duración: " + duracion / 1000 +
                            " segundos, Distancia al destino: " + distanceToDestination + " metros");
                } else {
                    showToast("Distancia recorrida: " + distancia + " metros, Duración: " + duracion / 1000 + " segundos");
                }

                // Re-programa el runnable para ejecutarse después de 10 segundos
                handler.postDelayed(this, 5000);
            }
        }
    };

    private float calcularDistancia(List<Location> locations) {
        float distanciaTotal = 0;
        for (int i = 1; i < locations.size(); i++) {
            distanciaTotal += locations.get(i).distanceTo(locations.get(i - 1));
        }
        return distanciaTotal;
    }

    private long calcularDuracion(long startTime, long endTime) {
        return endTime - startTime;
    }


    private void stopRecording() {
        if (isRecording) {
            isRecording = false;
            showToast("Grabación detenida");

            // Preparar la ruta nueva
            RouteModel newRoute = new RouteModel();
            newRoute.setNombre(this.routeName);
            newRoute.setInicio(this.startTime);
            newRoute.setFin(System.currentTimeMillis());
            newRoute.setUbicaciones(convertLocationsToFirebaseLatLng(routeLocations)); // Usa FirebaseLatLng

            // Guardar la nueva ruta en Firebase y añadirla a la lista
            saveRouteAndAddToList(newRoute);

            // Verificar y actualizar currentRoute si no es null
            if (currentRoute != null) {
                currentRoute.setFin(System.currentTimeMillis());
                currentRoute.setUbicaciones(convertLocationsToFirebaseLatLng(routeLocations));

                String routeId = locationDatabase.push().getKey();
                if (routeId != null) {
                    locationDatabase.child("rutas").child(routeId).setValue(currentRoute);
                }
            }

            fusedLocationClient.removeLocationUpdates(locationCallback);
            handler.removeCallbacks(updateTask);

            // Preparar y guardar datos de la ruta
            Map<String, Object> routeData = new HashMap<>();
            routeData.put("routeName", this.routeName);
            routeData.put("startTime", this.startTime);
            routeData.put("endTime", System.currentTimeMillis());

            if (currentRouteId != null) {
                locationDatabase.child(currentRouteId).setValue(routeData);
                currentRouteId = null;
            }
        } else {
            showToast("No hay una grabación en curso para detener.");
        }
    }










    public void onNewRouteClick(View view) {
        if (isRecording) {
            // Detén la grabación antes de empezar una nueva
            stopRecording();
            ((Button) findViewById(R.id.btnStartRecording)).setText("Comenzar Grabación");
        }

        // Mostrar diálogo para ingresar el nombre de la nueva ruta
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nombre de la Ruta");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String routeName = input.getText().toString();
            startRecording(routeName);  // Comienza la grabación con el nombre de la ruta
            ((Button) findViewById(R.id.btnStartRecording)).setText("Detener Grabación");
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }



    private void showDialogToStartRoute() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nombre de la Ruta");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String routeName = input.getText().toString();
            startRoute(routeName);
            ((Button) findViewById(R.id.btnStartRecording)).setText("Detener Grabación");
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }






    private void startRoute(String routeName) {
        // Aquí tu código para iniciar la grabación de la ruta

        // Ejemplo de cómo podrías estructurar los datos de la ruta para Firebase
        Map<String, Object> routeData = new HashMap<>();
        routeData.put("routeName", routeName);
        routeData.put("startTime", System.currentTimeMillis()); // Tiempo de inicio de la ruta
        // Puedes agregar más datos a routeData según sea necesario

        // Almacenar los datos de la ruta en Firebase
        String key = locationDatabase.push().getKey();
        if (key != null) {
            locationDatabase.child(key).setValue(routeData);
        }

        // Actualizar el estado de grabación
        isRecording = true;

        // Mostrar un mensaje al usuario
        showToast("Grabación de ruta iniciada: " + routeName);
    }
    private void saveRouteAndAddToList() {
        if (currentRoute != null) {
            List<FirebaseLatLng> firebaseLatLngs = new ArrayList<>();
            for (Location location : routeLocations) {
                firebaseLatLngs.add(new FirebaseLatLng(location.getLatitude(), location.getLongitude()));
            }

            currentRoute.setUbicaciones(firebaseLatLngs); // Aquí usamos FirebaseLatLng

            String routeId = databaseReference.push().getKey();
            if (routeId != null) {
                // Usar currentRoute en lugar de route
                databaseReference.child(routeId).setValue(currentRoute)
                        .addOnSuccessListener(aVoid -> {
                            showToast("Ruta guardada con éxito en Firebase");
                            routeList.add(currentRoute); // Añadir currentRoute a la lista
                        })
                        .addOnFailureListener(e -> showToast("Error al guardar la ruta en Firebase: " + e.getLocalizedMessage()));
            }
        }
    }

    private List<FirebaseLatLng> convertLocationsToFirebaseLatLng(List<Location> locations) {
        List<FirebaseLatLng> firebaseLatLngs = new ArrayList<>();
        for (Location loc : locations) {
            firebaseLatLngs.add(new FirebaseLatLng(loc.getLatitude(), loc.getLongitude()));
        }
        return firebaseLatLngs;
    }

    private void updateCurrentLocationMarker(Location location) {
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (currentLocationMarker == null) {
            currentLocationMarker = googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
        } else {
            currentLocationMarker.setPosition(currentLatLng);
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        routeUtils.setGoogleMap(googleMap);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    selectedLocation = latLng;
                    showToast("Ubicación seleccionada: " + latLng.latitude + ", " + latLng.longitude);
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(selectedLocation).title("Ubicación seleccionada"));

                    if (currentLocationMarker != null) {
                        LatLng currentLocation = new LatLng(currentLocationMarker.getPosition().latitude, currentLocationMarker.getPosition().longitude);
                        routeUtils.drawRouteToSelectedLocation(currentLocation, selectedLocation);
                    }
                }
            });

            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            addMarkerAtCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void addMarkerAtCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Ubicación actual"));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }


    private void saveLocationToFirebase(Location location) {
        FirebaseLatLng firebaseLatLng = new FirebaseLatLng(location.getLatitude(), location.getLongitude());
        String key = locationDatabase.push().getKey();
        if (key != null) {
            locationDatabase.child(key).setValue(firebaseLatLng);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }


    private float calcularDistanciaTotal(List<FirebaseLatLng> ubicaciones) {
        float distanciaTotal = 0;
        if (ubicaciones.size() > 1) {
            for (int i = 0; i < ubicaciones.size() - 1; i++) {
                FirebaseLatLng start = ubicaciones.get(i);
                FirebaseLatLng end = ubicaciones.get(i + 1);
                float[] results = new float[1];
                Location.distanceBetween(start.latitude, start.longitude,
                        end.latitude, end.longitude, results);
                distanciaTotal += results[0];
            }
        }
        return distanciaTotal;
    }


    private void guardarRuta(RouteModel ruta) {
        ruta.setDistancia(calcularDistanciaTotal(ruta.getUbicaciones()));

        ruta.setDuracion(ruta.getFin() - ruta.getInicio());

        String routeId = databaseReference.push().getKey();
        if (routeId != null) {
            databaseReference.child(routeId).setValue(ruta)
                    .addOnSuccessListener(aVoid -> {
                        // Actualizar UI, mostrar confirmación...
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    private void mostrarDetallesRuta(String routeId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("rutas");
        databaseReference.child(routeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RouteModel ruta = dataSnapshot.getValue(RouteModel.class);
                if (ruta != null) {
                    TextView distanciaTextView = findViewById(R.id.routeDistance);
                    TextView duracionTextView = findViewById(R.id.routeDuration);

                    distanciaTextView.setText("Distancia: " + ruta.getDistancia() + " m");
                    duracionTextView.setText("Duración: " + (ruta.getDuracion() / 1000) + " seg");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
