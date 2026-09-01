package ecom.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persistent User aggregate. Owns its own lifecycle concerns: timestamp population
 * ({@link #onCreate()} / {@link #onUpdate()}) and the soft-delete state transition
 * ({@link #deactivate()}).
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_phone_no", columnNames = "phone_no")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name",  length = 100)
    private String name;

    @Column(name = "phone_no",  length = 20)
    private String phoneNo;

    @Column(name = "email",  length = 255)
    private String email;

    @Column(name = "password",  length = 255)
    private String password;

    @Column(name = "created_at",  updatable = false)
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    void onCreate() {
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = new Date();
    }

    /** Soft-delete transition: marks this user inactive so it disappears from reads. */
    public void deactivate() {
        this.isActive = false;
    }
}
