package com.example.lenardgeorge.discounttracker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapActivity extends FragmentActivity implements LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    MainActivity mainActivity;
    ArrayList<ShopInfo> shopList = new ArrayList();
    ArrayList<ShopInfo> markerList = new ArrayList();
    ArrayList<ShopInfo> markertemplist = new ArrayList();
    Location location;
    LatLng ll;
    Circle circle, temp_circle;
    double rad, circle_lat, circle_long;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        //---------add shop coordinates
        addShopDetails();

        //---------get the user location(latitude and longitude)
        location = getUserLocation();
        ll = new LatLng(location.getLatitude(), location.getLongitude());

        //----------set up the map
        rad = 10;//2.0E-4;
        markertemplist = null ;
        try {
            setUpMapIfNeeded();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent intent = getIntent();

    }

    /**
     *---------- to get the current location of the user  -----------//
     */
    private Location getUserLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, this);
        location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

        return location;

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            setUpMapIfNeeded();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void setUpMapIfNeeded() throws InterruptedException {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * coordinate locations for shops
     * 12.9667° N, 77.5667° E - my location
     * shopA-12.966692, 77.566566 // shopB-12.966781, 77.566590
     * shopC-12.966820, 77.566657 // shopD-12.966820, 77.566711
     * shopE-12.965430, 77.565971 // shopF-12.965362, 77.567146
     * shopG-12.966739, 77.566732 // shopH-12.966739, 77.566732
     * shopI-12.966789, 77.566692 // shopJ-12.964980, 77.567908
     */


    /**
     * Method that adds the above coordinates to the List of shops (ShopList) *
     */
    public void addShopDetails() {
        String[] shopName;
        Double[] lat, lng;

        //contains the shop names
        shopName = new String[]{"shop_A", "shop_B", "shop_C",
                "shop_D", "shop_E", "shop_F",
                "shop_G", "shop_H", "shop_I", "shop_J"};

        //contains all the latitudes in order
        lat = new Double[]{12.966692, 12.966781, 12.966820, 12.966820, 12.966791,
                12.966750, 12.966739, 12.966730, 12.966789, 12.964980,};

        //contains all the longitudes in order
        lng = new Double[]{77.566566, 77.566590, 77.566657, 77.566711, 77.566764,
                77.566791, 77.566732, 77.566654, 77.566692, 77.567908};

        for (int i = 0; i < shopName.length; i++) {
            ShopInfo sh = new ShopInfo(shopName[i], lat[i], lng[i]);
            shopList.add(sh);
        }


        for (int i = 0; i < shopList.size(); i++) {
            Log.i("longi", String.valueOf(shopList.get(i).getLat()));
            Log.i("lati", String.valueOf(shopList.get(i).getLng()));
            Log.i("Shopname", shopList.get(i).getName());

            Log.i("END", "END");
        }

    }


    private void setUpMap() throws InterruptedException {

        mMap.setMyLocationEnabled(true);

        //Thread that handles the timer
        Thread circleDrawtimer = new Thread() {
            public void run() {

                try {
                    for (int i = 1; i < 5; i++) {

                        Log.i("*****-----******------*****-----*****", String.valueOf(i));
                        rad = rad * i;
                        new CircleDrawThread().execute(Double.valueOf(rad));
                        sleep(5000);
                        Log.i("///////////////ENDS HERE//////////////", String.valueOf(i));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        circleDrawtimer.start();

    }

    private double deg2rad(double deg) {

        return (deg * Math.PI / 180.0);

    }

    private double rad2deg(double rad) {

        return (rad * 180 / Math.PI);

    }

    //---------------Method that Calculates The Distance-----------//
    public Boolean isWithinBounds(LatLng latlng) {

        double theta = circle_long - latlng.longitude;
        double dist = Math.sin(deg2rad(circle_lat)) * Math.sin(deg2rad(latlng.latitude))
                + Math.cos(deg2rad(circle_lat)) * Math.cos(deg2rad(latlng.latitude)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);

        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;

        dist = dist * 1.609344;



       Log.i("TOTAL DIST", String.valueOf(dist));
        if (dist < rad/1000) {
            return true;
        } else {
            return false;
        }

    }

    //---------------Method that Checks if marker added-----------//
    private boolean checkIfExist(ShopInfo shopInfo) {
        Log.i("INSIDE ","");
        Log.i("MarkerList size", String.valueOf(markerList.size()));



        for (int j = 0;  j< markerList.size(); j++) {
            if (shopInfo == markerList.get(j))
                return true;
        }

        return false;

    }

    //---------------Method that returns  marker latitude-----------//
    public double getShopLatitude(int index, ArrayList<ShopInfo> list) {
        ShopInfo temp_sh = shopList.get(index);
        return temp_sh.getLat();

    }

    //---------------Method that returns  marker longitude-----------//
    public double getShopLongitude(int index, ArrayList<ShopInfo> list) {
        ShopInfo temp_sh = shopList.get(index);
        return temp_sh.getLng();
    }

    //---------------Method that returns  marker name-----------//
    public String getShopName(int index, ArrayList<ShopInfo> list) {
        ShopInfo temp_sh = list.get(index);
        return temp_sh.getName();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }



    //---------- Thread that filters the shop that falls within the bounds of the search circle ------//

    private class CircleDrawThread extends AsyncTask<Double, Void, CircleOptions> {

        @Override
        protected CircleOptions doInBackground(Double... radius) {

            Log.i("Inside CDT", "do in bg");
            Log.i("RADIUS --->", String.valueOf(radius[0].doubleValue()));
            CircleOptions options = new CircleOptions()
                    .center(ll)
                    .radius(radius[0].doubleValue())
                    .fillColor(Color.BLUE)
                    .strokeColor(Color.BLACK);

            // temp_circle = circle ;

            return options;


        }

        protected void onPostExecute(CircleOptions options) {
            Log.i("Inside CDT", "do in exec");
            circle = mMap.addCircle(options);
             // Log.i("PRINT", "CIRCLE AVLUE");
            // Log.i(String.valueOf(circle.getCenter().latitude), String.valueOf(circle.getCenter().longitude));
            // scannerThread.start();
            circle_lat = circle.getCenter().latitude;
            circle_long = circle.getCenter().longitude;
            new MarkerPlotThread().execute(shopList);

        }

    }


    /**
     * ----------Thread that filters the shop that falls within the bounds of the search circle -------//
     */
    private class MarkerPlotThread extends AsyncTask<ArrayList<ShopInfo>, Void, ArrayList<ShopInfo>> {

        @Override
        protected ArrayList<ShopInfo> doInBackground(ArrayList<ShopInfo>... arrayList) {
            ArrayList<ShopInfo> tempList = arrayList[0];
            LatLng latlng;

           Log.i("BEFOREmarkerlistSIZE ", String.valueOf(markerList.size()));
            for (int i = 0; i < tempList.size(); i++) {
                Log.i("Inside MPT", "do in bg");
                latlng = new LatLng(getShopLatitude(i, tempList), getShopLongitude(i, tempList));
                if (isWithinBounds(latlng) == true) {
                    Log.i("SHOP " + String.valueOf(getShopName(i, tempList)), "ACCEPTED");

                  //  if (checkIfExist(templist.get(i)))
                  //      Log.i("ENTERING checkif", "");

                  if(markerList.size() == 0 || checkIfExist(tempList.get(i)) == false)
                  { Log.i("SHOP " +String.valueOf(getShopName(i, tempList)), "ADDED");
                        markerList.add(tempList.get(i));}
                }
            }

            Log.i("AFTERmarkerlistSIZE ", String.valueOf(markerList.size()));
            return markerList;


        }

        protected void onPostExecute(ArrayList list) {

          //  if(list.size() != markertemplist.size())
            Log.i("Inside Scan", "do in exec");
            Log.i("List size", String.valueOf(list.size()));
            for (int i = 0; i < list.size(); i++) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(getShopLatitude(i, list),
                        getShopLongitude(i, list))).title("SHOP" + i));
            // markertemplist = list;

            }
        }

    }

}

