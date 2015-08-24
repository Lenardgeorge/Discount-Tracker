package com.example.lenardgeorge.discounttracker;

import android.util.Log;

/**
 * Created by lenardgeorge on 5/17/15.
 */
public class ShopInfo {
    private String name ;
    private double latitude;
    private double longitude;

    ShopInfo(String shop_name , double shop_lat, double shop_lng)
    {

        this.name = shop_name;
        this.latitude = shop_lat;
        this.longitude = shop_lng;
        //Log.i( String.valueOf(sp.longitude), String.valueOf(sp.latitude));
    }

    public String getName()
   {
       return name;
   }

    public double getLat()
    {
        return latitude;
    }

    public double getLng()
    {
        return longitude;
    }

}
