package eu.legnica.iilo.numerki;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class TimePreference extends DialogPreference {
    private int mTime;

    @SuppressWarnings("unused")
    public TimePreference(Context context) {
        this(context, null);
    }

    @SuppressWarnings("WeakerAccess")
    public TimePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    @SuppressWarnings("WeakerAccess")
    public TimePreference(Context context, AttributeSet attrs,
                          int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    @SuppressWarnings("WeakerAccess")
    public TimePreference(Context context, AttributeSet attrs,
                          int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    int getTime() {
        return mTime;
    }

    void setTime(int time) {
        mTime = time;
        // Zapisz do Shared Preferences
        persistInt(time);
        setSummary(R.string.check_time_summary);
        setSummary( getSummary() + " " + getFormattedTime(time) );
    }

    private String getFormattedTime(int time) {
        int hour = time / 60;
        int minutes = time % 60;

        String strHour = (hour < 10 ? "0" : "") + hour;
        String strMinutes = (minutes < 10 ? "0" : "") + minutes;
        return strHour + ":" + strMinutes;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Default value from attribute. Fallback value is set to 0.
        return a.getInt(index, 0);
    }
    @Override
    protected void onSetInitialValue(Object defaultValue) {
        int defaultTime = defaultValue == null ? 0 : (int)defaultValue;
        setTime(getPersistedInt(defaultTime));
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.pref_dialog_time;
    }
}