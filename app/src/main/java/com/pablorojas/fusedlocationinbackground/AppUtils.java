package com.pablorojas.fusedlocationinbackground;

import android.os.Build;

/**
 * @author Nayanesh Gupte
 */
public class AppUtils {

    /**
     * @return true If device has Android Marshmallow or above version
     */
    public static boolean hasM () {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static  final String ACTION = "LOCATION_ACTION";
    public static final String MESSAGE = "LOCATION_DATA";

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


}
