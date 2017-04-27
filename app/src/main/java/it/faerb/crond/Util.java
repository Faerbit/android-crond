package it.faerb.crond;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class Util {

    private static final String TAG = "Util";

    static final String CRONTAB_FILE = "/data/local/spool/cron/crontabs/root";
    static final String CROND_LOG_FILE = "/data/crond.log";

    static void startCrond() {
        Log.i(TAG, executeCommand(
                "crond -L " + CROND_LOG_FILE + " -l 4"));
    }

    static void killCrond() {
        Log.i(TAG, executeCommand(
                "ps | grep \"root.*crond\" | awk '{print $2}' | xargs kill"));
    }

    static void clearLogFile() {
        Log.i(TAG, executeCommand("echo -n \"\" > " + CROND_LOG_FILE));
    }

    static String displayFileContents(String filePath) {
        return executeCommand("cat " + filePath);
    }

    static String executeCommand(String command) {

        StringBuilder output = new StringBuilder();
        try {
            Process process = new ProcessBuilder()
                    .command(new String[]{"su", "-c", command, "-"})
                    .redirectErrorStream(true)
                    .start();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                output.append(line + "\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}
