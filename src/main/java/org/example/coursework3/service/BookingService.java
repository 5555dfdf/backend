package org.example.coursework3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursework3.dto.BookingListResponse;
import org.example.coursework3.dto.BookingResponse;
import org.example.coursework3.dto.CancelBookingRequest;
import org.example.coursework3.dto.CreateBookingRequest;
import org.example.coursework3.dto.RescheduleBookingRequest;
import org.example.coursework3.dto.response.BookingPageResult;
import org.example.coursework3.dto.response.CompleteResult;
import org.example.coursework3.dto.response.ConfirmResult;
import org.example.coursework3.dto.response.RejectResult;
import org.example.coursework3.entity.Booking;
import org.example.coursework3.entity.BookingHistory;
import org.example.coursework3.entity.BookingStatus;
import org.example.coursework3.entity.Role;
import org.example.coursework3.entity.Slot;
import org.example.coursework3.entity.User;
import org.example.coursework3.exception.MsgException;
import org.example.coursework3.repository.BookingHistoryRepository;
import org.example.coursework3.repository.BookingRepository;
import org.example.coursework3.repository.SlotRepository;
import org.example.coursework3.repository.UserRepository;
import org.example.coursework3.vo.BookingRequestVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final BookingHistoryRepository bookingHistoryRepository;

    public BookingPageResult getSpecialistBookings(String authHeader, String status, Integer page, Integer pageSize) {
        String specialistId = authService.getUserIdByToken(extractToken(authHeader));
        User specialist = userRepository.findById(specialistId)
                .orElseThrow(() -> new MsgException("User not found"));
        if (specialist.getRole() != Role.Specialist) {
            throw new MsgException("Only specialist can access this endpoint");
        }

        BookingStatus filterStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                filterStatus = BookingStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new MsgException("Invalid status: " + status);
            }
        }

        PageRequest pageable = PageRequest.of(Math.max(0, (page == null ? 1 : page) - 1),
                pageSize == null ? 10 : pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Booking> bookingPage = (filterStatus == null)
                ? bookingRepository.findBySpecialistId(specialistId, pageable)
                : bookingRepository.findBySpecialistIdAndStatus(specialistId, filterStatus, pageable);

        List<BookingRequestVo> voList = bookingPage.getContent().stream().map(booking -> {
            User customer = userRepository.findById(booking.getCustomerId()).orElse(null);
            Slot slot = slotRepository.findById(booking.getSlotId()).orElse(null);
            String customerName = customer == null ? "" : customer.getName();
            return BookingRequestVo.fromBooking(booking, customerName, slot);
        }).toList();

        return BookingPageResult.of(voList, bookingPage.getTotalElements(), page == null ? 1 : page, pageSize == null ? 10 : pageSize);
    }

    public ConfirmResult confirmBooking(String authHeader, String bookingId) {
        String specialistId = authService.getUserIdByToken(extractToken(authHeader));
        Booking booking = getBooking(bookingId);
        Slot slot = slotRepository.findById(booking.getSlotId()).orElseThrow(() -> new MsgException("Slot not found"));

        if (!booking.getSpecialistId().equals(specialistId)) throw new MsgException("No permission");
        if (booking.getStatus() != BookingStatus.Pending) throw new MsgException("Only pending booking can be confirmed");

        booking.setStatus(BookingStatus.Confirmed);
        bookingRepository.save(booking);
        slot.setAvailable(false);
        slotRepository.save(slot);

        ConfirmResult result = new ConfirmResult();
        result.setId(bookingId);
        return result;
    }

    public RejectResult rejectBooking(String authHeader, String bookingId, String reason) {
        String specialistId = authService.getUserIdByToken(extractToken(authHeader));
        Booking booking = getBooking(bookingId);

        if (!booking.getSpecialistId().equals(specialistId)) throw new MsgException("No permission");
        if (booking.getStatus() != BookingStatus.Pending) throw new MsgException("Only pending booking can be rejected");

        booking.setStatus(BookingStatus.Rejected);
        booking.setNote(reason);
        bookingRepository.save(booking);

        RejectResult result = new RejectResult();
        result.setId(bookingId);
        return result;
    }

    public CompleteResult completeBooking(String authHeader, String bookingId) {
        String specialistId = authService.getUserIdByToken(extractToken(authHeader));
        Booking booking = getBooking(bookingId);

        if (!booking.getSpecialistId().equals(specialistId)) throw new MsgException("No permission");
        if (booking.getStatus() != BookingStatus.Confirmed) throw new MsgException("Only confirmed booking can be completed");

        booking.setStatus(BookingStatus.Completed);
        bookingRepository.save(booking);

        CompleteResult result = new CompleteResult();
        result.setId(bookingId);
        return result;
    }

    public List<Booking> listBookings(Map<String, Object> ignored) {
        return bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Booking getBooking(String id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new MsgException("Booking not found: " + id));
    }

    public BookingListResponse listCustomerBookings(String customerId, String status) {
        List<Booking> bookings;
        if (status == null || status.isBlank()) {
            bookings = bookingRepository.findByCustomerId(customerId);
        } else {
            BookingStatus bookingStatus = BookingStatus.valueOf(status);
            bookings = bookingRepository.findByCustomerIdAndStatus(customerId, bookingStatus);
        }
        List<BookingResponse> items = bookings.stream().map(this::toBookingResponse).toList();
        return BookingListResponse.builder().items(items).total(items.size()).build();
    }

    public BookingResponse getBookingDetailForUser(String bookingId, User user) {
        Booking booking = getBooking(bookingId);
        if (!isBookingAccessibleByUser(booking, user)) {
            throw new MsgException("Not your booking");
        }
        return toBookingResponse(booking);
    }

    @Transactional
    public Booking createBooking(CreateBookingRequest request, String customerId) {
        Slot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new MsgException("Slot not found"));
        if (!Boolean.TRUE.equals(slot.getAvailable())) {
            throw new MsgException("Slot is unavailable");
        }

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID().toString());
        booking.setCustomerId(customerId);
        booking.setSpecialistId(request.getSpecialistId());
        booking.setSlotId(request.getSlotId());
        booking.setNote(request.getNote());
        booking.setStatus(BookingStatus.Pending);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(String bookingId, User user, String reason) {
        Booking booking = getBooking(bookingId);
        if (!isBookingAccessibleByUser(booking, user)) throw new MsgException("Not your booking");

        booking.setStatus(BookingStatus.Cancelled);
        if (reason != null) booking.setNote(reason);
        bookingRepository.save(booking);

        slotRepository.findById(booking.getSlotId()).ifPresent(slot -> {
            slot.setAvailable(true);
            slotRepository.save(slot);
        });
        return booking;
    }

    @Transactional
    public Booking rescheduleBooking(String bookingId, User user, String newSlotId) {
        Booking booking = getBooking(bookingId);
        if (!isBookingAccessibleByUser(booking, user)) throw new MsgException("Not your booking");

        Slot newSlot = slotRepository.findById(newSlotId)
                .orElseThrow(() -> new MsgException("Slot not found"));
        if (!Boolean.TRUE.equals(newSlot.getAvailable())) throw new MsgException("Slot is unavailable");

        slotRepository.findById(booking.getSlotId()).ifPresent(oldSlot -> {
            oldSlot.setAvailable(true);
            slotRepository.save(oldSlot);
        });

        newSlot.setAvailable(false);
        slotRepository.save(newSlot);

        booking.setSlotId(newSlotId);
        booking.setSpecialistId(newSlot.getSpecialistId());
        booking.setStatus(BookingStatus.Pending);
        return bookingRepository.save(booking);
    }

    public BookingResponse toBookingResponse(Booking booking) {
        Slot slot = slotRepository.findById(booking.getSlotId()).orElse(null);
        User specialist = userRepository.findById(booking.getSpecialistId()).orElse(null);
        return BookingResponse.builder()
                .id(booking.getId())
                .customerId(booking.getCustomerId())
                .specialistId(booking.getSpecialistId())
                .specialistName(specialist == null ? null : specialist.getName())
                .slotId(booking.getSlotId())
                .startTime(slot == null || slot.getStartTime() == null ? null : slot.getStartTime().toString())
                .time(slot == null || slot.getStartTime() == null ? null : slot.getStartTime().toString())
                .note(booking.getNote())
                .status(booking.getStatus() == null ? null : booking.getStatus().name())
                .createdAt(booking.getCreatedAt() == null ? null : booking.getCreatedAt().toString())
                .updatedAt(booking.getUpdatedAt() == null ? null : booking.getUpdatedAt().toString())
                .build();
    }

    @Transactional
    public void createBookingHistory(Booking booking) {
        boolean exists = bookingHistoryRepository.existsByBookingIdAndStatus(booking.getId(), booking.getStatus());
        if (exists) {
            log.info("Booking history already exists for booking={}", booking.getId());
            return;
        }

        BookingHistory history = new BookingHistory();
        history.setId(UUID.randomUUID().toString());
        history.setBookingId(booking.getId());
        history.setStatus(booking.getStatus());
        history.setReason(booking.getNote());
        history.setChangedAt(booking.getUpdatedAt());
        bookingHistoryRepository.save(history);
    }

    private static String extractToken(String authHeader) {
        if (authHeader == null) throw new MsgException("Missing Authorization header");
        return authHeader.replace("Bearer ", "");
    }

    private static boolean isBookingAccessibleByUser(Booking booking, User user) {
        if (user == null) return false;
        if (user.getRole() == Role.Admin) return true;
        if (user.getRole() == Role.Specialist) return booking.getSpecialistId().equals(user.getId());
        return booking.getCustomerId().equals(user.getId());
    }
}
