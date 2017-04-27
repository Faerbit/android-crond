package it.faerb.crond;

import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static it.faerb.crond.Util.CROND_LOG_FILE;
import static it.faerb.crond.Util.CRONTAB_FILE;
import static it.faerb.crond.Util.clearLogFile;
import static it.faerb.crond.Util.displayFileContents;
import static it.faerb.crond.Util.killCrond;
import static it.faerb.crond.Util.startCrond;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Handler refreshHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView crontabLabel = (TextView) findViewById(R.id.text_label_crontab);
        crontabLabel.setText(String.format("crontab %s:", CRONTAB_FILE));

        final TextView crontabContent = (TextView) findViewById(R.id.text_content_crontab);
        crontabContent.setMovementMethod(new ScrollingMovementMethod());

        final TextView crondLog = (TextView) findViewById(R.id.text_content_crond);
        crondLog.setMovementMethod(new ScrollingMovementMethod());

        final Button restartButton = (Button) findViewById(R.id.button_restart_crond);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                killCrond();
                startCrond();
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
                                clearLogFile();
                                refreshImmediately();
                            }
                        })
                        .show();
            }
        });

        refreshHandler.post(refresh);
    }

    @Override
    public void onResume() {
       super.onResume();
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
            crontabContent.setText(displayFileContents(CRONTAB_FILE));

            final TextView crondLog = (TextView) findViewById(R.id.text_content_crond);
            crondLog.setText(displayFileContents(CROND_LOG_FILE));
            refreshHandler.postDelayed(refresh, 10000);
        }
    };

}
