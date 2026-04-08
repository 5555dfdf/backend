package org.example.courework3.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookingListResponse {
    private List<BookingResponse> items;
    private int total;
}
