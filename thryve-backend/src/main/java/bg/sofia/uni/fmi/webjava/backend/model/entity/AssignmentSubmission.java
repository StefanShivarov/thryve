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
@Table(name = "assignment_submissions")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AssignmentSubmission extends BaseEntity {

    @Column(name = "submission_url", unique = true)
    private String submissionUrl;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "feedback")
    private String feedback;

    @Column(name = "grade")
    private double grade;

    @ManyToOne
    private Assignment assignment;

    @ManyToOne
    private User student;

}
