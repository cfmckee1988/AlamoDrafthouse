package com.colinmckee.alamodrafthouse;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

public class FourSquareLayoutManager extends LinearLayoutManager
{

    private static final float MILLISECONDS_PER_INCH = 50f; // Slows down the default scroll speed

    public FourSquareLayoutManager(Context context)
    {
        super(context);
    }

    public FourSquareLayoutManager(Context context, int orientation, boolean reverseLayout)
    {
        super(context, orientation, reverseLayout);
    }

    public FourSquareLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position)
    {
        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext())
        {

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition)
            {
                return FourSquareLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics)
            {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }
        };

        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    @Override
    public boolean supportsPredictiveItemAnimations()
    {
        return true;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state)
    {
        super.onLayoutChildren(recycler, state);
    }
}