package eu.legnica.iilo.numerki;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.MultiSelectListPreference;

public class ChooseNumbersPreference extends MultiSelectListPreference {

    @SuppressWarnings("unused")
    public ChooseNumbersPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressWarnings("unused")
    public ChooseNumbersPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public ChooseNumbersPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public ChooseNumbersPreference(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {

        if(!isEnabled()) {
            return super.getSummary();
        }

        boolean[] selectedItems = getSelectedItems();
        CharSequence[] entries = getEntries();

        String summary = getContext().getResources().getString(R.string.choose_numbers_summary);
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < selectedItems.length; i++) {
            if(selectedItems[i]) {
                sb.append(entries[i].toString());
                sb.append(", ");
            }
        }

        if(sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append(getContext().getResources().getString(R.string.no_numbers_chosen));
        }

        return summary + " " + sb.toString();
    }
}
