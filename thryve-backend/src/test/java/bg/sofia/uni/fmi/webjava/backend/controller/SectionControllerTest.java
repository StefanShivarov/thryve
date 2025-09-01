package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.service.SectionService;
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

import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestSectionResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SectionControllerTest {

    @Mock
    private SectionService sectionService;

    @InjectMocks
    private SectionController sectionController;

    private static final SectionResponseDto TEST_SECTION_RESPONSE_DTO = createTestSectionResponseDto();
    private static final UUID SECTION_ID = TEST_SECTION_RESPONSE_DTO.getId();
    private static final UUID COURSE_ID = UUID.randomUUID();

    @Test
    void testGetSectionsByCourseId() {
        Page<SectionResponseDto> page = new PageImpl<>(List.of(TEST_SECTION_RESPONSE_DTO));
        when(sectionService.getSectionsByCourseId(eq(COURSE_ID), any())).thenReturn(page);

        ResponseEntity<Page<SectionResponseDto>> response = sectionController
            .getSectionsByCourseId(COURSE_ID, 0, 10, "id", "ASC");

        assertEquals(page, response.getBody());
        verify(sectionService).getSectionsByCourseId(eq(COURSE_ID), any());
    }

    @Test
    void testGetSectionById() {
        when(sectionService.getSectionById(eq(SECTION_ID))).thenReturn(TEST_SECTION_RESPONSE_DTO);

        ResponseEntity<SectionResponseDto> response = sectionController.getSectionById(SECTION_ID);

        assertEquals(TEST_SECTION_RESPONSE_DTO, response.getBody());
        verify(sectionService).getSectionById(eq(SECTION_ID));
    }

    @Test
    void testCreateSectionForCourse() {
        SectionCreateDto createDto = new SectionCreateDto();
        createDto.setTitle(TEST_SECTION_RESPONSE_DTO.getTitle());
        createDto.setTextContent(TEST_SECTION_RESPONSE_DTO.getTextContent());
        createDto.setOrderNumber(TEST_SECTION_RESPONSE_DTO.getOrderNumber());

        when(sectionService.createSectionForCourse(eq(COURSE_ID), eq(createDto)))
            .thenReturn(TEST_SECTION_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<SectionResponseDto>> response = sectionController
            .createSectionForCourse(COURSE_ID, createDto);

        assertNotNull(response.getBody());
        assertEquals(SectionController.SECTION_CREATED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_SECTION_RESPONSE_DTO, response.getBody().getData());
        verify(sectionService).createSectionForCourse(eq(COURSE_ID), eq(createDto));
    }

    @Test
    void testUpdateSectionById() {
        SectionUpdateDto updateDto = new SectionUpdateDto();
        when(sectionService.updateSectionById(eq(SECTION_ID), eq(updateDto)))
            .thenReturn(TEST_SECTION_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<SectionResponseDto>> response = sectionController
            .updateSectionById(SECTION_ID, updateDto);

        assertNotNull(response.getBody());
        assertEquals(SectionController.SECTION_UPDATED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_SECTION_RESPONSE_DTO, response.getBody().getData());
        verify(sectionService).updateSectionById(eq(SECTION_ID), eq(updateDto));
    }

    @Test
    void testDeleteSectionById() {
        when(sectionService.deleteSectionById(eq(SECTION_ID))).thenReturn(TEST_SECTION_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<SectionResponseDto>> response = sectionController
            .deleteSectionById(SECTION_ID);

        assertNotNull(response.getBody());
        assertEquals(SectionController.SECTION_DELETED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_SECTION_RESPONSE_DTO, response.getBody().getData());
        verify(sectionService).deleteSectionById(eq(SECTION_ID));
    }

}
