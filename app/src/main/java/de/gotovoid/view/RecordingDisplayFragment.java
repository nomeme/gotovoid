package de.gotovoid.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.wear.widget.SwipeDismissFrameLayout;
import android.support.wear.widget.SwipeDismissFrameLayout.Callback;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.support.wear.widget.drawer.WearableActionDrawerView;
import android.support.wear.widget.drawer.WearableDrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.gotovoid.database.model.RecordingWithEntries;
import de.gotovoid.domain.model.GPXSerializer;
import de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;
import de.gotovoid.domain.model.geodata.GeoCoordinate;
import de.gotovoid.service.repository.LocationRepository;
import de.gotovoid.view.model.RecordingDisplayViewModel;
import de.gotovoid.R;
import de.gotovoid.database.model.RecordingEntry;

/**
 * This fragment shows a single recording.
 * It shows the GPS coordinates in a {@link GeoCoordinateView} and track attributes in a
 * {@link RecyclerView}.
 * <p>
 * Created by DJ on 05/01/18.
 */

public class RecordingDisplayFragment extends Fragment implements IUpdateableAmbientModeHandler {
    private static final String TAG = RecordingDisplayFragment.class.getSimpleName();
    public static final String RECORDING_ID_KEY = TAG.toLowerCase() + "_recording_id";

    /**
     * Root layout to enable dismiss of the {@link Fragment} with a swipe.
     */
    private SwipeDismissFrameLayout mDismissFrameLayout;
    private RecordingDisplayViewModel mModel;
    private WearableRecyclerView mRecyclerView;
    private WearableDrawerLayout mDrawer;
    private WearableActionDrawerView mActionDrawerView;
    private DisplayAdapter mAdapter;

    @Override
    public void onCreate(final @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = ViewModelProviders.of(this).get(RecordingDisplayViewModel.class);
        Log.d(TAG, "onCreate() called with: savedInstanceState = ["
                + savedInstanceState + "]");
        mModel.init(LocationRepository.getRepository(getActivity()));
        if (savedInstanceState != null && savedInstanceState.containsKey(RECORDING_ID_KEY)) {
            Log.d(TAG, "onCreate: set arguments");
            initModel(savedInstanceState.getLong(RECORDING_ID_KEY));
        }

    }

