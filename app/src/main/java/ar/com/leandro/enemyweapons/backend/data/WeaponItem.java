package ar.com.leandro.enemyweapons.backend.data;

import android.location.Location;

import java.io.Serializable;

/**
 * Created by leandro on 10/30/16.
 */

public class WeaponItem implements Serializable {

    private Location location;
    private int radius;
    private int radiusInMeter;
    private String code;
    private String kind;

    public WeaponItem () {}

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRadiusInMeter() {
        return radiusInMeter;
    }

    public void setRadiusInMeter(int radiusInMeter) {
        this.radiusInMeter = radiusInMeter;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public android.location.Location getGeoLocation() {
        android.location.Location geoLocation = new android.location.Location("");
        geoLocation.setLatitude(this.location.latitude);
        geoLocation.setLongitude(this.location.longitude);

        return geoLocation;
    }

    public class Location {
        double latitude;
        double longitude;

        public Location(){}

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
