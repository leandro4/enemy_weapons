package ar.com.leandro.enemyweapons.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import ar.com.leandro.enemyweapons.R;
import ar.com.leandro.enemyweapons.services.LocationService;
import ar.com.leandro.enemyweapons.utils.Constants;

public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BroadcastReceiver receiver;
    private TextView coordenadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        coordenadas = (TextView) findViewById(R.id.coordenadas);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double latitude = intent.getExtras().getDouble(Constants.SERVICE_LOCATION_ALT);
                double longitude = intent.getExtras().getDouble(Constants.SERVICE_LOCATION_LONG);
                calculateDistances(latitude, longitude);
                coordenadas.setText("Latitud: " + latitude + "\nLongitud: " + longitude);
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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

    private void calculateDistances(double latitude, double longitude) {

    }

}
