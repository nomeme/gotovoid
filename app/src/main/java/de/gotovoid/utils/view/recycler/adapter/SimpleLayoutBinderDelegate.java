package de.gotovoid.utils.view.recycler.adapter;

import android.databinding.ViewDataBinding;

import de.gotovoid.utils.view.recycler.BindingViewHolder;

/**
 * Created by DJ on 02/04/18.
 */


/**
 * Simple implementation of the {@link AbstractBindingAdapter.LayoutBinderDelegate} only
 * managing one {@link AbstractBindingAdapter.LayoutBinder}.
 *
 * @param <Data>    type of the data
 * @param <Binding> type of the {@link ViewDataBinding}
 * @param <Holder>  type of the {@link BindingViewHolder}
 */
public class SimpleLayoutBinderDelegate<Data,
        Binding extends ViewDataBinding,
        Holder extends BindingViewHolder<Data, Binding>>
        implements AbstractBindingAdapter.LayoutBinderDelegate<Data, Binding, Holder> {

    /**
     * The {@link AbstractBindingAdapter.LayoutBinder}
     */
    private final AbstractBindingAdapter.LayoutBinder<Data, Binding, Holder> mLayoutBinder;

    /**
     * Default constructor taking the {@link AbstractBindingAdapter.LayoutBinder} to be managed.
     *
     * @param layoutBinder the {@link AbstractBindingAdapter.LayoutBinder}
     */
    public SimpleLayoutBinderDelegate(
            final AbstractBindingAdapter.LayoutBinder<Data, Binding, Holder> layoutBinder) {
        mLayoutBinder = layoutBinder;
    }

    @Override
    public AbstractBindingAdapter.LayoutBinder<Data, Binding, Holder> getBinder(
            final int viewType) {
        if (viewType == 0) {
            return mLayoutBinder;
        } else {
            return null;
        }
    }

    @Override
    public int getViewType(final int position,
                           final AbstractBindingAdapter<Data, Binding, Holder> adapter) {
        // Always return 0 because we only allow one view type.
        return 0;
    }
}
