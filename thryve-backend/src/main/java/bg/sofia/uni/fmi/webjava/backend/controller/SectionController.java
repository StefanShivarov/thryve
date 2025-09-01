package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.section.SectionUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.service.SectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SectionController {

    public static final String SECTION_CREATED_MESSAGE = "Section created successfully!";
    public static final String SECTION_UPDATED_MESSAGE = "Section updated successfully!";
    public static final String SECTION_DELETED_MESSAGE = "Section deleted successfully!";

    private final SectionService sectionService;

    @PreAuthorize("hasAnyRole('STANDARD', 'CREATOR', 'ADMIN')")
    @GetMapping("/courses/{courseId}/sections")
    public ResponseEntity<Page<SectionResponseDto>> getSectionsByCourseId(
        @PathVariable("courseId") UUID courseId,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.ASC);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(sectionService.getSectionsByCourseId(courseId, pageable));
    }

    @PreAuthorize("hasAnyRole('STANDARD', 'CREATOR', 'ADMIN')")
    @GetMapping("/sections/{id}")
    public ResponseEntity<SectionResponseDto> getSectionById(@PathVariable UUID id) {
        return ResponseEntity.ok(sectionService.getSectionById(id));
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PostMapping("/courses/{courseId}/sections")
    public ResponseEntity<EntityModificationResponse<SectionResponseDto>> createSectionForCourse(
        @PathVariable("courseId") UUID courseId,
        @RequestBody @Valid SectionCreateDto sectionCreateDto
    ) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(SECTION_CREATED_MESSAGE,
                sectionService.createSectionForCourse(courseId, sectionCreateDto))
        );
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PatchMapping("/sections/{id}")
    public ResponseEntity<EntityModificationResponse<SectionResponseDto>> updateSectionById(
        @PathVariable UUID id,
        @RequestBody @Valid SectionUpdateDto sectionUpdateDto) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(SECTION_UPDATED_MESSAGE,
                sectionService.updateSectionById(id, sectionUpdateDto))
            );
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @DeleteMapping("/sections/{id}")
    public ResponseEntity<EntityModificationResponse<SectionResponseDto>> deleteSectionById(@PathVariable UUID id) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(SECTION_DELETED_MESSAGE, sectionService.deleteSectionById(id))
        );
    }

}
