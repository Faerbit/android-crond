package it.faerb.crond;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompleteReceiver";

    private static final String BOOT_ACTION_STRING = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // security check:
        if (!intent.getAction().equals(BOOT_ACTION_STRING)){
            Log.e(TAG, "Invalid action string: \"" + intent.getAction()
                    + "\" should be \"" + BOOT_ACTION_STRING + "\"");
            return;
        }
        Crond crond = new Crond(context);
        IO.logToLogFile(context.getString(R.string.log_boot));
        crond.setCrontab(IO.readFileContents(IO.getCrontabPath()));
        crond.scheduleCrontab();
    }
}
