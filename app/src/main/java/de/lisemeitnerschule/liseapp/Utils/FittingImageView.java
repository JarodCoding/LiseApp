package de.lisemeitnerschule.liseapp.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Pascal on 9.2.15.
 */
public class FittingImageView extends ImageView {
    public FittingImageView(Context context) {
        super(context);
    }

    public FittingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FittingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        System.err.println("onMesure");
        try {
            Drawable drawable = getDrawable();
            if (drawable == null) {
                setMeasuredDimension(0, 0);
            } else {
                int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
                int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
                if (measuredHeight == 0 && measuredWidth == 0) { //Height and width set to wrap_content
                    System.err.println("wrap boath");
                    setMeasuredDimension(measuredWidth, measuredHeight);
                } else if (measuredHeight == 0) { //Height set to wrap_content
                    int width = measuredWidth;
                    int height = width *  drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
                    setMeasuredDimension(width, height);
                    System.err.println("wrap height");
                    this.getLayoutParams().height=height;
                } else if (measuredWidth == 0){ //Width set to wrap_content
                    System.err.println("wrap width");
                    int height = measuredHeight;
                    int width = height * drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
                    setMeasuredDimension(width, height);
                    this.getLayoutParams().width=width;
                } else { //Width and height are explicitly set (either to match_parent or to exact value): this is wrong doesn't appear to be true
                    int width = measuredWidth;
                    int height = width *  drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
                    setMeasuredDimension(width, height);
                    System.err.println("wrap height");
                    this.getLayoutParams().height=height;
                }
            }
        } catch (Exception e) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

}
