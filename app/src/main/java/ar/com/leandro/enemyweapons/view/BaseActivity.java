package ar.com.leandro.enemyweapons.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import ar.com.leandro.enemyweapons.R;
import ar.com.leandro.enemyweapons.utils.Constants;
import butterknife.ButterKnife;

/**
 * Created by leandro on 10/29/16.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected View snackBarHolder;

    protected void checkPermission (final String permission, final int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Snackbar.make(snackBarHolder, getString(R.string.permission_needed_snack_bar), Snackbar.LENGTH_INDEFINITE).setAction(R.string.btn_snack_bar_permission, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(BaseActivity.this, new String[]{permission}, requestCode);
                    }
                }).show();

            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        } else {
            switch (requestCode) {
                case Constants.REQUEST_LOCATION_PERMISSION: {
                    onLocationPermissionGranted(true);
                }
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
