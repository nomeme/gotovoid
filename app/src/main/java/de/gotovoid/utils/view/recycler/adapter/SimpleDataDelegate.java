package de.gotovoid.utils.view.recycler.adapter;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DJ on 02/04/18.
 */

/**
 * Simple implementation of the {@link AbstractBindingAdapter.DataDelegate} interface.
 * Implements data storage as a simple list that just updates the whole content on a change.
 *
 * @param <Data> type of the data managed
 */
public class SimpleDataDelegate<Data>
        implements AbstractBindingAdapter.DataDelegate<Data> {
    private final List<Data> mData;

    /**
     * Default constructor.
     */
    public SimpleDataDelegate() {
        mData = new ArrayList<>();
    }

    @Override
    @Nullable
    public Data getItemAt(final int position) {
        if (position >= mData.size()) {
            return null;
        } else {
            return mData.get(position);
        }
    }

    /**
     * Returns the managed data items.
     *
     * @return the managed data items
     */
    protected List<Data> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public long getItemId(final int position) {
        return RecyclerView.NO_ID;
    }

    @Override
    public void setData(final @NonNull List<Data> data,
                        final @NonNull RecyclerView.Adapter adapter) {
        mData.clear();
        if (data == null) {
            return;
        }
        mData.addAll(data);
        adapter.notifyDataSetChanged();
    }
}
