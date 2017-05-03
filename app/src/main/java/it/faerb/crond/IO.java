package it.faerb.crond;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

import static android.content.Context.MODE_PRIVATE;

public class IO {

    private static final String TAG = "IO";

    private static final String ROOT_PREFIX = "/data/";
    private static final String CRONTAB_FILE_NAME= "crontab";
    private static final String CRONTAB_DEBUG_FILE_NAME= "crontab-debug";
    private static final String LOG_FILE_NAME = "crond.log";
    private static final String LOG_DEBUG_FILE_NAME = "crond-debug.log";


    private Context context = null;


    public IO(Context context) {
        this.context = context;
        reload();
    }

    public void reload() {
        /*use_root = context.getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE)
                .getBoolean(PREF_USE_ROOT, false);*/
    }

    public String getLogPath() {
        if (BuildConfig.DEBUG) {
            return new File(ROOT_PREFIX, LOG_DEBUG_FILE_NAME).getAbsolutePath();
        }
        else {
            return new File(ROOT_PREFIX, LOG_FILE_NAME).getAbsolutePath();
        }
    }

    public String getCrontabPath() {
        if (BuildConfig.DEBUG) {
            return new File(ROOT_PREFIX, CRONTAB_DEBUG_FILE_NAME).getAbsolutePath();
        }
        else {
            return new File(ROOT_PREFIX, CRONTAB_FILE_NAME).getAbsolutePath();
        }
    }

    void clearLogFile() {
        Log.i(TAG, executeCommand("echo -n \"\" > " + getLogPath()));
    }

    String readFileContents(String filePath) {
        return executeCommand("cat " + filePath);
    }

    String executeCommand(String cmd) {
        List<String> output = Shell.SU.run(cmd);
        if (output != null) {
            return TextUtils.join("\n", output);
        }
        else {
            return "Error when executing cmd:" + cmd;
        }
    }

    void logToLogFile(String msg) {
        msg = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS").format(new Date()) + " " + msg;
        Log.i(TAG, executeCommand("echo \"" + msg + "\" >> " + getLogPath()));
    }
}
