package com.example.tristangriffin.projectx.Resources;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;

public class CheckableImageView extends android.support.v7.widget.AppCompatImageView implements Checkable {

    private boolean isChecked = false;
    private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

    public CheckableImageView(Context context) {
        super(context);
    }

    public CheckableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked())
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        return drawableState;
    }

    @Override
    public void setChecked(boolean checked) {
        this.isChecked = checked;
        refreshDrawableState();
    }

    @Override
    public boolean isChecked(){
        return isChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked);
    }
}
