package org.example.courework3.service;

import lombok.RequiredArgsConstructor;
import org.example.courework3.entity.Role;
import org.example.courework3.entity.User;
import org.example.courework3.repository.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
//    private final BCryptPasswordEncoder passwordEncoder;
    public User login(String email, String password) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String passwordHash = user.getPasswordHash();
            if (!passwordHash.equals(password)) {
                throw new RuntimeException("密码不正确");
            }
            return user;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    public User register(String name,String email, String code, String password) {
        // 校验验证码
        try {
            String cachedCode = redisTemplate.opsForValue().get("captcha:" + email);
            if (cachedCode == null || !cachedCode.equals(code)) {
                throw new RuntimeException("验证码错误或已过期");
            }

            // 检查用户是否已存在
            if (userRepository.findByEmail(email).isPresent()) {
                throw new RuntimeException("该邮箱已被注册");
            }

            // 创建新用户
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setRole(Role.Customer);
            user.setPasswordHash(password);


            userRepository.save(user);

            // 注册成功后删除验证码
            redisTemplate.delete("captcha:" + email);
            return user;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}