package org.example.coursework3.service;

import lombok.RequiredArgsConstructor;
import org.example.coursework3.dto.request.CreateSpecialistRequest;
import org.example.coursework3.entity.*;
import org.example.coursework3.exception.MsgException;
import org.example.coursework3.repository.ExpertiseRepository;
import org.example.coursework3.repository.SpecialistsRepository;
import org.example.coursework3.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private  final SpecialistsRepository specialistsRepository;
    private final ExpertiseRepository expertiseRepository;

@Transactional
public Specialist createSpecialist(CreateSpecialistRequest request) {
    if (userRepository.findByEmail(request.getUserEmail()).isPresent()) {
        throw new MsgException("该邮箱已被注册");
    }

    // 1. 保存用户
    User user = new User();
    user.setName(request.getName());
    user.setEmail(request.getUserEmail());
    user.setRole(Role.Specialist);
    user.setPasswordHash(request.getPassword());
    userRepository.save(user);

    // 2. 创建专家（userId = user.getId()）
    Specialist specialist = new Specialist(user.getId(), user.getName(), request.getPrice(), request.getBio());
    List<Expertise> expertiseList = new ArrayList<>();
    for (String expertiseId : request.getExpertiseIds()) {
        Expertise expertise = expertiseRepository.findById(expertiseId)
                .orElseThrow(() -> new MsgException("专长不存在"));
        expertiseList.add(expertise);
    }
    specialist.setExpertises(expertiseList);
    specialistsRepository.save(specialist); // JPA 自动维护中间表


    return specialist;
}
    public Specialist updateSpecialist(String id, Map<String, Object> payload) {
        return new Specialist();
    }

    public Specialist setSpecialistStatus(String id, String status) {
        return new Specialist();
    }

    public void deleteSpecialist(String id) {
    }

    public Expertise createExpertise(String name, String description) {
        return new Expertise();
    }

    public Expertise updateExpertise(String id, String name, String description) {
        return new Expertise();
    }

    public void deleteExpertise(String id) {
    }
}
