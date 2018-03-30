package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;


import android.content.Context;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by ivan on 3/28/18.
 */

public class SplitToolbar extends Toolbar {
    public SplitToolbar(Context context) {
        super(context);
    }

    public SplitToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SplitToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (child instanceof ActionMenuView) {
            //params.width = LayoutParams.MATCH_PARENT;
            LinearLayout.LayoutParams newParams=new LinearLayout.LayoutParams(params);
            newParams.weight=1;
            newParams.width = 0;
            params = newParams;
        }
        super.addView(child, params);
    }
}
