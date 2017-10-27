package com.felixunlimited.pbbible;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashScreen extends Activity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1123;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1023;
    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1223;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int permissionCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        sharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        permissionCount = sharedPreferences.getInt(Constants.PERMISSION_COUNT, 0);
        grantWriteExternalStoragePermission();
//        getPermission(MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    public void grantWriteExternalStoragePermission () {
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        else
            grantRecordAudioPermission();
    }
    public void grantRecordAudioPermission () {
        if (ContextCompat.checkSelfPermission(this,
        Manifest.permission.RECORD_AUDIO)
        != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
        else
            grantGetAccountsPermission();
    }
    public void grantGetAccountsPermission () {
        if (ContextCompat.checkSelfPermission(this,
        Manifest.permission.GET_ACCOUNTS)
        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
        }
        else {
            if (permissionCount == 3) {
                editor = sharedPreferences.edit();
                editor.putBoolean(Constants.PERMISSION_GRANTED, true);
                editor.apply();
                startActivity(new Intent(SplashScreen.this, BiblesOffline.class));
            } else {
                Toast.makeText(this, "PB-Bible needs those permissions to work", Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }
//    public void getPermission (int which) {
//        if (which == 0){
//            return;
//        }
//        else if (which == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
//            which = MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;
//        // Here, thisActivity is the current activity
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                == PackageManager.PERMISSION_GRANTED) {
//            which = MY_PERMISSIONS_REQUEST_RECORD_AUDIO;
//        }
//        else if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.RECORD_AUDIO)
//                == PackageManager.PERMISSION_GRANTED) {
//            which = MY_PERMISSIONS_REQUEST_GET_ACCOUNTS;
//        }
//
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Write External Storage Permission granted", Toast.LENGTH_SHORT).show();
                Util.copyAssetsFilesToSdCard(this);
                editor = sharedPreferences.edit();
                permissionCount++;
                editor.putInt(Constants.PERMISSION_COUNT, permissionCount);
                editor.apply();

                // permission was granted, yay! Do the
                // contacts-related task you need to do.

            } else {
                Toast.makeText(this, "Write External Storage Permission not granted", Toast.LENGTH_SHORT).show();
                editor = sharedPreferences.edit();
                editor.putBoolean(Constants.PERMISSION_GRANTED, false);
                editor.apply();

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
            grantRecordAudioPermission();
            //getPermission(MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
        else if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Record Audio Permission granted", Toast.LENGTH_SHORT).show();
                editor = sharedPreferences.edit();
                permissionCount++;
                editor.putInt(Constants.PERMISSION_COUNT, permissionCount);
                editor.apply();
            } else {
                Toast.makeText(this, "Record Audio Permission not granted", Toast.LENGTH_SHORT).show();
                editor = sharedPreferences.edit();
                editor.putBoolean(Constants.PERMISSION_GRANTED, false);
                editor.apply();
            }
            grantGetAccountsPermission();
            //getPermission(MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
        }
        else if (requestCode == MY_PERMISSIONS_REQUEST_GET_ACCOUNTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Get Accounts Permission granted", Toast.LENGTH_SHORT).show();
                editor = sharedPreferences.edit();
                permissionCount++;
                editor.putInt(Constants.PERMISSION_COUNT, permissionCount);
                editor.apply();
            } else {
                Toast.makeText(this, "Get Accounts Permission not granted", Toast.LENGTH_SHORT).show();
                editor = sharedPreferences.edit();
                editor.putBoolean(Constants.PERMISSION_GRANTED, false);
                editor.apply();
            }
            if (permissionCount == 3) {
                editor = sharedPreferences.edit();
                editor.putBoolean(Constants.PERMISSION_GRANTED, true);
                editor.apply();
                startActivity(new Intent(SplashScreen.this, BiblesOffline.class));
            } else {
                Toast.makeText(this, "PB-Bible needs those permissions to work", Toast.LENGTH_LONG).show();
            }
            finish();

//            getPermission(0);
        }

//        finish();
    }
}