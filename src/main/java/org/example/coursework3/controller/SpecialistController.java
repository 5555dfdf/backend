package org.example.coursework3.controller;

import lombok.Data;
import org.apache.tomcat.util.compat.Jre21Compat;
import org.example.coursework3.dto.BookingPageResult;
import org.example.coursework3.dto.RejectRequest;
import org.example.coursework3.entity.Role;
import org.example.coursework3.entity.User;
import org.example.coursework3.result.CompleteResult;
import org.example.coursework3.result.ConfirmResult;

import org.example.coursework3.result.RejectResult;
import org.example.coursework3.result.Result;
import org.example.coursework3.service.AuthService;
import org.example.coursework3.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/specialist")
@CrossOrigin
public class SpecialistController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/booking-requests")
    public Result<BookingPageResult> getBookingRequests(@RequestHeader("Authorization") String authHeader,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(defaultValue = "1") Integer page,
                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        BookingPageResult pageResult = bookingService.getSpecialistBookings(authHeader, status, page, pageSize);
        System.out.println(pageResult);
        return Result.success(pageResult);
    }

    @PostMapping("/bookings/{id}/confirm")
    public Result<ConfirmResult> confirmBooking(@RequestHeader("Authorization") String authHeader,
                                                @PathVariable("id") String bookingId) {
        ConfirmResult confirmResult = bookingService.confirmBooking(authHeader, bookingId);
        return Result.success(confirmResult);
    }

    @PostMapping("/bookings/{id}/reject")
    public Result<RejectResult> rejectBooking(@RequestHeader("Authorization") String authHeader,
                                              @PathVariable("id") String bookingId,
                                              @RequestBody(required = false)RejectRequest rejectRequest) {
        String reason = rejectRequest != null? rejectRequest.getReason() : null;
        RejectResult rejectResult = bookingService.rejectBooking(authHeader,bookingId, reason);

        return Result.success(rejectResult);
    }
    @PostMapping("bookings/{id}/complete")
    public Result<CompleteResult> completeBooking(@RequestHeader("Authorization") String authHeader,
                                                  @PathVariable("id") String bookingId){
        CompleteResult completeResult = bookingService.completeBooking(authHeader,bookingId);
        return Result.success(completeResult);
    }
}