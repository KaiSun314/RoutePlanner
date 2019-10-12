package com.SunDragon.RoutePlanner;

import android.app.IntentService;
import android.content.Intent;
import android.util.Pair;

import java.util.ArrayList;

public class GetPlaceAutocompleteIntentService extends IntentService {

    private static final String NAME = "GetPlaceAutocompleteIntentService";
    public static final String QUERY = "query";
    public static final String POSITION = "position";
    public static final String CURRENT_LATITUDE = "currentLatitude";
    public static final String CURRENT_LONGITUDE = "currentLongitude";
    public static final String SESSION_TOKEN = "sessionToken";
    public static final String DESCRIPTIONS = "descriptions";
    public static final String PLACE_IDS = "place_ids";

    public GetPlaceAutocompleteIntentService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int position = intent.getIntExtra(POSITION, 0);
        String query = intent.getStringExtra(QUERY);
        double currentLatitude = intent.getDoubleExtra(CURRENT_LATITUDE, 0);
        double currentLongitude = intent.getDoubleExtra(CURRENT_LONGITUDE, 0);
        String key = getString(R.string.google_maps_key);
        String sessionToken = intent.getStringExtra(SESSION_TOKEN);

        if (query.length() == 0) {
            sendInfoToClient(new ArrayList<String>(), new ArrayList<String>());
        } else {
            try {
                Pair<Boolean, ArrayList<Pair<String, String>>> data = JSONParser.placeAutocomplete(
                        position, query, currentLatitude, currentLongitude, key, sessionToken);
                if (data.first) {
                    sessionToken = JSONParser.getSessionToken();
                    data = JSONParser.placeAutocomplete(position, query, currentLatitude,
                            currentLongitude, key, sessionToken);
                }

                ArrayList<String> descriptions = new ArrayList<>();
                ArrayList<String> placeIds = new ArrayList<>();
                for (Pair<String, String> info : data.second) {
                    descriptions.add(info.first);
                    placeIds.add(info.second);
                }
                sendInfoToClient(descriptions, placeIds);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void sendInfoToClient(ArrayList<String> descriptions, ArrayList<String> placeIds) {
        Intent intent = new Intent();
        intent.setAction(MapsActivity.ResponseReceiver.RESPONSE_ACTION);
        intent.putExtra(DESCRIPTIONS, descriptions);
        intent.putExtra(PLACE_IDS, placeIds);
        sendBroadcast(intent);
    }
}