package bg.sofia.uni.fmi.webjava.backend.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Notification extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "text_content", columnDefinition = "TEXT", nullable = false)
    private String textContent;

    @ManyToOne
    private User sender;

    @ManyToOne
    private Course course;

}
