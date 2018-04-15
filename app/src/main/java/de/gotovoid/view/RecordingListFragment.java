package de.gotovoid.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.wear.widget.SwipeDismissFrameLayout;
import android.support.wear.widget.SwipeDismissFrameLayout.Callback;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.gotovoid.R;
import de.gotovoid.databinding.RecordingListItemBinding;
import de.gotovoid.utils.view.recycler.BindingViewHolder;
import de.gotovoid.utils.view.recycler.adapter.AbstractBindingAdapter;
import de.gotovoid.utils.view.recycler.adapter.MutableDataDelegate;
import de.gotovoid.utils.view.recycler.adapter.SimpleBindingAdapter;
import de.gotovoid.utils.view.recycler.adapter.SimpleLayoutBinderDelegate;
import de.gotovoid.view.model.RecordingListViewModel;
import de.gotovoid.database.model.Recording;

/**
 * This {@link Fragment} shows a {@link List} of {@link Recording}s on a {@link RecyclerView}.
 * <p>
 * Created by DJ on 13/01/18.
 */
public class RecordingListFragment extends Fragment {
    private static final String TAG = RecordingListFragment.class.getSimpleName();

    private RecordingListViewModel mModel;

    /**
     * Root layout to enable dismiss of the {@link Fragment} with a swipe.
     */
    private SwipeDismissFrameLayout mDismissLayout;
    /**
     * {@link RecyclerView} to display the data.
     */
    private WearableRecyclerView mRecyclerView;
    /**
     * {@link SimpleBindingAdapter} to manage the data displayed by the {@link RecyclerView}.
     */
    private SimpleMutableAdapter<Recording, RecordingListItemBinding> mAdapter;
    /**
     * {@link WearableRecyclerView.LayoutManager} managing the layout of the {@link RecyclerView}.
     */
    private WearableRecyclerView.LayoutManager mLayoutManager;


    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: savedInstanceState = ["
                + savedInstanceState + "]");
        mModel = ViewModelProviders.of(this).get(RecordingListViewModel.class);
        // Observe the LiveData object for changes to the data.
        mModel.getRecordings().observe(this, new Observer<List<Recording>>() {
            @Override
            public void onChanged(@Nullable final List<Recording> recordings) {
                if (recordings == null) {
                    mAdapter.setData(new ArrayList<Recording>());
                    return;
                }
                Log.d(TAG, "onChanged: size: " + recordings.size());
                mAdapter.setData(recordings);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView() called with: inflater = ["
                + inflater + "], container = ["
                + container + "], savedInstanceState = ["
                + savedInstanceState + "]");
        final View contentView = inflater.inflate(R.layout.recording_list_fragment,
                container,
                false);
        // Get the RecyclerView and configure it.
        mRecyclerView = contentView.findViewById(R.id.recycler_view);
        // Tell the RecyclerView that it's size will not change because of the content.
        mRecyclerView.setHasFixedSize(true);
        // Wearable only. Align first and last element at the center of the RecyclerView.
        mRecyclerView.setEdgeItemsCenteringEnabled(true);
        // Request the focus so we can use rotary input devices.
        mRecyclerView.requestFocus();

        // Use a linear LayoutManager with the RecyclerView.
        mLayoutManager = new WearableLinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Use the RecordingListAdapter.
        mAdapter = new SimpleMutableAdapter<>(
                R.layout.recording_list_item,
                (data, binding) -> binding.setRecording(data));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setData(mModel.getRecordings().getValue());
        mAdapter.setOnItemClickListener((itemView, adapterPosition) -> {
            // TODO: create method for this.
            final Recording recording = mAdapter.getItemAt(adapterPosition);
            Toast.makeText(getContext(), recording.getName() + ", " + recording.getId(),
                    Toast.LENGTH_SHORT).show();
            Fragment fragment = new RecordingDisplayFragment();
            Bundle bundle = new Bundle();
            bundle.putLong(RecordingDisplayFragment.RECORDING_ID_KEY, recording.getId());
            fragment.setArguments(bundle);
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.content_container,
                    fragment,
                    fragment.getClass().getSimpleName());
            transaction.addToBackStack(fragment.getClass().getSimpleName());
            transaction.commit();
        });
        mDismissLayout = contentView.findViewById(R.id.dismiss_layout);
        mDismissLayout.addCallback(new Callback() {
            @Override
            public void onDismissed(final SwipeDismissFrameLayout layout) {
                layout.setVisibility(View.GONE);
                getFragmentManager().popBackStack();
            }
        });

        ItemTouchHelper.Callback callback = new SwipeDeleteTouchHelperCallback(mAdapter,
                (position) -> mModel.deleteRecording(mAdapter.getItemAt(position)));
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);
        return contentView;
    }

