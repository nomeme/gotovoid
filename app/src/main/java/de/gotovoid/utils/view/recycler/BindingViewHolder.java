package de.gotovoid.utils.view.recycler;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import de.gotovoid.utils.view.recycler.adapter.AbstractBindingAdapter;

/**
 * Created by DJ on 02/04/18.
 */

public class BindingViewHolder<Data, Binding extends ViewDataBinding>
        extends RecyclerView.ViewHolder {
    private final DataBinder<Data, Binding> mBinder;
    private final Binding mBinding;

    public BindingViewHolder(final @LayoutRes int layout,
                             final @NonNull ViewGroup parent,
                             final @NonNull DataBinder<Data, Binding> binder) {
        this(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                layout, parent, false), binder);
    }

    protected BindingViewHolder(final @NonNull Binding binding,
                              final @NonNull DataBinder<Data, Binding> binder) {
        super(binding.getRoot());
        mBinding = binding;
        mBinder = binder;
    }

    public void setData(final @NonNull Data data) {
        mBinder.applyData(data, mBinding);
    }

    public void setOnItemClickListener(final AbstractBindingAdapter.OnItemClickListener listener) {
        if (listener == null) {
            itemView.setOnClickListener(null);
        } else {
            itemView.setOnClickListener((view) -> {
                if (view != itemView) {
                    return;
                } else {
                    listener.onItemClick(view, getAdapterPosition());
                }
            });
        }
    }

    public interface DataBinder<Data, Binding extends ViewDataBinding> {
        void applyData(final @NonNull Data data, final @NonNull Binding binding);
    }
}
