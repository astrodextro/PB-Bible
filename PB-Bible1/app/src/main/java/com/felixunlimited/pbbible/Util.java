package com.felixunlimited.pbbible;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
	public static String parseVerse(String verse) {
		StringBuffer sbVerse = new StringBuffer(verse);
		while (sbVerse.indexOf("<CM>") > -1) {
			int posDelete = sbVerse.indexOf("<CM>");
			sbVerse.delete(posDelete, posDelete + 4);
		}
		while (sbVerse.indexOf("<CL>") > -1) {
			int posDelete = sbVerse.indexOf("<CL>");
			sbVerse.delete(posDelete, posDelete + 4);
			sbVerse.insert(posDelete, " ");
		}
		while (sbVerse.indexOf("<FR>") > -1) {
			int posDelete = sbVerse.indexOf("<FR>");
			sbVerse.delete(posDelete, posDelete + 4);
		}
		while (sbVerse.indexOf("<Fr>") > -1) {
			int posDelete = sbVerse.indexOf("<Fr>");
			sbVerse.delete(posDelete, posDelete + 4);
		}
		while (sbVerse.indexOf("<RF>") > -1) {
			int posDelete = sbVerse.indexOf("<RF>");
			int posEndDelete = sbVerse.indexOf("<Rf>", posDelete);
			sbVerse.delete(posDelete, posEndDelete + 4);
		}
		while (sbVerse.indexOf("<TS>") > -1) {
			int posDelete = sbVerse.indexOf("<TS>");
			int posEndDelete = sbVerse.indexOf("<Ts>", posDelete);
			sbVerse.delete(posDelete, posEndDelete + 4);
		}
		return sbVerse.toString();
	}
	
	public static String getRootCause(Throwable t) {
		Throwable cause = t;
		Throwable subCause = cause.getCause();
		while (subCause != null && !subCause.equals(cause)) {
			cause = subCause;
			subCause = cause.getCause();
		}
		return cause.getMessage();
	}

	final static String TARGET_BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
	static SharedPreferences sharedPreferences;
	static int numberOfFiles = 0;

	public static void copyFilesToSdCard(Context context) {
		sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		if (!sharedPreferences.getBoolean(Constants.PREF_COPIED_FROM_ASSETS, false))
			copyFileOrDir(context, ""); // copy all files in assets folder in my project
	}

	private static void copyFileOrDir(Context context, String path) {
		AssetManager assetManager = context.getAssets();
		String assets[] = null;
		try {
			Log.i("tag", "copyFileOrDir() "+path);
			assets = assetManager.list(path);
			if (assets.length == 0) {
				copyFile(context, path);
			} else {
				String fullPath =  TARGET_BASE_PATH + path;
				Log.i("tag", "path="+fullPath);
				File dir = new File(fullPath);
				if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
					if (!dir.mkdirs())
						Log.i("tag", "could not create dir "+fullPath);
				for (String asset : assets) {
					String p;
					if (path.equals(""))
						p = "";
					else
						p = path + "/";

					if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
						copyFileOrDir(context, p + asset);
				}
			}
		} catch (IOException ex) {
			Log.e("tag", "I/O Exception", ex);
		}
		if (numberOfFiles == 6) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean(Constants.PREF_COPIED_FROM_ASSETS, true);
			editor.apply();
		}
	}

	private static void copyFile(Context context, String filename) {
		AssetManager assetManager = context.getAssets();

		InputStream in = null;
		OutputStream out = null;
		String newFileName = null;
		try {
			Log.i("tag", "copyFile() "+filename);
			in = assetManager.open(filename);
			if (filename.endsWith(".jpg")) // extension was added to avoid compression on APK file
				newFileName = TARGET_BASE_PATH + filename.substring(0, filename.length()-4);
			else
				newFileName = TARGET_BASE_PATH + filename;
			out = new FileOutputStream(newFileName);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
			numberOfFiles++;
		} catch (Exception e) {
			Log.e("tag", "Exception in copyFile() of "+newFileName);
			Log.e("tag", "Exception in copyFile() "+e.toString());
		}
	}

	public static void goToScripture (String scripture) {

	}

    public static int[] getChapterAndVerse(String textMatchString) {
        ArrayList<String> numbers = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(textMatchString);
        while (matcher.find())
        {
            numbers.add(matcher.group());
        }
        int[] chapterAndVerse = new int[numbers.size()];
        for (int i = 0; i < chapterAndVerse.length; i++)
        {
            try {
                chapterAndVerse[i] = Integer.parseInt(numbers.get(i));
            }
            catch (NumberFormatException ignore) {}
        }
//        textMatchString.
//        String[] num = textMatchString.split(" ");
//        int[] chapterAndVerse = new int[num.length];
//        for (int i = 0, j = 0; i < num.length; i++)
//        {
//            try
//            {
//                chapterAndVerse[j] = Integer.parseInt(num[i]);
//                Toast.makeText(this, "ChapterAndVerse]"+j+"] = "+chapterAndVerse[j], Toast.LENGTH_SHORT).show();
//                j++;
//            }
//            catch (NumberFormatException e){
//                chapterAndVerse[j] = 0;
//                Toast.makeText(this, "InvalidChapterAndVerse: "+num[i], Toast.LENGTH_SHORT).show();
//            }
//            if (Integer.parseInt(num[i]) != 0)
//            {
//                chapterAndVerse[j] = Integer.parseInt(num[i]);
//                j++;
//            }
//        }
        return chapterAndVerse;
    }

	public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static void setTheme (Context context, int style) {
		String darkThemeDefault = context.getString(R.string.prefColorSchemeDefault);
		SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String themeChoice = defaultSharedPreferences.getString(context.getString(R.string.prefColorSchemeKey), darkThemeDefault);
		if (!themeChoice.equals(darkThemeDefault)) {
			context.setTheme(style);
		}
	}
}
