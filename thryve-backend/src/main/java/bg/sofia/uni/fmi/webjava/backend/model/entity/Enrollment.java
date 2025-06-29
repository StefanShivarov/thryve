package bg.sofia.uni.fmi.webjava.backend.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Enrollment extends BaseEntity {

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EnrollmentType enrollmentType;

    @ManyToOne
    private User user;

    @ManyToOne
    private Course course;
}
