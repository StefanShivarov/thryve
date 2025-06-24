package bg.sofia.uni.fmi.webjava.backend.model.dto.course;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CoursePreviewDto {

    private UUID id;
    private String title;
    private String imageUrl;

}
