package com.SunDragon.RoutePlanner;

import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.SunDragon.RoutePlanner.JSONParser.PlaceDetails;
import com.SunDragon.RoutePlanner.SearchHistoryContract.SearchHistoryEntry;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FragmentAdd extends Fragment implements SearchView.OnQueryTextListener, View.OnFocusChangeListener {
    private static final int REQUEST_MICROPHONE = 0;
    private static final int DELAY = 500;
    private static final String CURRENT_LOCATION = "CURRENT_LOCATION";

    private static MapsActivity mActivity;
    private Handler mHandler;
    private ImageButton mMicOrCloseBtn;
    private boolean mSubmitPressed = false;
    static String mSessionToken = null;
    private CheckBox mAddCurrentLocation;

    public SearchSuggestionsAdapter mSearchResultsAdapter;
    public RecyclerView mSearchResults;
    private LinearLayout mSearchResultsHeader;
    private ImageView mSearchResultsExpandBtn;
    public boolean mSearchResultsExpanded;

    public SearchSuggestionsAdapter mSearchHistoryAdapter;
    public RecyclerView mSearchHistory;
    private LinearLayout mSearchHistoryHeader;
    private ImageView mSearchHistoryExpandBtn;
    public boolean mSearchHistoryExpanded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mActivity = (MapsActivity) getActivity();
        mHandler = new Handler();
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        NestedScrollView fragmentAddLocation = mActivity.findViewById(R.id.fragment_add_location);
        ImageButton backBtn = mActivity.findViewById(R.id.back_btn);
        mActivity.mAddLocationSubmitBtn = mActivity.findViewById(R.id.addLocationSubmitBtn);
        mMicOrCloseBtn = mActivity.findViewById(R.id.mic_or_close_btn);

        mSearchResults = mActivity.findViewById(R.id.searchResults);
        mSearchResultsHeader = mActivity.findViewById(R.id.searchResultsHeader);
        mSearchResultsExpandBtn = mActivity.findViewById(R.id.searchResultsExpandBtn);

        mSearchHistory = mActivity.findViewById(R.id.searchHistory);
        mSearchHistoryHeader = mActivity.findViewById(R.id.searchHistoryHeader);
        mSearchHistoryExpandBtn = mActivity.findViewById(R.id.searchHistoryExpandBtn);

        mActivity.mSearchView = mActivity.findViewById(R.id.searchView);
        mAddCurrentLocation = mActivity.findViewById(R.id.add_current_location);

        fragmentAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mSearchResultsExpanded = true;
        mSearchResultsHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchResultsExpanded) {
                    mSearchResultsExpanded = false;
                    mSearchResultsExpandBtn.setImageResource(R.drawable.ic_expand_more_white);
                    mSearchResultsAdapter.expandLess();
                } else {
                    mSearchResultsExpanded = true;
                    mSearchResultsExpandBtn.setImageResource(R.drawable.ic_expand_less_white);
                    mSearchResultsAdapter.expandMore();
                }
            }
        });


        mSearchHistoryExpanded = true;
        mSearchHistoryHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchHistoryExpanded) {
                    mSearchHistoryExpanded = false;
                    mSearchHistoryExpandBtn.setImageResource(R.drawable.ic_expand_more_white);
                    mSearchHistoryAdapter.expandLess();
                } else {
                    mSearchHistoryExpanded = true;
                    mSearchHistoryExpandBtn.setImageResource(R.drawable.ic_expand_less_white);
                    mSearchHistoryAdapter.expandMore();
                }
            }
        });

        mActivity.mSearchView.setIconifiedByDefault(false);
        mActivity.mSearchView.setQueryHint(mActivity.getString(R.string.search_hint));
        mActivity.mSearchView.setOnQueryTextListener(this);
        LinearLayout linearLayout1 = (LinearLayout) mActivity.mSearchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setTextSize(14);

        mSearchResultsAdapter = new SearchSuggestionsAdapter((MapsActivity) getActivity(),
                new ArrayList<String>(), new ArrayList<String>(),
                mActivity.mSearchView);
        mSearchResults.setLayoutManager(new LinearLayoutManager(mActivity));
        mSearchResults.setAdapter(mSearchResultsAdapter);
        ViewCompat.setNestedScrollingEnabled(mSearchResults, false);

        mSearchHistoryAdapter = new SearchSuggestionsAdapter((MapsActivity) getActivity(),
                new ArrayList<String>(), new ArrayList<String>(),
                mActivity.mSearchView);
        mSearchHistory.setLayoutManager(new LinearLayoutManager(mActivity));
        mSearchHistory.setAdapter(mSearchHistoryAdapter);
        ViewCompat.setNestedScrollingEnabled(mSearchHistory, false);

        mActivity.mSearchView.setOnQueryTextFocusChangeListener(this);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.mBottomNavigationView.setSelectedItemId(
                        mActivity.mBottomNavigationViewState);
            }
        });

        mActivity.mAddLocationSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivity.mSearchView.getQuery().toString().trim().length() == 0
                        && !mAddCurrentLocation.isChecked()) {
                    Toast.makeText(mActivity, mActivity.getString(R.string.place_cannot_be_empty),
                            Toast.LENGTH_SHORT).show();
                } else {
                    mActivity.mBottomNavigationView.setSelectedItemId(
                            mActivity.mBottomNavigationViewState);
                    String placeId = mActivity.mPlaceIdsAdd;
                    String query = mActivity.mSearchView.getQuery().toString();
                    if (mAddCurrentLocation.isChecked()) {
                        placeId = CURRENT_LOCATION;
                        query = mActivity.getString(R.string.current_location);
                    }
                    mActivity.mSearchView.setQuery("", false);
                    mAddCurrentLocation.setChecked(false);
                    new GetPlaceDetailsTask().execute(placeId, query);

                    if (placeId == null || !placeId.equals(CURRENT_LOCATION)) {
                        ContentValues cv = new ContentValues();
                        cv.put(SearchHistoryEntry.TIMESTAMP, System.currentTimeMillis());
                        cv.put(SearchHistoryEntry.SEARCH_QUERY, query);
                        cv.put(SearchHistoryEntry.PLACE_ID, placeId);

                        mActivity.mSearchHistoryAsyncQueryHandler.startInsert(
                                SearchHistoryAsyncQueryHandler.SUBMIT_QUERY_TOKEN,
                                null,
                                SearchHistoryEntry.CONTENT_URI,
                                cv);

                        new DatabaseToFileTask(mActivity).execute();
                    }
                }
            }
        });

        mMicOrCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivity.mSearchView.getQuery().length() == 0) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intent, REQUEST_MICROPHONE);
                } else {
                    mActivity.mSearchView.setQuery("", false);
                }
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        mSubmitPressed = true;
        mActivity.hideKeyboard(mActivity);
        mActivity.mSearchView.clearFocus();

        return true;
    }

    @Override
    public boolean onQueryTextChange(final String s) {
        if (s.length() == 0) {
            mMicOrCloseBtn.setImageResource(R.drawable.ic_mic_grey);
        } else {
            mMicOrCloseBtn.setImageResource(R.drawable.ic_clear_grey);
        }

        mActivity.mPlaceIdsAdd = "";

        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mActivity, GetPlaceAutocompleteIntentService.class);
                intent.putExtra(GetPlaceAutocompleteIntentService.POSITION, 2);
                intent.putExtra(GetPlaceAutocompleteIntentService.QUERY, s);
                Location currentLocation = mActivity.mCurrentLocation;
                intent.putExtra(GetPlaceAutocompleteIntentService.CURRENT_LATITUDE,
                        (currentLocation != null ? currentLocation.getLatitude()
                                : MapsActivity.DEFAULT_LATITUDE));
                intent.putExtra(GetPlaceAutocompleteIntentService.CURRENT_LONGITUDE,
                        (currentLocation != null ? currentLocation.getLongitude()
                                : MapsActivity.DEFAULT_LONGITUDE));
                if (mSessionToken == null) {
                    try {
                        mSessionToken = JSONParser.getSessionToken();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                intent.putExtra(GetPlaceAutocompleteIntentService.SESSION_TOKEN, mSessionToken);
                mActivity.startService(intent);
            }
        }, DELAY);

        mActivity.mSearchHistoryAsyncQueryHandler.startQuery(
                SearchHistoryAsyncQueryHandler.SEARCH_SUGGESTIONS_TOKEN,
                new Pair<> (mActivity, s),
                SearchHistoryEntry.CONTENT_URI,
                null,
                null,
                new String[] { s },
                null);

        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mSearchResults.setVisibility(View.VISIBLE);
            mSearchHistory.setVisibility(View.VISIBLE);

            MapsActivity.showKeyboard(mActivity);
        } else {
            if (mSubmitPressed) {
                mSubmitPressed = false;
                mSearchResults.setVisibility(View.GONE);
                mSearchHistory.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case REQUEST_MICROPHONE:
                if(resultCode == RESULT_OK) {
                    List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    mActivity.mSearchView.setQuery(
                            (results.size() > 0 ? results.get(0) : ""), false);
                }
                break;

            default:
                break;
        }
    }

    public static class GetPlaceDetailsTask extends AsyncTask<String, Void, Pair<PlaceDetails, String>> {

        @Override
        protected Pair<PlaceDetails, String> doInBackground(String... placeId) {
            if (placeId[0] != null && placeId[0].equals(CURRENT_LOCATION)) {
                return new Pair<>(new PlaceDetails(new Pair<>(
                        Double.toString(mActivity.mCurrentLocation.getLatitude()),
                        Double.toString(mActivity.mCurrentLocation.getLongitude())),
                        placeId[0],
                        mActivity.getString(R.string.current_location),
                        null), placeId[1]);
            }

            try {
                if (mSessionToken == null) {
                    try {
                        mSessionToken = JSONParser.getSessionToken();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Pair<Boolean, PlaceDetails> placeDetails = JSONParser.placeDetails(placeId[0],
                        mActivity.getString(R.string.google_maps_key), mSessionToken);
                if (!placeDetails.first) {
                    mSessionToken = JSONParser.getSessionToken();
                    placeDetails = JSONParser.placeDetails(placeId[0],
                            mActivity.getString(R.string.google_maps_key), mSessionToken);
                }
                return new Pair<>(placeDetails.second, placeId[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new Pair<>(new PlaceDetails(), placeId[1]);
        }

        @Override
        protected void onPostExecute(Pair<PlaceDetails, String> placeDetailsQuery) {
            PlaceDetails placeDetails = placeDetailsQuery.first;
            String query = placeDetailsQuery.second;
            if (mActivity.mAddPosition != -1) {
                if (mActivity.mComposeUnusedColors.size() == 0) {
                    Toast.makeText(mActivity, mActivity.getString(
                            R.string.num_location_limit_reached,
                            Integer.toString(mActivity.mComposeMarkerColors.size())),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (placeDetails.getLatLng().first != null
                        && placeDetails.getLatLng().second != null) {
                    Pair<String, String> latlng = placeDetails.getLatLng();
                    mActivity.mComposeMarkers.add(mActivity.mAddPosition,
                            mActivity.mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(
                                            Double.parseDouble(latlng.first),
                                            Double.parseDouble(latlng.second)))
                                    .title((placeDetails.getAddress() != null
                                            ? mActivity.getString(R.string.name_address,
                                            placeDetails.getName(),
                                            placeDetails.getAddress())
                                            : mActivity.getString(R.string.name,
                                            placeDetails.getName())))
                                    .icon(BitmapDescriptorFactory.fromBitmap(mActivity.createMarker(
                                            mActivity.mComposeUnusedColors.peek(),
                                            mActivity.mAddPosition)))));
                } else {
                    mActivity.mComposeMarkers.add(mActivity.mAddPosition, null);
                }

                mActivity.mComposeRoute.add(mActivity.mAddPosition, query);
                mActivity.mComposePlaceDetails.add(mActivity.mAddPosition, placeDetails);

                if (placeDetails.getPlaceId() != null
                        && placeDetails.getPlaceId().equals(CURRENT_LOCATION)) {
                    mActivity.mComposeFlagged.add(mActivity.mAddPosition, true);
                } else {
                    mActivity.mComposeFlagged.add(mActivity.mAddPosition, false);
                }
                mActivity.mComposeMarkerColors.add(mActivity.mAddPosition,
                        mActivity.mComposeUnusedColors.remove());

                TextView numLocationsAdded = mActivity.findViewById(R.id.num_locations_added);
                if (numLocationsAdded != null) {
                    numLocationsAdded.setText(mActivity.getString((mActivity.mComposeRoute.size() == 1
                                    ? R.string.num_location_added : R.string.num_locations_added),
                            Integer.toString(mActivity.mComposeRoute.size())));
                }

                for (int i=mActivity.mAddPosition; i<mActivity.mComposeRoute.size(); i++) {
                    if (mActivity.mComposeMarkers.get(i) != null) {
                        mActivity.mComposeMarkers.get(i).setIcon(
                                BitmapDescriptorFactory.fromBitmap(mActivity.createMarker(
                                        mActivity.mComposeMarkerColors.get(i), i)));
                    }
                }
            } else {
                if (mActivity.mWhoseMarkers == R.id.edit_locations) {
                    if (mActivity.mComposeMarkers.get(mActivity.mEditPosition) != null) {
                        mActivity.mComposeMarkers.get(mActivity.mEditPosition).remove();
                    }
                    if (placeDetails.getLatLng().first != null
                            && placeDetails.getLatLng().second != null) {
                        Pair<String, String> latlng = placeDetails.getLatLng();
                        mActivity.mComposeMarkers.set(mActivity.mEditPosition,
                                mActivity.mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(
                                                Double.parseDouble(latlng.first),
                                                Double.parseDouble(latlng.second)))
                                        .title((placeDetails.getAddress() != null
                                                ? mActivity.getString(R.string.name_address,
                                                placeDetails.getName(),
                                                placeDetails.getAddress())
                                                : mActivity.getString(R.string.name,
                                                placeDetails.getName())))
                                        .icon(BitmapDescriptorFactory.fromBitmap(mActivity.createMarker(
                                                mActivity.mComposeMarkerColors.get(mActivity.mEditPosition),
                                                mActivity.mEditPosition)))));
                    } else {
                        mActivity.mComposeMarkers.set(mActivity.mEditPosition, null);
                    }
                }

                mActivity.mComposeRoute.set(mActivity.mEditPosition, query);
                mActivity.mComposePlaceDetails.set(mActivity.mEditPosition, placeDetails);
            }

            ComposeRouteAdapter composeRouteAdapter =
                    mActivity.mBottomSheetFragmentEditOptimize.mComposeRouteAdapter;
            if (composeRouteAdapter != null) {
                composeRouteAdapter.notifyDataSetChanged();
            }

            mSessionToken = null;
        }
    }
}