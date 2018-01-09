package com.felixunlimited.pbbible.ui.activities;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.felixunlimited.pbbible.models.Constants;
import com.felixunlimited.pbbible.models.DatabaseHelper;
import com.felixunlimited.pbbible.R;
import com.felixunlimited.pbbible.sync.Sync;
import com.felixunlimited.pbbible.utils.NotePadUtils;

public class BaeActivity extends AppCompatActivity implements DialogInterface.OnClickListener {

    BaeScripturesAdapter baeScripturesAdapter;
    ListView listView;
    DatabaseHelper databaseHelper;
    AlertDialog baeSendDialog, baeReceiveDialog, alertDialog;
    View baeSendView,baeReceiveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bae);
        databaseHelper = new DatabaseHelper(this);
        LayoutInflater li = LayoutInflater.from(this);
        baeReceiveView = li.inflate(R.layout.bae_receive, null);
        baeReceiveDialog = (new AlertDialog.Builder(this))
                .setTitle("You have a bae request from")
                .setView(baeReceiveView)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .setPositiveButton("Accept", this)
                .setNegativeButton("Reject", this)
                .create();
        baeSendView = li.inflate(R.layout.bae_send, null);
        baeSendDialog = (new AlertDialog.Builder(this))
                .setTitle("You need to make a bae request")
                .setView(baeSendView)
                .setPositiveButton("Send", this)
                .setNegativeButton("Cancel", this)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .create();
        alertDialog = (new AlertDialog.Builder(this))
                .setTitle("Bae request pending")
                .setMessage("Your request to "+Constants.BAE_EMAIl+" is pending approval")
                .setNeutralButton("OK", this)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .create();

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        if (sharedPreferences.getInt(Constants.BAE_CONFIRMED, 0) == 1) {
            databaseHelper.open();
            Cursor baeScripturesCursor = databaseHelper.getBaeScriptures();
            baeScripturesAdapter = new BaeScripturesAdapter(this, baeScripturesCursor, 0);
            listView = (ListView) findViewById(R.id.bae_scriptures_list);
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

    BroadcastReceiver baeSynced = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.unregisterReceiver(this);
            String table = intent.getStringExtra(Constants.BAE_SYNC);
            switch (table) {
                case "bae_confirm":
                    baeScripturesAdapter.notifyDataSetChanged();
                    break;
                case "bae_request":
                    Toast.makeText(context, "Bae request sent. Awaiting confirmation", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case "bae_cancel":
                    Toast.makeText(context, "Bae request cancelled. Bae deleted", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                default:
                    Toast.makeText(context, table, Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };

    @Override
    public void onClick(DialogInterface dialogInterface, int buttonId) {
        getApplicationContext().registerReceiver(baeSynced, new IntentFilter("com.felixunlimited.BroadcastReceiver"));
        if (dialogInterface.equals(baeReceiveDialog)) {
            TextView baeEmailView = baeReceiveView.findViewById(R.id.bae_email_receive);
            String baeEmail = String.valueOf(baeEmailView.getText());
            if (buttonId == DialogInterface.BUTTON_POSITIVE) {
                new Sync(getApplicationContext()).execute(baeEmail, "bae_confirm");
            }
            else if (buttonId == DialogInterface.BUTTON_NEGATIVE) {
                new Sync(getApplicationContext()).execute(baeEmail, "bae_cancel");
            }
        }
        else if (dialogInterface.equals(baeSendDialog)) {
            if (buttonId == DialogInterface.BUTTON_POSITIVE) {
                EditText baeEmailSend = baeSendView.findViewById(R.id.bae_email_send);
                String baeEmail = String.valueOf(baeEmailSend.getText());
                if (!baeEmail.isEmpty()) {
                    new Sync(getApplicationContext()).execute(baeEmail, "bae_request");
                }
                else {
                    getApplicationContext().unregisterReceiver(baeSynced);
                    Toast.makeText(this, "please enter a valid email address", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            else if (buttonId == DialogInterface.BUTTON_NEGATIVE) {
                getApplicationContext().unregisterReceiver(baeSynced);
                finish();
            }
        }
        else if (dialogInterface.equals(alertDialog)) {
            if (buttonId == DialogInterface.BUTTON_NEUTRAL)
                getApplicationContext().unregisterReceiver(baeSynced);
                finish();
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

            viewHolder.scriptureView.setText(cursor.getString(1));
            String category = cursor.getString(2);
            if (category.equals("bae_sent"))
                viewHolder.fromOrToView.setText("Sent");
            else if (category.equals("bae_received"))
                viewHolder.fromOrToView.setText("Received");
            viewHolder.dateView.setText(NotePadUtils.getDate(Long.parseLong(cursor.getString(3))));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
