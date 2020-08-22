package com.felixunlimited.pbbible.ui.activities;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.felixunlimited.pbbible.models.Constants;
import com.felixunlimited.pbbible.R;
import com.felixunlimited.pbbible.utils.SyncUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;

public class PDFViewer extends AppCompatActivity {
    boolean isTemp = true;
    private static final String GOOGLE_DRIVE_PDF_READER_PREFIX = "http://drive.google.com/viewer?url=";
    private static final String PDF_MIME_TYPE = "application/pdf";
    private static final String HTML_MIME_TYPE = "text/html";
    public final String COMMON_BEGINNING = "http://nouedu.net/sites/default/files/2017-03/";

    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
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
    public void open( View view) {
        TextView textView = (TextView) view;
        String fileName = (String) textView.getHint();

        downloadAndOpenPDF(fileName);
    }

    public void downloadAndOpenPDF (final String fileName) {
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        if (pdfFile.exists()) {
            openPDFFile(pdfFile);
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle("File does not exist")
                    .setMessage("The file requested for is not stored in your device. Do you want to download?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            downloadPDF(fileName);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .create().show();

        }

    }

    public void downloadPDF (final String fileName) {
        if (!isConnected(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

//        progressDialog = ProgressDialog.show(this, "Please wait", "Downloading PDF file");
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Downloading PDF file");
        progressDialog.setCancelable(true);
        progressDialog.show();

        final DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(COMMON_BEGINNING+Uri.encode(fileName)));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        BroadcastReceiver downloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ( !progressDialog.isShowing() ) {
                    return;
                }
                context.unregisterReceiver( this );

                progressDialog.dismiss();
                long downloadId = intent.getLongExtra( DownloadManager.EXTRA_DOWNLOAD_ID, -1 );
                Cursor c = downloadManager.query( new DownloadManager.Query().setFilterById( downloadId ) );

                if ( c.moveToFirst() ) {
                    int status = c.getInt( c.getColumnIndex( DownloadManager.COLUMN_STATUS ) );
                    if ( status == DownloadManager.STATUS_SUCCESSFUL ) {
                        new AlertDialog.Builder(context)
                                .setTitle("Download completed")
                                .setMessage("Do you want to open the file")
                                .setPositiveButton("PDF Viewers", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        openPDFFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName));
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .create().show();
                    }
                    else
                        Toast.makeText(context, "Error "+ c.getInt( c.getColumnIndex( DownloadManager.COLUMN_REASON )), Toast.LENGTH_SHORT).show();
                }
                c.close();
            }
        };
        registerReceiver(downloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        downloadManager.enqueue(request);
    }

