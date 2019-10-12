package com.SunDragon.RoutePlanner;

import android.app.Activity;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static com.SunDragon.RoutePlanner.MapsActivity.LAT;
import static com.SunDragon.RoutePlanner.MapsActivity.LNG;
import static com.SunDragon.RoutePlanner.OptimizeRouteTask.RESULTS_RETURNED;

public class JSONParser {

    private static final String BASE_URL_AUTOCOMPLETE =
            "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
    private static final String BASE_URL_QUERYAUTOCOMPLETE =
            "https://maps.googleapis.com/maps/api/place/queryautocomplete/json?";
    private static final String BASE_URL_PLACE_DETAILS =
            "https://maps.googleapis.com/maps/api/place/details/json?";
    private static final String BASE_URL_NEARBY_SEARCH =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String BASE_URL_DISTANCE_MATRIX =
            "https://maps.googleapis.com/maps/api/distancematrix/json?";
    private static final String BASE_URL_DIRECTIONS =
            "https://maps.googleapis.com/maps/api/directions/json?";

    public static class PlaceDetails {
        private Pair<String, String> mLatLng;
        private String mPlaceId;
        private String mName;
        private String mAddress;

        public PlaceDetails() {
            mLatLng = new Pair<>(null, null);
        }

        public PlaceDetails(Pair<String, String> latLng, String placeId, String name,
                            String address) {
            mLatLng = latLng;
            mPlaceId = placeId;
            mName = name;
            mAddress = address;
        }

        public Pair<String, String> getLatLng() {
            return mLatLng;
        }

        public String getPlaceId() {
            return mPlaceId;
        }

        public String getName() {
            return mName;
        }

        public String getAddress() {
            return mAddress;
        }
    }

    public static Pair<Boolean, ArrayList<Pair<String, String>>> placeAutocomplete(int position,
            String query, double currentLatitude, double currentLongitude, String key,
            String sessionToken) throws Exception {
        JSONObject jsonObject = getResponse(
                (position < 2 ? BASE_URL_AUTOCOMPLETE : BASE_URL_QUERYAUTOCOMPLETE)
                + "input=" + query
                + "&location=" + currentLatitude + "," + currentLongitude
                + "&radius=500"
                + (sessionToken != null ? "&sessiontoken=" + sessionToken : "")
                + "&key=" + key);

        ArrayList<Pair<String, String>> data = new ArrayList<>();

        String status = jsonObject.getString("status");
        if (status.equals("OK")) {
            JSONArray predictions = jsonObject.getJSONArray("predictions");
            for (int i=0; i<predictions.length(); i++) {
                JSONObject prediction = predictions.getJSONObject(i);
                String description = prediction.getString("description");
                String placeID = null;
                if (prediction.has("place_id")) {
                    placeID = prediction.getString("place_id");
                }
                data.add(new Pair<>(description, placeID));
            }
        }
        return new Pair<>(status.equals("OK"), data);
    }

    public static Pair<Boolean, PlaceDetails> placeDetails(String placeId, String key,
                                                           String sessionToken) throws Exception {
        if (placeId != null) {
            JSONObject jsonObject = getResponse(
                    BASE_URL_PLACE_DETAILS
                            + "placeid=" + placeId
                            + "&fields=formatted_address,geometry,name"
                            + (sessionToken != null ? "&sessiontoken=" + sessionToken : "")
                            + "&key=" + key);

            String status = jsonObject.getString("status");
            if (status.equals("OK")) {
                JSONObject result = jsonObject.getJSONObject("result");
                JSONObject geometry = result.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                String lat = location.getString("lat");
                String lng = location.getString("lng");

                String name = result.getString("name");
                String formatted_address = result.getString("formatted_address");

                return new Pair<>(true, new PlaceDetails(new Pair<>(lat, lng), placeId, name,
                        formatted_address));
            }
        }

        return new Pair<>(false, new PlaceDetails());
    }

