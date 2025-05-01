package bg.sofia.uni.fmi.webjava.backend.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "assignments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Assignment extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @ManyToOne
    private Course course;

    @OneToMany(mappedBy = "assignment")
    private Set<AssignmentSubmission> submissions;

}
