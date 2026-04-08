package org.example.courework3.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RescheduleBookingRequest {
    @NotBlank(message = "slotId is required")
    private String slotId;
}
