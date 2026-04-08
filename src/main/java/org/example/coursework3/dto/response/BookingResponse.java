package org.example.courework3.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingResponse {
    private String id;
    private String customerId;
    private String specialistId;
    private String specialistName;
    private String slotId;
    private String time;
    private String startTime;
    private String note;
    private String status;
    private String createdAt;
    private String updatedAt;
}
