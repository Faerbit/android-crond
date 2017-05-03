package it.faerb.crond;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static it.faerb.crond.Constants.INTENT_EXTRA_LINE_NAME;
import static it.faerb.crond.Constants.INTENT_EXTRA_LINE_NO_NAME;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        IO io = new IO(context);
        Crond crond = new Crond(context, io);
        String line = intent.getExtras().getString(INTENT_EXTRA_LINE_NAME);
        int lineNo = intent.getExtras().getInt(INTENT_EXTRA_LINE_NO_NAME);
        crond.executeLine(line, lineNo);
        crond.scheduleLine(line, lineNo);
    }
}
