package it.faerb.crond;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Arrays;
import java.util.Locale;

import static it.faerb.crond.IO.PREFERENCES_FILE;

public class Crond {

    private static final String TAG = "Crond";

    private final CronParser parser =
            new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
    private final CronDescriptor descriptor = CronDescriptor.instance(Locale.getDefault());

    private Context context = null;
    private IO io = null;
    private AlarmManager alarmManager = null;

    public static final String INTENT_EXTRA_LINE_NAME = "it.faerb.crond.line";
    public static final String INTENT_EXTRA_LINE_NO_NAME = "it.faerb.crond.lineNo";

    private static final String PREF_OLD_TAB_LINE_COUNT = "old_tab_line_count";

    public Crond(Context context, IO io) {
        this.context = context;
        this.io = io;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public SpannableStringBuilder describeCrontab(String crontab) {
        SpannableStringBuilder ret = new SpannableStringBuilder();
        for (String line : crontab.split("\n")){
            ret.append(line + "\n",
                    new TypefaceSpan("monospace"), Spanned.SPAN_COMPOSING);
            ret.append(describeLine(line));
        }
       return ret;
    }

    public void scheduleCrontab(String crontab) {
        SharedPreferences sharedPrefs= context.getSharedPreferences(PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        cancelAllAlarms(sharedPrefs.getInt(PREF_OLD_TAB_LINE_COUNT, 0));
        int i = 0;
        for (String line : crontab.split("\n")) {
            scheduleLine(line, i);
            i++;
        }
        sharedPrefs.edit().putInt(PREF_OLD_TAB_LINE_COUNT, crontab.split("\n").length).apply();
    }

    public void scheduleLine(String line, int lineNo) {
        ParsedLine parsedLine = parseLine(line);
        if (parsedLine == null) {
            return;
        }
        ExecutionTime time = ExecutionTime.forCron(parser.parse(parsedLine.cronExpr));
        DateTime next = time.nextExecution(DateTime.now());
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(INTENT_EXTRA_LINE_NAME, line);
        intent.putExtra(INTENT_EXTRA_LINE_NO_NAME, lineNo);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, lineNo, intent,
                PendingIntent.FLAG_UPDATE_CURRENT); // update current to replace the one used
                                                    // for cancelling any previous set alarms
        alarmManager.set(AlarmManager.RTC_WAKEUP, next.getMillis(), alarmIntent);
        io.logToLogFile(context.getString(R.string.scheduled_msg, lineNo,
                DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss.SSSS").print(next)));
    }

    public void executeLine(String line, int lineNo) {
        ParsedLine parsedLine = parseLine(line);
        if (parsedLine == null) {
            return;
        }
        io.executeCommand(parsedLine.runExpr);
        io.logToLogFile(context.getString(R.string.executec_msg, lineNo));
    }

    private SpannableStringBuilder describeLine(String line) {
        SpannableStringBuilder ret = new SpannableStringBuilder();
        ParsedLine parsedLine = parseLine(line);
        if (parsedLine == null) {
            ret.append(context.getResources().getString(R.string.invalid_cron) + "\n",
                    new StyleSpan(Typeface.ITALIC), Spanned.SPAN_COMPOSING);
        }
        else {
            ret.append(context.getResources().getString(R.string.run) + " ",
                    new StyleSpan(Typeface.ITALIC), Spanned.SPAN_COMPOSING);
            ret.append(parsedLine.runExpr + " ",
                    new TypefaceSpan("monospace"), Spanned.SPAN_COMPOSING);
            ret.append(descriptor.describe(parser.parse(parsedLine.cronExpr)) + "\n",
                    new StyleSpan(Typeface.ITALIC), Spanned.SPAN_COMPOSING);
        }
        if (Build.VERSION.SDK_INT < 23) {
            ret.setSpan(new ForegroundColorSpan(
                            context.getResources().getColor(R.color.colorPrimaryDark)), 0,
                    ret.length(), Spanned.SPAN_COMPOSING);
        }
        else {
            ret.setSpan(new ForegroundColorSpan(
                            context.getColor(R.color.colorPrimaryDark)), 0,
                    ret.length(), Spanned.SPAN_COMPOSING);
        }
        return ret;
    }

    private void cancelAllAlarms(int oldTabLineCount) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        for (int i = 0; i<oldTabLineCount; i++) {
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, i, intent, 0);
            alarmManager.cancel(alarmIntent);
        }
    }

    private ParsedLine parseLine(String line) {
        line = line.trim();
        if (line.charAt(0) != '*'
                && !Character.isDigit(line.charAt(0))) {
            return null;
        }
        String [] splitLine = line.split(" ");
        if (splitLine.length < 6) {
            return null;
        }
        String[] cronExpr = Arrays.copyOfRange(splitLine, 0, 5);
        String[] runExpr = Arrays.copyOfRange(splitLine, 5, splitLine.length);
        String joinedCronExpr = TextUtils.join(" ", cronExpr);
        String joinedRunExpr = TextUtils.join(" ", runExpr);
        return new ParsedLine(joinedCronExpr, joinedRunExpr);
    }

    private class ParsedLine {
        String cronExpr;
        String runExpr;
        ParsedLine(String cronExpr, String runExpr) {
            this.cronExpr = cronExpr;
            this.runExpr = runExpr;
        }
    }
}
