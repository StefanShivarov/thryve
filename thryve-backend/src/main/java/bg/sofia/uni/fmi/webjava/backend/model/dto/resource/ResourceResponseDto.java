package bg.sofia.uni.fmi.webjava.backend.model.dto.resource;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ResourceResponseDto {

    private UUID id;
    private String name;
    private String url;

}
