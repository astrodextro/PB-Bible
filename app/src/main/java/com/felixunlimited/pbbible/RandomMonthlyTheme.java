package com.felixunlimited.pbbible;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.felixunlimited.pbbible.Constants.DOCUMENT_FOLDER;
import static com.felixunlimited.pbbible.Constants.DOWNLOAD_FOLDER;
import static com.felixunlimited.pbbible.Constants.FROM_WIDGET;
import static com.felixunlimited.pbbible.Constants.MONTH;
import static com.felixunlimited.pbbible.Constants.MONTHLY_THEME;
import static com.felixunlimited.pbbible.Constants.PREFERENCE_NAME;
import static com.felixunlimited.pbbible.Constants.THEME_SCRIPTURE;
import static com.felixunlimited.pbbible.Constants.WIDGET_BOOK;
import static com.felixunlimited.pbbible.Constants.WIDGET_CHAPTER;
import static com.felixunlimited.pbbible.Constants.WIDGET_VERSE;

public class RandomMonthlyTheme extends Service {

    public RandomMonthlyTheme() {
    }

    SharedPreferences sharedPreferences;
    String month;
    String scripture;
    String theme;

    final Handler handler = new Handler();
    Timer timer = new Timer();
    TimerTask doAsynchronousTask;
    boolean paused;

    public void createTimerTask () {
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        if (randomize() && !paused)
                            displayNotification();
//                            displayAlert();
                    }
                });
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String displayNotificationsKey = getString(R.string.prefEnableNotificationKey);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(getString(R.string.prefEnableNotificationDefault)));

        if (!displayNotifications)
            onDestroy();
        else {
            month = sharedPreferences.getString(MONTH, "this month");
            scripture = sharedPreferences.getString(THEME_SCRIPTURE, "Revelation 15:11");
            theme = sharedPreferences.getString(MONTHLY_THEME, "Taking Over");
            createTimerTask();
            paused = false;
            timer.schedule(doAsynchronousTask, 1800000, 3600000);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    private void stopTimer() {
        if(timer != null) {
            paused = true;
            timer.purge();
            timer.cancel();
            timer = null;
            paused = true;
        }
    }

    public Intent shareTheme () {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, theme);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Theme for "+month);
        Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ DOWNLOAD_FOLDER+"/theme.jpg"));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);  //optional//use this when you want to send an image
        shareIntent.setType("image/jpg");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooserIntent = Intent.createChooser(shareIntent, "Share theme for "+month);
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return chooserIntent;
    }

    public boolean randomize () {
        return (new Random()).nextBoolean();
    }

    public void displayNotification1() {
        //Bitmap remote_picture = null;
        Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ DOWNLOAD_FOLDER+"/theme.png"));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        final Bitmap remote_picture = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + DOCUMENT_FOLDER + "/theme.png", options);


// Create the style object with BigPictureStyle subclass.
        NotificationCompat.BigPictureStyle notiStyle = new
                NotificationCompat.BigPictureStyle();
        notiStyle.setBigContentTitle("Big Picture Expanded");
        notiStyle.setSummaryText("Nice big picture.");


// Add the big picture to the style.
        notiStyle.bigPicture(remote_picture);

// Creates an explicit intent for an ResultActivity to receive.
        Intent resultIntent = new Intent(this, BiblesOffline.class);

// This ensures that the back button follows the recommended
// convention for the back key.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

// Adds the back stack for the Intent (but not the Intent itself).
        stackBuilder.addParentStack(BiblesOffline.class);

// Adds the Intent that starts the Activity to the top of the stack.
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification myNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setLargeIcon(remote_picture)
                .setContentIntent(resultPendingIntent)
                .setContentTitle("Big Picture Normal")
                .setContentText("This is an example of a Big Picture Style.")
                .setStyle(notiStyle)
                .build();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(123, myNotification);
    }

    public void displayNotification() {
        Intent resultIntent = new Intent(this, BiblesOffline.class);
        resultIntent.putExtra(FROM_WIDGET, true);
        resultIntent.putExtra(WIDGET_BOOK, Arrays.asList(Constants.arrBookName).indexOf(scripture.split(" ", 2)[0])+1);
        int[] chapterAndVerse = Util.getChapterAndVerse(scripture.split(" ", 2)[1]);
        resultIntent.putExtra(WIDGET_CHAPTER, chapterAndVerse[0]);
        resultIntent.putExtra(WIDGET_VERSE, chapterAndVerse[1]);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(BiblesOffline.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent sharePendingIntent = PendingIntent.getActivity(this, 0, shareTheme(), PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews expandedView = new RemoteViews(this.getPackageName(), R.layout.random_monthly_theme);
        Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+Constants.DOWNLOAD_FOLDER+"/theme.jpg"));
        expandedView.setImageViewUri(R.id.imageView, uri);
        expandedView.setOnClickPendingIntent(R.id.shareButton, sharePendingIntent);
//        expandedView.setImageViewBitmap(R.id.imageView, BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+Constants.DOCUMENT_FOLDER+"/theme.png"));
//        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ DOWNLOAD_FOLDER+"/theme.mp3";
//        Uri.Builder uriBuilder = new Uri.Builder();
//        Uri soundUri = uriBuilder.path(path).build();
//        soundFile.setReadable(true, false);
        File soundFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+Constants.DOWNLOAD_FOLDER+"/theme.mp3");
        Uri soundUri = Uri.fromFile(soundFile);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
//                .setDefaults(~Notification.DEFAULT_SOUND)
                .setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
                .setAutoCancel(true)
//                .addAction(android.R.drawable.ic_menu_share, "Share", sharePendingIntent)
                .setContentIntent(resultPendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("Theme for "+month)
                .setContentText(theme)
//                .setDefaults(Notification.DEFAULT_SOUND)
                .build();
        notification.bigContentView = expandedView;
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
////// The id of the channel.
////        String id = "my_channel_01";
////// The user-visible name of the channel.
////        CharSequence name = getString(R.string.channel_name);
////// The user-visible description of the channel.
////        String description = getString(R.string.channel_description);
////        int importance = NotificationManager.IMPORTANCE_HIGH;
////
////        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
////// Configure the notification channel.
////        mChannel.setDescription(description);
////        mChannel.enableLights(true);
////// Sets the notification light color for notifications posted to this
////// channel, if the device supports this feature.
////        mChannel.setLightColor(Color.RED);
////        mChannel.enableVibration(true);
////        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
////        mNotificationManager.createNotificationChannel(mChannel);
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(123, notification);

    }

    public void displayAlert () {
        stopTimer();
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Dialog);
        dialog.setContentView(R.layout.random_monthly_theme);
        dialog.setTitle("This month's theme");
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                timer = new Timer();
                timer.purge();
                createTimerTask();
                paused = false;
                timer.schedule(doAsynchronousTask, 0, 7200);
            }
        });
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        final Bitmap imageBitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + DOCUMENT_FOLDER + "/theme.png", options);

        Button shareButton = (Button) dialog.findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                startActivity(shareTheme());
            }
        });

        Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ DOWNLOAD_FOLDER+"/theme.png"));
        ImageView image = (ImageView) dialog.findViewById(R.id.imageView);
        image.setImageURI(uri);

        dialog.show();    }
}