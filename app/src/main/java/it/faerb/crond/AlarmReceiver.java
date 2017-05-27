package it.faerb.crond;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import static it.faerb.crond.Constants.INTENT_EXTRA_LINE_NAME;
import static it.faerb.crond.Constants.INTENT_EXTRA_LINE_NO_NAME;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
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
            return null;
        }
    }
}
