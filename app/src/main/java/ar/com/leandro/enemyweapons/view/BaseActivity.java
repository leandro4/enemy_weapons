package ar.com.leandro.enemyweapons.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import ar.com.leandro.enemyweapons.R;
import ar.com.leandro.enemyweapons.utils.Constants;

/**
 * Created by leandro on 10/29/16.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected void checkPermission (String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                Toast.makeText(this, getString(R.string.toast_permission_needed), Toast.LENGTH_SHORT).show();

            } else {

                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_LOCATION_PERMISSION: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    onLocationPermissionGranted(true);
                else
                    onLocationPermissionGranted(false);

                return;
            }
        }
    }

    protected abstract void onLocationPermissionGranted(boolean granted);

}
