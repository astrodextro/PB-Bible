package com.felixunlimited.pbbible.sync;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.felixunlimited.pbbible.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

/**
 * Created by Ando on 06/10/2017.
 */

public class Util {

    public static boolean downloadFile (Context context,String onlineDir, String filename) {
        File downloadsDir = new File(Environment.getExternalStorageDirectory(), Constants.DOWNLOAD_FOLDER);
        if (!downloadsDir.exists())
            downloadsDir.mkdirs();
        File file = new File(downloadsDir, filename);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (file.exists())
            file.delete();

        try {
            URL url = new URL(onlineDir+filename);
            URLConnection c = url.openConnection();
            int contentLength = c.getContentLength();
//            c.setRequestMethod("GET");
//            c.setDoOutput(true);
//            c.connect();

//            File outputFile = new File(file, )
            DataInputStream dataInputStream = new DataInputStream(url.openStream());

            byte[] buffer = new byte[contentLength];
            dataInputStream.readFully(buffer);
            dataInputStream.close();

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
            fos.write(buffer);
            fos.flush();
            fos.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;


//        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
//        Uri uri = Uri.parse(onlineDir+filename);
//        DownloadManager.Request req=new DownloadManager.Request(uri);
//
//        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
//                | DownloadManager.Request.NETWORK_MOBILE)
//                .setAllowedOverRoaming(false)
//                .setVisibleInDownloadsUi(false)
//                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
//                .setTitle("WordApp Syncing")
//                .setDescription("This will just take a while")
////                                .setDestinationUri(Uri.fromFile(Utility.chooseFile(context, fileNameT)));
//                .setDestinationInExternalFilesDir(context, null, filename)
//                .setDestinationInExternalPublicDir(null, downloadsDir.getAbsolutePath());
//        //Enqueue a new download and same the referenceId
//        long downloadReference = downloadManager.enqueue(req);
    }
    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    private static boolean isPhone(Context context){
        TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if(manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE){
            return true;
        }
        return false;

    }

    public static boolean isSimSupport(Context context)
    {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
        return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
    }

    public static boolean isConnected(Context context) {
        if (isAirplaneModeOn(context))
            return false;

        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

}
