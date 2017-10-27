package com.felixunlimited.pbbible;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class PDFViewer extends AppCompatActivity {
    boolean isTemp = true;
    private static final String GOOGLE_DRIVE_PDF_READER_PREFIX = "http://drive.google.com/viewer?url=";
    private static final String PDF_MIME_TYPE = "application/pdf";
    private static final String HTML_MIME_TYPE = "text/html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfviewer);
        Button openPDF = (Button) findViewById(R.id.open_pdf);
        openPDF.setOnClickListener(new View.OnClickListener() {
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
        final File tempFile = new File( context.getExternalFilesDir( Environment.DIRECTORY_DOWNLOADS ), filename );
        if ( tempFile.exists() ) {
            // If we have downloaded the file before, just go ahead and show it.
            openPDF( context, Uri.fromFile( tempFile ) );
            return;
        }
        downloadPDFViaDM(context, pdfUrl, filename, tempFile);
    }

    private static void downloadPDFViaDM(Context context, final String pdfUrl, String filename, final File tempFile) {

        if (!com.felixunlimited.pbbible.sync.Util.isConnected(context)) {
            Toast.makeText(context, "No internet connectivity", Toast.LENGTH_SHORT).show();
            return;
        }
        // Show progress dialog while downloading
        final ProgressDialog progress = ProgressDialog.show( context, context.getString( R.string.pdf_show_local_progress_title ), context.getString( R.string.pdf_show_local_progress_content ), true );

        // Create the download request
        DownloadManager.Request r = new DownloadManager.Request( Uri.parse( pdfUrl ) );
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

    public static class PDFTools {

    }
}
