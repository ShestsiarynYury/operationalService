package com.yura.repository;

import org.springframework.stereotype.Repository;
import com.yura.entity.Absence;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Integer> {
	
    Optional<Absence> findByName(String name);
}