    public void openPDFFile(File pdfFile) {
        if (!pdfFile.exists()) {
            Toast.makeText(this, "File does not exist", Toast.LENGTH_LONG).show();
            return;
        }

        Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
        pdfIntent.setDataAndType(Uri.fromFile(pdfFile), PDF_MIME_TYPE);
        if (getPackageManager().queryIntentActivities( pdfIntent, PackageManager.MATCH_DEFAULT_ONLY ).size() > 0)
            startActivity(pdfIntent);
        else {
            new AlertDialog.Builder(this)
                    .setTitle("PDF Reader not installed")
                    .setMessage("Could not detect any installed PDF Reader. Do you want to intall from google playstore")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // open google playstore
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pdf&c=apps")));
                            }
                            catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pdf&c=apps")));
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .show();
        }
    }

    private void downloadPDFViaHTTP(String downloadLink, File pdfFile) {
        try {
            final ProgressDialog progress = ProgressDialog.show( this, "Downloading", "PDF Downloading...", true );
            URL url = new URL(downloadLink);
            URLConnection c = url.openConnection();
            InputStream inputStream = c.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            FileOutputStream fos = new FileOutputStream(pdfFile.getPath());


            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ( (len1 = inputStream.read(buffer)) > 0 ) {
                fos.write(buffer,0, len1);
            }

            fos.flush();
            fos.close();
            progress.dismiss();

            openPDF(this, Uri.fromFile(pdfFile));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfviewer);

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method method = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                method.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Button btnOpenPDF = (Button) findViewById(R.id.open_pdf);
        btnOpenPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isTemp = true;
                showPDFUrl(PDFViewer.this, Constants.PB_BIBLE_FOLDER_URL+"sample.pdf");
            }
        });
        Button downloadPDF = (Button) findViewById(R.id.download_pdf);
        downloadPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isTemp = false;
                String pdfUrl = Constants.PB_BIBLE_FOLDER_URL + "sample.pdf";
                final String filename = pdfUrl.substring( pdfUrl.lastIndexOf( "/" ) + 1 );
                // The place where the downloaded PDF file will be put
                final File tempFile = new File( getExternalFilesDir( Environment.DIRECTORY_DOWNLOADS ), filename );
                downloadPDFViaDM(PDFViewer.this, pdfUrl, filename, tempFile);
            }
        });
    }

    /**
     * If a PDF reader is installed, download the PDF file and open it in a reader.
     * Otherwise ask the user if he/she wants to view it in the Google Drive online PDF reader.<br />
     * <br />
     * <b>BEWARE:</b> This method
     * @param context
     * @param pdfUrl
     * @return
     */
    public static void showPDFUrl( final Context context, final String pdfUrl ) {
        if ( isPDFSupported( context ) ) {
            downloadAndOpenPDF(context, pdfUrl);
        } else {
            askToOpenPDFThroughGoogleDrive( context, pdfUrl );
        }
    }

    /**
     * Downloads a PDF with the Android DownloadManager and opens it with an installed PDF reader app.
     * @param context
     * @param pdfUrl
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void downloadAndOpenPDF(final Context context, final String pdfUrl) {
        // Get filename
        final String filename = pdfUrl.substring( pdfUrl.lastIndexOf( "/" ) + 1 );
        // The place where the downloaded PDF file will be put
        final File tempFile = new File( Environment.getExternalStorageDirectory(), Constants.DOWNLOAD_FOLDER + filename );
        if ( tempFile.exists() ) {
            // If we have downloaded the file before, just go ahead and show it.
            openPDF( context, Uri.fromFile( tempFile ) );
            return;
        }
        downloadPDFViaDM(context, pdfUrl, filename, tempFile);
    }

    private static void downloadPDFViaDM(Context context, final String pdfUrl, String filename, final File tempFile) {

        if (!SyncUtils.isConnected(context)) {
            Toast.makeText(context, "No internet connectivity", Toast.LENGTH_SHORT).show();
            return;
        }
        // Show progress dialog while downloading
        final ProgressDialog progress = ProgressDialog.show( context, context.getString( R.string.pdf_show_local_progress_title ), context.getString( R.string.pdf_show_local_progress_content ), true );

        // Create the download request
        DownloadManager.Request r = new DownloadManager.Request( Uri.parse( pdfUrl ) );
//        r.setDestinationInExternalPublicDir(null, Environment.getExternalStorageDirectory().getAbsolutePath()+Constants.NOTES_FOLDER);
        r.setDestinationInExternalFilesDir( context, Environment.DIRECTORY_DOWNLOADS, filename );
        final DownloadManager dm = (DownloadManager) context.getSystemService( Context.DOWNLOAD_SERVICE );
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                if ( !progress.isShowing() ) {
                    return;
                }
                context.unregisterReceiver( this );

                progress.dismiss();
                long downloadId = intent.getLongExtra( DownloadManager.EXTRA_DOWNLOAD_ID, -1 );
                Cursor c = dm.query( new DownloadManager.Query().setFilterById( downloadId ) );

                if ( c.moveToFirst() ) {
                    int status = c.getInt( c.getColumnIndex( DownloadManager.COLUMN_STATUS ) );
                    if ( status == DownloadManager.STATUS_SUCCESSFUL ) {
                        new AlertDialog.Builder(context)
                                .setTitle("Download completed")
                                .setMessage("Do you want to open the file")
                                .setPositiveButton("PDF Viewers", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        openPDF( context, Uri.fromFile( tempFile ) );
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create().show();
                    }
                }
                c.close();
            }
        };
        context.registerReceiver( onComplete, new IntentFilter( DownloadManager.ACTION_DOWNLOAD_COMPLETE ) );

        // Enqueue the request
        dm.enqueue( r );
    }

    /**
     * Show a dialog asking the user if he wants to open the PDF through Google Drive
     * @param context
     * @param pdfUrl
     */
    public static void askToOpenPDFThroughGoogleDrive( final Context context, final String pdfUrl ) {
        new AlertDialog.Builder( context )
                .setTitle("Download using Google Drive" )
                .setMessage( "Do you want to download using Google Drive?" )
                .setNegativeButton( "No", null )
                .setPositiveButton( "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openPDFThroughGoogleDrive(context, pdfUrl);
                    }
                })
                .show();
    }

    /**
     * Launches a browser to view the PDF through Google Drive
     * @param context
     * @param pdfUrl
     */
    public static void openPDFThroughGoogleDrive(final Context context, final String pdfUrl) {
        Intent i = new Intent( Intent.ACTION_VIEW );
        i.setDataAndType(Uri.parse(GOOGLE_DRIVE_PDF_READER_PREFIX + pdfUrl ), HTML_MIME_TYPE );
        context.startActivity( i );
    }
    /**
     * Open a local PDF file with an installed reader
     * @param context
     * @param localUri
     */
    public static final void openPDF(Context context, Uri localUri ) {
//            if (Build.VERSION. == Build.VERSION_CODES.LOLLIPOP)
        Intent i = new Intent( Intent.ACTION_VIEW );
        i.setDataAndType( localUri, PDF_MIME_TYPE );
        context.startActivity( i );
    }
    /**
     * Checks if any apps are installed that supports reading of PDF files.
     * @param context
     * @return
     */
    public static boolean isPDFSupported( Context context ) {
        Intent i = new Intent( Intent.ACTION_VIEW );
        final File tempFile = new File( context.getExternalFilesDir( Environment.DIRECTORY_DOWNLOADS ), "test.pdf" );
        i.setDataAndType( Uri.fromFile( tempFile ), PDF_MIME_TYPE );
        return context.getPackageManager().queryIntentActivities( i, PackageManager.MATCH_DEFAULT_ONLY ).size() > 0;
    }
}
