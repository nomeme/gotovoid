package de.gotovoid.utils.view.recycler;

import android.databinding.ViewDataBinding;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.gotovoid.utils.view.recycler.adapter.AbstractBindingAdapter;


/**
 * Created by DJ on 02/04/18.
 */

/**
 * Verifies the functionality of the {@link BindingViewHolder}.
 */
public class BindingViewHolderTest {
    private View mItemView;
    private BindingViewHolder.DataBinder mBinder;
    private ViewDataBinding mBinding;
    private BindingViewHolder mViewHolder;

    /**
     * Prepares each test run.
     */
    @Before
    public void before() {
        mBinder = Mockito.mock(BindingViewHolder.DataBinder.class);
        mBinding = Mockito.mock(ViewDataBinding.class);
        mItemView = Mockito.mock(View.class);
        Mockito.when(mBinding.getRoot()).thenReturn(mItemView);
        mViewHolder = new BindingViewHolder(mBinding, mBinder);
    }

    /**
     * Verifies that setting the data works as expected.
     */
    @Test
    public void testSetData() {
        final Object data = new Object();
        mViewHolder.setData(data);
        Mockito.verify(mBinder, Mockito.times(1)).applyData(data, mBinding);
    }

    /**
     * Verifies that setting the click listener works as expected.
     */
    @Test
    public void testSetOnClickListener() {
        final AbstractBindingAdapter.OnItemClickListener listener =
                Mockito.mock(AbstractBindingAdapter.OnItemClickListener.class);

        mViewHolder.setOnItemClickListener(listener);
        Mockito.verify(mItemView, Mockito.times(1))
                .setOnClickListener(Mockito.any(View.OnClickListener.class));
        mViewHolder.setOnItemClickListener(null);
        Mockito.verify(mItemView, Mockito.times(1))
                .setOnClickListener(null);
    }
}
