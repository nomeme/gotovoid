package de.gotovoid.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.gotovoid.R;
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
     * {@link RecordingListAdapter} to manage the data displayed by the {@link RecyclerView}.
     */
    private RecordingListAdapter mAdapter;
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
                    mAdapter.setEntities(new ArrayList<Recording>());
                    return;
                }
                Log.d(TAG, "onChanged: size: " + recordings.size());
                mAdapter.setEntities(recordings);
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
        final View contenView = inflater.inflate(R.layout.recording_list_fragment,
                container,
                false);
        // Get the RecyclerView and configure it.
        mRecyclerView = contenView.findViewById(R.id.recycler_view);
        // Tell the RecyclerView that it's size will not change because of the content.
        mRecyclerView.setHasFixedSize(true);
        // Wearable only. Align first and last element at the center of the RecyclerView.
        mRecyclerView.setEdgeItemsCenteringEnabled(true);
        // Request the focus so we can use rotary input devices.
        mRecyclerView.requestFocus();
        // TODO: use ItemTouchHelper for swipe gestures!

        // Use a linear LayoutManager with the RecyclerView.
        mLayoutManager = new WearableLinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Use the RecordingListAdapter.
        mAdapter = new RecordingListAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setEntities(mModel.getRecordings().getValue());
        Log.d(TAG, "onCreateView: data: " + mModel.getRecordings().getValue());
        mDismissLayout = contenView.findViewById(R.id.dismiss_layout);
        mDismissLayout.addCallback(new Callback() {
            @Override
            public void onDismissed(final SwipeDismissFrameLayout layout) {
                layout.setVisibility(View.GONE);
                getFragmentManager().popBackStack();
            }
        });

        ItemTouchHelper.Callback callback = new SwipeDeleteTouchHelperCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);
        return contenView;
    }

    /**
     * {@link ItemTouchHelper.Callback} implementation for one directional swipe operation.
     * This is used to implement swipe delete operations.
     */
    private static class SwipeDeleteTouchHelperCallback extends ItemTouchHelper.Callback {
        private final Adapter mAdapter;

        /**
         * Constructor taking the {@link RecyclerView.Adapter} implementing the {@link Adapter}
         * interface to be notified when the deletion operation was performed.
         *
         * @param adapter {@link Adapter} to be notified on delete
         */
        public SwipeDeleteTouchHelperCallback(final Adapter adapter) {
            mAdapter = adapter;
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
            mAdapter.onItemDelete(viewHolder.getAdapterPosition());
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
    }

    /**
     * {@link RecyclerView.ViewHolder} for {@link Recording}s.
     */
    private class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mIcon;
        private final TextView mTextView;

        /**
         * Constructor taking the {@link View} to fill with data.
         *
         * @param itemView the {@link View} to hold.
         */
        public ViewHolder(final View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.image);
            mTextView = itemView.findViewById(R.id.title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (view != itemView) {
                        return;
                    }
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        final String name = mAdapter.getEntities().get(position).getName();
                        final long recordingId = mAdapter.getEntities().get(position).getId();
                        Toast.makeText(getContext(),
                                name + ", " + recordingId,
                                Toast.LENGTH_SHORT).show();
                        Fragment fragment = new RecordingDisplayFragment();
                        Bundle bundle = new Bundle();
                        bundle.putLong(RecordingDisplayFragment.RECORDING_ID_KEY, recordingId);
                        fragment.setArguments(bundle);
                        FragmentManager manager = getFragmentManager();
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.add(R.id.content_container,
                                fragment,
                                fragment.getClass().getSimpleName());
                        transaction.addToBackStack(fragment.getClass().getSimpleName());
                        transaction.commit();
                    }
                }
            });
        }

        /**
         * Set the text to the {@link View}.
         *
         * @param text text to set
         */
        public void setText(final String text) {
            mTextView.setText(text);
        }

        /**
         * Set the image to the {@link View}.
         *
         * @param drawable image to set
         */
        public void setIconImage(final Drawable drawable) {
            mIcon.setImageDrawable(drawable);
        }

    }

    /**
     * Implementation of the {@link GenericEntityAdapter} for {@link Recording} and
     * {@link ViewHolder}.
     */
    private class RecordingListAdapter extends GenericEntityAdapter<Recording, ViewHolder>
            implements SwipeDeleteTouchHelperCallback.Adapter {
        private static final int RECORDING = R.string.recycler_view_recording;

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent,
                                             final int viewType) {
            final ViewHolder viewHolder;
            final View view;
            // Actually whe do not need to check here because we should only use Recordings here.
            switch (viewType) {
                case RECORDING:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.recording_list_item, parent, false);
                    viewHolder = new ViewHolder(view);
                    break;
                default:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.recording_list_item, parent, false);
                    viewHolder = new ViewHolder(view);
                    break;
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder,
                                     final int position) {
            Recording recording = getEntities().get(position);
            // TODO: show different icons for hike and flight
            switch (recording.getType()) {
                case HIKE:
                    holder.setIconImage(getResources().getDrawable(R.mipmap.ic_launcher));
                    break;
                case FLIGHT:
                    holder.setIconImage(getResources().getDrawable(R.mipmap.ic_launcher));
                    break;
                default:
                    // Do nothing
                    break;
            }
            holder.setText(getEntities().get(position).getName());
        }

        @Override
        public void onItemDelete(final int position) {
            Recording toDelete = getEntities().remove(position);
            mModel.deleteRecording(toDelete);
            notifyItemRemoved(position);
        }

    }
}
