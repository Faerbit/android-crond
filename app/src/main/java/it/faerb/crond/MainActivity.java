package it.faerb.crond;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import eu.chainfire.libsuperuser.Shell;

import static android.view.View.VISIBLE;
import static it.faerb.crond.Constants.PREFERENCES_FILE;
import static it.faerb.crond.Constants.PREF_ENABLED;
import static it.faerb.crond.Constants.PREF_NOTIFICATION_ENABLED;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final Handler refreshHandler = new Handler();

    private Crond crond = null;

    private SharedPreferences sharedPrefs = null;
    private boolean rootAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new RootChecker().execute();
    }

    private class RootChecker extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            rootAvailable = Shell.SU.available();
            return rootAvailable;
        }

        @Override
        protected void onPostExecute(Boolean rootAvail) {
            if (rootAvail) {
                init();
            }
        }
    }

    private void init() {
        crond = new Crond(this);
        sharedPrefs = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);

        LinearLayout layout = (LinearLayout) findViewById(R.id.root_layout);
        for (int i = 0; i<layout.getChildCount(); i++) {
            View view = layout.getChildAt(i);
            view.setEnabled(true);
            view.setVisibility(VISIBLE);
        }
        final TextView crontabLabel = (TextView) findViewById(R.id.text_label_crontab);
        crontabLabel.setText(getString(R.string.crontab_label, IO.getCrontabPath()));

        final TextView crontabContent = (TextView) findViewById(R.id.text_content_crontab);
        crontabContent.setMovementMethod(new ScrollingMovementMethod());

        final TextView crondLog = (TextView) findViewById(R.id.text_content_crond_log);
        crondLog.setMovementMethod(new ScrollingMovementMethod());

        final CheckBox notificationCheckBox = (CheckBox) findViewById(
                R.id.check_notification_setting);
        notificationCheckBox.setChecked(sharedPrefs.getBoolean(PREF_NOTIFICATION_ENABLED, false));
        notificationCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPrefs.edit().putBoolean(PREF_NOTIFICATION_ENABLED,
                        notificationCheckBox.isChecked()).apply();
            }
        });

        final Button enableButton = (Button) findViewById(R.id.button_enable);
        enableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean oldEnabled = sharedPrefs.getBoolean(PREF_ENABLED, false);
                sharedPrefs.edit().putBoolean(PREF_ENABLED, !oldEnabled).apply();
                updateEnabled();
                crond.scheduleCrontab();
                refreshImmediately();
            }
        });

        final Button clearButton = (Button) findViewById(R.id.button_clear_log);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.dialog_clean_title)
                        .setMessage(R.string.dialog_clean_message)
                        .setNegativeButton(R.string.no, null)
                        .setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new LogClearer().execute();
                                refreshImmediately();
                            }
                        })
                        .show();
            }
        });

        updateEnabled();
        refreshHandler.post(refresh);
    }

    @Override
    public void onResume() {
       super.onResume();
        refreshHandler.removeCallbacksAndMessages(null);
        if (rootAvailable) {
            refreshHandler.post(refresh);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacksAndMessages(null);
    }

    private void refreshImmediately() {
        refreshHandler.removeCallbacksAndMessages(null);
        refreshHandler.post(refresh);
    }

    private void updateEnabled() {
        boolean enabled = sharedPrefs.getBoolean(PREF_ENABLED, false);
        final TextView crontabContent = (TextView) findViewById(R.id.text_content_crontab);
        final TextView crondLog = (TextView) findViewById(R.id.text_content_crond_log);
        final Button enableButton = (Button) findViewById(R.id.button_enable);

        if (enabled) {
            crontabContent.setBackgroundColor(Util.getColor(this, R.color.colorBackgroundActive));
            crondLog.setBackgroundColor(Util.getColor(this, R.color.colorBackgroundActive));
            enableButton.setText(getString(R.string.button_label_enabled));
        }
        else {
            crontabContent.setBackgroundColor(Util.getColor(this, R.color.colorBackgroundInactive));
            crondLog.setBackgroundColor(Util.getColor(this, R.color.colorBackgroundInactive));
            enableButton.setText(getString(R.string.button_label_disabled));
        }
    }

    static public void showNotification(Context context, String msg) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notificationManager.notify(1,
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setColor(Util.getColor(context, R.color.colorPrimary))
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(msg)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .build());
    }

    private final Runnable refresh = new Runnable() {
        @Override
        public void run() {
            new FileReader().execute();
            refreshHandler.postDelayed(refresh, 10000);
        }
    };

    private class FileReader extends AsyncTask<Void, Void, CharSequence[]> {
        @Override
        protected CharSequence[] doInBackground(Void... params) {
            CharSequence[] ret = new CharSequence[2];
            crond.setCrontab(IO.readFileContents(IO.getCrontabPath()));
            ret[0] = crond.processCrontab();

            ret[1] = IO.readFileContents(IO.getLogPath());
            return ret;
        }

        @Override
        protected void onPostExecute(CharSequence[] sequences) {
            final TextView crontabContent = (TextView) findViewById(R.id.text_content_crontab);
            crontabContent.setText(sequences[0]);

            final TextView crondLog = (TextView) findViewById(R.id.text_content_crond_log);
            crondLog.setText(sequences[1]);
        }
    }

    private class LogClearer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            IO.clearLogFile();
            return null;
        }
    }
}
