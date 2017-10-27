package com.felixunlimited.pbbible;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static int getNumberOfChapters(int bookSelected) {

        int numberOfChapters = 0;
        //check how many chapter
        if (bookSelected < 66) {
            int thisChapterStart = Constants.arrBookStart[bookSelected-1];
            int nextChapterStart = Constants.arrBookStart[bookSelected];
            numberOfChapters = nextChapterStart-thisChapterStart;
//            if (numberOfChapters == 1) {
//                int chapterIndex = (Constants.arrBookStart[bookSelected-1]);
//                SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE).edit();
//                editor.putInt(Constants.CHAPTER_INDEX, chapterIndex);
//                editor.commit();
//            }
        } else {
            //Revelation has 22 chapter
            numberOfChapters = 22;
        }

        return numberOfChapters;
    }

    public static ArrayList<String> createChaptersList(int bookNumer) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < getNumberOfChapters(bookNumer); i++)
            arrayList.add(String.valueOf((i+1)));
        return arrayList;
    }

	public int getBookNo (int chapterIndex) {
		return Integer.parseInt(Constants.arrVerseCount[chapterIndex].split(";")[0]);
	}

	public int getChapterNo (int chapterIndex) {
		return Integer.parseInt(Constants.arrVerseCount[chapterIndex].split(";")[1]);
	}

	public int getNoOfVerses (int chapterIndex) {
		return Integer.parseInt(Constants.arrVerseCount[chapterIndex].split(";")[2]);
	}

	public static ArrayList<String> createBooksList () {
        ArrayList<String> arrayList = new ArrayList<>(Constants.arrActiveBookName.length);
        Collections.addAll(arrayList, Constants.arrActiveBookName);
        return arrayList;
    }

	public static ArrayList<String> createVersesList(int bookNumber, int chapter) {
        String bookChapter = bookNumber+";"+chapter;
        int verseNumer = 0;
        for (int i = Constants.arrBookStart[bookNumber-1]; i < Constants.arrVerseCount.length; i++) {
            if (Constants.arrVerseCount[i].startsWith(bookChapter)) {
                String[] arrBookVerse = Constants.arrVerseCount[i].split(";");
                verseNumer = Integer.parseInt(arrBookVerse[2]);
                break;
            }
        }
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < verseNumer; i++)
            arrayList.add(String.valueOf((i+1)));
        return arrayList;
    }

    public static int getChapterIdx (int bookNumber, int chapter) {
        String bookChapter = bookNumber + ";" + chapter;
        int chapterIdx = 0;
        for (int i = Constants.arrBookStart[bookNumber - 1]; i < Constants.arrVerseCount.length; i++) {
            if (Constants.arrVerseCount[i].startsWith(bookChapter)) {
                chapterIdx = i;
                break;
            }
        }
        return chapterIdx;
    }

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

	final static String TARGET_BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
	static SharedPreferences sharedPreferences;
	static int numberOfFiles = 0;

	public static void copyAssetsFilesToSdCard(Context context) {
		sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		if (!sharedPreferences.getBoolean(Constants.PREF_COPIED_FROM_ASSETS, false))
			copyAssetsFileOrDir(context, ""); // copy all files in assets folder in my project
	}

	private static void copyAssetsFileOrDir(Context context, String path) {
		AssetManager assetManager = context.getAssets();
		String assets[] = null;
		try {
			Log.i("tag", "copyAssetsFileOrDir() " + path);
			assets = assetManager.list(path);
			if (assets.length == 0) {
				copyFile(context, path);
			} else {
				String fullPath = TARGET_BASE_PATH + path;
				Log.i("tag", "path=" + fullPath);
				File dir = new File(fullPath);
				if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
					if (!dir.mkdirs())
						Log.i("tag", "could not create dir " + fullPath);
				for (String asset : assets) {
					String p;
					if (path.equals(""))
						p = "";
					else
						p = path + "/";

					if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
						copyAssetsFileOrDir(context, p + asset);
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
			Log.i("tag", "copyFile() " + filename);
			in = assetManager.open(filename);
			if (filename.endsWith(".jpg")) // extension was added to avoid compression on APK file
				newFileName = TARGET_BASE_PATH + filename.substring(0, filename.length() - 4);
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
			Log.e("tag", "Exception in copyFile() of " + newFileName);
			Log.e("tag", "Exception in copyFile() " + e.toString());
		}
	}

	public static void goToScripture(String scripture) {

	}

	public static int[] getChapterAndVerse(String textMatchString) {
		ArrayList<String> numbers = new ArrayList<String>();
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(textMatchString);
		while (matcher.find()) {
			numbers.add(matcher.group());
		}
		int[] chapterAndVerse = new int[numbers.size()];
		for (int i = 0; i < chapterAndVerse.length; i++) {
			try {
				chapterAndVerse[i] = Integer.parseInt(numbers.get(i));
			} catch (NumberFormatException ignore) {
			}
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

	public static void setTheme(Context context, int style) {
		String darkThemeDefault = context.getString(R.string.prefColorSchemeDefault);
		SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String themeChoice = defaultSharedPreferences.getString(context.getString(R.string.prefColorSchemeKey), darkThemeDefault);
		if (!themeChoice.equals(darkThemeDefault)) {
			context.setTheme(style);
		}
	}


	public static void saveTextToFile(String fileContent) {
		File DOWNLOAD_DIR = new File(Environment.getExternalStorageDirectory(), Constants.DOWNLOAD_FOLDER);
		if (!DOWNLOAD_DIR.exists())
			DOWNLOAD_DIR.mkdirs();
		//if (mNote[INDEX_CREATED])
		try {
			FileWriter fileWriter = new FileWriter(DOWNLOAD_DIR + "/books.kws", false);
			fileWriter.write(fileContent);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Return pseudo unique ID
	 * @return ID
	 */
	public static String getUniquePsuedoID() {
		// If all else fails, if the user does have lower than API 9 (lower
		// than Gingerbread), has reset their device or 'Secure.ANDROID_ID'
		// returns 'null', then simply the ID returned will be solely based
		// off their Android device information. This is where the collisions
		// can happen.
		// Thanks http://www.pocketmagic.net/?p=1662!
		// Try not to use DISPLAY, HOST or ID - these items could change.
		// If there are collisions, there will be overlapping data
		String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);

		// Thanks to @Roman SL!
		// http://stackoverflow.com/a/4789483/950427
		// Only devices with API >= 9 have android.os.Build.SERIAL
		// http://developer.android.com/reference/android/os/Build.html#SERIAL
		// If a user upgrades software or roots their device, there will be a duplicate entry
		String serial = null;
		try {
			serial = android.os.Build.class.getField("SERIAL").get(null).toString();

			// Go ahead and return the serial for api => 9
			return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
		} catch (Exception exception) {
			// String needs to be initialized
			serial = "serial"; // some value
		}

		// Thanks @Joe!
		// http://stackoverflow.com/a/2853253/950427
		// Finally, combine the values we have found by using the UUID class to create a unique identifier
		return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
	}

	public static String getUserID(Context context) {
		String uniqueID = null;
		final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
		if (uniqueID == null) {
			SharedPreferences sharedPrefs = context.getSharedPreferences(
					"", Context.MODE_PRIVATE);
			uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
			if (uniqueID == null) {
				uniqueID = UUID.randomUUID().toString();
				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putString(PREF_UNIQUE_ID, uniqueID);
				editor.commit();

				//backup the changes
				BackupManager mBackupManager = new BackupManager(context);
				mBackupManager.dataChanged();
			}
		}

		return uniqueID;
	}

	public static String getEmail(Context context) {
		AccountManager accountManager = AccountManager.get(context);
		Account account = getAccount(accountManager);

		if (account == null) {
			return null;
		} else {
			return account.name;
		}
	}

	private static Account getAccount(AccountManager accountManager) {
		Account[] accounts = accountManager.getAccountsByType("com.google");
		Account account;
		if (accounts.length > 0) {
			account = accounts[0];
		} else {
			account = null;
		}
		return account;
	}


}