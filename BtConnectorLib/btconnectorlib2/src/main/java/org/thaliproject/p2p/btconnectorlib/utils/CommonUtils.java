/* Copyright (c) 2015-2016 Microsoft Corporation. This software is licensed under the MIT License.
 * See the license file delivered with this project for further information.
 */
package org.thaliproject.p2p.btconnectorlib.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import org.thaliproject.p2p.btconnectorlib.internal.bluetooth.BluetoothUtils;

/**
 * Commonly used utils and constants.
 */
public class CommonUtils {
    private static final String TAG = CommonUtils.class.getName();

    /**
     * @return True, if we are running on Lollipop (Android version 5.x, API level 21) or higher.
     * False otehrwise.
     */
    public static boolean isLollipopOrHigher() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    /**
     * @return True, if we are running on Marshmallow (Android version 6.x) or higher. False otehrwise.
     */
    public static boolean isMarshmallowOrHigher() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    /**
     * Checks whether the given permission is granted (by the user) for the given activity.
     *
     * @param permission The permission to check.
     * @param activity The activity.
     * @return True, if granted. False otherwise.
     */
    @TargetApi(23)
    public static boolean isPermissionGranted(String permission, Activity activity) {
        int permissionCheck = PackageManager.PERMISSION_DENIED;

        if (activity != null) {
            permissionCheck = ContextCompat.checkSelfPermission(activity, permission);
            Log.i(TAG, "isPermissionGranted: " + permission + ": " + permissionCheck);
        } else {
            throw new NullPointerException("The given activity is null");
        }

        return (permissionCheck == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Checks whether the given Bluetooth MAC address has the proper form or not.
     *
     * A valid Bluetooth MAC address has form of: 01:23:45:67:89:AB
     * Note that the possible alphabets in the string have to be upper case.
     *
     * @param bluetoothMacAddress The Bluetooth MAC address to validate.
     * @return True, if the address is valid. False otherwise.
     */
    public static boolean isValidBluetoothMacAddress(String bluetoothMacAddress) {
        return BluetoothUtils.isValidBluetoothMacAddress(bluetoothMacAddress);
    }

    /**
     * @param stringToCheck The string to check.
     * @return True, if the given string is not null and not empty.
     */
    public static boolean isNonEmptyString(String stringToCheck) {
        return (stringToCheck != null && stringToCheck.length() > 0);
    }

    /**
     * Creates a simple handshake message.
     *
     * @return The newly created handshake message as a byte array.
     */
    public static byte[] createSimpleHandshakeMessage() {
        byte[] message = new byte[1];
        message[0] = (byte) 0x0;
        return message;
    }
}
