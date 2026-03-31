package org.example.coursework3.controller;

import org.example.coursework3.result.Result;
import org.example.coursework3.service.SlotInfoService;
import org.example.coursework3.vo.SlotVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.example.coursework3.repository.slotRepository;

import java.util.List;

@RestController
@RequestMapping("/specialists")
@CrossOrigin
public class SpecialistsController {
    @Autowired
    private SlotInfoService slotInfoService;

    @GetMapping("/{id}/slots")
    public Result<List<SlotVo>> getSpecialistSlots(@PathVariable("id") String specialistId,
                                                   @RequestParam(required = false) String date,
                                                   @RequestParam(required = false) String from,
                                                   @RequestParam(required = false) String to){


        List<SlotVo> slots = slotInfoService.getSpecialistSlots(specialistId,date,from,to);
        return Result.success(slots);
    }
}
