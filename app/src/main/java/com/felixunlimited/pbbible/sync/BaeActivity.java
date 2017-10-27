package com.felixunlimited.pbbible.sync;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.felixunlimited.pbbible.Constants;
import com.felixunlimited.pbbible.DatabaseHelper;
import com.felixunlimited.pbbible.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BaeActivity extends Activity implements DialogInterface.OnClickListener {

    BaeScripturesAdapter baeScripturesAdapter;
    ListView listView;
    DatabaseHelper databaseHelper;
    AlertDialog baeSendDialog, baeReceiveDialog, alertDialog;
    View baeSendView,baeReceiveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bae);
        LayoutInflater li = LayoutInflater.from(this);
        baeReceiveView = li.inflate(R.layout.bae_receive, null);
        baeReceiveDialog = (new AlertDialog.Builder(this))
                .setTitle("You have a bae request from")
                .setView(baeReceiveView)
                .setPositiveButton("Accept", this)
                .setNegativeButton("Reject", this)
                .create();
        baeSendView = li.inflate(R.layout.bae_send, null);
        baeSendDialog = (new AlertDialog.Builder(this))
                .setTitle("You need to make a bae request")
                .setView(baeSendView)
                .setPositiveButton("Send", this)
                .setNegativeButton("Cancel", this)
                .create();
        alertDialog = (new AlertDialog.Builder(this))
                .setTitle("Bae request pending")
                .setMessage("Your request to "+Constants.BAE_EMAIl+" is pending approval")
                .setNeutralButton("OK", this)
                .create();

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        if (sharedPreferences.getInt(Constants.BAE_CONFIRMED, 0) == 1) {
            databaseHelper.open();
            baeScripturesAdapter = new BaeScripturesAdapter(this, databaseHelper.getBaeScriptures(), 0);
            listView = findViewById(R.id.bae_scriptures_list);
            listView.setAdapter(baeScripturesAdapter);
            databaseHelper = new DatabaseHelper(this);
        }
        else if (sharedPreferences.getInt(Constants.BAE_RECEIPT, 0) == 1) {
            baeReceiveDialog.show();
        }
        else if (sharedPreferences.getInt(Constants.BAE_REQUEST, 0) == 1) {
            alertDialog.show();
        }
        else
            baeSendDialog.show();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_bae, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onClick(DialogInterface dialogInterface, int buttonId) {
        if (dialogInterface.equals(baeReceiveDialog)) {
            TextView baeEmailView = baeReceiveView.findViewById(R.id.bae_email_receive);
            String baeEmail = String.valueOf(baeEmailView.getText());
            if (buttonId == DialogInterface.BUTTON_POSITIVE) {
                baeSync(baeEmail, "bae_confirm");
            }
            else if (buttonId == DialogInterface.BUTTON_NEGATIVE) {
                baeSync(baeEmail, "bae_cancel");
                finish();
            }
        }
        else if (dialogInterface.equals(baeSendDialog)) {
            if (buttonId == DialogInterface.BUTTON_POSITIVE) {
                EditText baeEmailSend = baeSendView.findViewById(R.id.bae_email_send);
                String baeEmail = String.valueOf(baeEmailSend.getText());
                if (!baeEmail.isEmpty()) {
                    baeSync(baeEmail, "bae_request");
                }
                else {
                    Toast.makeText(this, "please enter a valid email address", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            else if (buttonId == DialogInterface.BUTTON_NEGATIVE) {
                finish();
            }
        }
        else if (dialogInterface.equals(alertDialog)) {
            if (buttonId == DialogInterface.BUTTON_NEUTRAL)
                finish();
        }
    }

    private void baeSync(String baeEmail, String table) {
        if (!Util.isConnected(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject, response;
        jsonObject = new JSONObject();
        try {
            jsonObject.put("bae_email", baeEmail).put("user_email", com.felixunlimited.pbbible.Util.getEmail(BaeActivity.this));
            response = jsonParser.makeHttpRequest(Constants.WEBSERVICE_URL,"POST", jsonObject.toString(), table);
            if (response != null) {
                if (!response.getString("response").equals("error")) {
                    JSONArray baeJSONArray = response.getJSONArray("bae");
                    SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE).edit();
                    editor.putInt(Constants.BAE_CONFIRMED, baeJSONArray.getJSONObject(0).getInt("bae_confirm"));
                    editor.putInt(Constants.BAE_RECEIPT, baeJSONArray.getJSONObject(0).getInt("bae_receipt"));
                    editor.putInt(Constants.BAE_REQUEST, baeJSONArray.getJSONObject(0).getInt("bae_request"));
                    editor.putInt(Constants.BAE_EMAIl, baeJSONArray.getJSONObject(0).getInt("bae_email"));
                    editor.apply();
                    baeScripturesAdapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(this, "online database error.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            else {
                Toast.makeText(this, "couldn't access server. check your internet connection", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class BaeScripturesAdapter extends CursorAdapter {

        BaeScripturesAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        class ViewHolder {
            final TextView scriptureView;
            final TextView dateView;
            final TextView fromOrToView;

            ViewHolder(View view) {
                scriptureView = view.findViewById(R.id.scripture);
                dateView = view.findViewById(R.id.date);
                fromOrToView = view.findViewById(R.id.from_or_to);
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.fragment_bae, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();

            viewHolder.scriptureView.setText(cursor.getString(0));
            viewHolder.fromOrToView.setText(cursor.getString(1));
            viewHolder.dateView.setText(com.felixunlimited.pbbible.notes.Util.getDate(Long.parseLong(cursor.getString(2))));
        }
    }
}
