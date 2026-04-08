package org.example.coursework3.service;

import lombok.RequiredArgsConstructor;
import org.example.coursework3.entity.Expertise;
import org.example.coursework3.entity.Role;
import org.example.coursework3.entity.Specialist;
import org.example.coursework3.entity.User;
import org.example.coursework3.repository.ExpertiseRepository;
import org.example.coursework3.repository.SpecialistsRepository;
import org.example.coursework3.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ExpertiseRepository expertiseRepository;
    private final SpecialistsRepository specialistsRepository;
    private final UserRepository userRepository;

    @Transactional
    public Specialist createSpecialist(Map<String, Object> payload) {
        String userEmail = readRequiredString(payload, "userEmail", "userEmail is required");
        String displayName = readRequiredString(payload, "name", "name is required");

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user != null) {
            if (user.getRole() == Role.Admin) {
                throw new RuntimeException("Admin account cannot be promoted to specialist");
            }
            if (specialistsRepository.existsById(user.getId())) {
                throw new RuntimeException("This user is already a specialist");
            }
            user.setRole(Role.Specialist);
            user.setName(displayName);
            userRepository.save(user);
        } else {
            String password = readRequiredString(payload, "password", "password is required for a new user");
            user = new User();
            user.setEmail(userEmail);
            user.setName(displayName);
            user.setPasswordHash(password);
            user.setRole(Role.Specialist);
            userRepository.save(user);
        }

        Specialist specialist = new Specialist();
        specialist.setUserId(user.getId());
        specialist.setName(displayName);
        specialist.setBio(readOptionalString(payload, "bio"));

        Object priceValue = payload.get("price");
        if (priceValue != null && !priceValue.toString().isBlank()) {
            specialist.setPrice(new BigDecimal(priceValue.toString()));
        }

        List<String> expertiseIds = castStringList(payload.get("expertiseIds"));
        if (expertiseIds != null && !expertiseIds.isEmpty()) {
            specialist.setExpertises(expertiseRepository.findAllById(expertiseIds));
        }

        return specialistsRepository.save(specialist);
    }

    @Transactional
    public Specialist updateSpecialist(String id, Map<String, Object> payload) {
        Specialist specialist = specialistsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialist not found"));

        if (payload.containsKey("name")) {
            String name = readOptionalString(payload, "name");
            if (name != null && !name.isBlank()) {
                specialist.setName(name.trim());
            }
        }

        if (payload.containsKey("bio")) {
            specialist.setBio(readOptionalString(payload, "bio"));
        }

        if (payload.containsKey("price")) {
            Object priceValue = payload.get("price");
            if (priceValue != null && !priceValue.toString().isBlank()) {
                specialist.setPrice(new BigDecimal(priceValue.toString()));
            }
        }

        if (payload.containsKey("expertiseIds")) {
            List<String> expertiseIds = castStringList(payload.get("expertiseIds"));
            specialist.setExpertises(expertiseIds == null ? List.of() : expertiseRepository.findAllById(expertiseIds));
        }

        return specialistsRepository.save(specialist);
    }

    @Transactional
    public Specialist setSpecialistStatus(String id, String status) {
        Specialist specialist = specialistsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialist not found"));
        if (!"Active".equals(status) && !"Inactive".equals(status)) {
            throw new RuntimeException("Status must be Active or Inactive");
        }
        // Current data model has no status column for specialists; keep API contract and return entity.
        return specialist;
    }

    @Transactional
    public void deleteSpecialist(String id) {
        if (!specialistsRepository.existsById(id)) {
            throw new RuntimeException("Specialist not found");
        }
        specialistsRepository.deleteById(id);
    }

    @Transactional
    public Expertise createExpertise(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new RuntimeException("name is required");
        }

        List<Expertise> all = expertiseRepository.findAll();
        boolean exists = all.stream().anyMatch(e -> name.trim().equalsIgnoreCase(e.getName()));
        if (exists) {
            throw new RuntimeException("Expertise name already exists");
        }

        Expertise expertise = new Expertise();
        expertise.setName(name.trim());
        expertise.setDescription(description);
        return expertiseRepository.save(expertise);
    }

    @Transactional
    public Expertise updateExpertise(String id, String name, String description) {
        Expertise expertise = expertiseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expertise not found"));

        if (name != null && !name.isBlank() && !name.equals(expertise.getName())) {
            String normalized = name.trim();
            boolean exists = expertiseRepository.findAll().stream()
                    .anyMatch(e -> !e.getId().equals(id) && normalized.equalsIgnoreCase(e.getName()));
            if (exists) {
                throw new RuntimeException("Expertise name already exists");
            }
            expertise.setName(normalized);
        }

        if (description != null) {
            expertise.setDescription(description);
        }
        return expertiseRepository.save(expertise);
    }

    @Transactional
    public void deleteExpertise(String id) {
        if (!expertiseRepository.existsById(id)) {
            throw new RuntimeException("Expertise not found");
        }
        expertiseRepository.deleteById(id);
    }

    private static String readRequiredString(Map<String, Object> payload, String key, String error) {
        Object value = payload.get(key);
        String s = value == null ? null : value.toString().trim();
        if (s == null || s.isBlank()) {
            throw new RuntimeException(error);
        }
        return s;
    }

    private static String readOptionalString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? null : value.toString();
    }

    @SuppressWarnings("unchecked")
    private static List<String> castStringList(Object value) {
        if (value == null) return null;
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return null;
    }
}
