package it.faerb.crond;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static it.faerb.crond.Constants.PREFERENCES_FILE;
import static it.faerb.crond.Constants.PREF_ENABLED;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Handler refreshHandler = new Handler();
    private String crontab = "";

    private IO io = null;
    private Crond crond = null;

    private SharedPreferences sharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        io = new IO(this);
        crond = new Crond(this, io);
        sharedPreferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);

        final TextView crontabLabel = (TextView) findViewById(R.id.text_label_crontab);
        crontabLabel.setText(getString(R.string.crontab_label, io.getCrontabPath()));

        final TextView crontabContent = (TextView) findViewById(R.id.text_content_crontab);
        crontabContent.setMovementMethod(new ScrollingMovementMethod());

        final TextView crondLog = (TextView) findViewById(R.id.text_content_crond_log);
        crondLog.setMovementMethod(new ScrollingMovementMethod());

        final Button enableButton = (Button) findViewById(R.id.button_enable);
        enableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean oldEnabled = sharedPreferences.getBoolean(PREF_ENABLED, false);
                sharedPreferences.edit().putBoolean(PREF_ENABLED, !oldEnabled).apply();
                updateEnabled();
                // TODO schedule
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
        refreshHandler.post(refresh);
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
        boolean enabled = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE)
                .getBoolean(PREF_ENABLED, false);
        final TextView crontabContent = (TextView) findViewById(R.id.text_content_crontab);
        final TextView crondLog = (TextView) findViewById(R.id.text_content_crond_log);
        final Button enableButton = (Button) findViewById(R.id.button_enable);

        if (enabled) {
            if (Build.VERSION.SDK_INT >= 23) {
                crontabContent.setBackgroundColor(getColor(R.color.colorBackgroundActive));
                crondLog.setBackgroundColor(getColor(R.color.colorBackgroundActive));
            }
            else {
                crontabContent.setBackgroundColor(getResources()
                        .getColor(R.color.colorBackgroundActive));
                crondLog.setBackgroundColor(getResources()
                        .getColor(R.color.colorBackgroundActive));
            }
            enableButton.setText(getString(R.string.button_label_enabled));
        }
        else {
            if (Build.VERSION.SDK_INT >= 23) {
                crontabContent.setBackgroundColor(getColor(R.color.colorBackgroundInactive));
                crondLog.setBackgroundColor(getColor(R.color.colorBackgroundInactive));
            }
            else {
                crontabContent.setBackgroundColor(getResources()
                        .getColor(R.color.colorBackgroundInactive));
                crondLog.setBackgroundColor(getResources()
                        .getColor(R.color.colorBackgroundInactive));
            }
            enableButton.setText(getString(R.string.button_label_disabled));
        }
    }

    private final Runnable refresh = new Runnable() {
        @Override
        public void run() {
            new FileReader().execute();
            refreshHandler.postDelayed(refresh, 10000);
        }
    };

    private class FileReader extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            crontab = io.readFileContents(io.getCrontabPath());

            return io.readFileContents(io.getLogPath());
        }

        @Override
        protected void onPostExecute(String log) {
            final TextView crontabContent = (TextView) findViewById(R.id.text_content_crontab);
            crontabContent.setText(crond.describeCrontab(crontab));

            final TextView crondLog = (TextView) findViewById(R.id.text_content_crond_log);
            crondLog.setText(io.readFileContents(io.getLogPath()));
        }
    }

    private class LogClearer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            io.clearLogFile();
            return null;
        }
    }
}
