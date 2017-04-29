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

import java.lang.reflect.Type;
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

    public SpannableStringBuilder parseCrontab(String crontab) {
        SpannableStringBuilder ret = new SpannableStringBuilder();
       for (String line : crontab.split("\n")){
           ret.append(line + "\n",
                   new TypefaceSpan("monospace"), Spanned.SPAN_COMPOSING);
           ret.append(parseLine(line));
       }
       return ret;
    }

    private SpannableStringBuilder parseLine(String line) {
        SpannableStringBuilder ret = new SpannableStringBuilder();
        line = line.trim();
        if (line.charAt(0) != '*'
                && !Character.isDigit(line.charAt(0))) {
            ret.append(context.getResources().getString(R.string.invalid_cron) + "\n",
                    new StyleSpan(Typeface.ITALIC), Spanned.SPAN_COMPOSING);
        }
        else {
            String[] splitLine = line.split(" ");
            String[] cronExpr = Arrays.copyOfRange(splitLine, 0, 5);
            String[] runExpr = Arrays.copyOfRange(splitLine, 5, splitLine.length);
            String joinedCronExpr = TextUtils.join(" ", cronExpr);
            String joinedRunExpr = TextUtils.join(" ", runExpr);
            ret.append(context.getResources().getString(R.string.run) + " ",
                    new StyleSpan(Typeface.ITALIC), Spanned.SPAN_COMPOSING);
            ret.append(joinedRunExpr + " ",
                    new TypefaceSpan("monospace"), Spanned.SPAN_COMPOSING);
            ret.append(descriptor.describe(parser.parse(joinedCronExpr)) + "\n",
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
}
