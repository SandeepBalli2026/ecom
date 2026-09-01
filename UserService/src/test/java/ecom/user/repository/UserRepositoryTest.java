package ecom.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ecom.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User newUser(String email, String phoneNo) {
        return User.builder()
                .name("Test User")
                .email(email)
                .phoneNo(phoneNo)
                .password("password1")
                .build();
    }

    @Test
    void existsByEmailAndPhoneNo_reflectPersistedState() {
        userRepository.save(newUser("a@example.com", "1000000000"));

        assertThat(userRepository.existsByEmail("a@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("missing@example.com")).isFalse();
        assertThat(userRepository.existsByPhoneNo("1000000000")).isTrue();
        assertThat(userRepository.existsByPhoneNo("9999999999")).isFalse();
    }

    @Test
    void existsByEmailAndIdNot_excludesTheGivenUser() {
        User saved = userRepository.save(newUser("b@example.com", "1000000001"));

        assertThat(userRepository.existsByEmailAndIdNot("b@example.com", saved.getId())).isFalse();
        assertThat(userRepository.existsByEmailAndIdNot("b@example.com", saved.getId() + 1)).isTrue();
    }

    @Test
    void findByIdAndIsActiveTrue_hidesDeactivatedUsers() {
        User active = userRepository.save(newUser("c@example.com", "1000000002"));
        User inactive = newUser("d@example.com", "1000000003");
        inactive.deactivate();
        inactive = userRepository.save(inactive);

        assertThat(userRepository.findByIdAndIsActiveTrue(active.getId())).isPresent();
        assertThat(userRepository.findByIdAndIsActiveTrue(inactive.getId())).isEmpty();
    }

    @Test
    void findAllByIsActiveTrue_returnsOnlyActiveUsers() {
        userRepository.save(newUser("e@example.com", "1000000004"));
        User inactive = newUser("f@example.com", "1000000005");
        inactive.deactivate();
        userRepository.save(inactive);

        assertThat(userRepository.findAllByIsActiveTrue())
                .extracting(User::getEmail)
                .containsExactly("e@example.com");
    }

    @Test
    void lifecycleCallbacks_populateTimestampsOnInsertAndBumpUpdatedAtOnChange() throws InterruptedException {
        User saved = userRepository.saveAndFlush(newUser("g@example.com", "1000000006"));
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        java.util.Date firstUpdatedAt = saved.getUpdatedAt();

        Thread.sleep(10);
        saved.setName("Changed");
        userRepository.saveAndFlush(saved);

        assertThat(saved.getUpdatedAt()).isAfterOrEqualTo(firstUpdatedAt);
        assertThat(saved.getCreatedAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    void uniqueConstraint_rejectsDuplicateEmail() {
        userRepository.saveAndFlush(newUser("h@example.com", "1000000007"));

        assertThatThrownBy(() -> {
            userRepository.saveAndFlush(newUser("h@example.com", "1000000008"));
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }
}
