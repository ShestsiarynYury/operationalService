package com.yura.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.yura.entity.CategoryAbsence;
import com.yura.entity.Category;
import com.yura.entity.Absence;
import java.util.Optional;

@Repository
public interface CategoryAbsenceRepository extends JpaRepository<CategoryAbsence, Integer> {

	Optional<CategoryAbsence> findByCategoryAndAbsence(Category ategory, Absence absence);
}