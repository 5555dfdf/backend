package org.example.coursework3.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBookingRequest {
    @NotBlank(message = "specialistId is required")
    private String specialistId;

    @NotBlank(message = "slotId is required")
    private String slotId;

    private String note;
}
