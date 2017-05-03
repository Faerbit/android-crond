package it.faerb.crond;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        IO io = new IO(context);
        Crond crond = new Crond(context, io);
        io.logToLogFile(context.getString(R.string.log_boot));
        crond.setCrontab(io.readFileContents(io.getCrontabPath()));
        crond.scheduleCrontab();
    }
}
