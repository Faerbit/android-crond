package it.faerb.crond;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;

import static it.faerb.crond.Constants.INTENT_EXTRA_LINE_NAME;
import static it.faerb.crond.Constants.INTENT_EXTRA_LINE_NO_NAME;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    private PowerManager.WakeLock wakeLock = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();
        new LineExecuter(context).execute(intent);
    }

    private abstract class CrondAsyncTask extends AsyncTask<Intent, Void, Void> {
        private Context context = null;

        public CrondAsyncTask(Context context) {
            this.context = context;
        }
    }

    private class LineExecuter extends CrondAsyncTask {
        public LineExecuter(Context context) {
            super(context);
        }

        @Override
        protected Void doInBackground(Intent... intent) {
            Crond crond = new Crond(super.context);
            String line = intent[0].getExtras().getString(INTENT_EXTRA_LINE_NAME);
            int lineNo = intent[0].getExtras().getInt(INTENT_EXTRA_LINE_NO_NAME);
            crond.executeLine(line, lineNo);
            crond.scheduleLine(line, lineNo);
            wakeLock.release();
            return null;
        }
    }
}
