package org.example.coursework3.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.coursework3.dto.BookingListResponse;
import org.example.coursework3.dto.BookingResponse;
import org.example.coursework3.dto.CancelBookingRequest;
import org.example.coursework3.dto.CreateBookingRequest;
import org.example.coursework3.dto.RescheduleBookingRequest;
import org.example.coursework3.entity.Booking;
import org.example.coursework3.result.Result;
import org.example.coursework3.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.example.coursework3.entity.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@CrossOrigin
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<BookingListResponse>> listMyBookings(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status) {
        try {
            return ResponseEntity.ok(Result.success(bookingService.listCustomerBookings(user.getId(), status)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error("FETCH_ERROR", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<BookingResponse>> getBooking(@PathVariable String id,
                                                              @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(Result.success(bookingService.getBookingDetailForUser(id, user)));
        } catch (Exception e) {
            if ("Booking not found: ".concat(id).equals(e.getMessage())) {
                return ResponseEntity.status(404).body(Result.error("NOT_FOUND", e.getMessage()));
            }
            if ("Not your booking".equals(e.getMessage())) {
                return ResponseEntity.status(403).body(Result.error("FORBIDDEN", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Result.error("FETCH_ERROR", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Customer','Admin')")
    public ResponseEntity<Result<BookingResponse>> createBooking(@AuthenticationPrincipal User user,
                                                                 @Valid @RequestBody CreateBookingRequest request) {
        try {
            Booking booking = bookingService.createBooking(request, user.getId());
            return ResponseEntity.ok(Result.success(bookingService.toBookingResponse(booking)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error("CREATE_ERROR", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<BookingResponse>> cancelBooking(@PathVariable String id,
                                                                 @AuthenticationPrincipal User user,
                                                                 @RequestBody(required = false) CancelBookingRequest request) {
        try {
            String reason = request != null ? request.getReason() : null;
            Booking updated = bookingService.cancelBooking(id, user, reason);
            return ResponseEntity.ok(Result.success(bookingService.toBookingResponse(updated)));
        } catch (Exception e) {
            if ("Booking not found: ".concat(id).equals(e.getMessage())) {
                return ResponseEntity.status(404).body(Result.error("NOT_FOUND", e.getMessage()));
            }
            if ("Not your booking".equals(e.getMessage())) {
                return ResponseEntity.status(403).body(Result.error("FORBIDDEN", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Result.error("CANCEL_ERROR", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reschedule")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Result<BookingResponse>> rescheduleBooking(@PathVariable String id,
                                                                     @AuthenticationPrincipal User user,
                                                                     @Valid @RequestBody RescheduleBookingRequest request) {
        try {
            Booking updated = bookingService.rescheduleBooking(id, user, request.getSlotId());
            return ResponseEntity.ok(Result.success(bookingService.toBookingResponse(updated)));
        } catch (Exception e) {
            if ("Booking not found: ".concat(id).equals(e.getMessage())) {
                return ResponseEntity.status(404).body(Result.error("NOT_FOUND", e.getMessage()));
            }
            if ("Not your booking".equals(e.getMessage())) {
                return ResponseEntity.status(403).body(Result.error("FORBIDDEN", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Result.error("RESCHEDULE_ERROR", e.getMessage()));
        }
    }
}
