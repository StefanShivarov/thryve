package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResourceController {

    private static final String RESOURCE_CREATED_MESSAGE = "Resource created successfully!";
    private static final String RESOURCE_UPDATED_MESSAGE = "Resource updated successfully!";
    private static final String RESOURCE_DELETED_MESSAGE = "Resource deleted successfully!";

    private final ResourceService resourceService;

    @PostMapping("/sections/{sectionId}/resources")
    public ResponseEntity<EntityModificationResponse<ResourceResponseDto>> createResourceForSection(
        @PathVariable("sectionId") UUID sectionId,
        @RequestBody @Valid ResourceCreateDto resourceCreateDto
    ) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(RESOURCE_CREATED_MESSAGE,
                resourceService.createResourceForSection(sectionId, resourceCreateDto))
        );
    }

    @GetMapping("/resources/{id}")
    public ResponseEntity<ResourceResponseDto> getResourceById(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    @PatchMapping("/resources/{id}")
    public ResponseEntity<EntityModificationResponse<ResourceResponseDto>> updateResourceById(
        @PathVariable UUID id,
        @RequestBody @Valid ResourceUpdateDto resourceUpdateDto
    ) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(RESOURCE_UPDATED_MESSAGE,
                resourceService.updateResourceById(id, resourceUpdateDto))
        );
    }

    @DeleteMapping("/resources/{id}")
    public ResponseEntity<EntityModificationResponse<ResourceResponseDto>> deleteResourceById(@PathVariable UUID id) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(RESOURCE_DELETED_MESSAGE,
                resourceService.deleteResourceById(id))
        );
    }

}
