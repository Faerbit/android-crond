package it.faerb.crond;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

class IO {

    private static final String TAG = "IO";

    private static final String ROOT_PREFIX = "/data/";
    private static final String CRONTAB_FILE_NAME= "crontab";
    private static final String CRONTAB_DEBUG_FILE_NAME= "crontab-debug";
    private static final String LOG_FILE_NAME = "crond.log";
    private static final String LOG_DEBUG_FILE_NAME = "crond-debug.log";

    public static String getLogPath() {
        if (BuildConfig.DEBUG) {
            return new File(ROOT_PREFIX, LOG_DEBUG_FILE_NAME).getAbsolutePath();
        }
        else {
            return new File(ROOT_PREFIX, LOG_FILE_NAME).getAbsolutePath();
        }
    }

    public static String getCrontabPath() {
        if (BuildConfig.DEBUG) {
            return new File(ROOT_PREFIX, CRONTAB_DEBUG_FILE_NAME).getAbsolutePath();
        }
        else {
            return new File(ROOT_PREFIX, CRONTAB_FILE_NAME).getAbsolutePath();
        }
    }

    static void clearLogFile() {
        Log.i(TAG, executeCommand("echo -n \"\" > " + getLogPath()));
    }

    static String readFileContents(String filePath) {
        return executeCommand("cat " + filePath);
    }

    static String executeCommand(String cmd) {
        List<String> output = Shell.SU.run(cmd);
        if (output != null) {
            return TextUtils.join("\n", output);
        }
        else {
            return "Error when executing cmd:" + cmd;
        }
    }

    static void logToLogFile(String msg) {
        msg = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS").format(new Date()) + " " + msg;
        Log.i(TAG, executeCommand("echo \"" + msg + "\" >> " + getLogPath()));
    }
}
