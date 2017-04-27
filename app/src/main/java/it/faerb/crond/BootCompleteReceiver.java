package it.faerb.crond;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static it.faerb.crond.Util.startCrond;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        startCrond();
    }
}
