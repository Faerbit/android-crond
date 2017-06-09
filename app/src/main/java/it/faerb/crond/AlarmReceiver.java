package it.faerb.crond;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PowerManager;

import static it.faerb.crond.Constants.INTENT_EXTRA_LINE_NAME;
import static it.faerb.crond.Constants.INTENT_EXTRA_LINE_NO_NAME;
import static it.faerb.crond.Constants.PREFERENCES_FILE;
import static it.faerb.crond.Constants.PREF_USE_WAKE_LOCK;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    private PowerManager.WakeLock wakeLock = null;
    private SharedPreferences sharedPrefs = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        sharedPrefs = context.getSharedPreferences(PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        if (sharedPrefs.getBoolean(PREF_USE_WAKE_LOCK, false)) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            wakeLock.acquire();
        }
        new LineExecutor(context).execute(intent);
    }

    private abstract class CrondAsyncTask extends AsyncTask<Intent, Void, Void> {
        private Context context = null;

        public CrondAsyncTask(Context context) {
            this.context = context;
        }
    }

    private class LineExecutor extends CrondAsyncTask {
        public LineExecutor(Context context) {
            super(context);
        }

        @Override
        protected Void doInBackground(Intent... intent) {
            Crond crond = new Crond(super.context);
            String line = intent[0].getExtras().getString(INTENT_EXTRA_LINE_NAME);
            int lineNo = intent[0].getExtras().getInt(INTENT_EXTRA_LINE_NO_NAME);
            crond.executeLine(line, lineNo);
            crond.scheduleLine(line, lineNo);
            if (sharedPrefs.getBoolean(PREF_USE_WAKE_LOCK, false)) {
                wakeLock.release();
            }
            return null;
        }
    }
}
