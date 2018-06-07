package com.colinmckee.alamodrafthouse.DataModels;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URI;

public class FourSquare implements Parcelable
{
    private static final String TAG = "FourSquare";
    private String id;
    private String name;
    private String category;
    private Bitmap icon;
    private String iconURI;
    private String distance;
    private LatLng location;
    private boolean isFavorited;
    private String url;

    /**
     * Default constructor
     */
    public FourSquare() {
        // Empty
    }

    public FourSquare(String id, String name, String category, Bitmap icon, String distance, LatLng location, boolean isFavorited, String url) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.icon = icon;
        this.distance = distance;
        this.location = location;
        this.isFavorited = isFavorited;
        this.url = url;
    }

    /**
     * Constructor method with a Parcel.
     * @param in FourSquare represented as a Parcel
     */
    public FourSquare(Parcel in) {
        id = in.readString();
        name = in.readString();
        category = in.readString();
        String iconBase64 = in.readString();
        icon = (iconBase64 != null && !iconBase64.isEmpty()) ? decodeBase64(iconBase64) : null;
        distance = in.readString();
        double lat = in.readDouble();
        double lng = in.readDouble();
        location = new LatLng(lat, lng);
        url = in.readString();
    }

    /**
     * Constructor method with a JSONObject.
     * @param in FourSquare object represented as a JSONObject
     */
    public FourSquare(JSONObject in)
    {
        try
        {
            id = in.getString("id");
            name = in.getString("name");
            category = in.getString("category");
            String iconBase64 = in.getString("icon");
            icon = (iconBase64 != null && !iconBase64.isEmpty()) ? decodeBase64(iconBase64) : null;
            distance = in.getString("distance");
            double lat = in.getDouble("lat");
            double lng = in.getDouble("lng");
            location = new LatLng(lat, lng);
            url = in.getString("id");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public JSONObject writeToJSON()
    {
        JSONObject jo = new JSONObject();
        try
        {
            jo.put("id", id);
            jo.put("name", name);
            jo.put("category", category);
            String iconBase64 = (icon != null) ? encodeBase64(icon) : "";
            jo.put("icon", iconBase64);
            jo.put("distance", distance);
            if (location != null) {
                jo.put("lat", location.latitude);
                jo.put("lng", location.longitude);
            }
            jo.put("url", url);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }

    public String getId() { return id; }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Bitmap getIcon()
    {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getIconURI() {
        return iconURI;
    }

    public void setIconURI(String iconURI) {
        this.iconURI = iconURI;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public boolean getIsFavorited() {
        return isFavorited;
    }

    public void setIsFavorited(boolean isFavorited) {
        this.isFavorited = isFavorited;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(category);
        String iconBase64 = (icon != null) ? encodeBase64(icon) : "";
        parcel.writeString(iconBase64);
        parcel.writeString(distance);
        if (location != null) {
            parcel.writeDouble(location.latitude);
            parcel.writeDouble(location.longitude);
        }
        parcel.writeString(url);
    }

    // Parcelable creator
    public static final Creator<FourSquare> CREATOR = new Creator<FourSquare>() {
        @Override
        public FourSquare createFromParcel(Parcel source) {
            return new FourSquare(source);
        }

        @Override
        public FourSquare[] newArray(int size) {
            return new FourSquare[size];
        }
    };

    /**
     * Helper method to decode a base64 String to a bitmap
     * @param input base64 String
     * @return bitmap decoded from base64 String
     */
    private Bitmap decodeBase64(String input)
    {
        Bitmap bmp = null;
        try
        {
            byte[] decodedByte = Base64.decode(input, Base64.DEFAULT);
            if (decodedByte != null)
            {
                bmp = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return bmp;
    }

    /**
     * Helper method to encode a Bitmap to a base64 String.
     * @param bmp bitmap to encode
     * @return base64 String encoded from Bitmap
     */
    private String encodeBase64(@NonNull Bitmap bmp)
    {
        String base64 = "";
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return base64;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth)
            {
                inSampleSize *= 2;
            }
        }
        Log.d(TAG,"calculate "+inSampleSize);
        return inSampleSize;
    }
}
