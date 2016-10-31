package ar.com.leandro.enemyweapons.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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

    @BindView(R.id.coordenadas)
    TextView coordenadas;
    @BindView(R.id.progres_view)
    RelativeLayout popup;

    ArrayList<WeaponItem> weapons = new ArrayList<>();

    private GoogleMap mMap;
    private BroadcastReceiver receiver;
    private boolean firstLocation = true;

    private static final int mapZoom = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        snackBarHolder = findViewById(R.id.rl_content);

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
            }
        };
    }

    private void gotoMyLocation (LatLng latLng) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mapZoom);
        mMap.animateCamera(cameraUpdate);
        firstLocation = false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        WeaponsInfoRequest.getInstance(this).get(Constants.REQUEST_TAG, Constants.SERVER_URL, this, this);
        enableLocation();
    }

    @Override
    public void onStart() {
        super.onStart();
        popup.setVisibility(View.VISIBLE);
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
        for (WeaponItem weaponItem : weapons) {
            if (myLocation.distanceTo(weaponItem.getGeoLocation()) <= weaponItem.getRadiusInMeter()) {
                alertDangerPosition();
                break;
            }
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
                    .strokeWidth(3)
                    .strokeColor(ColorHelper.getColor(this, R.color.orange))
                    .fillColor(ColorHelper.getColor(this, R.color.overlay_orange)));
        }
    }

    private void alertDangerPosition() {

    }
}
