package ar.com.leandro.enemyweapons.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

/**
 * Created by leandro on 10/30/16.
 */

public class ColorHelper {

    public static int getColor(Context context, int id) {
        try {
            return ContextCompat.getColor(context, id);
        } catch (Exception ex) {
            if (context != null) {
                return ColorHelper.getColor(context, id);
            } else {
                return id;
            }
        }
    }
}
