package com.github.danilkozyrev.filestorageapi.persistence;

import com.github.danilkozyrev.filestorageapi.domain.User;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findUserByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u")
    @EntityGraph(attributePaths = "roles")
    List<User> findAllJoinRoles();

}
