package bg.sofia.uni.fmi.webjava.backend.mapper;

import bg.sofia.uni.fmi.webjava.backend.model.dto.NotificationResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserDtoMapper.class, CourseDtoMapper.class})
public interface NotificationDtoMapper {

    NotificationResponseDto mapToResponseDto(Notification notification);

}
