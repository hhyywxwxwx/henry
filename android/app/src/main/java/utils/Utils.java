package utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.NaN.dji_vjoy.MainActivity;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class Utils {
    public static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    public static final int REQUEST_PERMISSION_CODE = 12345;
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";

    public static void checkAndRequestPermissions(AppCompatActivity activity) {
        ArrayList<String> missingPermissions = new ArrayList<>();
        for (String permission : Utils.REQUIRED_PERMISSION_LIST) {
            if(ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED){
                missingPermissions.add(permission);
            }
        }
        if(!missingPermissions.isEmpty()){
            ActivityCompat.requestPermissions(activity,
                    missingPermissions.toArray(new String[missingPermissions.size()]),
                    Utils.REQUEST_PERMISSION_CODE);
        }
    }

    public static boolean isValidIP(String str) {
        String rex = "((\\d{1,2}|25[0-5]|2[0-4]\\d|1\\d{2})\\.){3}(\\d{1,2}|25[0-5]|2[0-4]\\d|1\\d{2})";
        Pattern pat = Pattern.compile(rex);
        return pat.matcher(str).matches();
    }

    public static byte[] Int16ToByte(int i){
        return new byte[]{
                (byte) ((i>>8)&0xff), (byte) (i&0xff)
        };
    }
    public static byte[] ByteArrayConcat(byte[] a, byte[] b){
        byte[] dest = new byte[a.length+b.length];
        System.arraycopy(a, 0, dest, 0, a.length);
        System.arraycopy(b, 0, dest, a.length, b.length);
        return dest;
    }
}
