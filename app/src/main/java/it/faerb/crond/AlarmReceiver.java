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
            IO io = new IO(super.context);
            Crond crond = new Crond(super.context, io);
            String line = intent[0].getExtras().getString(INTENT_EXTRA_LINE_NAME);
            int lineNo = intent[0].getExtras().getInt(INTENT_EXTRA_LINE_NO_NAME);
            crond.executeLine(line, lineNo);
            crond.scheduleLine(line, lineNo);
            return null;
        }
    }
}