    /**
     * {@link ItemTouchHelper.Callback} implementation for one directional swipe operation.
     * This is used to implement swipe delete operations.
     */
    private static class SwipeDeleteTouchHelperCallback extends ItemTouchHelper.Callback {
        private final Adapter mAdapter;
        private final ModelCallback mModelCallback;

        /**
         * Constructor taking the {@link RecyclerView.Adapter} implementing the {@link Adapter}
         * interface to be notified when the deletion operation was performed.
         *
         * @param adapter {@link Adapter} to be notified on delete
         */
        public SwipeDeleteTouchHelperCallback(final @NonNull Adapter adapter,
                                              final @NonNull ModelCallback modelCallback) {
            mAdapter = adapter;
            mModelCallback = modelCallback;
        }

        @Override
        public int getMovementFlags(final RecyclerView recyclerView,
                                    final RecyclerView.ViewHolder viewHolder) {
            int dragFlags = 0;
            int swipeFlags = ItemTouchHelper.START;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(final RecyclerView recyclerView,
                              final RecyclerView.ViewHolder viewHolder,
                              final RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int direction) {
            final int adapterPopsition = viewHolder.getAdapterPosition();
            mAdapter.onItemDelete(adapterPopsition);
            mModelCallback.onItemDelete(adapterPopsition);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            // So we can start swiping from a long press.
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        /**
         * Interface to be implemented by an {@link RecyclerView.Adapter} to delete an item.
         */
        public interface Adapter {
            /**
             * Telling the {@link RecyclerView.Adapter} which item needs to be deleted.
             *
             * @param position position of the item
             */
            void onItemDelete(final int position);
        }

        /**
         * Interface to be implemented by the model, so the change can be propagated to the
         * model.
         */
        public interface ModelCallback {
            /**
             * Telling the model which item to delete.
             *
             * @param position position of the item
             */
            void onItemDelete(final int position);
        }
    }

    /**
     * Modifyable {@link AbstractBindingAdapter}. Allows to delete items.
     *
     * @param <Data>    type of the data
     * @param <Binding> type of the {@link ViewDataBinding}
     */
    final class SimpleMutableAdapter<Data, Binding extends ViewDataBinding>
            extends AbstractBindingAdapter<Data, Binding, BindingViewHolder<Data, Binding>>
            implements SwipeDeleteTouchHelperCallback.Adapter {

        /**
         * Constructor taking the layout id and {@link BindingViewHolder.DataBinder},
         *
         * @param layoutId the layout id
         * @param binder   the {@link BindingViewHolder.DataBinder}
         */
        public SimpleMutableAdapter(int layoutId,
                                    BindingViewHolder.DataBinder<Data, Binding> binder) {
            this(new LayoutBinder<>(layoutId, binder));
        }

        /**
         * Constructor taking the {@link AbstractBindingAdapter.LayoutBinder}.
         *
         * @param binder the {@link AbstractBindingAdapter.LayoutBinder}
         */
        protected SimpleMutableAdapter(final LayoutBinder<Data,
                Binding, BindingViewHolder<Data, Binding>> binder) {
            super(new SimpleLayoutBinderDelegate<>(binder), new MutableDataDelegate<>());
        }

        @Override
        protected MutableDataDelegate<Data> getDataDelegate() {
            // TODO: code smell... this could be solved by another generic... still ugly though
            return (MutableDataDelegate<Data>) super.getDataDelegate();
        }

        @Override
        public void onItemDelete(final int position) {
            getDataDelegate().removeItemAt(position, this);
        }
    }
}
