package de.gotovoid.utils.view.recycler.adapter;

import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;

import de.gotovoid.utils.view.recycler.BindingViewHolder;


/**
 * Created by DJ on 02/04/18.
 */

/**
 * Simple implementation of the {@link AbstractBindingAdapter}.
 * Only manages one data type
 * Can only handle one type of {@link AbstractBindingAdapter.LayoutBinder}.
 *
 * @param <Data>    type of the data
 * @param <Binding> type of the {@link ViewDataBinding}
 */
public class SimpleBindingAdapter<Data, Binding extends ViewDataBinding>
        extends AbstractBindingAdapter<Data, Binding, BindingViewHolder<Data, Binding>> {

    /**
     * Constructor taking the id of the layout to be inflated and the
     * {@link BindingViewHolder.DataBinder} to define how the data will be set.
     *
     * @param layoutId id of the layout
     * @param binder   the {@link BindingViewHolder.DataBinder}
     */
    public SimpleBindingAdapter(final @LayoutRes int layoutId,
                                final BindingViewHolder.DataBinder<Data, Binding> binder) {
        this(new LayoutBinder<>(layoutId, binder));
    }

    /**
     * Private constructor setting the {@link LayoutBinderDelegate} and {@link DataDelegate}.
     *
     * @param binder the {@link LayoutBinder} to use
     */
    protected SimpleBindingAdapter(
            final LayoutBinder<Data, Binding, BindingViewHolder<Data, Binding>> binder) {
        super(new SimpleLayoutBinderDelegate<>(binder), new SimpleDataDelegate<>());
    }
}
