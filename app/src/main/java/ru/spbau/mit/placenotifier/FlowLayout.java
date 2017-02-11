package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Andrey Apanasik
 *         get it from https://github.com/Suvitruf/Android-sdk-examples/tree/master/FlowLayout
 */
public class FlowLayout extends ViewGroup {

    private int padH;
    private int padV;
    private int mHeight;

    public FlowLayout(Context context) {
        super(context);
        setPadding(0, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPadding(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPadding(context, attrs);
    }

    protected void setPadding(int v, int h) {
        padH = h;
        padV = v;
    }

    protected void setPadding(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx
                .obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        String h = a.getString(R.styleable.FlowLayout_paddingH);
        String v = a.getString(R.styleable.FlowLayout_paddingV);
        if (h == null || v == null) {
            setPadding(v == null ? 0 : Integer.parseInt(v), h == null ? 0 : Integer.parseInt(h));
        } else {
            setPadding(Integer.parseInt(v), Integer.parseInt(h));
            a.recycle();
        }

    }

    @Override
    public void addView(@Nullable View child) {
        if (child != null) {
            super.addView(child);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        int count = getChildCount();
        int xpos = getPaddingLeft();
        int ypos = getPaddingTop();
        int childHeightMeasureSpec;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        } else {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        mHeight = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                        childHeightMeasureSpec);
                final int childw = child.getMeasuredWidth();
                mHeight = Math.max(mHeight, child.getMeasuredHeight() + padV);
                if (xpos + childw > width) {
                    xpos = getPaddingLeft();
                    ypos += mHeight;
                }
                xpos += childw + padH;
            }
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            height = ypos + mHeight;
        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            if (ypos + mHeight < height) {
                height = ypos + mHeight;
            }
        }
        height += 5; // Fudge to avoid clipping bottom of last row.
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        int xpos = getPaddingLeft();
        int ypos = getPaddingTop();
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final int childW = child.getMeasuredWidth();
                final int childH = child.getMeasuredHeight();
                if (xpos + childW > width) {
                    xpos = getPaddingLeft();
                    ypos += mHeight;
                }
                child.layout(xpos, ypos, xpos + childW, ypos + childH);
                xpos += childW + padH;
            }
        }
    }
}
