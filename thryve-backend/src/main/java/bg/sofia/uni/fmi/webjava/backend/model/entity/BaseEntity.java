package bg.sofia.uni.fmi.webjava.backend.model.entity;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@MappedSuperclass
@Data
@Getter
public abstract class BaseEntity {

    @Id
    private UUID id;

    @PrePersist
    private void prePersist() {
        if (id == null) {
            id = UlidCreator.getUlid().toUuid();
        }
    }

}
