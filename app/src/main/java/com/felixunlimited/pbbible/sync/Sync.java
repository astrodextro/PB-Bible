package com.felixunlimited.pbbible.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.felixunlimited.pbbible.Constants;
import com.felixunlimited.pbbible.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.felixunlimited.pbbible.Util.getEmail;
import static com.felixunlimited.pbbible.Util.getUserID;

/**
 * Created by Ando on 02/10/2017.
 */

public class Sync extends AsyncTask<String, Void, String> {
    public static final String WEBSERVICE_URL =
            "http://www.felixunlimited.com/pbbible_webservice.php";
    public static SharedPreferences mPreferences;
    public static SharedPreferences.Editor mEditor;
    public static Context mContext;
    public static DatabaseHelper databaseHelper;
    public SQLiteDatabase sqLiteDatabase;

    public Sync(Context context) {
        super();
        mContext = context;
        mPreferences = mContext.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(mContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        databaseHelper.open();
        if (!mPreferences.getBoolean(Constants.NOT_NEW_DEVICE_SYNC, false)) {
            syncNewDevice();
        }
        if (!mPreferences.getBoolean(Constants.SCRIPTURES_SYNC, false)) {
            syncScriptures();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        databaseHelper.close();
    }

    public static void createNewUser (Context context) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject, resp;
        jsonObject = new JSONObject();
        try {
            jsonObject.put("user_email", getEmail(context));
            jsonObject.put("user_id", getUserID(context));
            resp = jsonParser.makeHttpRequest(WEBSERVICE_URL,"POST", jsonObject.toString(), "create");
            if (resp.get("response").equals("user created")) {
//                Toast.makeText(context, "user created", Toast.LENGTH_SHORT).show();
                mEditor = mPreferences.edit();
                mEditor.putBoolean(Constants.NOT_NEW_DEVICE_SYNC, true);
                mEditor.putBoolean(Constants.NEW_USER, true);
                mEditor.apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void syncNewDevice () {
//        sqLiteDatabase = databaseHelper.;
        JSONParser jsonParser = new JSONParser();
        JSONArray userJSONArray;
        JSONArray scripturesJSONArray;

        String response = "";
        JSONObject jsonObject;
        jsonObject = jsonParser.makeHttpRequest(WEBSERVICE_URL,"POST", getEmail(mContext), "get");
        if (jsonObject != null) {
            try {
                response = jsonObject.getString("response");
                if (response.equals("error") || jsonObject.getJSONArray("user").length() == 0) {
                    createNewUser(mContext);
                    return;
                }
                userJSONArray = jsonObject.getJSONArray("user");
                mEditor = mPreferences.edit();
                mEditor.putString(Constants.BAE_EMAIl, userJSONArray.getJSONObject(0).getString("bae_email"));
                mEditor.putString(Constants.BAE_REQUEST, userJSONArray.getJSONObject(0).getString("bae_request"));
                mEditor.putString(Constants.BAE_RECEIPT, userJSONArray.getJSONObject(0).getString("bae_receipt"));
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

    public static void syncScriptures ()
    {
        JSONParser jsonParser = new JSONParser();
        JSONObject resp;
        resp = jsonParser.makeHttpRequest(WEBSERVICE_URL, "POST", databaseHelper.getScriptures(mContext, null), "sync");
        if (resp != null) {
            try {
                if (resp.getString("response").equals("ok")) {
                    //Toast.makeText(mContext, "Sync successful", Toast.LENGTH_SHORT).show();
                    mEditor = mPreferences.edit();
                    mEditor.putBoolean(Constants.SCRIPTURES_SYNC, true);
                    mEditor.apply();
                } else{}
                    //Toast.makeText(mContext, "Sync failed", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                //Toast.makeText(mContext, "Sync failed " + e, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public static void baeRequest() {

    }

    public static void baeReceipt() {

    }

}