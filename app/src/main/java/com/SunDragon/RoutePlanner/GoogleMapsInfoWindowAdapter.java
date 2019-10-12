package com.SunDragon.RoutePlanner;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class GoogleMapsInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private LayoutInflater inflater;

    public GoogleMapsInfoWindowAdapter(Context context){
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View v = inflater.inflate(R.layout.google_maps_info_window, null);

        TextView title = v.findViewById(R.id.title);
        title.setText(Html.fromHtml(marker.getTitle()));

        return v;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
