package org.example.courework3.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.courework3.dto.CaptchaRequest;
import org.example.courework3.dto.LoginRequest;
import org.example.courework3.dto.RegisterRequest;
import org.example.courework3.result.Result;
import org.example.courework3.service.AliyunMailService;
import org.example.courework3.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private AliyunMailService mailService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 固定账号密码校验
        if ("123@qq.com".equals(request.getEmail()) && "123".equals(request.getPassword())) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 200);
            map.put("message", "登录成功");
            map.put("token", "UUID-TEST-TOKEN-" + UUID.randomUUID());


            // 模拟用户信息
            Map<String, Object> user = new HashMap<>();
            user.put("email", request.getEmail());
            user.put("name", "测试用户");
            user.put("role", "Customer");
            map.put("user", user);
            log.info("用户登录：{}",map);
            return ResponseEntity.ok(map);

        } else {
            System.out.println("2");
            return ResponseEntity.status(401).body(Map.of("message", "用户名或密码错误"));
        }
    }
    @PostMapping("/send-email-code")
    public Result<Void> sendCaptcha(@Valid @RequestBody CaptchaRequest request) throws Exception {
        mailService.sendCaptcha(request.getEmail());
        log.info("向{}发送验证码",request.getEmail());
        return Result.success("验证码已发送");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
//        authService.register(request.getEmail(), request.getVerificationCode(), request.getPassword());
        Map<String, Object> user = new HashMap<>();
        user.put("id",1);
        user.put("name", request.getName());
        user.put("email",request.getEmail());
        user.put("role","Customer");
        Map<String, Object> map = new HashMap<>();
        map.put("token", UUID.randomUUID());
        map.put("user",user);
        return ResponseEntity.ok(map);
    }
}