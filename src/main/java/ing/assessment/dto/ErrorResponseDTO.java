package ing.assessment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        int status,
        String error,
        String message
) {}
