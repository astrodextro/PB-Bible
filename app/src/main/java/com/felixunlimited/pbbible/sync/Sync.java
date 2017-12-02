package com.felixunlimited.pbbible.sync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;

import com.felixunlimited.pbbible.Constants;
import com.felixunlimited.pbbible.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.felixunlimited.pbbible.Util.getEmail;
import static com.felixunlimited.pbbible.Util.getUserID;
import static java.util.Calendar.LONG;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.getInstance;

/**
 * Created by Ando on 02/10/2017.
 */

public class Sync extends AsyncTask<String, Void, String> {
    private static SharedPreferences mPreferences;
    private static SharedPreferences.Editor mEditor;
    private static Context mContext;
    public static DatabaseHelper databaseHelper;

    public Sync(Context context) {
        super();
        mContext = context;
        mPreferences = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(mContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        databaseHelper.open();
        if (params.length > 0) {
            baeSync(mContext, params[0], params[1]);
        }
        else {
            if (!mPreferences.getBoolean(Constants.NOT_NEW_DEVICE_SYNC, false)) {
                syncNewDevice();
            }
            if (!mPreferences.getBoolean(Constants.SCRIPTURES_SYNC, false)) {
                syncScriptures();
            }

            if (!mPreferences.getString(Constants.MONTH, "").toUpperCase().equals(getInstance().getDisplayName(MONTH, LONG, Locale.getDefault()).toUpperCase())) {
                syncTheme();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (databaseHelper != null)
            databaseHelper.close();
    }

    private void baeSync(Context context, String baeEmail, String table) {

        if (!Util.isConnected(context)) {
            context.sendBroadcast((new Intent().putExtra(Constants.BAE_SYNC, "No internet connection")));
            return;
        }

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject, response;
        jsonObject = new JSONObject();
        try {
            jsonObject.put("bae_email", baeEmail).put("user_email", com.felixunlimited.pbbible.Util.getEmail(context));
            response = jsonParser.makeHttpRequest(Constants.WEBSERVICE_URL,"POST", jsonObject.toString(), table);
            if (response != null) {
                if (!response.getString("response").equals("error")) {
                    JSONArray baeJSONArray = response.getJSONArray("bae");
                    SharedPreferences.Editor editor = context.getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE).edit();
                    editor.putInt(Constants.BAE_CONFIRMED, baeJSONArray.getJSONObject(0).getInt("bae_confirm"));
                    editor.putInt(Constants.BAE_RECEIPT, baeJSONArray.getJSONObject(0).getInt("bae_receipt"));
                    editor.putInt(Constants.BAE_REQUEST, baeJSONArray.getJSONObject(0).getInt("bae_request"));
                    editor.putString(Constants.BAE_EMAIl, baeJSONArray.getJSONObject(0).getString("bae_email"));
                    editor.apply();
                    Intent intent = new Intent("com.felixunlimited.BroadcastReceiver");
                    intent.putExtra(Constants.BAE_SYNC, table);
                    context.sendBroadcast(intent);
                }
                else {
                    Intent intent = new Intent("com.felixunlimited.BroadcastReceiver");
                    intent.putExtra(Constants.BAE_SYNC, "online database error");
                    context.sendBroadcast(intent);
                }
            }
            else {
                Intent intent = new Intent("com.felixunlimited.BroadcastReceiver");
                intent.putExtra(Constants.BAE_SYNC, "couldn't access server. check your internet connection");
                context.sendBroadcast(intent);
            }
        } catch (JSONException e) {
            Intent intent = new Intent("com.felixunlimited.BroadcastReceiver");
            intent.putExtra(Constants.BAE_SYNC, "Exception "+e);
            context.sendBroadcast(intent);
            e.printStackTrace();
        }
    }

    private void syncTheme() {
        if (Util.downloadFile(mContext, Constants.PB_BIBLE_FOLDER_URL, "theme.jpg"))
            if (Util.downloadFile(mContext, Constants.PB_BIBLE_FOLDER_URL, "theme.mp3"))
                if (Util.downloadFile(mContext, Constants.PB_BIBLE_FOLDER_URL, "theme.txt")) {
                    File file = new File(Environment.getExternalStorageDirectory(), Constants.DOWNLOAD_FOLDER + "/theme.txt");
                    if (file.exists()) {
                        FileInputStream fin;
                        try {
                            fin = new FileInputStream(file);
                            InputStreamReader tmp = new InputStreamReader(fin);
                            BufferedReader reader = new BufferedReader(tmp);
                            String str;
                            StringBuilder buf = new StringBuilder();
                            while ((str = reader.readLine()) != null)
                                buf.append(str).append("\n");
                            fin.close();
                            str = buf.toString();
                            SharedPreferences.Editor editor = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE).edit();
                            editor.putString(Constants.MONTH, str.split("\n")[0]);
                            editor.putString(Constants.MONTHLY_THEME, str.split("\n")[1]);
                            editor.putString(Constants.THEME_SCRIPTURE, str.split("\n")[2]);
                            editor.apply();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
    }

    private static void createNewUser(Context context) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject, resp;
        jsonObject = new JSONObject();
        try {
            jsonObject.put("user_email", getEmail(context));
            jsonObject.put("user_id", getUserID(context));
            resp = jsonParser.makeHttpRequest(Constants.WEBSERVICE_URL, "POST", jsonObject.toString(), "create");
            if (resp != null) {
                if (resp.get("response").equals("user created")) {
//                Toast.makeText(context, "user created", Toast.LENGTH_SHORT).show();
                    mEditor = mPreferences.edit();
                    mEditor.putBoolean(Constants.NOT_NEW_DEVICE_SYNC, false);
                    mEditor.putBoolean(Constants.NEW_USER, true);
                    mEditor.apply();
                }
            }
            } catch(JSONException e){
                e.printStackTrace();
            }
    }

    private void syncNewDevice() {
//        sqLiteDatabase = databaseHelper.;
        JSONParser jsonParser = new JSONParser();
        JSONArray userJSONArray;
        JSONArray scripturesJSONArray;

        String response;
        JSONObject jsonObject;
        jsonObject = jsonParser.makeHttpRequest(Constants.WEBSERVICE_URL,"POST", getEmail(mContext), "get");
        if (jsonObject != null) {
            try {
                response = jsonObject.getString("response");
                if (response.equals("error") || jsonObject.getJSONArray("user").length() == 0) {
                    createNewUser(mContext);
                    return;
                }
                userJSONArray = jsonObject.getJSONArray("user");
                mEditor = mPreferences.edit();
                mEditor.putBoolean(Constants.NOT_NEW_DEVICE_SYNC, true);
                mEditor.putString(Constants.BAE_EMAIl, userJSONArray.getJSONObject(0).getString("bae_email"));
                mEditor.putInt(Constants.BAE_REQUEST, userJSONArray.getJSONObject(0).getInt("bae_request"));
                mEditor.putInt(Constants.BAE_RECEIPT, userJSONArray.getJSONObject(0).getInt("bae_receipt"));
                mEditor.putInt(Constants.BAE_CONFIRMED, userJSONArray.getJSONObject(0).getInt("bae_confirm"));
                mEditor.apply();

                scripturesJSONArray = jsonObject.getJSONArray("scriptures");
                databaseHelper.insertScriptures(scripturesJSONArray.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
            createNewUser(mContext);
    }

    private static void syncScriptures() {
        JSONParser jsonParser = new JSONParser();
        JSONObject resp;
        JSONArray scripturesJSONArray, baeJSONArray;

        resp = jsonParser.makeHttpRequest(Constants.WEBSERVICE_URL, "POST", databaseHelper.getScriptures(mContext, null), "sync");
        if (resp != null) {
            try {
                if (resp.getString("response").equals("inserted")) {
                    scripturesJSONArray = resp.getJSONArray("scriptures");
                    if (scripturesJSONArray.length() != 0) {
                        databaseHelper.insertScriptures(scripturesJSONArray.toString());
                    }
                    baeJSONArray = resp.getJSONArray("bae");

                    //Toast.makeText(mContext, "Sync successful", Toast.LENGTH_SHORT).show();
                    mEditor = mPreferences.edit();
                    mEditor.putBoolean(Constants.SCRIPTURES_SYNC, true);
                    mEditor.putInt(Constants.BAE_REQUEST, baeJSONArray.getJSONObject(0).getInt("bae_request"));
                    mEditor.putInt(Constants.BAE_RECEIPT, baeJSONArray.getJSONObject(0).getInt("bae_receipt"));
                    mEditor.putInt(Constants.BAE_CONFIRMED, baeJSONArray.getJSONObject(0).getInt("bae_confirm"));
                    mEditor.putString(Constants.BAE_EMAIl, baeJSONArray.getJSONObject(0).getString("bae_email"));
                    mEditor.apply();
                }
            } catch (JSONException e) {
                //Toast.makeText(mContext, "Sync failed " + e, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}