package com.github.danilkozyrev.filestorageapi.persistence;

import com.github.danilkozyrev.filestorageapi.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface FileRepository extends JpaRepository<File, Long> {

    @Query("SELECT COALESCE(SUM(f.size), 0) FROM File f WHERE f.owner.id = :ownerId")
    long calculateTotalFileSizeByOwnerId(@Param("ownerId") Long ownerId);

    @Query(
            value = "SELECT * FROM files WHERE parent_id IN (" +
                    "WITH RECURSIVE tree AS (" +
                    "SELECT id, parent_id FROM folders WHERE id IN :parentsIdSet " +
                    "UNION ALL " +
                    "SELECT tb.id, tb.parent_id FROM folders AS tb " +
                    "JOIN tree ON tree.id = tb.parent_id) " +
                    "SELECT id FROM tree);",
            nativeQuery = true)
    List<File> deepFindAllFilesByParentIdIn(@Param("parentsIdSet") Set<Long> parentIdSet);

    @Query("SELECT f FROM File f WHERE f.owner.id = :ownerId AND f.parent IS NULL")
    List<File> findDisconnectedFilesByOwnerId(@Param("ownerId") Long ownerId);

    List<File> findFilesByDateModifiedAfterAndOwnerId(Instant date, Long ownerId);

}
