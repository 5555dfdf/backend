package org.example.coursework3.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminSlotVo {
    private String id;
    private String specialistId;
    private String date;
    private String start;
    private String end;
    private Boolean available;
    private BigDecimal amount;
    private String currency;
    private Integer duration;
    private String type;
    private String detail;
}
