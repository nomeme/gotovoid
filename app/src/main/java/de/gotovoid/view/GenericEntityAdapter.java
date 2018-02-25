package de.gotovoid.view;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import de.gotovoid.database.AppDatabase;
import de.gotovoid.database.model.GenericEntity;

/**
 * This class is a generic implementation of the {@link RecyclerView.ViewHolder} for
 * {@link android.arch.persistence.room.Entity} of the
 * {@link AppDatabase}.
 * Should be extended for all {@link android.arch.persistence.room.Entity} implementations
 * displayed in {@link RecyclerView}s.
 * <p>
 * Created by DJ on 13/01/18.
 */
public abstract class GenericEntityAdapter<Entity extends GenericEntity,
        Holder extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<Holder> {
    private List<Entity> mEntities;

    /**
     * Set the new {@link List} of {@link Entity}s to be managed by the adapter.
     *
     * @param entities {@link Entity}s to be managed
     */
    public void setEntities(@NonNull final List<Entity> entities) {
        if (mEntities == null) {
            if (entities == null) {
                return;
            }
            mEntities = entities;
            notifyItemRangeInserted(0, mEntities.size());
        } else {
            final DiffUtil.DiffResult result = getDiffUtilResult(entities);
            mEntities = entities;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public int getItemCount() {
        if (mEntities == null) {
            return 0;
        }
        return mEntities.size();
    }

    /**
     * Returns a {@link List} of the currently managed {@link Entity}s.
     *
     * @return currently managed {@link Entity}s
     */
    protected List<Entity> getEntities() {
        return mEntities;
    }

    /**
     * Computes the {@link DiffUtil.DiffResult} for newly added
     * {@link Entity}s to the already existing {@link Entity}s.
     * Contains a generic implementation for the {@link GenericEntity}.
     *
     * @param entities newly added {@link Entity}s
     * @return the {@link DiffUtil.DiffResult}
     */
    protected DiffUtil.DiffResult getDiffUtilResult(@NonNull final List<Entity> entities) {
        return DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mEntities.size();
            }

            @Override
            public int getNewListSize() {
                return entities.size();
            }

            @Override
            public boolean areItemsTheSame(final int oldItemPosition,
                                           final int newItemPosition) {
                return mEntities.get(oldItemPosition).getId()
                        == entities.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(final int oldItemPosition,
                                              final int newItemPosition) {
                Entity newRecording = entities.get(newItemPosition);
                Entity oldRecording = mEntities.get(oldItemPosition);
                return newRecording.isContentEqual(oldRecording);
            }
        });
    }
}
