package com.example.clitz_arestaurantapp.Utils;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int space;  //put some space

    public SpaceItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        // set left & right space
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;

        // ser the first item space
        if (parent.getChildLayoutPosition(view) < 2) {
            outRect.top = space;
        }
    }
}
