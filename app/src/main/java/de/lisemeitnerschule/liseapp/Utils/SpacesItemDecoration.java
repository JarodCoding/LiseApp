package de.lisemeitnerschule.liseapp.Utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Pascal on 27.4.15.
*/
public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int paddingX;
    private int paddingY;

    public SpacesItemDecoration(int paddingX,int paddingY) {
        this.paddingX = paddingX;
        this.paddingY = paddingY;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = paddingX;
        outRect.right = paddingX;
        outRect.bottom = paddingY;

        // Add top margin only for the first item to avoid double space between items
        if(parent.getChildPosition(view) == 0)
            outRect.top = paddingY;
    }
}