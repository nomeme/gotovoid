package gotovoid.de.gotovoid.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.SwipeDismissFrameLayout;
import android.support.wear.widget.WearableRecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import gotovoid.de.gotovoid.R;
import gotovoid.de.gotovoid.database.model.Recording;
import gotovoid.de.gotovoid.database.model.RecordingEntry;
import gotovoid.de.gotovoid.domain.model.geodata.GeoCoordinate;
import gotovoid.de.gotovoid.service.repository.LocationRepository;
import gotovoid.de.gotovoid.view.model.RecorderViewModel;

/**
 * Created by DJ on 20/01/18.
 */

public class RecorderFragment extends Fragment implements IUpdateableAmbientModeHandler {
    private static final String TAG = RecorderFragment.class.getSimpleName();
    private RecorderViewModel mViewModel;

    private SwipeDismissFrameLayout mDismissLayout;
    private WearableRecyclerView mRecyclerView;

    private RecorderAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        mViewModel = ViewModelProviders.of(this).get(RecorderViewModel.class);
        mViewModel.init(LocationRepository.getRepository(getActivity()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called with: inflater = ["
                + inflater + "], container = [" + container + "], savedInstanceState = ["
                + savedInstanceState + "]");
        View view = super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.recorder_fragment, container, false);
        // Add the back action.
        mDismissLayout = (SwipeDismissFrameLayout) rootView.findViewById(R.id.dismiss_layout);
        mDismissLayout.addCallback(new SwipeDismissFrameLayout.Callback() {
            @Override
            public void onDismissed(final SwipeDismissFrameLayout layout) {
                getFragmentManager().popBackStack();
                mDismissLayout.setVisibility(View.GONE);
            }
        });

        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.requestFocus();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new RecorderAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mViewModel.getEntries().observe(this, new Observer<List<RecordingEntry>>() {
            @Override
            public void onChanged(@Nullable final List<RecordingEntry> recordingEntries) {
                Log.d(TAG, "onChanged() called with: recordingEntries = ["
                        + recordingEntries + "]");
                final int size = recordingEntries.size();
                if (size < 2) {
                    return;
                }
                final RecordingEntry lastEntry = recordingEntries.get(size - 1);
                final RecordingEntry previousEntry = recordingEntries.get(size - 2);
                final GeoCoordinate lastCoord = new GeoCoordinate(lastEntry.getLatitude(),
                        lastEntry.getLongitude());
                final GeoCoordinate previousCoord = new GeoCoordinate(previousEntry.getLatitude(),
                        previousEntry.getLongitude());
                // Time difference in seconds
                final long timeDiff = (lastEntry.getTimeStamp() - previousEntry.getTimeStamp())
                        / 1000;

                final double distance = previousCoord.getHaversineDistanceTo(lastCoord);
                final double heightDiff = lastEntry.getAltitude() - previousEntry.getAltitude();

                final double horizontalSpeed = (distance / timeDiff) * 3600 / 1000;
                final double verticalSpeed = (heightDiff / timeDiff);
                final FlightInfoData flightInfoData = new FlightInfoData(
                        (int) verticalSpeed,
                        (int) horizontalSpeed,
                        lastEntry.getAltitude());
                mAdapter.setFlightInfoData(flightInfoData);
                List<GeoCoordinate> coordinates = new ArrayList<>();
                for (RecordingEntry entry : recordingEntries) {
                    coordinates.add(new GeoCoordinate(entry.getLatitude(), entry.getLongitude()));
                }
                mAdapter.setGeoCoordinates(coordinates);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called", new NullPointerException());
        mViewModel.startRecording(getContext().getApplicationContext(), Recording.Type.HIKE);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        mViewModel.stopRecording(getContext().getApplicationContext());
        super.onPause();
    }

    @Override
    public void setIsAmbient(boolean isAmbient) {
        Log.d(TAG, "setIsAmbient() called with: isAmbient = [" + isAmbient + "]");
        mAdapter.setIsAmbient(isAmbient);
        if (isAmbient) {
            mRecyclerView.setBackgroundColor(ContextCompat
                    .getColor(getContext(), R.color.background_default_ambient));
        } else {
            mRecyclerView.setBackgroundColor(ContextCompat
                    .getColor(getContext(), R.color.background_default));
        }
    }

    @Override
    public void onUpdateAmbient() {
        mViewModel.requestUpdate();
    }

    private class FlightInfoData {
        final int mAscendingSpeed;
        final int mAltitude;
        final int mSpeed;

        public FlightInfoData(final int ascendingSpeed,
                              final int speed,
                              final int altitude) {
            mAscendingSpeed = ascendingSpeed;
            mAltitude = altitude;
            mSpeed = speed;
        }

        public int getAscendingSpeed() {
            return mAscendingSpeed;
        }

        public int getAltitude() {
            return mAltitude;
        }

        public int getSpeed() {
            return mSpeed;
        }
    }

    private class FlightInfoViewHolder extends RecyclerView.ViewHolder {
        private final FlightInfoView mView;

        public FlightInfoViewHolder(final View itemView) {
            super(itemView);
            mView = itemView.findViewById(R.id.flight_info);
        }

        public void setData(final FlightInfoData data) {
            if (data != null && mView != null) {
                mView.setAscendingSpeed(data.getAscendingSpeed());
                mView.setAltitude(data.getAltitude());
                mView.setSpeed(data.getSpeed());
            }
        }
    }

    private class GeoCoordinateViewHolder extends RecyclerView.ViewHolder {
        private final GeoCoordinateView mView;

        public GeoCoordinateViewHolder(final View itemView) {
            super(itemView);
            mView = itemView.findViewById(R.id.coordinate_view);
        }

        public void setData(final List<GeoCoordinate> coordinates) {
            if (mView != null) {
                mView.setGeoData(coordinates);
            }
        }

        public void setIsAmbient(final boolean isAmbient) {
            if (mView != null) {
                mView.setIsAmbient(isAmbient);
            }
        }
    }

    private class RecorderAdapter extends RecyclerView.Adapter {
        private static final int GEO_COORDINATE_VIEW = 0;
        private static final int FLIGHT_INFO_VIEW = 1;
        private static final int DEFAULT_VIEW = 2;

        private boolean mIsAmbient;
        private List<GeoCoordinate> mGeoCoordinates;
        private FlightInfoData mFlightInfoData;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                          final int viewType) {
            Log.d(TAG, "onCreateViewHolder() called with: parent = ["
                    + parent + "], viewType = [" + viewType + "]");
            final RecyclerView.ViewHolder holder;
            final View view;
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case GEO_COORDINATE_VIEW:
                    view = inflater.inflate(R.layout.geo_coordinate_list_item, parent, false);
                    holder = new GeoCoordinateViewHolder(view);
                    break;
                case FLIGHT_INFO_VIEW:
                    view = inflater.inflate(R.layout.flight_info_list_item, parent, false);
                    holder = new FlightInfoViewHolder(view);
                    break;
                default:
                    view = null;
                    holder = null;
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder,
                                     final int position) {
            Log.d(TAG, "onBindViewHolder() called with: holder = ["
                    + holder + "], position = [" + position + "]");
            if (holder instanceof GeoCoordinateViewHolder) {
                ((GeoCoordinateViewHolder) holder).setData(mGeoCoordinates);
                ((GeoCoordinateViewHolder) holder).setIsAmbient(mIsAmbient);
            } else if (holder instanceof FlightInfoViewHolder) {
                ((FlightInfoViewHolder) holder).setData(mFlightInfoData);
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (isGeoCoordinateViewType(position)) {
                return GEO_COORDINATE_VIEW;
            } else if (isFlightInfoView(position)) {
                return FLIGHT_INFO_VIEW;
            } else {
                return DEFAULT_VIEW;
            }
        }

        public boolean isGeoCoordinateViewType(final int position) {
            return position == 0;
        }

        public boolean isFlightInfoView(final int position) {
            return position == 1;
        }

        public void setGeoCoordinates(final List<GeoCoordinate> coordinates) {
            Log.d(TAG, "setGeoCoordinates() called with: coordinates = [" + coordinates + "]");
            mGeoCoordinates = coordinates;
            GeoCoordinateViewHolder holder = (GeoCoordinateViewHolder) mRecyclerView
                    .findViewHolderForItemId(0);
            if (holder == null) {
                notifyItemChanged(0);
            } else {
                holder.setData(coordinates);
                holder.itemView.invalidate();
            }
        }

        public void setFlightInfoData(final FlightInfoData data) {
            Log.d(TAG, "setFlightInfoData() called with: data = [" + data + "]");
            mFlightInfoData = data;
            FlightInfoViewHolder holder = (FlightInfoViewHolder) mRecyclerView
                    .findViewHolderForAdapterPosition(1);
            if (holder == null) {
                notifyItemChanged(1);
            } else {
                holder.setData(data);
                holder.itemView.invalidate();
            }
        }

        private void setIsAmbient(final boolean isAmbient) {
            Log.d(TAG, "setIsAmbient() called with: isAmbient = [" + isAmbient + "]");
            mIsAmbient = isAmbient;
            GeoCoordinateViewHolder holder = (GeoCoordinateViewHolder) mRecyclerView
                    .findViewHolderForAdapterPosition(0);
            Log.d(TAG, "setIsAmbient: holder: " + holder);
            if (holder != null) {
                holder.mView.setIsAmbient(isAmbient);
                holder.mView.invalidate();
            }
        }
    }
}
