package it.faerb.crond;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import eu.chainfire.libsuperuser.Shell;

class IO implements Shell.OnCommandResultListener {

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
        Log.i(TAG, executeCommand("echo -n \"\" > " + getLogPath()).getOutput());
    }

    static String readFileContents(String filePath) {
        return executeCommand("cat " + filePath).getOutput();
    }

    synchronized static CommandResult executeCommand(String cmd) {
        get().shell.addCommand(cmd, 0, get());
        get().cmdReturned.acquireUninterruptibly();
        if (!get().lastResult.success()) {
            Log.w(TAG, String.format("Error while executing command:\"%sx\":\n%s",
                    cmd, get().lastResult.getOutput()));
        }
        return get().lastResult;
    }

    static void logToLogFile(String msg) {
        msg = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS").format(new Date()) + " " + msg;
        Log.i(TAG, executeCommand("echo \"" + msg + "\" >> " + getLogPath()).getOutput());
    }

    class CommandResult {
        private int exitCode;
        private String output;
        CommandResult(int returnCode, List<String> output) {
            this.exitCode = returnCode;
            this.output = TextUtils.join("\n", output);
        }

        boolean success() {
            return exitCode == 0;
        }

        int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }
    }

    private static IO instance = null;
    private CommandResult lastResult = null;
    private Shell.Interactive shell = null;
    private Semaphore cmdReturned = new Semaphore(0);

    IO() {
        shell = new Shell.Builder()
                .useSU()
                .setMinimalLogging(BuildConfig.DEBUG)
                .open();
    }

    public static IO get() {
        if (instance == null) {
            synchronized (IO.class) {
                return instance = new IO();
            }
        }
        return instance;
}

    @Override
    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
        get().lastResult = new CommandResult(exitCode, output);
        get().cmdReturned.release();
    }
}
