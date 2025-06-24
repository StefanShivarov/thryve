package bg.sofia.uni.fmi.webjava.backend.model.dto;

import lombok.Getter;

@Getter
public class EntityModificationResponse<T> extends MessageResponse {

    private final T data;

    public EntityModificationResponse(String message, T data) {
        super(message);
        this.data = data;
    }

}
