package org.example.coursework3.repository;

import org.example.coursework3.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface slotRepository extends JpaRepository<Slot, String> {
    Optional<Slot> findById(String id);

    List<Slot> findBySpecialistId(String id);
}
