package com.SunDragon.RoutePlanner;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class DragDropSwipeHelper extends ItemTouchHelper.Callback {

    private ActionCompletionContract mContract;

    public DragDropSwipeHelper(ActionCompletionContract contract) {
        mContract = contract;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        ComposeRouteAdapter adapter = (ComposeRouteAdapter) recyclerView.getAdapter();
        if (viewHolder.getItemViewType() == 0) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.LEFT;
            return makeMovementFlags(dragFlags, swipeFlags);
        } else {
            return makeMovementFlags(0, 0);
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder oldViewHolder,
                          @NonNull RecyclerView.ViewHolder newViewHolder) {
        mContract.onViewMoved(oldViewHolder.getAdapterPosition(),
                newViewHolder.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mContract.onViewSwiped(viewHolder.getAdapterPosition());
    }

    public interface ActionCompletionContract {
        void onViewMoved(int oldPosition, int newPosition);
        void onViewSwiped(int position);
    }
}
