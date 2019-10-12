package com.SunDragon.RoutePlanner;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.SunDragon.RoutePlanner.JSONParser.PlaceDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class OptimizeRouteTask extends AsyncTask<Void, Void, Void> {

    public static final int NUM_LOCATIONS = 10;
    public static final int RESULTS_RETURNED = 10;
    public static final long INFINITY = 1000000000000000000L;

    private MapsActivity mActivity;
    private ProgressBar mProgressBarMap;
    private LinearLayout mCalculatingMessage;
    private RecyclerView mDirectionsRecyclerView;

    private ArrayList<List<PlaceDetails>> mLocations = new ArrayList<>();
    private long[][][][] dist =
            new long[NUM_LOCATIONS][NUM_LOCATIONS][RESULTS_RETURNED][RESULTS_RETURNED];
    private long[][][] DP = new long[1<<NUM_LOCATIONS][NUM_LOCATIONS][RESULTS_RETURNED];
    private int[][][] parent = new int[1<<NUM_LOCATIONS][NUM_LOCATIONS][RESULTS_RETURNED];
    private boolean[][][] visited =
            new boolean[1<<NUM_LOCATIONS][NUM_LOCATIONS][RESULTS_RETURNED];
    private Queue<Pair<Integer, Pair<Integer, Integer>>> q = new LinkedList<>();

    private ArrayList<Integer> mFlaggedLocations = new ArrayList<>();
    private int mSize;
    private ArrayList<Pair<String, String>> mCenters = new ArrayList<>();

    public OptimizeRouteTask(MapsActivity activity) {
        mActivity = activity;
        mProgressBarMap = mActivity.findViewById(R.id.progressBarMap);
        mCalculatingMessage = mActivity.findViewById(R.id.calculatingMessage);
        mDirectionsRecyclerView = mActivity.findViewById(R.id.directionLocations);
        mSize = mActivity.mDirectionsRoute.size();
        mFlaggedLocations.add(-1);
        for (int i=0; i<mSize; i++) {
            if (mActivity.mDirectionsFlagged.get(i)) {
                mFlaggedLocations.add(i);
            }
        }
        mFlaggedLocations.add(mSize);
    }

    @Override
    protected void onPreExecute() {
        mProgressBarMap.setVisibility(View.VISIBLE);
        mCalculatingMessage.setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(Void... params) {
        for (int i=0; i<mSize; i++) {
            PlaceDetails placeDetails = mActivity.mDirectionsPlaceDetails.get(i);
            if (placeDetails != null && placeDetails.getLatLng() != null
                    && placeDetails.getLatLng().first != null
                    && placeDetails.getLatLng().second != null
                    && !mCenters.contains(placeDetails.getLatLng())) {
                mCenters.add(placeDetails.getLatLng());
            }
        }
        if (mCenters.size() == 0) {
            if (mActivity.mCurrentLocation != null) {
                mCenters.add(new Pair<>(Double.toString(mActivity.mCurrentLocation.getLatitude()),
                        Double.toString(mActivity.mCurrentLocation.getLongitude())));
            } else {
                mCenters.add(new Pair<>(MapsActivity.DEFAULT_LATITUDE_STRING,
                        MapsActivity.DEFAULT_LONGITUDE_STRING));
            }
        }

        try {
            for (int i=0; i<mSize; i++) {
                String route = mActivity.mDirectionsRoute.get(i);
                PlaceDetails placeDetails = mActivity.mDirectionsPlaceDetails.get(i);
                if (placeDetails != null && placeDetails.getPlaceId() != null) {
                    mLocations.add(Collections.singletonList(placeDetails));
                } else {
                    ArrayList<PlaceDetails> locations = new ArrayList<>();
                    for (int j=0; j<mCenters.size(); j++) {
                        ArrayList<PlaceDetails> locationsInfo = JSONParser.nearbySearch(
                                mCenters.get(j), route, mActivity, RESULTS_RETURNED/mCenters.size());
                        if (locationsInfo != null) {
                            locations.addAll(locationsInfo);
                        }
                    }
                    mLocations.add(locations);
                }
            }

            for (int i=0; i<mSize; i++) {
                for (int j=0; j<mSize; j++) {
                    for (int k=0; k<RESULTS_RETURNED; k++) {
                        for (int l=0; l<RESULTS_RETURNED; l++) {
                            dist[i][j][k][l] = INFINITY;
                        }
                    }
                }
            }

            for (int t=0; t<mFlaggedLocations.size()-1; t++) {
                if ((mFlaggedLocations.get(t) == -1 && mFlaggedLocations.get(t+1) == 0)
                        || (mFlaggedLocations.get(t) == mSize - 1 && mFlaggedLocations.get(t+1) == mSize)) {
                    continue;
                }

                for (int i=Math.max(mFlaggedLocations.get(t), 0);
                     i<=Math.min(mFlaggedLocations.get(t+1), mSize-1); i++) {
                    for (int j=Math.max(mFlaggedLocations.get(t), 0);
                         j<=Math.min(mFlaggedLocations.get(t+1), mSize-1); j++) {
                        if (i == j) continue;
                        StringBuilder origins = new StringBuilder();
                        for (int k=0; k<mLocations.get(i).size(); k++) {
                            origins.append((k > 0 ? "|" : "")
                                    + mLocations.get(i).get(k).getLatLng().first + ","
                                    + mLocations.get(i).get(k).getLatLng().second);
                        }
                        StringBuilder destinations = new StringBuilder();
                        for (int l=0; l<mLocations.get(j).size(); l++) {
                            destinations.append((l > 0 ? "|" : "")
                                    + mLocations.get(j).get(l).getLatLng().first + ","
                                    + mLocations.get(j).get(l).getLatLng().second);
                        }

                        dist[i][j] = JSONParser.distanceMatrix(origins.toString(),
                                destinations.toString(), mActivity);
                    }
                }
            }

            bitmask_DP_BFS();

            mActivity.mDirections.clear();
            for (int i=0; i<mActivity.mDirectionsOptimizedRoute.size()-1; i++) {
                mActivity.mDirections.add(JSONParser.directions(
                        mActivity.mDirectionsOptimizedRoute.get(i).getLatLng(),
                        mActivity.mDirectionsOptimizedRoute.get(i+1).getLatLng(), mActivity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mActivity.mWhoseMarkers == R.id.directions) {
            mActivity.showMarkers(R.id.directions, true);
        }

        mProgressBarMap.setVisibility(View.GONE);
        mCalculatingMessage.setVisibility(View.GONE);
        mDirectionsRecyclerView.setVisibility(View.VISIBLE);
        mDirectionsRecyclerView.getAdapter().notifyDataSetChanged();
    }

    private int serialize(int bitmask, int locationNum, int resultNum) {
        return resultNum+RESULTS_RETURNED*locationNum+RESULTS_RETURNED*NUM_LOCATIONS*bitmask;
    }

    private Pair<Integer, Pair<Integer, Integer>> deserialize(int serializedNum) {
        int bitmask = serializedNum/(RESULTS_RETURNED*NUM_LOCATIONS);
        int res = serializedNum-RESULTS_RETURNED*NUM_LOCATIONS*bitmask;
        int locationNum = res/RESULTS_RETURNED;
        int resultNum = res-RESULTS_RETURNED*locationNum;
        return new Pair<>(bitmask, new Pair<>(locationNum, resultNum));
    }

    private void bitmask_DP_BFS() throws Exception {
        long start = System.currentTimeMillis();
        for (int i=0; i<(1<<mSize); i++) {
            for (int j=0; j<mSize; j++) {
                for (int k=0; k<mLocations.get(j).size(); k++) {
                    DP[i][j][k] = INFINITY;
                    visited[i][j][k] = false;
                }
            }
        }

        for (int i=0; i<=Math.min(mFlaggedLocations.get(1), mSize-1); i++) {
            for (int j=0; j<mLocations.get(i).size(); j++) {
                DP[(1<<i)][i][j] = 0;
                q.add(new Pair<>((1<<i), new Pair<>(i, j)));
                visited[(1<<i)][i][j] = true;
                parent[(1<<i)][i][j] = 0;
            }
        }

        while (! q.isEmpty()) {
            Pair<Integer, Pair<Integer, Integer>> v = q.remove();

            for (int i=0; i<mSize; i++) {
                if ((v.first & (1<<i)) == 0) {
                    int newBitmask = (v.first | (1<<i));
                    for (int j=0; j<mLocations.get(i).size(); j++) {
                        if (DP[newBitmask][i][j] > DP[v.first][v.second.first][v.second.second]
                                + dist[v.second.first][i][v.second.second][j]) {
                            DP[newBitmask][i][j] = DP[v.first][v.second.first][v.second.second]
                                    + dist[v.second.first][i][v.second.second][j];
                            parent[newBitmask][i][j] =
                                    serialize(v.first, v.second.first, v.second.second);
                            if (! visited[newBitmask][i][j]) {
                                q.add(new Pair<>(newBitmask, new Pair<>(i, j)));
                                visited[newBitmask][i][j] = true;
                            }
                        }
                    }
                }
            }
        }

        int allVisited = (1<<mSize)-1;
        Pair<Integer, Pair<Integer, Integer>> state;
        int bestI = 0;
        int bestJ = 0;
        long dp = INFINITY;
        for (int i=Math.max(mFlaggedLocations.get(mFlaggedLocations.size()-2), 0); i<mSize; i++) {
            for (int j=0; j<mLocations.get(i).size(); j++) {
                if (DP[allVisited][i][j] < dp) {
                    bestI = i;
                    bestJ = j;
                    dp = DP[allVisited][i][j];
                }
            }
        }
        state = new Pair<>(allVisited, new Pair<>(bestI, bestJ));

        ArrayList<PlaceDetails> chosenLocations = new ArrayList<>();
        ArrayList<Integer> order = new ArrayList<>();
        while (state.first != 0) {
            if (mLocations.get(state.second.first).size() == 1) {
                chosenLocations.add(mLocations.get(state.second.first).get(0));
            } else {
                chosenLocations.add(JSONParser.placeDetails(
                        mLocations.get(state.second.first).get(state.second.second).getPlaceId(),
                        mActivity.getString(R.string.google_maps_key), null).second);
            }
            order.add(state.second.first);
            state = deserialize(parent[state.first][state.second.first][state.second.second]);
        }

        Collections.reverse(chosenLocations);
        mActivity.mDirectionsOptimizedRoute.clear();
        mActivity.mDirectionsOptimizedRoute.addAll(chosenLocations);

        Collections.reverse(order);
        mActivity.mDirectionsOrder.clear();
        mActivity.mDirectionsOrder.addAll(order);
    }
}
