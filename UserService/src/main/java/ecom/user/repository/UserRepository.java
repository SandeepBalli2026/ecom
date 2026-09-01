package ecom.user.repository;

import ecom.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link User}. Lookup methods are scoped to active users
 * so that soft-deleted rows are invisible to the application.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhoneNo(String phoneNo);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhoneNoAndIdNot(String phoneNo, Long id);

    Optional<User> findByIdAndIsActiveTrue(Long id);

    List<User> findAllByIsActiveTrue();
}
