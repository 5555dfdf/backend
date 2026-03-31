package org.example.coursework3.service;

import lombok.RequiredArgsConstructor;
import org.example.coursework3.entity.Slot;
import org.example.coursework3.repository.slotRepository;
import org.example.coursework3.vo.SlotVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotInfoService {
    @Autowired
    private slotRepository slotRepository;

    public List<SlotVo> getSpecialistSlots(String specialistId, String date, String from, String to) {
        List<Slot> allSlots = slotRepository.findBySpecialistId(specialistId);
        LocalDate localDate = null;
        if (date != null && !date.isEmpty()){
            localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        }
        OffsetDateTime fromTime;
        if (from != null && !from.isEmpty()) {
            fromTime = OffsetDateTime.parse(from);
        } else {
            fromTime = null;
        }
        OffsetDateTime toTime;
        if (to != null && !to.isEmpty()) {
            toTime = OffsetDateTime.parse(to);
        } else {
            toTime = null;
        }
        if (date != null) {
            OffsetDateTime startOfDay = localDate.atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
            OffsetDateTime endOfDay = startOfDay.plusDays(1);
            allSlots = allSlots.stream()
                    .filter(slot -> !slot.getStartTime().isBefore(startOfDay) && slot.getStartTime().isBefore(endOfDay))
                    .toList();
        }
        if (from != null) {
            allSlots = allSlots.stream()
                    .filter(slot -> !slot.getStartTime().isBefore(fromTime))
                    .toList();
        }
        if (to != null) {
            allSlots = allSlots.stream()
                    .filter(slot -> !slot.getStartTime().isAfter(toTime))
                    .toList();
        }
        return allSlots.stream()
                .map(SlotVo::fromSlot)
                .toList();
    }

}
