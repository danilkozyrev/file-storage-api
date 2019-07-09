package com.github.danilkozyrev.filestorageapi.persistence;

import com.github.danilkozyrev.filestorageapi.domain.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
}
