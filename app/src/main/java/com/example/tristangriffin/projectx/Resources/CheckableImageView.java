package com.example.tristangriffin.projectx.Resources;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.example.tristangriffin.projectx.R;

public class CheckableImageView extends android.support.v7.widget.AppCompatImageView {

    private boolean isChecked = false;

    public CheckableImageView(Context context) {
        super(context);
    }

    public CheckableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }
    public boolean isChecked(){
        return isChecked;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isChecked) {
            Bitmap check = BitmapFactory.decodeResource(getResources(), R.drawable.baseline_done_black_18dp);
            canvas.drawBitmap(check, canvas.getWidth() - check.getWidth() - 15,
                    canvas.getHeight() - check.getHeight() - 15, new Paint());
        }
    }
}
