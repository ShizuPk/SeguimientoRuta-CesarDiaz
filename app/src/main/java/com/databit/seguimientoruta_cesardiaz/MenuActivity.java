package com.databit.seguimientoruta_cesardiaz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button startRouteButton = findViewById(R.id.startRouteButton);
        Button viewRoutesButton = findViewById(R.id.viewRoutesButton);

        startRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(MenuActivity.this, StartRouteActivity.class));
            }
        });

        viewRoutesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, ViewRoutesActivity.class));
            }
        });

        // Agrega más botones y lógica según tus funciones
    }
}
