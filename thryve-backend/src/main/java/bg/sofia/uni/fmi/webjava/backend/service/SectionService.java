package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.SectionDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Section;
import bg.sofia.uni.fmi.webjava.backend.repository.SectionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class SectionService {

    private static final String SECTION_NOT_FOUND_MESSAGE = "Section with id %s not found";

    private final SectionRepository sectionRepository;
    private final SectionDtoMapper sectionDtoMapper;
    private final CourseService courseService;

    @Transactional
    public Page<SectionResponseDto> getSectionsByCourseId(UUID courseId, Pageable pageable) {
        return sectionRepository.findSectionsByCourseIdOrderByOrderNumber(courseId, pageable)
            .map(sectionDtoMapper::mapToResponseDto);
    }

    @Transactional
    public SectionResponseDto getSectionById(UUID id) {
        return sectionDtoMapper.mapToResponseDto(getSectionEntityById(id));
    }

    @Transactional
    protected Section getSectionEntityById(UUID id) {
        return sectionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(format(SECTION_NOT_FOUND_MESSAGE, id)));
    }

    @Transactional
    public SectionResponseDto createSectionForCourse(UUID courseId, SectionCreateDto sectionCreateDto) {
        Section section = sectionDtoMapper.mapDtoToSection(sectionCreateDto);
        Course course = courseService.getCourseEntityById(courseId);
        section.setCourse(course);
        return sectionDtoMapper.mapToResponseDto(sectionRepository.save(section));
    }

    @Transactional
    public SectionResponseDto updateSectionById(UUID id, SectionUpdateDto sectionUpdateDto) {
        Section section = getSectionEntityById(id);
        sectionDtoMapper.updateSectionFromDto(sectionUpdateDto, section);
        return sectionDtoMapper.mapToResponseDto(sectionRepository.save(section));
    }

    @Transactional
    public SectionResponseDto deleteSectionById(UUID id) {
        SectionResponseDto response = getSectionById(id);
        sectionRepository.deleteById(id);
        return response;
    }

}
