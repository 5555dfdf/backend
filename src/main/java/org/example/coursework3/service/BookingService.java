package org.example.coursework3.service;

import org.example.coursework3.dto.BookingPageResult;
import org.example.coursework3.entity.*;
import org.example.coursework3.exception.MsgException;
import org.example.coursework3.repository.BookingRepository;
import org.example.coursework3.repository.UserRepository;
import org.example.coursework3.result.CompleteResult;
import org.example.coursework3.result.ConfirmResult;
import org.example.coursework3.result.RejectResult;
import org.example.coursework3.vo.BookingRequestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.example.coursework3.repository.slotRepository;
import java.util.List;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private slotRepository slotRepository;
    public BookingPageResult getSpecialistBookings(String authHeader, String status, Integer page, Integer pageSize) {
        String token = authHeader.replace("Bearer ","");
        String specialistId = authService.getUserIdByToken(token);
        User specialist = userRepository.findById(specialistId);
        System.out.println(specialist);
        if (specialist.getRole() != Role.Specialist){
            throw new MsgException("您不是专家，无权访问");
        }
        //强转类型 从String -> BookingStatus
        Page<Booking> bookingPage;
        List<BookingRequestVo> voList = null;
        try {
            BookingStatus status1 = null;
            if (status != null && !status.isEmpty()) {
                try {
                    status1 = BookingStatus.valueOf(status);
                } catch (IllegalArgumentException e) {
                    throw new MsgException("无效的状态值：" + status);
                }
            }
            PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            try {
                if (status1 == null) {
                    bookingPage = bookingRepository.findBySpecialistId(specialistId, pageRequest);
                    System.out.println(bookingPage);
                } else {
                    bookingPage = bookingRepository.findBySpecialistIdAndStatus(specialistId, status1, pageRequest);
                    System.out.println(bookingPage);
                }
            } catch (Exception e) {
                throw new MsgException("没搜到数据");
            }

            voList = bookingPage.getContent().stream()
                    .map(booking ->{
                        User customer = userRepository.findById(booking.getCustomerId());
                        System.out.println(customer);
                        String customerName = customer.getName();
                        Slot slot = slotRepository.findById(booking.getSlotId()).orElse(null);
                        return BookingRequestVo.fromBooking(booking,customerName, slot);
                    }).toList();
        } catch (MsgException e) {
            throw new MsgException("SQL出错");
        }

        return BookingPageResult.of(voList, bookingPage.getTotalElements(),page,pageSize);
    }


    public ConfirmResult confirmBooking(String authHeader, String bookingId) {
        String token = authHeader.replace("Bearer ","");
        String specialistId = authService.getUserIdByToken(token);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new MsgException("No such reservation"));
        Slot slot = slotRepository.findById(booking.getSlotId()).orElseThrow(() -> new MsgException("No such slot"));
        if (!booking.getSpecialistId().equals(specialistId)) {
            throw new MsgException("No right to handle this reservation");
        }
        if (booking.getStatus() != BookingStatus.Pending){
            throw new MsgException("Can just handling pending reservations");
        }
        booking.setStatus(BookingStatus.Confirmed);
        bookingRepository.save(booking);
        slot.setAvailable(Boolean.FALSE);
        slotRepository.save(slot);
        ConfirmResult result = new ConfirmResult();
        result.setId(bookingId);
        return result;
    }

    public RejectResult rejectBooking(String authHeader, String bookingId, String reason) {
        String token = authHeader.replace("Bearer ","");
        String specialistId = authService.getUserIdByToken(token);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new MsgException("No such reservation"));

        if (!booking.getSpecialistId().equals(specialistId)) {
            throw new MsgException("No right to handle this reservation");
        }
        if (booking.getStatus() != BookingStatus.Pending){
            throw new MsgException("Can just handling pending reservations");
        }
        booking.setStatus(BookingStatus.Rejected);
        booking.setNote(reason);
        bookingRepository.save(booking);

        RejectResult result = new RejectResult();
        result.setId(bookingId);
        return result;
    }

    public CompleteResult completeBooking(String authHeader, String bookingId) {
        String token = authHeader.replace("Bearer ","");
        String specialistId = authService.getUserIdByToken(token);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new MsgException("No such reservation"));

        if (!booking.getSpecialistId().equals(specialistId)) {
            throw new MsgException("No right to handle this reservation");
        }
        if (booking.getStatus() != BookingStatus.Confirmed){
            throw new MsgException("Can just handling Confirmed reservations");
        }
        booking.setStatus(BookingStatus.Completed);
        bookingRepository.save(booking);
        CompleteResult result = new CompleteResult();
        result.setId(bookingId);
        return result;
    }

    // ===================== 客户端预约方法 =====================

    public Booking createBooking(String authHeader, String specialistId, String slotId, String note) {
        String token = authHeader.replace("Bearer ", "");
        String customerId = authService.getUserIdByToken(token);

        // 校验 slot 存在且可用
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new MsgException("时段不存在"));
        if (!slot.getAvailable()) {
            throw new MsgException("该时段已不可预约");
        }
        if (!slot.getSpecialistId().equals(specialistId)) {
            throw new MsgException("时段与专家不匹配");
        }

        Booking booking = new Booking();
        booking.setId(java.util.UUID.randomUUID().toString());
        booking.setCustomerId(customerId);
        booking.setSpecialistId(specialistId);
        booking.setSlotId(slotId);
        booking.setNote(note);
        booking.setStatus(BookingStatus.Pending);

        bookingRepository.save(booking);
        return booking;
    }

    public BookingPageResult getCustomerBookings(String authHeader, String status, Integer page, Integer pageSize) {
        String token = authHeader.replace("Bearer ", "");
        String customerId = authService.getUserIdByToken(token);
        User customer = userRepository.findById(customerId);
        String customerName = customer != null ? customer.getName() : null;

        BookingStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = BookingStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new MsgException("无效的状态值：" + status);
            }
        }

        PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Booking> bookingPage;
        if (statusEnum == null) {
            bookingPage = bookingRepository.findByCustomerId(customerId, pageRequest);
        } else {
            bookingPage = bookingRepository.findByCustomerIdAndStatus(customerId, statusEnum, pageRequest);
        }

        List<BookingRequestVo> voList = bookingPage.getContent().stream()
                .map(booking -> {
                    Slot slot = slotRepository.findById(booking.getSlotId()).orElse(null);
                    return BookingRequestVo.fromBooking(booking, customerName, slot);
                }).toList();

        return BookingPageResult.of(voList, bookingPage.getTotalElements(), page, pageSize);
    }

    public Booking getBookingDetail(String authHeader, String bookingId) {
        String token = authHeader.replace("Bearer ", "");
        String customerId = authService.getUserIdByToken(token);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new MsgException("预约不存在"));
        if (!booking.getCustomerId().equals(customerId)) {
            throw new MsgException("无权查看此预约");
        }
        return booking;
    }

    public Booking customerCancelBooking(String authHeader, String bookingId, String reason) {
        String token = authHeader.replace("Bearer ", "");
        String customerId = authService.getUserIdByToken(token);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new MsgException("预约不存在"));
        if (!booking.getCustomerId().equals(customerId)) {
            throw new MsgException("无权操作此预约");
        }
        if (booking.getStatus() == BookingStatus.Cancelled || booking.getStatus() == BookingStatus.Completed) {
            throw new MsgException("该预约无法取消");
        }

        booking.setStatus(BookingStatus.Cancelled);
        if (reason != null && !reason.isEmpty()) {
            booking.setNote(reason);
        }

        // 释放时段
        Slot slot = slotRepository.findById(booking.getSlotId()).orElse(null);
        if (slot != null) {
            slot.setAvailable(true);
            slotRepository.save(slot);
        }

        bookingRepository.save(booking);
        return booking;
    }

    public Booking customerRescheduleBooking(String authHeader, String bookingId, String newSlotId) {
        String token = authHeader.replace("Bearer ", "");
        String customerId = authService.getUserIdByToken(token);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new MsgException("预约不存在"));
        if (!booking.getCustomerId().equals(customerId)) {
            throw new MsgException("无权操作此预约");
        }
        if (booking.getStatus() == BookingStatus.Cancelled || booking.getStatus() == BookingStatus.Completed) {
            throw new MsgException("该预约无法改期");
        }

        // 校验新时段
        Slot newSlot = slotRepository.findById(newSlotId)
                .orElseThrow(() -> new MsgException("新时段不存在"));
        if (!newSlot.getAvailable()) {
            throw new MsgException("新时段不可用");
        }
        if (!newSlot.getSpecialistId().equals(booking.getSpecialistId())) {
            throw new MsgException("新时段与原专家不匹配");
        }

        // 释放旧时段
        Slot oldSlot = slotRepository.findById(booking.getSlotId()).orElse(null);
        if (oldSlot != null) {
            oldSlot.setAvailable(true);
            slotRepository.save(oldSlot);
        }

        booking.setSlotId(newSlotId);
        booking.setStatus(BookingStatus.Rescheduled);
        bookingRepository.save(booking);
        return booking;
    }

}

