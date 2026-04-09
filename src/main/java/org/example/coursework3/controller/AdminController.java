package org.example.coursework3.controller;

import lombok.RequiredArgsConstructor;
import org.example.coursework3.dto.request.CreateSpecialistRequest;
import org.example.coursework3.entity.Role;
import org.example.coursework3.entity.Specialist;
import org.example.coursework3.result.Result;
import org.example.coursework3.service.AdminService;
import org.example.coursework3.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private AuthService authService;

    // 1. 创建专家
    @PostMapping("/specialists")
    public Result<Specialist> createSpecialist(@RequestHeader("Authorization") String authHeader, @RequestBody CreateSpecialistRequest request) {
        String token = authHeader.replace("Bearer ", "");
        String userId = authService.getUserIdByToken(token);
        Role role = authService.getRoleByUserId(userId);
        if (role == Role.Admin) {
            return Result.success(adminService.createSpecialist(request));
        }
        return Result.error("ERROR","请以管理员身份创建");
    }

//    // 2. 更新专家信息
//    @PatchMapping("/specialists/{id}")
//    public ResponseEntity<?> updateSpecialist(@PathVariable String id,
//                                              @RequestBody Map<String, Object> payload) {
//        try {
//            Specialist specialist = adminService.updateSpecialist(id, payload);
//            return ResponseEntity.ok(specialist);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    // 3. 设置专家状�?
//    @PostMapping("/specialists/{id}/status")
//    public ResponseEntity<?> setSpecialistStatus(@PathVariable String id,
//                                                 @RequestBody Map<String, String> payload) {
//        try {
//            String status = payload.get("status");
//            Specialist specialist = adminService.setSpecialistStatus(id, status);
//            return ResponseEntity.ok(specialist);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    // 4. 删除专家
//    @DeleteMapping("/specialists/{id}")
//    public ResponseEntity<?> deleteSpecialist(@PathVariable String id) {
//        try {
//            adminService.deleteSpecialist(id);
//            return ResponseEntity.ok(Map.of("message", "Specialist deleted successfully"));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    // 5. 创建专长
//    @PostMapping("/expertise")
//    public ResponseEntity<?> createExpertise(@RequestBody Map<String, String> payload) {
//        try {
//            String name = payload.get("name");
//            String description = payload.get("description");
//            Expertise expertise = adminService.createExpertise(name, description);
//            return ResponseEntity.status(HttpStatus.CREATED).body(expertise);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    // 6. 更新专长
//    @PatchMapping("/expertise/{id}")
//    public ResponseEntity<?> updateExpertise(@PathVariable String id,
//                                             @RequestBody Map<String, String> payload) {
//        try {
//            String name = payload.get("name");
//            String description = payload.get("description");
//            Expertise expertise = adminService.updateExpertise(id, name, description);
//            return ResponseEntity.ok(expertise);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    // 7. 删除专长
//    @DeleteMapping("/expertise/{id}")
//    public ResponseEntity<?> deleteExpertise(@PathVariable String id) {
//        try {
//            adminService.deleteExpertise(id);
//            return ResponseEntity.ok(Map.of("message", "Expertise deleted successfully"));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        }
//    }
}