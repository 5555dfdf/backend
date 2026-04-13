package org.example.coursework3.controller;

import lombok.RequiredArgsConstructor;
import org.example.coursework3.dto.request.CreateBookingRequest;
import org.example.coursework3.dto.response.CreateBookingResult;
import org.example.coursework3.result.Result;
import org.example.coursework3.service.AuthService;
import org.example.coursework3.service.CustomerBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    @Autowired
    private CustomerBookingService bookingService;
    @Autowired
    private AuthService authService;

    @PostMapping
    public Result<CreateBookingResult> createBooking(@RequestHeader("Authorization") String authHeader, @RequestBody CreateBookingRequest request){
        if (!authService.verifyAsCustomer(authHeader)) {
            return Result.error("ERROR", "请以顾客身份修改");
        }
        return Result.success(bookingService.creatBooking(authService.getUserIdByAuth(authHeader), request));

    }
}
