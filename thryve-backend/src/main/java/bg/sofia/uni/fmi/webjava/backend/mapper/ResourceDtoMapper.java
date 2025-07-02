package bg.sofia.uni.fmi.webjava.backend.mapper;

import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Resource;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ResourceDtoMapper {

    Resource mapDtoToResource(ResourceCreateDto resourceCreateDto);

    ResourceResponseDto mapToResponseDto(Resource resource);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    void updateResourceFromDto(ResourceUpdateDto resourceUpdateDto, @MappingTarget Resource resource);

}
