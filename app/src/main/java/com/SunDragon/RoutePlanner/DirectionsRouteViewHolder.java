package com.SunDragon.RoutePlanner;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DirectionsRouteViewHolder extends RecyclerView.ViewHolder {

    ImageView mMarker;
    TextView mPlaceTitle;
    TextView mPlaceAddress;

    public DirectionsRouteViewHolder(@NonNull View itemView) {
        super(itemView);

        mMarker = itemView.findViewById(R.id.marker);
        mPlaceTitle = itemView.findViewById(R.id.placeTitle);
        mPlaceAddress = itemView.findViewById(R.id.placeAddress);
    }
}
