package org.example.coursework3.controller;

import org.example.coursework3.dto.CancelBookingRequest;
import org.example.coursework3.dto.CreateBookingRequest;
import org.example.coursework3.dto.RescheduleBookingRequest;
import org.example.coursework3.entity.Booking;
import org.example.coursework3.dto.BookingPageResult;
import org.example.coursework3.result.Result;
import org.example.coursework3.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@CrossOrigin
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /**
     * 1. 创建预约
     */
    @PostMapping
    public Result<Booking> createBooking(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody CreateBookingRequest request) {
        Booking booking = bookingService.createBooking(
                authHeader,
                request.getSpecialistId(),
                request.getSlotId(),
                request.getNote()
        );
        return Result.success(booking);
    }

    /**
     * 2. 获取我的预约列表
     */
    @GetMapping
    public Result<BookingPageResult> listMyBookings(@RequestHeader("Authorization") String authHeader,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        BookingPageResult pageResult = bookingService.getCustomerBookings(authHeader, status, page, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 3. 获取单个预约详情
     */
    @GetMapping("/{id}")
    public Result<Booking> getBooking(@RequestHeader("Authorization") String authHeader,
                                      @PathVariable("id") String bookingId) {
        Booking booking = bookingService.getBookingDetail(authHeader, bookingId);
        return Result.success(booking);
    }

    /**
     * 4. 取消预约
     */
    @PostMapping("/{id}/cancel")
    public Result<Booking> cancelBooking(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable("id") String bookingId,
                                          @RequestBody(required = false) CancelBookingRequest request) {
        String reason = request != null ? request.getReason() : null;
        Booking booking = bookingService.customerCancelBooking(authHeader, bookingId, reason);
        return Result.success(booking);
    }

    /**
     * 5. 改期预约
     */
    @PostMapping("/{id}/reschedule")
    public Result<Booking> rescheduleBooking(@RequestHeader("Authorization") String authHeader,
                                              @PathVariable("id") String bookingId,
                                              @RequestBody RescheduleBookingRequest request) {
        Booking booking = bookingService.customerRescheduleBooking(authHeader, bookingId, request.getSlotId());
        return Result.success(booking);
    }
}