    public static ArrayList<PlaceDetails> nearbySearch(
            Pair<String, String> locationLatLng, String placeName, Activity mActivity, int maxSize)
            throws Exception {
        JSONObject jsonObject = getResponse(
                BASE_URL_NEARBY_SEARCH
                + "location=" + locationLatLng.first + "," + locationLatLng.second
                + "&rankby=distance"
                + "&keyword=" + URLEncoder.encode(placeName, "UTF-8")
                + "&key=" + mActivity.getString(R.string.google_maps_key));

        ArrayList<PlaceDetails> locations = new ArrayList<>();
        String status = jsonObject.getString("status");
        if (status.equals("OK")) {
            JSONArray results = jsonObject.getJSONArray("results");
            for (int i=0; i<Math.min(results.length(), maxSize); i++) {
                JSONObject result = results.getJSONObject(i);
                JSONObject geometry = result.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                String lat = location.getString("lat");
                String lng = location.getString("lng");

                String placeId = result.getString("place_id");

                locations.add(new PlaceDetails(new Pair<>(lat, lng), placeId, null, null));
            }
            return locations;
        }

        return null;
    }

    public static long[][] distanceMatrix(String origins, String destinations, Activity mActivity)
            throws Exception {
        JSONObject jsonObject = getResponse(
                BASE_URL_DISTANCE_MATRIX
                + "origins=" + origins
                + "&destinations=" + destinations
                + "&key=" + mActivity.getString(R.string.google_maps_key));

        long[][] dist = new long[RESULTS_RETURNED][RESULTS_RETURNED];
        for (int k=0; k<RESULTS_RETURNED; k++) {
            for (int l=0; l<RESULTS_RETURNED; l++) {
                dist[k][l] = OptimizeRouteTask.INFINITY;
            }
        }
        String status = jsonObject.getString("status");
        if (status.equals("OK")) {
            JSONArray rows = jsonObject.getJSONArray("rows");
            for (int k=0; k<rows.length(); k++) {
                JSONObject row = rows.getJSONObject(k);
                JSONArray elements = row.getJSONArray("elements");
                for (int l=0; l<elements.length(); l++) {
                    JSONObject element = elements.getJSONObject(l);
                    JSONObject duration = element.getJSONObject("duration");
                    long value = duration.getLong("value");

                    dist[k][l] = value;
                }
            }

            return dist;
        }

        return null;
    }

    public static ArrayList<ArrayList<HashMap<String, String>>> directions(
            Pair<String, String> origin, Pair<String, String> destination, Activity mActivity)
            throws Exception {
        JSONObject jsonObject = getResponse(
                BASE_URL_DIRECTIONS
                + "origin=" + origin.first + "," + origin.second
                + "&destination=" + destination.first + "," + destination.second
                + "&key=" + mActivity.getString(R.string.google_maps_key));

        ArrayList<ArrayList<HashMap<String, String>>> directions = new ArrayList<>();

        String status = jsonObject.getString("status");
        if (status.equals("OK")) {
            JSONArray routes = jsonObject.getJSONArray("routes");

            // Traversing all routes
            for (int i=0; i<routes.length(); i++) {
                JSONArray legs = routes.getJSONObject(i).getJSONArray("legs");
                ArrayList<HashMap<String, String>> path = new ArrayList<>();

                // Traversing all legs
                for (int j = 0; j < legs.length(); j++) {
                    JSONArray steps = legs.getJSONObject(j).getJSONArray("steps");

                    // Traversing all steps
                    for (int k = 0; k < steps.length(); k++) {
                        String polyline = steps.getJSONObject(k).getJSONObject("polyline").
                                getString("points");
                        ArrayList<LatLng> list = decodePoly(polyline);

                        // Traversing all points
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put(LAT, Double.toString(list.get(l).latitude));
                            hm.put(LNG, Double.toString(list.get(l).longitude));
                            path.add(hm);
                        }
                    }
                    directions.add(path);
                }
            }

            return directions;
        }

        return null;
    }

    private static ArrayList<LatLng> decodePoly(String encoded) {

        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    public static String getSessionToken() {
        return UUID.randomUUID().toString();
    }

    private static String getResponseString(String URL) throws Exception {
        java.net.URL url = new URL(URL);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.connect();

        InputStream stream = urlConnection.getInputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        StringBuffer buffer = new StringBuffer();
        String line = "";

        while ((line = reader.readLine()) != null) {
            buffer.append(line+"\n");
        }

        return buffer.toString();
    }

    private static JSONObject getResponse(String URL) throws Exception {
        return new JSONObject(getResponseString(URL));
    }

}
