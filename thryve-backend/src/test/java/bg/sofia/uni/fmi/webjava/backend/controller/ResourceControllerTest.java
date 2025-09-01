package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestResourceResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceControllerTest {

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private ResourceController resourceController;

    private static final ResourceResponseDto TEST_RESOURCE_RESPONSE_DTO = createTestResourceResponseDto();
    private static final UUID RESOURCE_ID = TEST_RESOURCE_RESPONSE_DTO.getId();
    private static final UUID SECTION_ID = UUID.randomUUID();

    @Test
    void testCreateResourceForSection() {
        ResourceCreateDto createDto = new ResourceCreateDto(
            TEST_RESOURCE_RESPONSE_DTO.getName(),
            TEST_RESOURCE_RESPONSE_DTO.getUrl()
        );

        when(resourceService.createResourceForSection(eq(SECTION_ID), eq(createDto)))
            .thenReturn(TEST_RESOURCE_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<ResourceResponseDto>> response = resourceController
            .createResourceForSection(SECTION_ID, createDto);

        assertNotNull(response.getBody());
        assertEquals(ResourceController.RESOURCE_CREATED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_RESOURCE_RESPONSE_DTO, response.getBody().getData());
        verify(resourceService).createResourceForSection(eq(SECTION_ID), eq(createDto));
    }

    @Test
    void testListResourcesForSection() {
        Page<ResourceResponseDto> page = new PageImpl<>(List.of(TEST_RESOURCE_RESPONSE_DTO));
        when(resourceService.listResourcesForSection(eq(SECTION_ID), eq(0), eq(50)))
            .thenReturn(page);

        ResponseEntity<Page<ResourceResponseDto>> response = resourceController
            .listResourcesForSection(SECTION_ID, 0, 50);

        assertEquals(page, response.getBody());
        verify(resourceService).listResourcesForSection(eq(SECTION_ID), eq(0), eq(50));
    }

    @Test
    void testGetResourceById() {
        when(resourceService.getResourceById(eq(RESOURCE_ID))).thenReturn(TEST_RESOURCE_RESPONSE_DTO);

        ResponseEntity<ResourceResponseDto> response = resourceController.getResourceById(RESOURCE_ID);

        assertEquals(TEST_RESOURCE_RESPONSE_DTO, response.getBody());
        verify(resourceService).getResourceById(eq(RESOURCE_ID));
    }

    @Test
    void testUpdateResourceById() {
        ResourceUpdateDto updateDto = new ResourceUpdateDto();
        when(resourceService.updateResourceById(eq(RESOURCE_ID), eq(updateDto)))
            .thenReturn(TEST_RESOURCE_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<ResourceResponseDto>> response = resourceController
            .updateResourceById(RESOURCE_ID, updateDto);

        assertNotNull(response.getBody());
        assertEquals(ResourceController.RESOURCE_UPDATED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_RESOURCE_RESPONSE_DTO, response.getBody().getData());
        verify(resourceService).updateResourceById(eq(RESOURCE_ID), eq(updateDto));
    }

    @Test
    void testDeleteResourceById() {
        when(resourceService.deleteResourceById(eq(RESOURCE_ID))).thenReturn(TEST_RESOURCE_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<ResourceResponseDto>> response = resourceController
            .deleteResourceById(RESOURCE_ID);

        assertNotNull(response.getBody());
        assertEquals(ResourceController.RESOURCE_DELETED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_RESOURCE_RESPONSE_DTO, response.getBody().getData());
        verify(resourceService).deleteResourceById(eq(RESOURCE_ID));
    }

}
