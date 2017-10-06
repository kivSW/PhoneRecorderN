package phonerecorder.kivsw.com.faithphonerecorder.ui;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.CheckBox;

/**
 * Created by ivan on 10/6/17.
 */

public class CheckBoxNoPersistentText extends CheckBox {

    public CheckBoxNoPersistentText(final Context context) {
        super(context);
    }

    public CheckBoxNoPersistentText(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckBoxNoPersistentText(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onRestoreInstanceState(final Parcelable state) {

        final CharSequence text = getText(); // the text has been resolved anew

        super.onRestoreInstanceState(state); // this restores the old text

        setText(text); // this overwrites the restored text with the newly resolved text

    }
}
