package de.gotovoid.utils.view.recycler.adapter;

import android.support.v7.widget.RecyclerView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
/**
 * Created by DJ on 03/04/18.
 */

/**
 * Verifies the functionality of the {@link SimpleDataDelegate}.
 */
public class SimpleDataDelegateTest {
    private static final int OBJECT_COUNT = 10;
    private static final int POSITION = 3;
    private SimpleDataDelegate mDelegate;
    private RecyclerView.Adapter mAdapter;

    /**
     * Prepares the test run.
     */
    @Before
    public void before() {
        mDelegate = new SimpleDataDelegate();
        mAdapter = Mockito.mock(RecyclerView.Adapter.class);
    }

    /**
     * Generates fake data.
     *
     * @return fake data
     */
    public List getFakeData() {
        final List list = new ArrayList();
        for (int i = 0; i < OBJECT_COUNT; i++) {
            list.add(new Object());
        }
        return list;
    }

    /**
     * Verifies that the data is initially empty.
     */
    @Test
    public void testInitial() {
        assertThat(mDelegate.getItemCount(), is(0));
    }

    /**
     * Verifies that setting data works as expected.
     */
    @Test
    public void testSetData() {
        List list = getFakeData();
        try {
            mDelegate.setData(list, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        assertThat(mDelegate.getItemCount(), is(OBJECT_COUNT));
    }

    /**
     * Verifies that getting an item at a position works as expected.
     */
    @Test
    public void testGetItemAt() {
        List list = getFakeData();
        try {
            mDelegate.setData(list, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        assertThat(mDelegate.getItemAt(POSITION), is(list.get(POSITION)));
    }

    /**
     * Verifies that adding different data is working as expected.
     */
    @Test
    public void testSetDataSeveral() {
        List list = getFakeData();
        try {
            mDelegate.setData(list, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        list = getFakeData();
        try {
            mDelegate.setData(list, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        assertThat(mDelegate.getItemCount(), is(OBJECT_COUNT));
    }

    /**
     * Verifies that getting data after adding data several times works as expected.
     */
    @Test
    public void testGetItemAtSeveral() {
        List list = getFakeData();
        try {
            mDelegate.setData(list, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        list = getFakeData();
        try {
            mDelegate.setData(list, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        assertThat(mDelegate.getItemAt(POSITION), is(list.get(POSITION)));
    }

    /**
     * Verifies that the correct item id is returned.
     */
    @Test
    public void testgetItemId() {
        assertThat(mDelegate.getItemId(0), is(RecyclerView.NO_ID));
    }
}
