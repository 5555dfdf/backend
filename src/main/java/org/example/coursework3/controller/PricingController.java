package org.example.coursework3.controller;

import lombok.RequiredArgsConstructor;
import org.example.coursework3.dto.request.PricingQuoteRequest;
import org.example.coursework3.result.Result;
import org.example.coursework3.service.PricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pricing")
@CrossOrigin
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/quote")
    public ResponseEntity<?> quote(@RequestBody Map<String, Object> payload) {
        try {
            PricingQuoteRequest request = new PricingQuoteRequest(
                    payload.get("specialistId") == null ? null : String.valueOf(payload.get("specialistId")),
                    payload.get("duration") == null ? null : Integer.valueOf(String.valueOf(payload.get("duration"))),
                    payload.get("type") == null ? null : String.valueOf(payload.get("type"))
            );
            Object result = pricingService.getQuote(request);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error("QUOTE_ERROR", e.getMessage()));
        }
    }
}
