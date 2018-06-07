package com.colinmckee.alamodrafthouse;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;

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
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MapsActivity";
    private GoogleMap _map;
    private static final LatLng AUSTIN = new LatLng(30.2672, -97.7431);
    private ArrayList<FourSquare> _fourSquareList = new ArrayList<>();
    private HashMap<String, Marker> _markerIdMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ImageButton backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadIntentExtras();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void loadIntentExtras() {
        Bundle extra = getIntent().getBundleExtra("extra");
        _fourSquareList = (ArrayList<FourSquare>) extra.getSerializable("fourSquare");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        _map = googleMap;

        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, r.getDisplayMetrics());

        //_map.setPadding(0, px, 0, 0);
        _map.setBuildingsEnabled(true);
        _map.getUiSettings().setZoomControlsEnabled(true);

        // Add a marker in Sydney and move the camera
        _map.addMarker(new MarkerOptions().position(AUSTIN).title("Austin, TX"));
        _map.setOnInfoWindowClickListener(this);

        // This builder will allow us to display all markers within our array
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(AUSTIN);
        // Add all markers from the _fourSquareList array
        for (FourSquare fs : _fourSquareList) {
            // Ignore if location is null
            if (fs.getLocation() == null) continue;
            MarkerOptions mo = new MarkerOptions().position(fs.getLocation()).title(fs.getName());
            Marker m = _map.addMarker(mo);
            _markerIdMap.put(fs.getId(), m);
            builder.include(fs.getLocation());
        }
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.20); // offset from edges of the map 20% of screen
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        _map.animateCamera(cu);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker == null) return;

        Object key = getKeyFromValue(_markerIdMap, marker);
        if (key == null) return;

        String id = (String) key;

        Log.d(TAG, id);
        for (FourSquare fs : _fourSquareList) {
            if (fs.getId().equals(id)) {
                Intent intent = new Intent(MapsActivity.this, DetailsActivity.class);
                intent.putExtra("fourSquareObject", fs);
                startActivity(intent);
                break;
            }
        }
    }

    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }
}
