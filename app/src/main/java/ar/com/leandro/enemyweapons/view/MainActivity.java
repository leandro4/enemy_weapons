package ar.com.leandro.enemyweapons.view;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.TransitionDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import ar.com.leandro.enemyweapons.R;
import ar.com.leandro.enemyweapons.backend.api.WeaponsInfoRequest;
import ar.com.leandro.enemyweapons.backend.data.WeaponItem;
import ar.com.leandro.enemyweapons.services.LocationService;
import ar.com.leandro.enemyweapons.utils.ColorHelper;
import ar.com.leandro.enemyweapons.utils.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements OnMapReadyCallback,
        Response.Listener<JSONObject>, Response.ErrorListener {

    @BindView(R.id.progres_view)
    RelativeLayout popup;
    @BindView(R.id.rl_danger_screen)
    RelativeLayout dangerScreen;
    @BindView(R.id.tv_distance_danger_zone)
    TextView tvDistanceDangerZone;

    ArrayList<WeaponItem> weapons = new ArrayList<>();

    private GoogleMap mMap;
    private BroadcastReceiver receiver;
    private boolean dangerPosition = false;
    private boolean firstLocation = true;
    private float minDistanceToDangerZone = -1;

    private static final int mapZoom = 14;

    // transition color for dangerous position
    int colorFrom;
    int colorTo;
    boolean changed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        snackBarHolder = findViewById(R.id.rl_content);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initializeBroadcastRec();

        colorFrom = ColorHelper.getColor(this, R.color.transparent_background);
        colorTo = ColorHelper.getColor(this, R.color.danger_background);
    }

    private void initializeBroadcastRec() {
        // recibimos nuestra localizacion y calculamos las distancias a las armas enemigas
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double latitude = intent.getExtras().getDouble(Constants.SERVICE_LOCATION_LAT);
                double longitude = intent.getExtras().getDouble(Constants.SERVICE_LOCATION_LONG);
                Location location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                calculateDistances(location);

                if (firstLocation)
                    gotoMyLocation(new LatLng(latitude, longitude));
                tvDistanceDangerZone.setText(getString(R.string.min_distance_danger_zone)
                        .replace("[X]", String.valueOf((int) minDistanceToDangerZone)));
            }
        };
    }

    private void gotoMyLocation (final LatLng latLng) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mapZoom);
        mMap.animateCamera(cameraUpdate);
        firstLocation = false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        popup.setVisibility(View.VISIBLE);
        WeaponsInfoRequest.getInstance(this).get(Constants.REQUEST_TAG, Constants.SERVER_URL, this, this);

        enableLocation();
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(Constants.SERVICE_INT));
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, Constants.REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onStop() {
        super.onStop();
        WeaponsInfoRequest.getInstance(this).cancelRequests(Constants.REQUEST_TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(Constants.SERVICE_INT));
    }

    @Override
    protected void onLocationPermissionGranted(boolean granted) {
        if (granted) {
            enableLocation();
            Intent i = new Intent(MainActivity.this, LocationService.class);
            startService(i);
        }
    }

    private void enableLocation() {
        if (mMap != null)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                mMap.setMyLocationEnabled(true);
    }

    private void calculateDistances(Location myLocation) {
        minDistanceToDangerZone = -1;

        for (WeaponItem weaponItem : weapons) {
            float distance = myLocation.distanceTo(weaponItem.getGeoLocation());
            if (minDistanceToDangerZone == -1)
                minDistanceToDangerZone = distance - weaponItem.getRadiusInMeter();

            if (distance  <= weaponItem.getRadiusInMeter()) {
                if (!dangerPosition) {
                    dangerPosition = true;
                    alertDangerPosition(true);
                }
                minDistanceToDangerZone = 0;
                return;
            }
            if (distance - weaponItem.getRadiusInMeter() < minDistanceToDangerZone)
                minDistanceToDangerZone = distance - weaponItem.getRadiusInMeter();
        }
        if (dangerPosition) {
            alertDangerPosition(false);
            dangerPosition = false;
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<ArrayList<WeaponItem>>() {
            }.getType();
            weapons = gson.fromJson(response.getJSONArray("items").toString(), listType);
            setUpClips();
            popup.setVisibility(View.GONE);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error trying to decode enemy data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(MainActivity.this, "request error", Toast.LENGTH_SHORT).show();
        popup.setVisibility(View.GONE);
    }

    private void setUpClips() {
        if (mMap == null) return;

        for (WeaponItem weapon : weapons) {
            LatLng latLng = new LatLng(weapon.getLocation().getLatitude(), weapon.getLocation().getLongitude());

            mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(weapon.getRadiusInMeter())
                    .strokeWidth(2)
                    .strokeColor(ColorHelper.getColor(this, R.color.orange))
                    .fillColor(ColorHelper.getColor(this, R.color.overlay_orange)));
        }
    }

    private void alertDangerPosition(boolean danger) {
        if (danger) {
            dangerScreen.setVisibility(View.VISIBLE);
            setRedAlertScreen();
        }
        else
            dangerScreen.setVisibility(View.GONE);
    }

    private void setRedAlertScreen() {
        if (dangerScreen.getVisibility() == View.VISIBLE) {
            Handler alertHandler = new Handler(Looper.getMainLooper());
            final Runnable runnable = new Runnable() {
                public void run() {
                    dangerScreen.setBackgroundColor(colorTo);
                    invertColors();
                    setRedAlertScreen();
                }
            };
            alertHandler.postDelayed(runnable, 1200);
        }
    }

    private void invertColors() {
        if (changed) {
            colorFrom = ColorHelper.getColor(this, R.color.transparent_background);
            colorTo = ColorHelper.getColor(this, R.color.danger_background);
            changed = false;
        } else {
            colorFrom = ColorHelper.getColor(this, R.color.danger_background);
            colorTo = ColorHelper.getColor(this, R.color.transparent_background);
            changed = true;
        }
    }
}
