package bg.sofia.uni.fmi.webjava.backend.model.dto.section;

import bg.sofia.uni.fmi.webjava.backend.model.dto.resource.ResourceResponseDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class SectionResponseDto {

    private UUID id;
    private String title;
    private String textContent;
    private int orderNumber;
    private List<ResourceResponseDto> resources = new ArrayList<>();

}
