package com.example.mapmapbox.until;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

public class LinearLayoutManagerWithSmoothScroller extends LinearLayoutManager {

    public LinearLayoutManagerWithSmoothScroller(Context context) {
        super(context, HORIZONTAL, false);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                       int position) {
        RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    private class TopSnappedSmoothScroller extends LinearSmoothScroller {
        TopSnappedSmoothScroller(Context context) {
            super(context);

        }

        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return LinearLayoutManagerWithSmoothScroller.this
                    .computeScrollVectorForPosition(targetPosition);
        }

        @Override
        protected int getVerticalSnapPreference() {
            return SNAP_TO_START;
        }
    }
}