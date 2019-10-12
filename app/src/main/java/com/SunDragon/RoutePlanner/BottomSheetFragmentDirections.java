package com.SunDragon.RoutePlanner;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class BottomSheetFragmentDirections extends Fragment {

    public MapsActivity mActivity;
    public ProgressBar mProgressBarDirections;
    public DirectionsRouteAdapter mDirectionsRouteAdapter;
    public RecyclerView mDirectionsRouteLocations;
    public TextView mNumLocationsAdded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mActivity = (MapsActivity) getActivity();
        return inflater.inflate(R.layout.bottom_sheet_fragment_directions, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mProgressBarDirections = getView().findViewById(R.id.progressBarDirections);
        mDirectionsRouteLocations = mActivity.findViewById(R.id.directionLocations);
        mNumLocationsAdded = mActivity.findViewById(R.id.num_locations_added);
        ImageButton moreOptions = getView().findViewById(R.id.moreOptions);
        Button editRoute = getView().findViewById(R.id.edit_button);

        mActivity.mSlidingUpPanelLayout.setDragView(R.id.header);

        mDirectionsRouteAdapter = new DirectionsRouteAdapter(mActivity, mActivity.mDirectionsRoute,
                mActivity.mDirectionsOptimizedRoute, mActivity.mDirectionsMarkerColors,
                mActivity.mDirectionsUnusedColors, mActivity.mDirectionsOrder);
        mDirectionsRouteLocations.setLayoutManager(new LinearLayoutManager(mActivity));
        mDirectionsRouteLocations.setAdapter(mDirectionsRouteAdapter);

        mNumLocationsAdded.setText(mActivity.getString(R.string.num_locations_added,
                Integer.toString(mActivity.mDirectionsRoute.size())));

        moreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mActivity, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.directions_overall_popup_menu, popup.getMenu());
                popup.show();

                MenuPopupHelper menuHelper = new MenuPopupHelper(mActivity,
                        (MenuBuilder) popup.getMenu(), v);
                menuHelper.setForceShowIcon(true);
                menuHelper.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.clear_route:
                                int size = mActivity.mDirectionsRoute.size();
                                mActivity.mDirectionsRoute.clear();
                                mActivity.mDirectionsPlaceDetails.clear();
                                mActivity.mDirectionsFlagged.clear();
                                mActivity.mDirectionsMarkerColors.clear();
                                mActivity.mDirectionsMarkers.clear();
                                mActivity.mMap.clear();
                                mActivity.mCurrentLocationMarker =
                                        mActivity.addCurrentLocationMarker(
                                        new LatLng(mActivity.mCurrentLocation.getLatitude(),
                                                mActivity.mCurrentLocation.getLongitude()),
                                                true);
                                mActivity.mDirectionsOptimizedRoute.clear();
                                mActivity.mDirectionsOrder.clear();
                                mActivity.mDirections.clear();
                                mDirectionsRouteAdapter.notifyItemRangeRemoved(0, size);
                                mNumLocationsAdded.setText(mActivity.getString(
                                        R.string.num_locations_added,
                                        Integer.toString(mActivity.mDirectionsRoute.size())));
                                return true;

                            default:
                                return false;
                        }
                    }
                });
            }
        });

        editRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.mComposeRoute.clear();
                mActivity.mComposeRoute.addAll(mActivity.mDirectionsRoute);
                mActivity.mComposePlaceDetails.clear();
                mActivity.mComposePlaceDetails.addAll(mActivity.mDirectionsPlaceDetails);
                mActivity.mComposeFlagged.clear();
                mActivity.mComposeFlagged.addAll(mActivity.mDirectionsFlagged);
                mActivity.mComposeMarkerColors.clear();
                mActivity.mComposeMarkerColors.addAll(mActivity.mDirectionsMarkerColors);
                mActivity.mComposeUnusedColors.clear();
                mActivity.mComposeUnusedColors.addAll(mActivity.mDirectionsUnusedColors);

                mActivity.mBottomNavigationView.setSelectedItemId(R.id.edit_locations);
            }
        });

        if (mActivity.mOptimizeClicked) {
            mActivity.mOptimizeClicked = false;
            mActivity.mOptimizeRouteTask.cancel(true);
            mActivity.mOptimizeRouteTask = new OptimizeRouteTask(mActivity);
            mActivity.mOptimizeRouteTask.execute();
        }
    }
}