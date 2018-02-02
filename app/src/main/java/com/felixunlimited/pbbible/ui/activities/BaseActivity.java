package com.felixunlimited.pbbible.ui.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.felixunlimited.pbbible.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.felixunlimited.pbbible.models.Constants.BOOK_LANGUAGE;
import static com.felixunlimited.pbbible.models.Constants.CHAPTER_INDEX;
import static com.felixunlimited.pbbible.models.Constants.CURRENT_BIBLE;
import static com.felixunlimited.pbbible.models.Constants.FONT_SIZE;
import static com.felixunlimited.pbbible.models.Constants.FULL_SCREEN;
import static com.felixunlimited.pbbible.models.Constants.HELP_CONTENT;
import static com.felixunlimited.pbbible.models.Constants.LANG_ENGLISH;
import static com.felixunlimited.pbbible.models.Constants.PARALLEL;
import static com.felixunlimited.pbbible.models.Constants.POSITION_BIBLE_NAME;
import static com.felixunlimited.pbbible.models.Constants.POSITION_BIBLE_NAME_2;
import static com.felixunlimited.pbbible.models.Constants.PREFERENCE_NAME;
import static com.felixunlimited.pbbible.models.Constants.arrVerseCount;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //persist
    public int currentChapterIdx;
    public String currentBibleFilename;
    public String currentBibleFilename2;
    public String currentBookLanguage;
    public int currentFontSize;
    public boolean isFullScreen;
    //not persist
    public String currentBibleName;

    public boolean isParallel;

    public static final int MY_PERMISSIONS_REQUEST_WRITE_CONTACTS = 1;
    DrawerLayout drawer;
    public static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    SharedPreferences sharedPreferences;

    SharedPreferences.Editor editor;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        int hour = (Calendar.getInstance()).get(Calendar.HOUR_OF_DAY);

        if (hour >= 6 && hour <= 8)
        {
            sync();
            notifyMe();
        }
        else
        {
            editor.putBoolean("synced", false);
            editor.apply();
        }
    }

    public void readPreference() {
        //SpeechRecognizer
        SharedPreferences preference = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        currentChapterIdx = preference.getInt(CHAPTER_INDEX, 0);
        if (currentChapterIdx < 0 || currentChapterIdx >= arrVerseCount.length) {
            currentChapterIdx = 0;
        }
        currentBibleFilename = preference.getString(POSITION_BIBLE_NAME, "");
        currentBibleFilename2 = preference.getString(POSITION_BIBLE_NAME_2, "");
        currentFontSize = preference.getInt(FONT_SIZE, 18);
        isFullScreen = preference.getBoolean(FULL_SCREEN, false);
        isParallel = preference.getBoolean(PARALLEL, false);

        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        currentBookLanguage = defaultPrefs.getString(BOOK_LANGUAGE, LANG_ENGLISH);
    }

    /**
     * called in extending activities instead of setContentView...
     *
     * @param layoutId The content Layout Id of extending activities
     */
    public <T extends ViewDataBinding> T addContentView(int layoutId) {
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(layoutId, null, false);
        drawer.addView(contentView, 0);
        return DataBindingUtil.inflate(layoutId);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.parallel:
                if (bibleList == null || bibleList.size() < 2) {
                    Toast.makeText(this, R.string.needMoreTranslation, Toast.LENGTH_LONG).show();
                    return true;
                }
                isParallel = !isParallel;
                applyParallel(isParallel);
                if (isParallel) {
                    displayBible(currentBibleFilename, currentChapterIdx);
                }
                updateBibleInfo();
                if (isParallel && (currentBibleFilename2 == null || "".equals(currentBibleFilename2))) {
                    gotoSelectParallel = true;
                    startActivity(new Intent(this, SelectParallelBible.class));
                }
                return true;
			case R.id.contactAuthor:
//				Intent i = new Intent(Intent.ACTION_SEND);
//				i.setType("message/rfc822");
//				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"astrodextro@gmail.com"});
//				i.putExtra(Intent.EXTRA_SUBJECT, "[OpenBibles] Question");
//				i.putExtra(Intent.EXTRA_TEXT   , "");
//			    startActivity(Intent.createChooser(i, "Send mail..."));
//				return true;
				startActivity(new Intent(this, NoteListActivity.class));
            case R.id.bookmark:
                String state = Environment.getExternalStorageState();
                if (!Environment.MEDIA_MOUNTED.equals(state)) {
                    Toast.makeText(this, R.string.sdcardNotReady, Toast.LENGTH_LONG).show();
                    return true;
                }
                startActivity(new Intent(this, BookmarksActivity.class));
                return true;
            case R.id.find:
                Intent find = new Intent(this, FindActivity.class);
                find.putExtra(CURRENT_BIBLE, currentBibleName);
                startActivity(find);
                return true;
            case R.id.about:
                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                String[] arrImport = new String[] {"About " + currentBibleName, "About PB-Bible"};
                ListView viewChooseAbout = new ListView(this);
                ad.setView(viewChooseAbout);
                final AlertDialog dialogChooseAbout = ad.create();
                dialogChooseAbout.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                viewChooseAbout.setAdapter(new ArrayAdapter<String>(this, R.layout.listitemmedium, arrImport));
                viewChooseAbout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        dialogChooseAbout.dismiss();
                        if (position==0) {
                            Intent i = new Intent(BiblesOfflineActivity.this, AboutBibleActivity.class);
                            i.putExtra(CURRENT_BIBLE, currentBibleName);
                            startActivity(i);
                        } else if (position==1) {
                            startActivity(new Intent(BiblesOfflineActivity.this, AboutActivity.class));
                        }
                    }
                });
                dialogChooseAbout.show();
                return true;
            case R.id.download:
                gotoDownloadBible = true;
                startActivity(new Intent(this, DownloadBible.class));
                return true;
            case R.id.history:
                dialogHistory.show();
                return true;
            case R.id.help:
                Intent iHelp = new Intent(this, HelpActivity.class);
                iHelp.putExtra(FONT_SIZE, currentFontSize);
                iHelp.putExtra(HELP_CONTENT, R.string.help_main);
                startActivity(iHelp);
                return true;
			case R.id.document:
				state = Environment.getExternalStorageState();
				if (!Environment.MEDIA_MOUNTED.equals(state)) {
					Toast.makeText(this, R.string.sdcardNotReady, Toast.LENGTH_LONG).show();
					return true;
				}
				startActivity(new Intent(this, DocumentsActivity.class));
				return true;
            case R.id.settings:
                gotoPrefs = true;
                startActivity(new Intent(this, SettingsActivity.class));
                //finish();
                return true;
			case R.id.downloadBookname:
				gotoPrefs = true;
				startActivity(new Intent(this, DownloadBookname.class));
				return true;
        }
        return false;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void sync ()
    {
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastSync = sharedPreferences.getLong(lastNotificationKey, 0);

        if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS || sharedPreferences.getBoolean("synced", false)) {
            new DBSyncTask(this).execute();
        }
    }

    public void notifyMe() {
        //checking the last update and notifyMe if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.prefEnableNotificationKey);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.prefEnableNotificationDefault)));

        if ( displayNotifications ) {

            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                // Last sync was more than 1 day ago, let's send a notification with the message.
                DbHelper dbHelper = new DbHelper(context);

                // we'll query our contentProvider, as always
                Cursor cursor = dbHelper.getDayCourses(DBContract.TimetableEntry.TABLE_NAME, Calendar.DAY_OF_WEEK);


                if (cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndex(DBContract.TimetableEntry._ID));
                    int start = cursor.getInt(cursor.getColumnIndex(DBContract.TimetableEntry.COLUMN_START_TIME));
                    int end = cursor.getInt(cursor.getColumnIndex(DBContract.TimetableEntry.COLUMN_END_TIME));
                    String course = cursor.getString(cursor.getColumnIndex(DBContract.CoursesEntry.COLUMN_COURSE_CODE));
                    String lecturer = cursor.getString(cursor.getColumnIndex(DBContract.LecturersEntry.COLUMN_NAME));
                    String venue = cursor.getString(cursor.getColumnIndex(DBContract.VenuesEntry.COLUMN_NAME));

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aaa");
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, start);
                    String startTime = simpleDateFormat.format(calendar.getTime());
                    calendar.set(Calendar.HOUR_OF_DAY, end);
                    String endTime = simpleDateFormat.format(calendar.getTime());

                    int iconId = R.mipmap.ic_launcher;
                    Resources resources = context.getResources();
                    Bitmap largeIcon = BitmapFactory.decodeResource(resources,
                            R.mipmap.ic_launcher);
                    String title = "Courses for today "+(Calendar.getInstance()).getTime();

                    // Define the text of the message.
                    String contentText = "You have "+cursor.getCount()+" classes today. Pull down for details";
                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
                    // notifications.  Just throw in some data.
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setColor(resources.getColor(R.color.wordapp_light_orange))
                                    .setSmallIcon(iconId)
                                    .setLargeIcon(largeIcon)
                                    .setContentTitle(title)
                                    .setContentText(contentText);

                     /* Add Big View Specific Configuration */
                    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                    String[] events = new String[6];
                    for (int i = 0; i < cursor.getCount(); i++)
                    {
                        events[i] = course+" starts at "+startTime+" by "+lecturer+" @ "+venue;
                    }

                    // Sets a title for the Inbox style big view
                    inboxStyle.setBigContentTitle("Today's classes");

                    // Moves events into the big view
                    for (int i=0; i < events.length; i++) {
                        inboxStyle.addLine(events[i]);
                    }

                    mBuilder.setStyle(inboxStyle);

                    // Make something interesting happen when the user clicks on the notification.
                    // In this case, opening the app is sufficient.
                    Intent resultIntent = new Intent(context, TimetableScreen.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                    mNotificationManager.notify(1122, mBuilder.build());

                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.apply();
                }
                cursor.close();
            }
        }
    }
}
