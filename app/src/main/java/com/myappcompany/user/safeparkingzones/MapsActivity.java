package com.myappcompany.user.safeparkingzones;
/**
 * @author Bilaval Sharma
 */
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.opencsv.CSVReader;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.myappcompany.user.safeparkingzones.ParkingSpotStats.finalHT;
import static com.myappcompany.user.safeparkingzones.ParkingSpotStats.hashST;


/**
 *The main map activity to show the map focused on the city of
 *chicago which fetches data from the dataset and provides services as requested by the user.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    EditText locationSearch;
    List<Address> userAddressList;
    String location="";
    Geocoder geocoder;
    double userLat;
    double userLon;
    LatLng userLocation;
    static Location[] parkingZones;
    static Location[] safestNearestParkingZones;
    Marker markerUser;
    Marker markerSpot;
    ArrayList<Marker> markerArray;
    static CSVReader readFile;
    private static List<String> myList;
    EditText locationCheck;
    String checkLocation="";
    static List<String> res;
    List<Address> parkingAddressList;
    double parkLat;
    double parkLon;
    LatLng parkLocation;
    Marker markerParking;
    
    /**
     * Defines the starting state of the MapsActivity
     * @param savedInstanceState Previous saved instance of the app
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public List<String> read(String fileName) {
        myList = new ArrayList<String>();
        try {
            //File f = new File("data/parking.csv");
            readFile = new CSVReader((new InputStreamReader(getAssets().open(fileName))));

            readFile.readNext();

            String[] str = readFile.readNext();

            while(str != null) {
                myList.add(str[0].toLowerCase()); //check this
                Log.i("Address",str[0]);
                str = readFile.readNext();
            }
            readFile.close();
        }
        catch ( Exception e) {
            Log.i("Error",e.toString());
        }

        return myList;

    }

    /**
     * Checks if there is are any parking spots avaialable on the searched street and shows one if there is
     * @param view
     */
    public void onMapCheck(View view){
        locationCheck=(EditText) findViewById(R.id.editTextSearch);
        checkLocation = locationCheck.getText().toString();

        //hide the keyboard after user enters the location
        InputMethodManager mgr= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(locationCheck.getWindowToken(),0);

        List<String> list = new ArrayList<String>();
        list = read("final_parking_zones.csv");
        res = new ArrayList<String>();
        char[] pattern = checkLocation.toLowerCase().toCharArray();

        for(int i =0 ; i <list.size();i++) {
            SearchAlg bm = new SearchAlg();
            char[] loc = list.get(i).toCharArray();

            if(bm.search(loc, pattern)) {
                res.add(list.get(i));
            }
        }

        //if a parking spot is found, show markers
        if(res.size()>0){
            Toast toast = Toast.makeText(getApplicationContext(), "Parking spot found at: " + res.get(0), Toast.LENGTH_SHORT);
            toast.show();
            //just showing one spot for now
            Log.i("Search result","Parking spots found!");
                //convert addresses to coordinates here and show them using markers
            geocoder = new Geocoder(this);
            try {
                parkingAddressList = geocoder.getFromLocationName(res.get(0), 1); //just showing one for now because geocoding takes time to convert
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address searchedAddress = parkingAddressList.get(0);
            parkLat= searchedAddress.getLatitude();
            parkLon= searchedAddress.getLongitude();
            parkLocation= new LatLng(parkLat, parkLon);
            //adds marker to searched location
            mMap.animateCamera(CameraUpdateFactory.newLatLng(parkLocation));

            markerParking= mMap.addMarker(new MarkerOptions().position(parkLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(parkLocation,15));
        }else{
            Log.i("Search result", "Nothing found!");
            Toast toast = Toast.makeText(getApplicationContext(), "nothing found!", Toast.LENGTH_SHORT);
            toast.show();

        }
    }

    /**
     * Defines what to do when the search button is pressed
     * @param view
     */
    public void onMapSearch(View view){
        locationSearch=(EditText) findViewById(R.id.editTextSearch);
        location = locationSearch.getText().toString();

        //hide the keyboard after user enters the location
        InputMethodManager mgr= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(locationSearch.getWindowToken(),0);
        if(location.isEmpty() || location.length() == 0 || location.equals("") || location == null){
            Toast toast =Toast.makeText(getApplicationContext(),"Invalid Location", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            geocoder = new Geocoder(this);
            try {
                userAddressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                Toast toast =Toast.makeText(getApplicationContext(),"Invalid Location", Toast.LENGTH_SHORT);
                toast.show();
                e.printStackTrace();
            }
            if(userAddressList.isEmpty() || userAddressList.size() == 0 || userAddressList.equals("") || userAddressList == null){
                Toast toast =Toast.makeText(getApplicationContext(),"Invalid Location", Toast.LENGTH_SHORT);
                toast.show();
            }
            else{
                Address searchedAddress = userAddressList.get(0);
                userLat= searchedAddress.getLatitude();
                userLon= searchedAddress.getLongitude();
                userLocation= new LatLng(userLat, userLon);
                //adds marker to searched location
                mMap.animateCamera(CameraUpdateFactory.newLatLng(userLocation));

                //remove previous user location marker
                if(markerUser != null){
                    markerUser.remove();
                }
                markerUser= mMap.addMarker(new MarkerOptions().position(userLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title(location));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));

                //loads parkings spots (enter an if-else condition here)
                showMarkers("final_parking_coord.csv",userLat, userLon);
            }
        }
    }

    /**
     * Sorts the dataset by distance from the user location and implements the addMarker method
     * @param fileName The input parking spot dataset
     * @param userLat User location latitude
     * @param userLon User location longitude
     */
    public void showMarkers(String fileName, double userLat, double userLon){
        parkingZones=Sort.readData(fileName, getApplicationContext());
        //sorts parkingZones by distance from the user
        Sort.nearestParkingZones(userLat,userLon,parkingZones);
        //sorts sorted (by distance) parking spots by safety
        safestNearestParkingZones= Sort.nearestSafestParkingZones(parkingZones, getApplicationContext());


        //Add different markers
        addMarkers(safestNearestParkingZones);
    }

    /**
     * Shows the the user location and the 30 nearest and marked by safety
     * Green markers represent the safest parking zones, yellow markers represent the parking zones with intermediate safety whereas red
     * markers represent the highly unsafe ones.
     * @param sortedZones The array containing sorted parking spots
     */
    public void addMarkers(Location[] sortedZones ){
        ParkingSpotStats.markerInfo(sortedZones);

        markerArray= new ArrayList<Marker>();

        for(int i=0; i<sortedZones.length; i++)
        {
            LatLng parkingSpot = new LatLng(sortedZones[i].getLat(),sortedZones[i].getLon());
            if(i<=10){
                ParkingSpotStats.getStats(hashST.get(i).getLat(), hashST.get(i).getLon());
                for(Location key: finalHT.keys()){
                    Location adjSpot = null;
                    Double Lat= key.getLat();
                    Double Lon= key.getLon();
                    if(key.getAdj().size() == 0) {
                        adjSpot = null;
                    } else {
                        for(int j = 0; j < key.getAdj().size(); j++) {
                             adjSpot=key.getAdj().get(j);
                        }
                    }
                    Integer safetyRank = finalHT.get(key);
                    markerArray.add(mMap.addMarker(new MarkerOptions()
                            .position(parkingSpot)
                            .title("Coordinates: " + String.valueOf(Lat) + "," + String.valueOf((Lon)) + "\n" +"Safety Rank: " + String.valueOf(safetyRank))// change title to something more descriptive
                            .snippet("Adjacent spots: " + adjSpot)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
                }

            }
            else if (i>10 && i <=20){
                ParkingSpotStats.getStats(hashST.get(i).getLat(), hashST.get(i).getLon());
                for(Location key: finalHT.keys()){
                    Location adjSpot = null;
                    Double Lat= key.getLat();
                    Double Lon= key.getLon();
                    if(key.getAdj().size() == 0) {
                        adjSpot = null;
                    } else {
                        for(int j = 0; j < key.getAdj().size(); j++) {
                            adjSpot=key.getAdj().get(j);
                        }
                    }
                    Integer safetyRank = finalHT.get(key);
                    markerArray.add(mMap.addMarker(new MarkerOptions()
                            .position(parkingSpot)
                            .title("Coordinates: " + String.valueOf(Lat) + "," + String.valueOf((Lon)) + "\n" +"Safety Rank: " + String.valueOf(safetyRank))// change title to something more descriptive
                            .snippet("Adjacent spots: " + adjSpot)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));
                }
            }

            else if (i>20 && i <=30){
                ParkingSpotStats.getStats(hashST.get(i).getLat(), hashST.get(i).getLon());
                for(Location key: finalHT.keys()){
                    Location adjSpot = null;
                    Double Lat= key.getLat();
                    Double Lon= key.getLon();
                    if(key.getAdj().size() == 0) {
                        adjSpot = null;
                    } else {
                        for(int j = 0; j < key.getAdj().size(); j++) {
                            adjSpot=key.getAdj().get(j);
                        }
                    }
                    Integer safetyRank = finalHT.get(key);
                    markerArray.add(mMap.addMarker(new MarkerOptions()
                            .position(parkingSpot)
                            .title("Coordinates: " + String.valueOf(Lat) + "," + String.valueOf((Lon)))// change title to something more descriptive
                            .snippet("Safety rank: " + String.valueOf(safetyRank))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));
                }
            }
        }
    }

    /**
     * Manuplates the map once ready
     * @param googleMap The google map object
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng chicagoLocation = new LatLng(41.8781, -87.6298);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chicagoLocation,14));
        }

    /**
     * Goes to SortedByDistActivity on clicking the sort by distance button
     * @param view The map view
     */
     public void goToSortedDistView(View view){
         Intent intent = new Intent(getApplicationContext(), SortedByDistActivity.class);
         startActivity(intent);
     }

    /**
     * Goes to SortedBySafetyActivty on clicking the sort by safety button
     * @param view The map view
     */
    public void goToSortedSafetyView(View view){
        Intent intent = new Intent(getApplicationContext(), SortedBySafetyActivity.class);
        startActivity(intent);
    }
    }

