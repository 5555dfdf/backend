package org.example.coursework3.dto;

import lombok.Data;

@Data
public class CreateBookingRequest {
    private String specialistId;
    private String slotId;
    private String note;
}
