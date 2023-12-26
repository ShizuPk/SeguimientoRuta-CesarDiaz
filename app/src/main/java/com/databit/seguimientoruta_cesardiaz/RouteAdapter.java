package com.databit.seguimientoruta_cesardiaz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    private final List<RouteModel> routeList;

    public RouteAdapter(List<RouteModel> routeList) {
        this.routeList = routeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RouteModel route = routeList.get(position);
        holder.routeName.setText(route.getNombre());
        // Si tienes más datos en RouteModel, como distancia y duración, puedes configurarlos aquí.
        // Ejemplo:
        // holder.routeDistance.setText("Distancia: " + route.getDistancia() + " m");
        // holder.routeDuration.setText("Duración: " + route.getDuracion() + " min");
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView routeName;
        // Agrega más TextViews si tienes más datos para mostrar, por ejemplo:
        // TextView routeDistance;
        // TextView routeDuration;

        public ViewHolder(View itemView) {
            super(itemView);
            routeName = itemView.findViewById(R.id.routeName);
            // Inicializa aquí otras vistas si es necesario, por ejemplo:
            // routeDistance = itemView.findViewById(R.id.routeDistance);
            // routeDuration = itemView.findViewById(R.id.routeDuration);
        }
    }
}
