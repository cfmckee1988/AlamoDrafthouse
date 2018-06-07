package com.colinmckee.alamodrafthouse;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.colinmckee.alamodrafthouse.DataModels.FourSquare;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class DetailsFragment extends Fragment {
    MapView _mapView;
    private GoogleMap _map;

    public interface OnDetailsListener {
        void onFavorited(FourSquare fs, boolean favorited);
        void onDismissed();
    }

    private OnDetailsListener _listener;
    private ImageButton _backBtn;
    private static final LatLng AUSTIN = new LatLng(30.2672, -97.7431);
    private FourSquare _fs;
    private TextView _url;
    private ImageButton _favoriteBtn;
    private Drawable _favorite;
    private Drawable _notFavorite;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        loadFavoriteInfo();
        _mapView = (MapView) rootView.findViewById(R.id.mapView);
        _mapView.onCreate(savedInstanceState);

        _mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        _mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                _map = mMap;

                // Set padding for the map
                Resources r = getResources();
                int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, r.getDisplayMetrics());

                _map.setPadding(0, px, 0, 0);
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
        });

        _backBtn = rootView.findViewById(R.id.back_btn);
        _backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_listener != null) _listener.onDismissed();

                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        _url = rootView.findViewById(R.id.url);
        _url.setMovementMethod(LinkMovementMethod.getInstance());

        _favoriteBtn = rootView.findViewById(R.id.favorite_btn);
        _favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_fs == null || _favoriteBtn == null) return;

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (_fs.getIsFavorited()) {
                    _fs.setIsFavorited(false);
                    prefs.edit().putBoolean("fav_"+_fs.getId(), false).commit();
                    _favoriteBtn.setImageDrawable(_notFavorite);

                    if (_listener != null) _listener.onFavorited(_fs, false);
                } else {
                    _fs.setIsFavorited(true);
                    prefs.edit().putBoolean("fav_"+_fs.getId(), true).commit();
                    _favoriteBtn.setImageDrawable(_favorite);

                    if (_listener != null) _listener.onFavorited(_fs, true);
                }
            }
        });

        if (_fs != null) {
            _favoriteBtn.setImageDrawable(_fs.getIsFavorited() ? _favorite : _notFavorite);
            _url.setText(_fs.getUrl());
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        _mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        _mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        _mapView.onLowMemory();
    }

    public void setOnFavoritesListener(OnDetailsListener listener) {
        _listener = listener;
    }

    public void setFourSquareItem(FourSquare fs) {
        _fs = fs;
    }

    private void loadFavoriteInfo() {
        Resources resources = this.getResources();
        final int favoriteId = resources.getIdentifier("baseline_favorite", "drawable",
                getActivity().getPackageName());
        final int notFavoriteId = resources.getIdentifier("baseline_favorite_border", "drawable",
                getActivity().getPackageName());
        _favorite = resources.getDrawable(favoriteId, null);
        _notFavorite = resources.getDrawable(notFavoriteId, null);

    }
}
