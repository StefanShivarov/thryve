package bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission;

import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserResponseDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class AssignmentSubmissionResponseDto {

    private UUID id;
    private String submissionUrl;
    private String feedback;
    private String comment;
    private double grade;
    private AssignmentResponseDto assignment;
    private UserResponseDto user;

}
