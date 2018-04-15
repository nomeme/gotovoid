package de.gotovoid.utils.view.recycler.adapter;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
/**
 * Created by DJ on 03/04/18.
 */

/**
 * Verifies the functionality of the {@link SimpleDataDelegate}.
 */
public class MutableDataDelegateTest extends SimpleDataDelegateTest {
    private MutableDataDelegate mDelegate;

    /**
     * Prepares the test run.
     */
    @Before
    public void before() {
        super.before();
        mDelegate = new MutableDataDelegate();
    }

    /**
     * Verify adding an item to an empty list works.
     */
    @Test
    public void testAddItemToEmptyList() {
        final Object object = new Object();
        try {
            mDelegate.addItemAt(0, object, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        assertThat(mDelegate.getItemAt(0), is(object));
    }

    /**
     * Verify adding an item at the start works.
     */
    @Test
    public void testAddItemAtStart() {
        final Object object = new Object();

        try {
            mDelegate.setData(getFakeData(), mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        try {
            mDelegate.addItemAt(0, object, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        assertThat(mDelegate.getItemAt(0), is(object));
    }

    /**
     * Verify adding an item at the end works.
     */
    @Test
    public void testAddItemAtEnd() {
        final Object object = new Object();
        final List data = getFakeData();
        try {
            mDelegate.setData(data, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        try {
            mDelegate.addItemAt(data.size(), object, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        assertThat(mDelegate.getItemAt(data.size()), is(object));
    }

    /**
     * Verify adding a null item does not modify the list.
     */
    @Test
    public void testAddNullItem() {
        final List data = getFakeData();
        try {
            mDelegate.setData(data, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        try {
            mDelegate.addItemAt(data.size(), null, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        assertThat(mDelegate.getItemCount(), is(data.size()));
    }

    /**
     * Verify adding an item at an invalid position does no crash.
     */
    @Test
    public void testAddItemInvalid() {
        final Object object = new Object();
        try {
            mDelegate.addItemAt(-1, object, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        try {
            mDelegate.addItemAt(1, object, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        assertThat(mDelegate.getItemCount(), is(0));
    }

    /**
     * Verify that removing an item works.
     */
    @Test
    public void testRemove() {
        final List data = getFakeData();
        final int position = 4;
        try {
            mDelegate.setData(data, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        try {
            mDelegate.removeItemAt(position, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        assertThat(mDelegate.getItemCount(), is(data.size() - 1));
        assertThat(mDelegate.getItemAt(position), not(data.get(position)));
    }

    /**
     * Verify that removing an item at an invalid position does not cause crashes.
     */
    @Test
    public void testRemoveInvalid() {
        final List data = getFakeData();
        try {
            mDelegate.setData(data, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        try {
            mDelegate.removeItemAt(-1, mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        try {
            mDelegate.removeItemAt(data.size(), mAdapter);
        } catch (final NullPointerException exception) {
            // Can not override final method notifyDataSetChanged()
        }
        assertThat(mDelegate.getItemCount(), is(data.size()));
    }
}
