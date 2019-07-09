package com.github.danilkozyrev.filestorageapi.event;

import com.github.danilkozyrev.filestorageapi.domain.BaseEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event raised when entities are created.
 *
 * @param <T> type of entities in the payload.
 */
@Getter
public class EntityCreationEvent<T extends BaseEntity> extends ApplicationEvent {

    private final Iterable<T> createdEntities;

    public EntityCreationEvent(Object source, Iterable<T> createdEntities) {
        super(source);
        this.createdEntities = createdEntities;
    }

}
