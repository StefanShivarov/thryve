package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Resource;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Section;
import bg.sofia.uni.fmi.webjava.backend.repository.ResourceRepository;
import bg.sofia.uni.fmi.webjava.backend.repository.SectionRepository;
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

    private static final String SECTION_NOT_FOUND = "Section with id %s was not found!";
    private static final String RESOURCE_NOT_FOUND = "Resource with id %s was not found!";

    private final ResourceRepository resourceRepository;
    private final SectionRepository sectionRepository;

    @Transactional
    public ResourceResponseDto createResourceForSection(UUID sectionId, ResourceCreateDto dto) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new EntityNotFoundException(format(SECTION_NOT_FOUND, sectionId)));

        Resource r = new Resource();
        r.setName(dto.getName());
        r.setUrl(dto.getUrl());
        r.setSection(section);

        Resource saved = resourceRepository.save(r);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponseDto> listResourcesForSection(UUID sectionId, int pageNumber, int pageSize) {
        return resourceRepository.findBySectionId(sectionId, PageRequest.of(pageNumber, pageSize))
            .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ResourceResponseDto getResourceById(UUID id) {
        Resource r = resourceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(format(RESOURCE_NOT_FOUND, id)));
        return toDto(r);
    }

    @Transactional
    public ResourceResponseDto updateResourceById(UUID id, ResourceUpdateDto dto) {
        Resource r = resourceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(format(RESOURCE_NOT_FOUND, id)));

        if (dto.getName() != null) r.setName(dto.getName());
        if (dto.getUrl() != null) r.setUrl(dto.getUrl());

        return toDto(resourceRepository.save(r));
    }

    @Transactional
    public ResourceResponseDto deleteResourceById(UUID id) {
        Resource r = resourceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(format(RESOURCE_NOT_FOUND, id)));
        resourceRepository.delete(r);
        return toDto(r);
    }

    private ResourceResponseDto toDto(Resource r) {
        ResourceResponseDto dto = new ResourceResponseDto();
        dto.setId(r.getId());
        dto.setName(r.getName());
        dto.setUrl(r.getUrl());
        return dto;
    }
}
