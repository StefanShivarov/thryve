package bg.sofia.uni.fmi.webjava.backend.mapper;

import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Section;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {ResourceDtoMapper.class})
public interface SectionDtoMapper {

    Section mapDtoToSection(SectionCreateDto sectionCreateDto);

    SectionResponseDto mapToResponseDto(Section section);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSectionFromDto(SectionUpdateDto sectionUpdateDto, @MappingTarget Section section);

}
