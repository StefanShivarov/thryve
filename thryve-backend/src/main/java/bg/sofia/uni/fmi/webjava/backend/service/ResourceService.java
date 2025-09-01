package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.ResourceDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Resource;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Section;
import bg.sofia.uni.fmi.webjava.backend.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class ResourceService {

    public static final String RESOURCE_NOT_FOUND = "Resource with id %s was not found!";

    private final ResourceRepository resourceRepository;
    private final ResourceDtoMapper resourceDtoMapper;
    private final SectionService sectionService;

    @Transactional
    public ResourceResponseDto createResourceForSection(UUID sectionId, ResourceCreateDto dto) {
        Section section = sectionService.getSectionEntityById(sectionId);
        Resource resource = resourceDtoMapper.mapDtoToResource(dto);
        resource.setSection(section);
        return resourceDtoMapper.mapToResponseDto(resourceRepository.save(resource));
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponseDto> listResourcesForSection(UUID sectionId, int pageNumber, int pageSize) {
        return resourceRepository.findBySectionId(sectionId, PageRequest.of(pageNumber, pageSize))
            .map(resourceDtoMapper::mapToResponseDto);
    }

    @Transactional(readOnly = true)
    public ResourceResponseDto getResourceById(UUID id) {
        Resource r = resourceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(format(RESOURCE_NOT_FOUND, id)));
        return resourceDtoMapper.mapToResponseDto(r);
    }

    @Transactional
    public ResourceResponseDto updateResourceById(UUID id, ResourceUpdateDto dto) {
        Resource resource = resourceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(format(RESOURCE_NOT_FOUND, id)));
        resourceDtoMapper.updateResourceFromDto(dto, resource);
        return resourceDtoMapper.mapToResponseDto(resourceRepository.save(resource));
    }

    @Transactional
    public ResourceResponseDto deleteResourceById(UUID id) {
        Resource r = resourceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(format(RESOURCE_NOT_FOUND, id)));
        resourceRepository.delete(r);
        return resourceDtoMapper.mapToResponseDto(r);
    }

}
