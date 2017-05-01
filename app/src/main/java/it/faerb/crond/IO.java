package it.faerb.crond;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class IO {

    private static final String TAG = "IO";

    static final String ROOT_PREFIX = "/data/";
    static final String CRONTAB_FILE_NAME= "crontab";
    static final String LOG_FILE_NAME = "crond.log";

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
        return new File(getPathPrefix(), LOG_FILE_NAME).getAbsolutePath();
    }

    public String getCrontabPath() {
        return new File(getPathPrefix(), CRONTAB_FILE_NAME).getAbsolutePath();
    }

    void clearLogFile() {
        Log.i(TAG, executeCommand("echo -n \"\" > " + getLogPath()));
    }

    String readFileContents(String filePath) {
        return executeCommand("cat " + filePath);
    }

    String executeCommand(String pCommand) {
        String[] cmd;
        if (use_root) {
            cmd = new String[]{"su", "-c", pCommand, "-"};
        }
        else {
            cmd = new String[]{"sh", "-c", pCommand};
        }

        StringBuilder output = new StringBuilder();
        try {
            Process process = new ProcessBuilder()
                    .command(cmd)
                    .redirectErrorStream(true)
                    .start();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    void logToLogFile(String msg) {
        msg = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS").format(new Date()) + " " + msg;
        executeCommand("echo \"" + msg + "\" >> " + getLogPath());
    }
}
