package de.gotovoid.utils.view.recycler.adapter;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import de.gotovoid.utils.view.recycler.BindingViewHolder;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by DJ on 02/04/18.
 */

/**
 * Verifies the functionality of the {@link AbstractBindingAdapter}.
 */
public class AbstractBindingAdapterTest {
    private static final int POSITION = 0;
    private static final int VIEW_TYPE = 0;
    private AbstractBindingAdapter.LayoutBinderDelegate mLayoutDelegate;
    private AbstractBindingAdapter.DataDelegate mDataDelegate;
    private AbstractBindingAdapter mAdapter;

    /**
     * Prepare the test run.
     */
    @Before
    public void before() {
        mLayoutDelegate = Mockito.mock(AbstractBindingAdapter.LayoutBinderDelegate.class);
        mDataDelegate = Mockito.mock(AbstractBindingAdapter.DataDelegate.class);
        mAdapter = new TestBindingAdapter(mLayoutDelegate, mDataDelegate);
    }

    /**
     * Verify that the item count is delegated appropriately.
     */
    @Test
    public void testGetItemCount() {
        mAdapter.getItemCount();
        Mockito.verify(mDataDelegate, Mockito.times(1))
                .getItemCount();
    }

    /**
     * Verify that item fetching is delegated correctly.
     */
    @Test
    public void testGetItemAt() {
        mAdapter.getItemAt(POSITION);
        Mockito.verify(mDataDelegate, Mockito.times(1))
                .getItemAt(POSITION);
    }

    /**
     * Verify that fetching the item id is delegated correctly.
     */
    @Test
    public void testGetItemId() {
        mAdapter.getItemId(POSITION);
        Mockito.verify(mDataDelegate, Mockito.times(1))
                .getItemId(POSITION);
    }

    /**
     * Verify that setting data is delegated correctly.
     */
    @Test
    public void testSetData() {
        final List data = new ArrayList();
        mAdapter.setData(data);
        Mockito.verify(mDataDelegate, Mockito.times(1))
                .setData(data, mAdapter);
    }

    /**
     * Verify that fetching the correct {@link AbstractBindingAdapter.LayoutBinder} is
     * delegated correctly.
     */
    @Test
    public void testGetLayoutBinder() {
        mAdapter.getLayoutBinder(VIEW_TYPE);
        Mockito.verify(mLayoutDelegate, Mockito.times(1))
                .getBinder(VIEW_TYPE);
    }

    /**
     * Verify that fetching the item type is delegated correctly.
     */
    @Test
    public void testGetItemViewType() {
        mAdapter.getItemViewType(POSITION);
        Mockito.verify(mLayoutDelegate, Mockito.times(1))
                .getViewType(POSITION, mAdapter);
    }

    /**
     * Verify that creating the view holder is delegated correctly.
     */
    @Test
    public void testOnCreateViewHolder() {
        final ViewGroup parent = Mockito.mock(ViewGroup.class);
        final AbstractBindingAdapter.LayoutBinder binder =
                Mockito.mock(AbstractBindingAdapter.LayoutBinder.class);
        final BindingViewHolder holder = Mockito.mock(BindingViewHolder.class);
        Mockito.when(mLayoutDelegate.getBinder(VIEW_TYPE)).thenReturn(binder);
        Mockito.doReturn(holder).when(binder).createHolder(parent);

        assertThat(mAdapter.onCreateViewHolder(parent, VIEW_TYPE), is(holder));
    }

    /**
     * Verify that binding the view holder is delegated correctly.
     */
    @Test
    public void testOnBindViewHolder() {
        final Object data = new Object();
        Mockito.when(mDataDelegate.getItemAt(POSITION)).thenReturn(data);
        final BindingViewHolder holder = Mockito.mock(BindingViewHolder.class);
        mAdapter.onBindViewHolder(holder, POSITION);
        Mockito.verify(holder, Mockito.times(1))
                .setData(data);
    }

    /**
     * Verify that binding an {@link AbstractBindingAdapter.OnItemClickListener} works as expected.
     */
    @Test
    public void testOnBindViewHolderClickListener() {
        final Object data = new Object();
        Mockito.when(mDataDelegate.getItemAt(POSITION)).thenReturn(data);
        final BindingViewHolder holder = Mockito.mock(BindingViewHolder.class);
        final AbstractBindingAdapter.OnItemClickListener listener =
                Mockito.mock(AbstractBindingAdapter.OnItemClickListener.class);
        mAdapter.onBindViewHolder(holder, POSITION);
        Mockito.verify(holder, Mockito.times(1))
                .setOnItemClickListener(null);
        mAdapter.setOnItemClickListener(listener);
        mAdapter.onBindViewHolder(holder, POSITION);
        Mockito.verify(holder, Mockito.times(1))
                .setOnItemClickListener(listener);
    }

    /**
     * Implementation of the {@link AbstractBindingAdapter} for testing purposes.
     */
    public class TestBindingAdapter extends AbstractBindingAdapter {

        /**
         * Constructor taking the {@link LayoutBinderDelegate} and {@link DataDelegate}.
         *
         * @param layoutDelegate the {@link LayoutBinderDelegate}
         * @param dataDelegate   the {@link DataDelegate}
         */
        protected TestBindingAdapter(final @NonNull LayoutBinderDelegate layoutDelegate,
                                     final @NonNull DataDelegate dataDelegate) {
            super(layoutDelegate, dataDelegate);
        }
    }
}