    @Nullable
    @Override
    public View onCreateView(final @NonNull LayoutInflater inflater,
                             final @Nullable ViewGroup container,
                             final @Nullable Bundle savedInstanceState) {
        final View rootView = super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView() called with: inflater = [" + inflater + "], container = ["
                + container + "], savedInstanceState = [" + savedInstanceState + "]");
        final Bundle arguments = getArguments();
        // TODO: check if this is necessary here
        if (arguments != null && arguments.containsKey(RECORDING_ID_KEY)) {
            Log.d(TAG, "onCreateView: set arguments");
            initModel(arguments.getLong(RECORDING_ID_KEY));
        }
        final View view = inflater.inflate(R.layout.recording_display_fragment,
                container,
                false);
        // Get the RecyclerView and configure it.
        mRecyclerView = view.findViewById(R.id.recycler_view);
        // Tell the RecyclerView that it's size will not change because of the content.
        mRecyclerView.setHasFixedSize(true);
        // Wearable only. Align first and last element at the center of the RecyclerView.
        mRecyclerView.setEdgeItemsCenteringEnabled(true);
        // Set the layout manager for the RecyclerView.
        mRecyclerView.setLayoutManager(new WearableLinearLayoutManager(getContext()));

        mAdapter = new DisplayAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // Configure the dismiss layout.
        mDismissFrameLayout = (SwipeDismissFrameLayout) view.findViewById(R.id.dismiss_layout);
        mDismissFrameLayout.addCallback(new Callback() {
            @Override
            public void onDismissed(final SwipeDismissFrameLayout layout) {
                layout.setVisibility(View.GONE);
                getFragmentManager().popBackStack();
            }
        });
        // Drawer containing the RecyclerView and the ActionDrawerView.
        mDrawer = view.findViewById(R.id.drawer_layout);
        // Request the focus so we can use rotary input devices.
        mDrawer.requestFocus();
        // The ActionDrawerView containing the actions for this screen.
        mActionDrawerView = (WearableActionDrawerView) view.findViewById(R.id.bottom_action_drawer);
        mActionDrawerView.setIsAutoPeekEnabled(false);
        mActionDrawerView.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                Log.d(TAG, "onMenuItemClick: item: " + item.getItemId());
                switch (item.getItemId()) {
                    case R.id.menu_gps:
                        Log.d(TAG, "onMenuItemClick: activate gps");
                        configureGPS();
                        configureGPSMenuItem(item);
                        break;
                    case R.id.menu_save_action:
                        Log.d(TAG, "onMenuItemClick: save action");
                        saveToFile();
                        break;
                }
                return false;
            }
        });
        // TODO: move this in external class
        mRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            private boolean mIsPeeking;

            @Override
            public void onScrollChange(final View view,
                                       final int scrollX,
                                       final int scrollY,
                                       final int oldScrollX,
                                       final int oldScrollY) {
                if (mRecyclerView.canScrollVertically(1)) {
                    // Not Reached bottom
                    if (mIsPeeking) {
                        mIsPeeking = false;
                        mActionDrawerView.getController().closeDrawer();
                    }
                } else {
                    // Reached bottom
                    mIsPeeking = true;
                    mActionDrawerView.getController().peekDrawer();
                }

            }
        });
        MenuItem item = mActionDrawerView.getMenu().findItem(R.id.menu_gps);
        configureGPSMenuItem(item);
        return view;
    }

    @Override
    public void onPause() {
        mModel.getLocation().removeObservers(this);
        super.onPause();
    }

    /**
     * Enable or disable the GPS depending on the current state.
     */
    private void configureGPS() {
        if (mModel.isGPSActive()) {
            mModel.getLocation().removeObservers(RecordingDisplayFragment.this);
            GeoCoordinateHolder data = mAdapter.getHeaderData();
            final List<GeoCoordinate> coords;
            if (data == null) {
                coords = null;
            } else {
                coords = data.getData();
            }
            mAdapter.setHeaderData(new GeoCoordinateHolder(coords, null));
        } else {
            mModel.getLocation().observe(RecordingDisplayFragment.this,
                    new Observer<ExtendedGeoCoordinate>() {
                        @Override
                        public void onChanged(@Nullable final ExtendedGeoCoordinate location) {
                            Log.d(TAG, "onChanged() called with: location = ["
                                    + location + "]");
                            final GeoCoordinateHolder data = mAdapter.getHeaderData();
                            final List<GeoCoordinate> coords;
                            if (data == null) {
                                coords = null;
                            } else {
                                coords = data.getData();
                            }
                            mAdapter.setHeaderData(new GeoCoordinateHolder(coords, location));
                        }
                    });
        }
    }

    /**
     * Save the {@link RecordingWithEntries} to a file
     */
    // TODO: possibly move this to the model.
    private void saveToFile() {
        final RecordingWithEntries recording = mModel.getRecordingWithEntries().getValue();
        final File sdCard = Environment.getExternalStorageDirectory();
        final File dir = new File(sdCard.getAbsolutePath() + "/Recordings");
        dir.mkdirs();
        final File file = new File(dir, recording.getRecording().getName());
        try {
            file.createNewFile();
            final FileWriter writer = new FileWriter(file);
            GPXSerializer.serializeRecording(
                    mModel.getRecordingWithEntries().getValue(),
                    writer);
        } catch (final IOException exception) {
            Log.e(TAG, "onMenuItemClick: ", exception);
        }
    }

    /**
     * Sets the text and icon for the GPS {@link MenuItem} to represent the current state.
     *
     * @param item the {@link MenuItem} to change
     */
    private void configureGPSMenuItem(final MenuItem item) {
        if (mModel.isGPSActive()) {
            item.setTitle("Deactivate GPS");
            item.setIcon(R.drawable.ic_gps_fixed_white_48dp);
        } else {
            item.setTitle("Activate GPS");
            item.setIcon(R.drawable.ic_gps_off_white_48dp);
        }
    }

    /**
     * Initialize the {@link RecordingDisplayViewModel} and register necessary observers in order
     * to being able to show the {@link RecordingWithEntries}.
     *
     * @param recordingId the id of the {@link RecordingDisplayViewModel} to show
     */
    private void initModel(final long recordingId) {
        Log.d(TAG, "initModel() called with: recordingId = [" + recordingId + "]");
        mModel.setRecordingId(recordingId);
        mModel.getRecordingEntries().observe(this, new Observer<List<RecordingEntry>>() {
            @Override
            public void onChanged(@Nullable final List<RecordingEntry> recordingEntries) {
                /*
                This method extracts the necessary data from the RecordingEntries to be displayed
                in the RecyclerView.
                 */
                // TODO: put this into ViewModel???
                Log.d(TAG, "onChanged() called with: recordingEntries = ["
                        + recordingEntries + "]");
                // Holder list for additional information.
                final List<GenericDataHolder> holders = new ArrayList<>();

                final List<GeoCoordinate> coordinates = new ArrayList<>();
                // TODO: consider adding this information to the Recording object.
                double ascending = 0;
                double descending = 0;
                RecordingEntry prev = null;
                RecordingEntry curr = null;
                if (recordingEntries != null) {
                    // Convert the RecordingEntries tp GeoCoordinates and extract the data
                    for (RecordingEntry entry : recordingEntries) {
                        prev = curr;
                        curr = entry;
                        // This is needed for the GeoCoordinateView
                        // Convert to GeoCoordinates
                        coordinates.add(new GeoCoordinate(entry.getLatitude(),
                                entry.getLongitude()));
                        // This is needed for the additional info.
                        // Get total altitude change.
                        if (prev != null) {
                            final double diff = curr.getAltitude() - prev.getAltitude();
                            if (diff > 0) {
                                ascending += diff;
                            } else if (diff < 0) {
                                descending += diff;
                            }
                        }
                    }
                }
                // This is needed in order to display the current location.
                final GeoCoordinateHolder old = mAdapter.getHeaderData();
                final ExtendedGeoCoordinate location;
                if (old == null) {
                    location = null;
                } else {
                    location = old.getPosition();
                }
                // Update the data for the GeoCoordinateView.
                mAdapter.setHeaderData(new GeoCoordinateHolder(coordinates, location));
                // Prepare the additional information data.
                holders.add(new ShortSummaryHolder(GenericDataHolder.Type.ASCENDED_SUMMARY,
                        (int) ascending));
                holders.add(new ShortSummaryHolder(GenericDataHolder.Type.DESCENDED_SUMMARY,
                        (int) descending));
                // Set the additional data.
                mAdapter.setData(holders);
            }
        });

        mModel.getRecordingWithEntries().observe(this, new Observer<RecordingWithEntries>() {
            @Override
            public void onChanged(@Nullable final RecordingWithEntries recordingWithEntries) {
                // Just observe
            }
        });
    }

    /**
     * This class defines a generic data holder object.
     * This class should also enable comparison for {@link android.support.v7.util.DiffUtil}.
     *
     * @param <DataType> type of data to hold.
     */
    private static class GenericDataHolder<DataType> {
        /**
         * Enumeration for the available types of data to be held by the data holder.
         * This is not really generic.
         *
         * @deprecated use instanceof concrete implementation instead
         */
        @Deprecated
        enum Type {
            GEO_COORDINATES,
            ASCENDED_SUMMARY,
            DESCENDED_SUMMARY
        }

        private final Type mType;
        private final DataType mData;

        /**
         * Constructor taking the {@link Type} and data to store.
         *
         * @param type the type of the data
         * @param data the data
         */
        public GenericDataHolder(final Type type, final DataType data) {
            mType = type;
            mData = data;
        }

        /**
         * Returns the {@link Type} of the data managed by this object.
         *
         * @return the {@link Type}
         * @deprecated use instanceof concrete implementation.
         */
        @Deprecated
        public Type getType() {
            return mType;
        }

        /**
         * Returns the data managed by this {@link GenericDataHolder}.
         *
         * @return the data
         */
        public DataType getData() {
            return mData;
        }
    }

    /**
     * Data holder for the {@link GeoCoordinateView}.
     * Stores a {@link List} of {@link GeoCoordinate}s to be displayed by a
     * {@link GeoCoordinateView} that is managed by a {@link GeoCoordinateViewHolder}.
     * Also takes the current location as {@link GeoCoordinate}.
     */
    private class GeoCoordinateHolder extends GenericDataHolder<List<GeoCoordinate>> {
        private final ExtendedGeoCoordinate mPosition;

        /**
         * Constructor taking the {@link List} of {@link GeoCoordinate}s to display and the current
         * position as {@link GeoCoordinate}.
         *
         * @param entries  the {@link List} of {@link GeoCoordinate}s to show
         * @param position the current position as {@link GeoCoordinate}
         */
        public GeoCoordinateHolder(@Nullable final List<GeoCoordinate> entries,
                                   @Nullable final ExtendedGeoCoordinate position) {
            super(Type.GEO_COORDINATES, entries);
            mPosition = position;
        }

        /**
         * Returns the current position.
         *
         * @return the current position
         */
        private ExtendedGeoCoordinate getPosition() {
            return mPosition;
        }
    }

    private class ShortSummaryHolder extends GenericDataHolder<Integer> {
        public ShortSummaryHolder(final Type type, final Integer data) {
            super(type, data);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(RECORDING_ID_KEY, mModel.getRecordingId());
    }

    @Override
    public void setIsAmbient(final boolean isAmbient) {
        // TODO: remove all content except GeoCoordinateView and set ambient on it
        /*
        if (mCoordinateView != null) {
            mCoordinateView.setIsAmbient(isAmbientActive);
        }
        if (isAmbientActive) {
            getView().setBackgroundColor(ContextCompat
                    .getColor(getContext(), R.color.background_default_ambient));
        } else {
            getView().setBackgroundColor(ContextCompat
                    .getColor(getContext(), R.color.background_default));
        }*/
    }

    @Override
    public void onUpdateAmbient() {
        // Do nothing
    }

    private class GeoCoordinateViewHolder extends RecyclerView.ViewHolder {
        private final GeoCoordinateView mCoordinateView;

        public GeoCoordinateViewHolder(final View itemView) {
            super(itemView);
            mCoordinateView = itemView.findViewById(R.id.coordinate_view);
        }

        public void setData(final GeoCoordinateHolder data) {
            if (mCoordinateView == null) {
                return;
            }
            mCoordinateView.setGeoData(data.getData());
            mCoordinateView.setCurrentPosition(data.getPosition());
        }

    }

    private class ShortSummaryViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTextView;
        private final ImageView mImageView;

        public ShortSummaryViewHolder(final View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.title);
            mImageView = itemView.findViewById(R.id.image);
        }

        public void setData(final ShortSummaryHolder data) {
            mTextView.setText(data.getData() + "m");
        }
    }

    /**
     * Adapter for the {@link RecyclerView}. Has one header entry, being the
     * {@link GeoCoordinateView} that is always available.
     * The {@link GeoCoordinateView} is managed by a {@link GeoCoordinateViewHolder}.
     * The data for the {@link GeoCoordinateViewHolder} needs to be provided in a
     * {@link GeoCoordinateHolder}.
     * Additional entries can be added as {@link List} content being of type
     * {@link ShortSummaryHolder} and will be added to a {@link View} managed by an instance of the
     * {@link ShortSummaryViewHolder}.
     */
    private class DisplayAdapter extends RecyclerView.Adapter<ViewHolder> {
        private static final int GEO_COORDINATE_VIEW = 0;
        private static final int SHORT_SUMMARY_VIEW = 1;
        private static final int GRAPH_SUMMARY_VIEW = 2;

        private GeoCoordinateHolder mHeaderData;

        private List<GenericDataHolder> mData;

        public void setData(final List<GenericDataHolder> data) {
            mData = data;
            notifyDataSetChanged();
            // TODO: implement DiffUtil to notify about item range changes!.
        }

        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            Log.d(TAG, "onCreateViewHolder() called with: parent = ["
                    + parent + "], viewType = [" + viewType + "]");
            final ViewHolder viewHolder;
            final View view;
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case GEO_COORDINATE_VIEW:
                    view = inflater.inflate(R.layout.geo_coordinate_list_item, parent, false);
                    viewHolder = new GeoCoordinateViewHolder(view);
                    break;
                case SHORT_SUMMARY_VIEW:
                    view = inflater.inflate(R.layout.recording_list_item, parent, false);
                    viewHolder = new ShortSummaryViewHolder(view);
                    break;
                default:
                    view = null;
                    viewHolder = null;
                    break;
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (holder instanceof GeoCoordinateViewHolder) {
                GeoCoordinateViewHolder geoViewHolder = (GeoCoordinateViewHolder) holder;
                geoViewHolder.setData(mHeaderData);
            } else if (holder instanceof ShortSummaryViewHolder) {
                GenericDataHolder data = mData.get(position - 1);
                ShortSummaryViewHolder shortViewHolder = (ShortSummaryViewHolder) holder;
                ShortSummaryHolder shortData = (ShortSummaryHolder) data;
                shortViewHolder.setData(shortData);
            }
        }

        @Override
        public int getItemViewType(final int position) {
            if (isPositionHeader(position)) {
                return GEO_COORDINATE_VIEW;
            } else {
                GenericDataHolder data = mData.get(position - 1);
                if (GenericDataHolder.Type.ASCENDED_SUMMARY.equals(data.getType())) {
                    return SHORT_SUMMARY_VIEW;
                } else {
                    return SHORT_SUMMARY_VIEW;
                }
            }
        }

        @Override
        public int getItemCount() {
            if (mData == null) {
                return 0;
            }
            return mData.size() + 1;
        }

        private boolean isPositionHeader(int position) {
            return position == 0;
        }

        public void setHeaderData(final GeoCoordinateHolder data) {
            mHeaderData = data;
            Log.d(TAG, "setHeaderData() called with: data = [" + data + "]");
            GeoCoordinateViewHolder holder = (GeoCoordinateViewHolder) mRecyclerView
                    .findViewHolderForAdapterPosition(0);
            if (holder == null) {
                notifyItemChanged(0);
            } else {
                holder.setData(mHeaderData);
                holder.itemView.invalidate();
                // TODO: only call if content is different!
                notifyItemChanged(0);
            }
        }

        public GeoCoordinateHolder getHeaderData() {
            return mHeaderData;
        }
    }
}
