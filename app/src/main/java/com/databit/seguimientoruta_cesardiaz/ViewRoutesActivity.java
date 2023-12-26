package com.databit.seguimientoruta_cesardiaz;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ViewRoutesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RouteAdapter routeAdapter;
    private List<RouteModel> routeList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_routes);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        routeList = new ArrayList<>();
        routeAdapter = new RouteAdapter(routeList);
        recyclerView.setAdapter(routeAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("rutas");

        databaseReference.addValueEventListener(new ValueEventListener() {
            // Dentro de onDataChange en ViewRoutesActivity

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                routeList.clear();
                for (DataSnapshot routeSnapshot : dataSnapshot.getChildren()) {
                    RouteModel route = routeSnapshot.getValue(RouteModel.class);
                    if (route != null && route.getUbicaciones() != null && !route.getUbicaciones().isEmpty()) {
                        // Añade la latitud y longitud de la primera ubicación de la lista
                        FirebaseLatLng firstLocation = route.getUbicaciones().get(0);
                        route.setLatitud(firstLocation.latitude);
                        route.setLongitud(firstLocation.longitude);
                        routeList.add(route);
                    }
                }
                routeAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewRoutesActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
