package ar.com.leandro.enemyweapons.backend.api;

import android.content.Context;
import android.util.JsonReader;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import ar.com.leandro.enemyweapons.utils.Constants;

/**
 * Created by leandro on 10/30/16.
 */

public class WeaponsInfoRequest {

    /*
    * Obtenecion de datos del servidor del enemigo.
    * */

    private static WeaponsInfoRequest singleton;
    private static RequestQueue queue;

    public static synchronized WeaponsInfoRequest getInstance (Context ctx) {
        if (singleton == null) {
            singleton = new WeaponsInfoRequest();
            queue = Volley.newRequestQueue(ctx);
        }
        return singleton;
    }

    public void get(String tag, String url, Response.Listener<JSONObject> onSucces, Response.ErrorListener onError) {

        try {
            Request stringRequest = new JsonObjectRequest(Request.Method.GET, url, null, onSucces, onError);
            stringRequest.setTag(tag);
            queue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void cancelRequests(String tag) {
        if (queue != null) {
            queue.cancelAll(tag);
        }
    }

}
