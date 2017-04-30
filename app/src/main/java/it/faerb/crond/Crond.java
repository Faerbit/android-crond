package it.faerb.crond;

import android.content.Context;
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
import com.cronutils.parser.CronParser;

import java.util.Arrays;
import java.util.Locale;

public class Crond {

    private static final String TAG = "Crond";

    private final CronParser parser =
            new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
    private final CronDescriptor descriptor = CronDescriptor.instance(Locale.getDefault());

    private Context context = null;

    public Crond(Context context) {
        this.context = context;
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
