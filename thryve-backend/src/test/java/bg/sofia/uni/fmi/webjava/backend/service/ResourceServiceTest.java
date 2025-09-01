package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.ResourceDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Resource;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Section;
import bg.sofia.uni.fmi.webjava.backend.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static bg.sofia.uni.fmi.webjava.backend.service.ResourceService.RESOURCE_NOT_FOUND;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestResource;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestResourceResponseDto;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ResourceDtoMapper resourceDtoMapper;

    @Mock
    private SectionService sectionService;

    @InjectMocks
    private ResourceService resourceService;

    private static final Resource TEST_RESOURCE = createTestResource();
    private static final ResourceResponseDto
        TEST_RESOURCE_RESPONSE_DTO = createTestResourceResponseDto();
    private static final Section TEST_SECTION = TEST_RESOURCE.getSection();
    private static final UUID RESOURCE_ID = TEST_RESOURCE.getId();
    private static final UUID SECTION_ID = TEST_RESOURCE.getSection().getId();

    @Test
    void testCreateResourceForSection() {
        ResourceCreateDto createDto = new ResourceCreateDto();
        when(sectionService.getSectionEntityById(eq(SECTION_ID))).thenReturn(TEST_SECTION);
        when(resourceDtoMapper.mapDtoToResource(eq(createDto))).thenReturn(TEST_RESOURCE);
        when(resourceRepository.save(eq(TEST_RESOURCE))).thenReturn(TEST_RESOURCE);
        when(resourceDtoMapper.mapToResponseDto(eq(TEST_RESOURCE))).thenReturn(TEST_RESOURCE_RESPONSE_DTO);

        ResourceResponseDto result = resourceService.createResourceForSection(SECTION_ID, createDto);

        verify(sectionService, times(1)).getSectionEntityById(eq(SECTION_ID));
        verify(resourceDtoMapper, times(1)).mapDtoToResource(eq(createDto));
        verify(resourceRepository, times(1)).save(eq(TEST_RESOURCE));
        verify(resourceDtoMapper, times(1)).mapToResponseDto(eq(TEST_RESOURCE));
        assertEquals(TEST_RESOURCE_RESPONSE_DTO, result);
    }

    @Test
    void testListResourcesForSection() {
        int pageNumber = 0, pageSize = 10;
        Page<Resource> page = new PageImpl<>(List.of(TEST_RESOURCE));
        when(resourceRepository.findBySectionId(eq(SECTION_ID), any())).thenReturn(page);
        when(resourceDtoMapper.mapToResponseDto(eq(TEST_RESOURCE))).thenReturn(TEST_RESOURCE_RESPONSE_DTO);

        Page<ResourceResponseDto> result = resourceService.listResourcesForSection(SECTION_ID, pageNumber, pageSize);

        verify(resourceRepository, times(1)).findBySectionId(eq(SECTION_ID), any());
        verify(resourceDtoMapper, times(1)).mapToResponseDto(eq(TEST_RESOURCE));
        assertNotNull(result);
        assertTrue(result.getContent().contains(TEST_RESOURCE_RESPONSE_DTO));
    }

    @Test
    void testGetResourceById() {
        when(resourceRepository.findById(eq(RESOURCE_ID))).thenReturn(Optional.of(TEST_RESOURCE));
        when(resourceDtoMapper.mapToResponseDto(eq(TEST_RESOURCE))).thenReturn(TEST_RESOURCE_RESPONSE_DTO);

        ResourceResponseDto result = resourceService.getResourceById(RESOURCE_ID);

        verify(resourceRepository, times(1)).findById(eq(RESOURCE_ID));
        verify(resourceDtoMapper, times(1)).mapToResponseDto(eq(TEST_RESOURCE));
        assertEquals(TEST_RESOURCE_RESPONSE_DTO, result);
    }

    @Test
    void testGetResourceByIdNotFound() {
        when(resourceRepository.findById(eq(RESOURCE_ID))).thenReturn(Optional.empty());
        Exception ex = assertThrows(EntityNotFoundException.class,
            () -> resourceService.getResourceById(RESOURCE_ID));
        assertTrue(ex.getMessage().contains(RESOURCE_ID.toString()));
    }

    @Test
    void testUpdateResourceById() {
        ResourceUpdateDto updateDto = new ResourceUpdateDto();
        when(resourceRepository.findById(eq(RESOURCE_ID))).thenReturn(Optional.of(TEST_RESOURCE));
        doNothing().when(resourceDtoMapper).updateResourceFromDto(eq(updateDto), eq(TEST_RESOURCE));
        when(resourceRepository.save(eq(TEST_RESOURCE))).thenReturn(TEST_RESOURCE);
        when(resourceDtoMapper.mapToResponseDto(eq(TEST_RESOURCE))).thenReturn(TEST_RESOURCE_RESPONSE_DTO);

        ResourceResponseDto result = resourceService.updateResourceById(RESOURCE_ID, updateDto);

        verify(resourceRepository, times(1)).findById(eq(RESOURCE_ID));
        verify(resourceDtoMapper, times(1)).updateResourceFromDto(eq(updateDto), eq(TEST_RESOURCE));
        verify(resourceRepository, times(1)).save(eq(TEST_RESOURCE));
        verify(resourceDtoMapper, times(1)).mapToResponseDto(eq(TEST_RESOURCE));
        assertEquals(TEST_RESOURCE_RESPONSE_DTO, result);
    }

    @Test
    void testUpdateResourceByIdNotFound() {
        ResourceUpdateDto updateDto = new ResourceUpdateDto();
        when(resourceRepository.findById(eq(RESOURCE_ID))).thenReturn(Optional.empty());
        Exception ex = assertThrows(EntityNotFoundException.class,
            () -> resourceService.updateResourceById(RESOURCE_ID, updateDto));
        assertTrue(ex.getMessage().contains(RESOURCE_ID.toString()));
    }

    @Test
    void testDeleteResourceById() {
        when(resourceRepository.findById(eq(RESOURCE_ID))).thenReturn(Optional.of(TEST_RESOURCE));
        when(resourceDtoMapper.mapToResponseDto(eq(TEST_RESOURCE))).thenReturn(TEST_RESOURCE_RESPONSE_DTO);

        ResourceResponseDto result = resourceService.deleteResourceById(RESOURCE_ID);

        verify(resourceRepository, times(1)).findById(eq(RESOURCE_ID));
        verify(resourceRepository, times(1)).delete(eq(TEST_RESOURCE));
        verify(resourceDtoMapper, times(1)).mapToResponseDto(eq(TEST_RESOURCE));
        assertEquals(TEST_RESOURCE_RESPONSE_DTO, result);
    }

    @Test
    void testDeleteResourceByIdNotFound() {
        when(resourceRepository.findById(eq(RESOURCE_ID))).thenReturn(Optional.empty());
        Exception ex = assertThrows(EntityNotFoundException.class,
            () -> resourceService.deleteResourceById(RESOURCE_ID));
        assertEquals(format(RESOURCE_NOT_FOUND, RESOURCE_ID), ex.getMessage());
    }

}
