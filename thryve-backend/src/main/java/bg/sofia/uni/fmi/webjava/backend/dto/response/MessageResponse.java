package bg.sofia.uni.fmi.webjava.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public class MessageResponse {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss");
    private final String message;
    private final String timestamp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final List<String> errors;

    public MessageResponse(String message) {
        this.message = message;
        this.timestamp = OffsetDateTime.now().format(DATE_TIME_FORMATTER);
        this.errors = null;
    }

    public MessageResponse(String message, List<String> errors) {
        this.message = message;
        this.timestamp = OffsetDateTime.now().format(DATE_TIME_FORMATTER);
        this.errors = errors;
    }

}