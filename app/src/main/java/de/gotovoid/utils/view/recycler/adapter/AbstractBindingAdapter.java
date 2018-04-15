package de.gotovoid.utils.view.recycler.adapter;

import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.gotovoid.utils.view.recycler.BindingViewHolder;


/**
 * Created by DJ on 02/04/18.
 */

/**
 * Abstract implementation of an {@link RecyclerView.Adapter} using the {@link BindingViewHolder}
 * class.
 * This abstract implementation relies on an implementation of the {@link DataDelegate}
 * for data management. The actual instantiation of the {@link BindingViewHolder} is managed
 * by {@link LayoutBinder} objects. The Management of the
 * This enhances flexibility because concrete implementations
 *
 * @param <Data>    type if the data
 * @param <Binding> type of the {@link ViewDataBinding}
 * @param <Holder>  type of the {@link BindingViewHolder}
 */
public class AbstractBindingAdapter<Data,
        Binding extends ViewDataBinding,
        Holder extends BindingViewHolder<Data, Binding>>
        extends RecyclerView.Adapter<Holder> {

    private final LayoutBinderDelegate<Data, Binding, Holder> mLayoutDelegate;
    private final DataDelegate<Data> mDataDelegate;

    private OnItemClickListener mOnItemClickListener;

    /**
     * Constructor taking the {@link LayoutBinderDelegate} and {@link DataDelegate}.
     *
     * @param layoutDelegate the {@link LayoutBinderDelegate}
     * @param dataDelegate   the {@link DataDelegate}
     */
    protected AbstractBindingAdapter(
            final @NonNull LayoutBinderDelegate<Data, Binding, Holder> layoutDelegate,
            final @NonNull DataDelegate<Data> dataDelegate) {
        mLayoutDelegate = layoutDelegate;
        mDataDelegate = dataDelegate;
    }

    @Override
    @NonNull
    public Holder onCreateViewHolder(final @NonNull ViewGroup parent, final int viewType) {
        return (Holder) getLayoutBinder(viewType).createHolder(parent);
    }

    @Override
    public void onBindViewHolder(final @NonNull Holder holder, final int position) {
        holder.setData(getItemAt(position));
        holder.setOnItemClickListener(mOnItemClickListener);
    }

    /**
     * Sets the {@link OnItemClickListener} to be added to each and every
     * {@link RecyclerView.ViewHolder}.
     *
     * @param listener the {@link OnItemClickListener}
     */
    public void setOnItemClickListener(final @Nullable OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return mDataDelegate.getItemCount();
    }

    /**
     * Returns the data item at the given position.
     *
     * @param position the position of the item
     * @return the data item
     */
    @Nullable
    public Data getItemAt(final int position) {
        return mDataDelegate.getItemAt(position);
    }

    @Override
    public long getItemId(final int position) {
        return mDataDelegate.getItemId(position);
    }

    /**
     * Set the data to be managed by the {@link RecyclerView.Adapter}.
     *
     * @param data the data.
     */
    public void setData(final List<Data> data) {
        mDataDelegate.setData(data, this);
    }

    /**
     * Returns the appropriate {@link LayoutBinder} for the given view type.
     *
     * @param viewType the view type
     * @return the {@link LayoutBinder}
     */
    protected LayoutBinder getLayoutBinder(final int viewType) {
        return mLayoutDelegate.getBinder(viewType);
    }

    @Override
    public int getItemViewType(final int position) {
        return mLayoutDelegate.getViewType(position, this);
    }

    /**
     * Returns the {@link DataDelegate}.
     *
     * @return the {@link DataDelegate}
     */
    protected DataDelegate<Data> getDataDelegate() {
        return mDataDelegate;
    }

    /**
     * Creates new {@link BindingViewHolder} instances.
     * Holds the layout id and a {@link BindingViewHolder.DataBinder} instance used to create
     * new instances of a concrete {@link BindingViewHolder}.
     *
     * @param <Data>    type of the data
     * @param <Binding> type of the {@link ViewDataBinding}
     * @param <Holder>  type of the {@link BindingViewHolder}
     */
    public static class LayoutBinder<Data,
            Binding extends ViewDataBinding,
            Holder extends BindingViewHolder<Data, Binding>> {
        private final BindingViewHolder.DataBinder<Data, Binding> mDataBinder;
        @LayoutRes
        private final int mLayoutId;

        /**
         * Constructor taking the layout id and {@link BindingViewHolder.DataBinder}.
         *
         * @param layoutId   the layout id
         * @param dataBinder the {@link BindingViewHolder.DataBinder}
         */
        public LayoutBinder(final @LayoutRes int layoutId,
                            final @NonNull BindingViewHolder.DataBinder<Data, Binding> dataBinder) {
            mLayoutId = layoutId;
            mDataBinder = dataBinder;
        }

        /**
         * Creates a new {@link BindingViewHolder} instance.
         *
         * @param parent the parent {@link ViewGroup}
         * @return the {@link BindingViewHolder} instance
         */
        @NonNull
        public Holder createHolder(final @NonNull ViewGroup parent) {
            return (Holder) new BindingViewHolder<>(mLayoutId, parent, mDataBinder);
        }
    }

    /**
     * Delegate for the data management of the {@link AbstractBindingAdapter}.
     *
     * @param <Data> type of the data managed
     */
    public interface DataDelegate<Data> {
        /**
         * Returns the item at the given position.
         *
         * @param position the position
         * @return the {@link Data} item
         */
        @Nullable
        Data getItemAt(final int position);

        /**
         * Returns the number of items stored.
         *
         * @return number of items
         */
        int getItemCount();

        /**
         * Returns the id of the item at the given position.
         *
         * @param position position of the item
         * @return id of the item
         */
        long getItemId(final int position);

        /**
         * Sets the new data to be managed.
         *
         * @param data    the new data to be set
         * @param adapter the {@link RecyclerView.Adapter} to be notified
         */
        void setData(final @NonNull List<Data> data,
                     final @NonNull RecyclerView.Adapter adapter);
    }

    /**
     * Delegate for the {@link LayoutBinder} management of the {@link AbstractBindingAdapter}.
     *
     * @param <Data>    the type of data
     * @param <Binding> the type of {@link ViewDataBinding}
     * @param <Holder>  the type of {@link BindingViewHolder}
     */
    public interface LayoutBinderDelegate<Data,
            Binding extends ViewDataBinding,
            Holder extends BindingViewHolder<Data, Binding>> {
        /**
         * Returns the appropriate {@link LayoutBinder} instance for the given view type.
         * May return null if there is no matching {@link LayoutBinder} for the given view type.
         *
         * @param viewType the view type
         * @return the {@link LayoutBinder}
         */
        @Nullable
        LayoutBinder<Data, Binding, Holder> getBinder(final int viewType);

        /**
         * Returns the appropriate view type for the data stored ad the given position.
         *
         * @param position the position of the data
         * @param adapter  the {@link AbstractBindingAdapter} storing the data
         * @return the view type
         */
        // TODO: couldn't we just use the DataDelegate?
        int getViewType(final int position,
                        final AbstractBindingAdapter<Data, Binding, Holder> adapter);
    }

    /**
     * This interface defines the item click listener for the {@link AbstractBindingAdapter}.
     */
    public interface OnItemClickListener {
        /**
         * Called when an item of the {@link RecyclerView} was clicked.
         *
         * @param view            the {@link View} that was clicked
         * @param adapterPosition the position of the item
         */
        void onItemClick(final @NonNull View view, final int adapterPosition);
    }
}
