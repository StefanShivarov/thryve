package bg.sofia.uni.fmi.webjava.backend.mapper;

import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionGradeDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.AssignmentSubmission;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {UserDtoMapper.class, AssignmentDtoMapper.class})
public interface AssignmentSubmissionDtoMapper {

    AssignmentSubmission mapDtoToAssignmentSubmission(AssignmentSubmissionCreateDto assignmentSubmissionCreateDto);

    AssignmentSubmissionResponseDto mapToResponseDto(AssignmentSubmission assignmentSubmission);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAssignmentSubmissionFromDto(AssignmentSubmissionUpdateDto updateDto,
                                           @MappingTarget AssignmentSubmission assignmentSubmission);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void gradeAssignmentSubmissionFromDto(AssignmentSubmissionGradeDto updateDto,
                                          @MappingTarget AssignmentSubmission assignmentSubmission);

}
