package de.gotovoid.view;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
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

import de.gotovoid.R;
import de.gotovoid.database.model.Recording;
import de.gotovoid.database.model.RecordingEntry;
import de.gotovoid.databinding.RecorderFragmentBinding;
import de.gotovoid.domain.model.geodata.GeoCoordinate;
import de.gotovoid.service.repository.LocationRepository;
import de.gotovoid.view.binding.FlightInfoData;
import de.gotovoid.view.model.RecorderViewModel;

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
    public void onCreate(@Nullable final Bundle savedInstanceState) {
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
        RecorderFragmentBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.recorder_fragment, container, false);
        // Add the back action.
        mDismissLayout = binding.dismissLayout;
        mDismissLayout.addCallback(new SwipeDismissFrameLayout.Callback() {
            @Override
            public void onDismissed(final SwipeDismissFrameLayout layout) {
                getFragmentManager().popBackStack();
                mDismissLayout.setVisibility(View.GONE);
            }
        });

        mRecyclerView = binding.recyclerView;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.requestFocus();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new RecorderAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mViewModel.getEntries().observe(this, (recordingEntries) -> {
            Log.d(TAG, "onChanged() called with: recordingEntries = ["
                    + recordingEntries + "]");
            final int size = recordingEntries.size();
            if (size < 2) {
                return;
            }
            final RecordingEntry lastEntry = recordingEntries.get(size - 1);
            final RecordingEntry previousEntry = recordingEntries.get(size - 2);
            // TODO move this to async model code!
            final FlightInfoData flightInfoData = new FlightInfoData(previousEntry, lastEntry);
            mAdapter.setFlightInfoData(flightInfoData);
            List<GeoCoordinate> coordinates = new ArrayList<>();
            for (final RecordingEntry entry : recordingEntries) {
                coordinates.add(new GeoCoordinate(entry.getLatitude(), entry.getLongitude()));
            }
            mAdapter.setGeoCoordinates(coordinates);
        });

        mViewModel.getState().observe(this, (state) -> binding.calibrating.setState(state));
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
        mViewModel.startRecording(Recording.Type.HIKE);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        mViewModel.stopRecording();
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
                mView.setSpeed(data.getGroundSpeed());
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
                    .findViewHolderForAdapterPosition(0);
            if (holder == null) {
                Log.d(TAG, "setGeoCoordinates: holder is null");
                notifyItemChanged(0);
            } else {
                Log.d(TAG, "setGeoCoordinates: holder is !null");
                holder.setData(coordinates);
                holder.mView.invalidate();
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
                holder.mView.invalidate();
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
