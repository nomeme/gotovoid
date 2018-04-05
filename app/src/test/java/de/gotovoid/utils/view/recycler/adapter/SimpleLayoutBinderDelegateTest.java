package de.gotovoid.utils.view.recycler.adapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by DJ on 03/04/18.
 */

/**
 * Verifies the functionality of the {@link SimpleLayoutBinderDelegate}.
 */
public class SimpleLayoutBinderDelegateTest {
    private AbstractBindingAdapter.LayoutBinder mLayoutBinder;
    private SimpleLayoutBinderDelegate mLayoutBinderDelegate;
    private AbstractBindingAdapter mAdapter;

    /**
     * Prepares each test run.
     */
    @Before
    public void before() {
        mLayoutBinder = Mockito.mock(AbstractBindingAdapter.LayoutBinder.class);
        mLayoutBinderDelegate = new SimpleLayoutBinderDelegate(mLayoutBinder);
        mAdapter = Mockito.mock(AbstractBindingAdapter.class);
    }

    /**
     * Verifies that the correct view type is returned.
     */
    @Test
    public void testGetViewType() {
        assertThat(mLayoutBinderDelegate.getViewType(0, mAdapter), is(0));
    }

    /**
     * Verifies that the correct {@link AbstractBindingAdapter.LayoutBinder} is returned.
     */
    @Test
    public void testGetBinder() {
        assertThat(mLayoutBinderDelegate.getBinder(0), is(mLayoutBinder));
    }

    /**
     * Tests for the {@link SimpleLayoutBinderDelegate} with parameters.
     */
    @RunWith(Parameterized.class)
    public static class ViewTypeTest {
        private static final int[] POSITIONS = new int[]{1, 100, 65, 9828347, 3484};
        private SimpleLayoutBinderDelegate mLayoutBinderDelegate;
        private AbstractBindingAdapter mAdapter;

        @Parameterized.Parameter
        public int mIntValue;

        /**
         * Prepare the parameters.
         *
         * @return the parameters
         */
        @Parameterized.Parameters
        public static final Collection<Object[]> getParameters() {
            final List<Object[]> values = new ArrayList<>();
            for (int value : POSITIONS) {
                values.add(new Object[]{value});
            }
            return values;
        }

        /**
         * Prepares each test run.
         */
        @Before
        public void before() {
            final AbstractBindingAdapter.LayoutBinder binder =
                    Mockito.mock(AbstractBindingAdapter.LayoutBinder.class);
            mLayoutBinderDelegate = new SimpleLayoutBinderDelegate(binder);
            mAdapter = Mockito.mock(AbstractBindingAdapter.class);
        }

        /**
         * Verify that the correct view type is returned.
         */
        @Test
        public void testGetViewType() {
            assertThat(mLayoutBinderDelegate.getViewType((int) mIntValue, mAdapter), is(0));
        }

        /**
         * Verifies that the correct {@link AbstractBindingAdapter.LayoutBinder} is returned.
         */
        @Test
        public void testGetBinder() {
            assertThat(mLayoutBinderDelegate.getBinder((int) mIntValue), nullValue());
        }
    }

}
