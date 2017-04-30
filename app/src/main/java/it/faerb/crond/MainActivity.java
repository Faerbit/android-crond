package it.faerb.crond;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import static it.faerb.crond.IO.PREFERENCES_FILE;
import static it.faerb.crond.IO.USE_ROOT_PREFERENCE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Handler refreshHandler = new Handler();

    private IO io = null;
    private Crond crond = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        io = new IO(this);
        crond = new Crond(this);
        reload();
    }

    private void reload() {
        final TextView crontabLabel = (TextView) findViewById(R.id.text_label_crontab);
        crontabLabel.setText(String.format("crontab %s:", io.getCrontabPath()));

        final TextView crontabContent = (TextView) findViewById(R.id.text_content_crontab);
        crontabContent.setMovementMethod(new ScrollingMovementMethod());

        final TextView crondLog = (TextView) findViewById(R.id.text_content_crond_log);
        crondLog.setMovementMethod(new ScrollingMovementMethod());

        final Button restartButton = (Button) findViewById(R.id.button_schedule);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                                io.clearLogFile();
                                refreshImmediately();
                            }
                        })
                        .show();
            }
        });

        final CheckBox rootCheck = (CheckBox) findViewById(R.id.check_root);
        rootCheck.setChecked(getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE)
                .getBoolean(USE_ROOT_PREFERENCE, false));
        rootCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE).edit()
                        .putBoolean(USE_ROOT_PREFERENCE, rootCheck.isChecked())
                        .apply();
                io.reload();
                reload();
            }
        });

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



    private final Runnable refresh = new Runnable() {
        @Override
        public void run() {
            final TextView crontabContent = (TextView) findViewById(R.id.text_content_crontab);
            String fileContent = io.readFileContents(io.getCrontabPath());
            crontabContent.setText(crond.describeCrontab(fileContent));
            io.logToLogFile("Parsed crontab");

            final TextView crondLog = (TextView) findViewById(R.id.text_content_crond_log);
            crondLog.setText(io.readFileContents(io.getLogPath()));
            refreshHandler.postDelayed(refresh, 10000);
        }
    };

}
