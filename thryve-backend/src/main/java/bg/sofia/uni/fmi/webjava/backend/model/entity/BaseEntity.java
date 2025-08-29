package bg.sofia.uni.fmi.webjava.backend.model.entity;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Data
@Getter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime modifiedAt;

    @PrePersist
    private void prePersist() {
        id = UlidCreator.getUlid().toUuid();
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        modifiedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        modifiedAt = LocalDateTime.now();
    }

}
