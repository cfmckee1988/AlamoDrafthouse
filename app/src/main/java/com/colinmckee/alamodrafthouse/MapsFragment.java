package com.colinmckee.alamodrafthouse;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.colinmckee.alamodrafthouse.DataModels.FourSquare;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsFragment extends Fragment {

    private static final String TAG = "MapsFragment";
    MapView _mapView;
    private GoogleMap _map;

    private DetailsFragment.OnDetailsListener _listener;
    private ImageButton _backBtn;
    private static final LatLng AUSTIN = new LatLng(30.2672, -97.7431);
    private ArrayList<FourSquare> _fourSquareList = new ArrayList<>();
    private HashMap<String, Marker> _markerIdMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

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

                Resources r = getResources();
                int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, r.getDisplayMetrics());

                _map.setPadding(0, px, 0, 0);
                _map.setBuildingsEnabled(true);
                _map.getUiSettings().setZoomControlsEnabled(true);

                // Add a marker in Sydney and move the camera
                _map.addMarker(new MarkerOptions().position(AUSTIN).title("Austin, TX"));
                _map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        if (marker == null) return;

                        Object key = getKeyFromValue(_markerIdMap, marker);
                        if (key == null) return;

                        String id = (String) key;

                        Log.d(TAG, id);
                        for (FourSquare fs : _fourSquareList) {
                            if (fs.getId().equals(id)) {
                                DetailsFragment detailsFragment = new DetailsFragment();
                                detailsFragment.setFourSquareItem(fs);
                                detailsFragment.setOnFavoritesListener(_listener);
                                showFragment(detailsFragment, "DetailsFragment");
                                break;
                            }
                        }
                    }
                });

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
        });

        _backBtn = rootView.findViewById(R.id.back_btn);
        _backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_listener != null) _listener.onDismissed();

                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

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

    public void setOnFavoritesListener(DetailsFragment.OnDetailsListener listener) {
        _listener = listener;
    }

    public void setFourSquareList(ArrayList<FourSquare> list) {
        _fourSquareList = list;
    }

    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    private void showFragment(Fragment fragment, String name) {

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container, fragment);
        transaction.addToBackStack(name);
        transaction.commit();
    }
}
