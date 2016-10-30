package ar.com.leandro.enemyweapons.utils;

/**
 * Created by leandro on 10/29/16.
 */

public class Constants {

    // RequestPermission constant
    public static final int REQUEST_LOCATION_PERMISSION = 101;

    // Lap - location update milis
    public static final int LOCATION_TIME_LAP = 8000;
    public static final int LOCATION_FAST_TIME_LAP = 3000;

    // Service
    static final public String SERVICE_INT = "service_intent";
    static final public String SERVICE_LOCATION_LAT = "location_latitude";
    static final public String SERVICE_LOCATION_LONG = "location_longitude";

    // api URL
    static final public String SERVER_URL = "https://redarmyserver.appspot.com/_ah/api/myApi/v1/torretinfocollection";
    static final public String REQUEST_TAG = "weapons_request_tag";
}
