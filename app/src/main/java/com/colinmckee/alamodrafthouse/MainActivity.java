package com.colinmckee.alamodrafthouse;

import android.animation.FloatArrayEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.colinmckee.alamodrafthouse.Adapters.FourSquareAdapter;
import com.colinmckee.alamodrafthouse.DataModels.FourSquare;
import com.colinmckee.alamodrafthouse.Listeners.OnFourSquareClick;
import com.colinmckee.alamodrafthouse.REST.RestRequest;
import com.colinmckee.alamodrafthouse.REST.RestResponse;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnFourSquareClick, TextWatcher {

    private static final String TAG = "MainActivity";
    private static final String CLIENT_ID = "M10HZO4TFP2MXBTIM0TJ4Q3VQSWQ2YAIXSYCB414IOZF50L2";
    private static final String CLIENT_SECRET = "NA04YVCQKGVNETL5N00LMDKDNOZ3SKGLBVRWMPEPAOVAL3EL";
    private static final LatLng AUSTIN = new LatLng(30.2672, -97.7431);
    private static final double METERS_TO_MILES = 0.000621371192;
    // Used to calculate distance
    private Location _austinLoc = new Location("");
    private EditText _editText;
    private FourSquareAdapter _adapter;
    private FourSquareView _fourSquareView;
    private ArrayList<FourSquare> _fourSquareList = new ArrayList<>();
    private FloatingActionButton _fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _fourSquareView = findViewById(R.id.foursquare_view);
        _fourSquareView.setOnFourSquareClickListener(this);

        _austinLoc.setLatitude(AUSTIN.latitude);
        _austinLoc.setLongitude(AUSTIN.longitude);

        _editText = findViewById(R.id.edit_text_field);
        _editText.addTextChangedListener(this);
        _editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Search button was pressed
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (_editText != null && _editText.getText() != null && _editText.getText().length() > 0) {

                        fourSquareQuery(_editText.getText().toString());
                    }
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });

        _fourSquareView.setFourSquareList(_fourSquareList);

        initFloatingActionButton();
    }

    private void initFloatingActionButton() {
        _fab = findViewById(R.id.fab);
        _fab.setAlpha(0.0f);
        _fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_fourSquareList == null || _fourSquareList.size() == 0) return;

                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                Bundle extra = new Bundle();
                extra.putSerializable("fourSquare", _fourSquareList);
                intent.putExtra("extra", extra);
                startActivity(intent);
            }
        });
    }

    private void animateFab(final boolean in) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (_fab == null) return;

                float v = animation.getAnimatedFraction();
                if (!in) v = 1.0f - v;
                _fab.setAlpha(v);
            }
        });
        valueAnimator.setFloatValues(0, 1); // Ignored.
        valueAnimator.setStartDelay(500);
        valueAnimator.setDuration(800);
        valueAnimator.start();
    }

    private void fourSquareQuery(String query) {
        String baseUrl = "https://api.foursquare.com/v2/venues/explore?near=Austin,+TX&limit=8&v=20180323";
        String url = baseUrl + "&query=" + query;
        RestRequest rr = new RestRequest("GET", url, CLIENT_ID, CLIENT_SECRET);
        rr.asyncRequest(new RestRequest.OnComplete() {
            @Override
            public void onComplete(boolean success, RestResponse response) {
                if (success && response.jsonObject != null) {
                    _fourSquareList.clear();
                    JSONObject jo = response.jsonObject;
                    try {
                        JSONObject resp = jo.getJSONObject("response");
                        if (!resp.has("groups")) return;

                        JSONArray groups = resp.getJSONArray("groups");
                        for (int i = 0; i < groups.length(); i++) {
                            JSONObject group = groups.getJSONObject(i);
                            if (group == null || !group.has("items")) continue;

                            JSONArray items = group.getJSONArray("items");
                            for (int j = 0; j < items.length(); j++) {
                                JSONObject item = items.getJSONObject(j);
                                if (item == null || !item.has("venue")) continue;
                                JSONObject venue = item.getJSONObject("venue");
                                if (!venue.has("name") || venue.isNull("name")) continue;

                                String id = venue.getString("id");
                                String name = venue.getString("name");
                                String categoriesStr = "";
                                LatLng ll = null;
                                double distance = 0;

                                if (venue.has("categories") && !venue.isNull("categories")) {
                                    JSONArray categories = venue.getJSONArray("categories");
                                    List<String> names = new ArrayList<>();
                                    for (int k = 0; k < categories.length(); k++) {
                                        JSONObject category = categories.getJSONObject(k);
                                        names.add(category.getString("name"));
                                    }
                                    categoriesStr = names.toString().replace("[", "").replace("]", "");
                                }

                                if (venue.has("location") && !venue.isNull("location")) {
                                    JSONObject location = venue.getJSONObject("location");
                                    if (location != null) {
                                        double lat = location.getDouble("lat");
                                        double lng = location.getDouble("lng");
                                        ll = new LatLng(lat, lng);
                                        Location loc = new Location("");
                                        loc.setLatitude(lat);
                                        loc.setLongitude(lng);
                                        distance = loc.distanceTo(_austinLoc);
                                        // This converts it to miles
                                        distance *= METERS_TO_MILES;
                                    }
                                }
                                FourSquare fs = new FourSquare();
                                fs.setId(id);
                                fs.setName(name);
                                fs.setCategory(categoriesStr);
                                fs.setLocation(ll);
                                fs.setDistance(String.format("Distance: %.2f miles", distance));
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                boolean isFavorited = prefs.getBoolean("fav_"+id, false);
                                fs.setIsFavorited(isFavorited);
                                _fourSquareList.add(fs);

                                venueQuery(fs);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (_fourSquareView != null) _fourSquareView.setFourSquareList(_fourSquareList);
                    // Show the floating action button
                    if (_fourSquareList.size() > 0) animateFab(true);

                } else {
                    Log.e(TAG, response.msg);
                }
            }
        });
    }

    /**
     * This query is necessary to retrieve the icon and url associated with
     * the FourSquare venue.
     * @param fs FourSquare object
     */
    private void venueQuery(final FourSquare fs) {
        final String id = fs.getId();
        String url = "https://api.foursquare.com/v2/venues/"+id+"?v=20180323";
        RestRequest rr = new RestRequest("GET", url, CLIENT_ID, CLIENT_SECRET);
        rr.asyncRequest(new RestRequest.OnComplete() {
            @Override
            public void onComplete(boolean success, RestResponse response) {
                if (success && response.jsonObject != null) {
                    JSONObject jo = response.jsonObject;
                    try {
                        JSONObject resp = jo.getJSONObject("response");
                        if (!resp.has("venue") || resp.isNull("venue")) return;

                        JSONObject venue = resp.getJSONObject("venue");
                        String url = venue.getString("url");
                        String iconUrl = "";

                        JSONObject photos = venue.getJSONObject("photos");
                        JSONArray groups = photos.getJSONArray("groups");
                        if (groups != null && groups.length() > 0) {
                            JSONObject group = groups.getJSONObject(0);
                            JSONArray items = group.getJSONArray("items");
                            if (items != null && items.length() > 0) {
                                JSONObject item = items.getJSONObject(0);
                                iconUrl = item.getString("prefix") + "100x100" + item.getString("suffix");
                            }
                        }
                        fs.setUrl(url);
                        fs.setIconURI(iconUrl);
                        Log.d(TAG, url+" icon: "+iconUrl);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (_fourSquareView != null) _fourSquareView.updateFourSquareItem(fs);
                } else if (response.msg != null) {
                    try {
                        JSONObject error = new JSONObject(response.msg);
                        if (error != null) {
                            JSONObject meta = error.getJSONObject("meta");
                            if (meta != null) {
                                String errorDetail = "Error: " + meta.getString("errorDetail");
                                Snackbar.make(_fourSquareView, errorDetail, Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Snackbar.make(_fourSquareView, "No results found.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(FourSquareAdapter.ViewHolder holder, @NonNull FourSquare fourSquare, int position) {
        if (fourSquare == null) return;

        Log.d(TAG, fourSquare.getName());

        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra("fourSquareObject", fourSquare);
        startActivity(intent);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (_editText == null) return;

        Log.d(TAG, _editText.getText().toString());
    }
}
