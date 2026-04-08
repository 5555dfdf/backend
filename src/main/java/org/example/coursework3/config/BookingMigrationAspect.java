package org.example.coursework3.config;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.example.coursework3.service.BookingService; // 假设你的方法在这个类里
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class BookingMigrationAspect {

    @Autowired
    private BookingService bookingService;

    @AfterReturning("execution(* org.example.coursework3.repository.BookingRepository.save*(..))")
    public void afterBookingSave() {
        try {
            bookingService.migrateFinishedBookingToHistory();
        } catch (Exception e) {
            System.err.println("自动迁移任务执行失败: " + e.getMessage());
        }
    }
}