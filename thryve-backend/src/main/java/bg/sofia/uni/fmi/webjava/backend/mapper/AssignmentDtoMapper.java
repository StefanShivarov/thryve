package bg.sofia.uni.fmi.webjava.backend.mapper;

import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Assignment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {CourseDtoMapper.class})
public interface AssignmentDtoMapper {

    Assignment mapDtoToAssignment(AssignmentCreateDto assignmentCreateDto);

    AssignmentResponseDto mapToResponseDto(Assignment assignment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAssignmentFromDto(AssignmentUpdateDto assignmentUpdateDto, @MappingTarget Assignment assignment);

}
