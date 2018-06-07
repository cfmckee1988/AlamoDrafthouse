package com.colinmckee.alamodrafthouse;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.colinmckee.alamodrafthouse.DataModels.FourSquare;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class DetailsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap _map;
    private ImageButton _backBtn;
    private static final LatLng AUSTIN = new LatLng(30.2672, -97.7431);
    private FourSquare _fs;
    private TextView _url;
    private ImageButton _favoriteBtn;
    private Drawable _favorite;
    private Drawable _notFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        _backBtn = findViewById(R.id.back_btn);
        _backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        _url = findViewById(R.id.url);
        _url.setMovementMethod(LinkMovementMethod.getInstance());

        _favoriteBtn = findViewById(R.id.favorite_btn);
        _favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_fs == null || _favoriteBtn == null) return;

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DetailsActivity.this);
                if (_fs.getIsFavorited()) {
                    _fs.setIsFavorited(false);
                    prefs.edit().putBoolean("fav_"+_fs.getId(), false).commit();
                    _favoriteBtn.setImageDrawable(_notFavorite);
                } else {
                    _fs.setIsFavorited(true);
                    prefs.edit().putBoolean("fav_"+_fs.getId(), true).commit();
                    _favoriteBtn.setImageDrawable(_favorite);
                }
            }
        });

        loadFavoriteInfo();
        loadIntent();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        _map = googleMap;

        // Set padding for the map
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, r.getDisplayMetrics());

        //_map.setPadding(0, px, 0, px*2);
        _map.setBuildingsEnabled(true);
        _map.getUiSettings().setZoomControlsEnabled(true);

        // Add a marker in Sydney and move the camera
        _map.addMarker(new MarkerOptions().position(AUSTIN).title("Austin, TX"));

        // This builder will allow us to display all markers within our array
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(AUSTIN);

        if (_fs != null && _fs.getLocation() != null) {
            MarkerOptions mo = new MarkerOptions().position(_fs.getLocation()).title(_fs.getName());
            _map.addMarker(mo);
            builder.include(_fs.getLocation());
        }

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.20); // offset from edges of the map 20% of screen
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        _map.animateCamera(cu);
    }

    private void loadFavoriteInfo() {
        Resources resources = this.getResources();
        final int favoriteId = resources.getIdentifier("baseline_favorite", "drawable",
                this.getPackageName());
        final int notFavoriteId = resources.getIdentifier("baseline_favorite_border", "drawable",
                this.getPackageName());
        _favorite = resources.getDrawable(favoriteId, null);
        _notFavorite = resources.getDrawable(notFavoriteId, null);
    }

    private void loadIntent() {
        _fs = getIntent().getParcelableExtra("fourSquareObject");
        if (_fs != null) {
            Log.d("Test", _fs.getName());
            _url.setText(_fs.getUrl());
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean isFavorited = prefs.getBoolean("fav_"+_fs.getId(), false);
            _favoriteBtn.setImageDrawable(isFavorited ? _favorite : _notFavorite);

        }
    }
}
