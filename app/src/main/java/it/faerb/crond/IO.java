package it.faerb.crond;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

import static android.content.Context.MODE_PRIVATE;

public class IO {

    private static final String TAG = "IO";

    static final String ROOT_PREFIX = "/data/";
    private static final String CRONTAB_FILE_NAME= "crontab";
    private static final String CRONTAB_DEBUG_FILE_NAME= "crontab-debug";
    private static final String LOG_FILE_NAME = "crond.log";
    private static final String LOG_DEBUG_FILE_NAME = "crond-debug.log";

    static final String PREFERENCES_FILE = "preferences.conf";
    static final String PREF_USE_ROOT = "use_root";

    private Context context = null;

    private boolean use_root = false;


    public IO(Context context) {
        this.context = context;
        reload();
    }

    public void reload() {
        use_root = context.getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE)
                .getBoolean(PREF_USE_ROOT, false);
    }

    private String getPathPrefix() {
        if (use_root) {
            return ROOT_PREFIX;
        }
        else {
            return Environment.getExternalStorageDirectory().toString();
        }
    }

    public String getLogPath() {
        if (BuildConfig.DEBUG) {
            return new File(getPathPrefix(), LOG_DEBUG_FILE_NAME).getAbsolutePath();
        }
        else {
            return new File(getPathPrefix(), LOG_FILE_NAME).getAbsolutePath();
        }
    }

    public String getCrontabPath() {
        if (BuildConfig.DEBUG) {
            return new File(getPathPrefix(), CRONTAB_DEBUG_FILE_NAME).getAbsolutePath();
        }
        else {
            return new File(getPathPrefix(), CRONTAB_FILE_NAME).getAbsolutePath();
        }
    }

    void clearLogFile() {
        Log.i(TAG, executeCommand("echo -n \"\" > " + getLogPath()));
    }

    String readFileContents(String filePath) {
        return executeCommand("cat " + filePath);
    }

    String executeCommand(String cmd) {
        List<String> output;
        if (use_root) {
            output = Shell.SU.run(cmd);
        }
        else {
            output = Shell.SH.run(cmd);
        }
        if (output != null) {
            return TextUtils.join("\n", output);
        }
        else {
            return "Error when executing cmd:" + cmd;
        }
    }

    void logToLogFile(String msg) {
        msg = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS").format(new Date()) + " " + msg;
        executeCommand("echo \"" + msg + "\" >> " + getLogPath());
    }
}
