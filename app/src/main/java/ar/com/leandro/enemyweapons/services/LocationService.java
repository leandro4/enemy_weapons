package ar.com.leandro.enemyweapons.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ar.com.leandro.enemyweapons.utils.Constants;

/**
 * Created by leandro on 10/29/16.
 */

public class LocationService extends Service {

    LocalBroadcastManager broadcaster;

    public void sendResult(Location location) {
        Intent intent = new Intent(Constants.SERVICE_INT);
        Bundle extras = new Bundle();
        if (location != null) {
            extras.putDouble(Constants.SERVICE_LOCATION_ALT, location.getAltitude());
            extras.putDouble(Constants.SERVICE_LOCATION_LONG, location.getLongitude());
        }
        intent.putExtras(extras);
        broadcaster.sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    LocationManager locationManager;

    @Override
    public int onStartCommand(Intent intenc, int flags, int idArranque) {
        Log.d("LocationService", "started");

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                out();
            } else {
                if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.LOCATION_TIME_LAP, 10, locationNETListener);

                if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.LOCATION_TIME_LAP, 10, locationGPSListener);
            }
        } else
            out();

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        out();
    }

    @Override
    public void onDestroy () {
        out();
        super.onDestroy();
    }

    private void out () {
        Log.d("LocationService", "stopped");

        if (ContextCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
                locationManager.removeUpdates(locationGPSListener);
            if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
                locationManager.removeUpdates(locationNETListener);
        }
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intencion) {
        return null;
    }

    LocationListener locationGPSListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            sendResult(location);
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener locationNETListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            sendResult(location);
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
    };
}
