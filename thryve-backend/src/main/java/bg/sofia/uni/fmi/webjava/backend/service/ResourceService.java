package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.ResourceDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Resource;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Section;
import bg.sofia.uni.fmi.webjava.backend.repository.ResourceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private static final String RESOURCE_NOT_FOUND_MESSAGE = "Resource with id %s not found!";

    private final ResourceRepository resourceRepository;
    private final ResourceDtoMapper resourceDtoMapper;
    private final SectionService sectionService;

    @Transactional
    public ResourceResponseDto createResourceForSection(UUID sectionId, ResourceCreateDto resourceCreateDto) {
        Section section = sectionService.getSectionEntityById(sectionId);
        Resource resource = resourceDtoMapper.mapDtoToResource(resourceCreateDto);
        resource.setSection(section);
        return resourceDtoMapper.mapToResponseDto(resourceRepository.save(resource));
    }

    @Transactional
    public ResourceResponseDto getResourceById(UUID id) {
        return resourceDtoMapper.mapToResponseDto(getResourceEntityById(id));
    }

    @Transactional
    protected Resource getResourceEntityById(UUID id) {
        return resourceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(format(RESOURCE_NOT_FOUND_MESSAGE, id)));
    }

    @Transactional
    public ResourceResponseDto updateResourceById(UUID id, ResourceUpdateDto resourceUpdateDto) {
        Resource resource = getResourceEntityById(id);
        resourceDtoMapper.updateResourceFromDto(resourceUpdateDto, resource);
        return resourceDtoMapper.mapToResponseDto(resourceRepository.save(resource));
    }

    @Transactional
    public ResourceResponseDto deleteResourceById(UUID id) {
        ResourceResponseDto response = getResourceById(id);
        resourceRepository.deleteById(id);
        return response;
    }

}
