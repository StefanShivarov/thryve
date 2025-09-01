package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.SectionDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Section;
import bg.sofia.uni.fmi.webjava.backend.repository.SectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static bg.sofia.uni.fmi.webjava.backend.service.SectionService.SECTION_NOT_FOUND_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestSection;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestSectionResponseDto;
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
public class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SectionDtoMapper sectionDtoMapper;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private SectionService sectionService;

    private static final Section TEST_SECTION = createTestSection();
    private static final SectionResponseDto TEST_SECTION_RESPONSE_DTO = createTestSectionResponseDto();

    @Test
    void testGetSectionsByCourseId() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Section> expectedSections = List.of(TEST_SECTION);
        Page<Section> page = new PageImpl<>(expectedSections);

        when(sectionRepository.findSectionsByCourseIdOrderByOrderNumber(any(), eq(pageable)))
            .thenReturn(page);
        when(sectionDtoMapper.mapToResponseDto(eq(TEST_SECTION)))
            .thenReturn(TEST_SECTION_RESPONSE_DTO);

        Page<SectionResponseDto> result = sectionService
            .getSectionsByCourseId(TEST_SECTION.getCourse().getId(), pageable);

        assertNotNull(result);
        assertEquals(expectedSections.size(), result.getContent().size());
        assertTrue(result.getContent().contains(TEST_SECTION_RESPONSE_DTO));
    }

    @Test
    void testGetSectionEntityById() {
        when(sectionRepository.findById(eq(TEST_SECTION.getId()))).thenReturn(Optional.of(TEST_SECTION));
        Section result = sectionService.getSectionEntityById(TEST_SECTION.getId());
        verify(sectionRepository, times(1)).findById(eq(TEST_SECTION.getId()));
        assertEquals(TEST_SECTION, result);
    }

    @Test
    void testGetSectionEntityByIdForNonExistingSectionId() {
        when(sectionRepository.findById(eq(TEST_SECTION.getId()))).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
            () -> sectionService.getSectionEntityById(TEST_SECTION.getId()));
        assertEquals(format(SECTION_NOT_FOUND_MESSAGE, TEST_SECTION.getId()), exception.getMessage());
    }

    @Test
    void testGetSectionById() {
        when(sectionRepository.findById(eq(TEST_SECTION.getId()))).thenReturn(Optional.of(TEST_SECTION));
        when(sectionDtoMapper.mapToResponseDto(eq(TEST_SECTION))).thenReturn(TEST_SECTION_RESPONSE_DTO);

        SectionResponseDto result = sectionService.getSectionById(TEST_SECTION.getId());

        verify(sectionRepository, times(1)).findById(eq(TEST_SECTION.getId()));
        verify(sectionDtoMapper, times(1)).mapToResponseDto(eq(TEST_SECTION));
        assertEquals(TEST_SECTION_RESPONSE_DTO, result);
    }

    @Test
    void testCreateSectionForCourse() {
        when(sectionDtoMapper.mapDtoToSection(any(SectionCreateDto.class))).thenReturn(TEST_SECTION);
        when(courseService.getCourseEntityById(eq(TEST_SECTION.getCourse().getId())))
            .thenReturn(TEST_SECTION.getCourse());
        when(sectionRepository.save(eq(TEST_SECTION))).thenReturn(TEST_SECTION);
        when(sectionDtoMapper.mapToResponseDto(eq(TEST_SECTION))).thenReturn(TEST_SECTION_RESPONSE_DTO);

        SectionResponseDto result = sectionService.createSectionForCourse(TEST_SECTION.getCourse().getId(),
            new SectionCreateDto());

        assertEquals(TEST_SECTION_RESPONSE_DTO, result);
    }

    @Test
    void testUpdateSectionById() {
        when(sectionRepository.findById(eq(TEST_SECTION.getId())))
            .thenReturn(Optional.of(TEST_SECTION));
        doNothing().when(sectionDtoMapper)
                   .updateSectionFromDto(any(SectionUpdateDto.class), eq(TEST_SECTION));
        when(sectionRepository.save(eq(TEST_SECTION))).thenReturn(TEST_SECTION);
        when(sectionDtoMapper.mapToResponseDto(eq(TEST_SECTION)))
            .thenReturn(TEST_SECTION_RESPONSE_DTO);

        SectionResponseDto result = sectionService
            .updateSectionById(TEST_SECTION.getId(), new SectionUpdateDto());

        verify(sectionRepository, times(1)).findById(eq(TEST_SECTION.getId()));
        verify(sectionDtoMapper, times(1))
                   .updateSectionFromDto(any(SectionUpdateDto.class), eq(TEST_SECTION));
        verify(sectionRepository, times(1)).save(eq(TEST_SECTION));
        verify(sectionDtoMapper, times(1)).mapToResponseDto(eq(TEST_SECTION));
        assertEquals(TEST_SECTION_RESPONSE_DTO, result);
    }

    @Test
    void testDeleteSectionById() {
        when(sectionRepository.findById(eq(TEST_SECTION.getId())))
            .thenReturn(Optional.of(TEST_SECTION));
        when(sectionDtoMapper.mapToResponseDto(eq(TEST_SECTION)))
            .thenReturn(TEST_SECTION_RESPONSE_DTO);
        doNothing().when(sectionRepository).deleteById(eq(TEST_SECTION.getId()));

        SectionResponseDto result = sectionService.deleteSectionById(TEST_SECTION.getId());

        verify(sectionRepository, times(1)).findById(eq(TEST_SECTION.getId()));
        verify(sectionDtoMapper, times(1)).mapToResponseDto(eq(TEST_SECTION));
        verify(sectionRepository, times(1)).deleteById(eq(TEST_SECTION.getId()));
        assertEquals(TEST_SECTION_RESPONSE_DTO, result);
    }

}
