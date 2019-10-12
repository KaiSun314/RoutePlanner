package com.SunDragon.RoutePlanner;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ComposeRouteViewHolder extends RecyclerView.ViewHolder {

    ImageView mMarker;
    TextView mPlaceTitle;
    TextView mPlaceAddress;
    ImageButton mLockedInPlace;
    ImageButton mMoreOptions;

    public ComposeRouteViewHolder(@NonNull View itemView) {
        super(itemView);

        mMarker = itemView.findViewById(R.id.marker);
        mPlaceTitle = itemView.findViewById(R.id.placeTitle);
        mPlaceAddress = itemView.findViewById(R.id.placeAddress);
        mLockedInPlace = itemView.findViewById(R.id.lockedInPlace);
        mMoreOptions = itemView.findViewById(R.id.moreOptions);
    }

}
