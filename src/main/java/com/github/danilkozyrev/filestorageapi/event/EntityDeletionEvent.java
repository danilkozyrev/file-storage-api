package com.github.danilkozyrev.filestorageapi.event;

import com.github.danilkozyrev.filestorageapi.domain.BaseEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event raised when entities are deleted.
 *
 * @param <T> type of entities in the payload.
 */
@Getter
public class EntityDeletionEvent<T extends BaseEntity> extends ApplicationEvent {

    private final Iterable<T> deletedEntities;

    public EntityDeletionEvent(Object source, Iterable<T> deletedEntities) {
        super(source);
        this.deletedEntities = deletedEntities;
    }

}
