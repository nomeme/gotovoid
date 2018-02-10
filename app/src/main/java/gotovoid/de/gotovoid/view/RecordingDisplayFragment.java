package gotovoid.de.gotovoid.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.wear.widget.SwipeDismissFrameLayout;
import android.support.wear.widget.SwipeDismissFrameLayout.Callback;
import android.support.wear.widget.WearableRecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gotovoid.de.gotovoid.R;
import gotovoid.de.gotovoid.database.model.RecordingEntry;
import gotovoid.de.gotovoid.database.model.RecordingWithEntries;
import gotovoid.de.gotovoid.domain.model.GPXSerializer;
import gotovoid.de.gotovoid.domain.model.geodata.GeoCoordinate;
import gotovoid.de.gotovoid.service.repository.LocationRepository;
import gotovoid.de.gotovoid.view.model.RecordingDisplayViewModel;

/**
 * Created by DJ on 05/01/18.
 */

public class RecordingDisplayFragment extends Fragment implements IAmbientModeHandler {
    private static final String TAG = RecordingDisplayFragment.class.getSimpleName();
    public static final String RECORDING_ID_KEY = TAG.toLowerCase() + "_recording_id";

    /**
     * Root layout to enable dismiss of the {@link Fragment} with a swipe.
     */
    private SwipeDismissFrameLayout mDismissFrameLayout;
    private RecordingDisplayViewModel mModel;
    private WearableRecyclerView mRecyclerView;
    private DisplayAdapter mAdapter;

    @Override
    public void onCreate(final @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = ViewModelProviders.of(this).get(RecordingDisplayViewModel.class);
        Log.d(TAG, "onCreate() called with: savedInstanceState = ["
                + savedInstanceState + "]");
        mModel.init(LocationRepository.getRepository(getActivity()));
        if (savedInstanceState != null && savedInstanceState.containsKey(RECORDING_ID_KEY)) {
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
        if (arguments != null && arguments.containsKey(RECORDING_ID_KEY)) {
            initModel(arguments.getLong(RECORDING_ID_KEY));
        }
        final View view = inflater.inflate(R.layout.recording_display_fragment,
                container,
                false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.requestFocus();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new DisplayAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mDismissFrameLayout = (SwipeDismissFrameLayout) view.findViewById(R.id.dismiss_layout);
        mDismissFrameLayout.addCallback(new Callback() {
            @Override
            public void onDismissed(final SwipeDismissFrameLayout layout) {
                layout.setVisibility(View.GONE);
                getFragmentManager().popBackStack();
            }
        });
        return view;
    }

    private void initModel(final long recordingId) {
        Log.d(TAG, "initModel() called with: recordingId = [" + recordingId + "]");
        mModel.setRecordingId(recordingId);
        mModel.getRecordingEntries().observe(this, new Observer<List<RecordingEntry>>() {
            @Override
            public void onChanged(@Nullable final List<RecordingEntry> recordingEntries) {
                // TODO: put this into ViewModel
                Log.d(TAG, "onChanged() called with: recordingEntries = ["
                        + recordingEntries + "]");
                final List<GenericDataHolder> holders = new ArrayList<>();

                final List<GeoCoordinate> coordinates = new ArrayList<>();
                double ascending = 0;
                double descending = 0;
                RecordingEntry prev = null;
                RecordingEntry curr = null;
                if (recordingEntries != null) {
                    for (RecordingEntry entry : recordingEntries) {
                        prev = curr;
                        curr = entry;
                        coordinates.add(new GeoCoordinate(entry.getLatitude(),
                                entry.getLongitude()));
                        if (prev != null) {
                            final double diff = curr.getAltitude() - prev.getAltitude();
                            if (diff > 0) {
                                ascending += diff;
                            } else if (diff < 0) {
                                descending += diff;
                            }
                        }
                    }
                }/*
                holders.add(new GeoCoordinateHolder(coordinates));
                */
                final GeoCoordinateHolder old = mAdapter.getHeaderData();
                final Location location;
                if (old == null) {
                    location = null;
                } else {
                    location = old.getPosition();
                }
                mAdapter.setHeaderData(new GeoCoordinateHolder(coordinates, location));

                holders.add(new ShortSummaryHolder(GenericDataHolder.Type.ASCENDED_SUMMARY,
                        (int) ascending));
                holders.add(new ShortSummaryHolder(GenericDataHolder.Type.DESCENDED_SUMMARY,
                        (int) descending));
                mAdapter.setData(holders);
            }
        });

        mModel.getLocation().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable final Location location) {
                Log.d(TAG, "onChanged() called with: location = [" + location + "]");
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

        mModel.getRecordingWithEntries().observe(this, new Observer<RecordingWithEntries>() {
            @Override
            public void onChanged(@Nullable final RecordingWithEntries recordingWithEntries) {
                // Just observe
            }
        });
    }

    private static class GenericDataHolder<DataType> {
        enum Type {
            GEO_COORDINATES,
            ASCENDED_SUMMARY,
            DESCENDED_SUMMARY
        }

        private final Type mType;
        private final DataType mData;

        public GenericDataHolder(final Type type, final DataType data) {
            mType = type;
            mData = data;
        }

        public Type getType() {
            return mType;
        }

        public DataType getData() {
            return mData;
        }
    }

    private class GeoCoordinateHolder extends GenericDataHolder<List<GeoCoordinate>> {
        private final Location mPosition;

        public GeoCoordinateHolder(final List<GeoCoordinate> entries,
                                   final Location position) {
            super(Type.GEO_COORDINATES, entries);
            mPosition = position;
        }

        private Location getPosition() {
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

    private class SaveButtonViewHolder extends RecyclerView.ViewHolder {
        public SaveButtonViewHolder(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    try {
                        GPXSerializer.serializeRecording(
                                mModel.getRecordingWithEntries().getValue());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private class DisplayAdapter extends RecyclerView.Adapter<ViewHolder> {
        private static final int GEO_COORDINATE_VIEW = 0;
        private static final int SHORT_SUMMARY_VIEW = 1;
        private static final int GRAPH_SUMMARY_VIEW = 2;
        private static final int SAVE_BUTTON_VIEW = 3;

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
                case SAVE_BUTTON_VIEW:
                    view = inflater.inflate(R.layout.save_button_item, parent, false);
                    viewHolder = new SaveButtonViewHolder(view);
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
            } else if (holder instanceof SaveButtonViewHolder) {
                // Do nothing
            }
        }

        @Override
        public int getItemViewType(final int position) {
            if (isPositionHeader(position)) {
                return GEO_COORDINATE_VIEW;
            } else if (isPositionFooter(position)) {
                return SAVE_BUTTON_VIEW;
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
            return mData.size() + 2;
        }

        private boolean isPositionHeader(int position) {
            return position == 0;
        }

        private boolean isPositionFooter(int position) {
            return position == getItemCount() - 1;
        }

        public void setHeaderData(final GeoCoordinateHolder data) {
            Log.d(TAG, "setHeaderData: ");
            mHeaderData = data;
            GeoCoordinateViewHolder holder = (GeoCoordinateViewHolder) mRecyclerView
                    .findViewHolderForAdapterPosition(0);
            if (holder == null) {
                notifyItemChanged(0);
            } else {
                holder.setData(mHeaderData);
                holder.mCoordinateView.invalidate();
            }
        }

        public GeoCoordinateHolder getHeaderData() {
            return mHeaderData;
        }
    }
}
