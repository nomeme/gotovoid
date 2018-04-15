package de.gotovoid.utils.view.recycler.adapter;

import android.support.v7.widget.RecyclerView;

/**
 * Created by DJ on 15/04/18.
 */

/**
 * Modifyable implementation of the
 * {@link de.gotovoid.utils.view.recycler.adapter.AbstractBindingAdapter.DataDelegate}.
 *
 * @param <Data> type of the data
 */
public class MutableDataDelegate<Data> extends SimpleDataDelegate<Data> {
    /**
     * Deletes the data item at the given position and notifies the given
     * {@link RecyclerView.Adapter} of the change.
     *
     * @param position position of the item to be removed.
     * @param adapter  the {@link RecyclerView.Adapter} to be informed
     */
    public void removeItemAt(final int position, final RecyclerView.Adapter adapter) {
        if (position < 0 || position > getItemCount() - 1) {
            return;
        }
        getData().remove(position);
        adapter.notifyItemRemoved(position);
    }

    /**
     * Adds a data item at the given position and notifies the given {@link RecyclerView.Adapter}
     * of this change.
     *
     * @param position the position to add the item at
     * @param data     the data item to be added
     * @param adapter  the {@link RecyclerView.Adapter} to be informed
     */
    public void addItemAt(final int position,
                          final Data data,
                          final RecyclerView.Adapter adapter) {
        if (position < 0 || position > getItemCount() || data == null) {
            return;
        }
        getData().add(position, data);
        adapter.notifyItemInserted(position);
    }
}
